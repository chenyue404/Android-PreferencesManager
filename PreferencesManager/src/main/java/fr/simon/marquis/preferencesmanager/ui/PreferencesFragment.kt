/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.model.PreferenceSortType
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.hideSoftKeyboard
import kotlin.collections.Map.Entry

class PreferencesFragment : Fragment() {

    private var mIconUri: Uri? = null
    private var mFile: String? = null
    private var mPackageName: String? = null

    private var mSearchView: SearchView? = null

    var preferenceFile: PreferenceFile? = null

    private var mListener: OnPreferenceFragmentInteractionListener? = null

    private var gridView: GridView? = null
    private var loadingView: View? = null
    private var emptyViewText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mIconUri = arguments!!.getParcelable(ARG_ICON_URI)
            mFile = arguments!!.getString(ARG_FILE)
            mPackageName = arguments!!.getString(ARG_PACKAGE_NAME)
        }

        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preferences, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingView = view.findViewById(R.id.loadingView)
        emptyViewText = view.findViewById(R.id.emptyView)
        gridView = view.findViewById(R.id.gridView)
        gridView!!.choiceMode = GridView.CHOICE_MODE_MULTIPLE_MODAL

        updateFilter(null)

        if (preferenceFile == null) {
            launchTask()
        } else {
            updateListView(preferenceFile, false)
        }
    }

    private fun launchTask() {
        val task = ParsingTask(mFile!!)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.preferences_fragment, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        mSearchView = searchItem.actionView as SearchView
        mSearchView!!.queryHint = getString(R.string.action_search_preference)
        mSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(s: String): Boolean {
                activity?.hideSoftKeyboard(mSearchView!!)
                mSearchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                return mSearchView!!.hasFocus() && updateFilter(s)
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(arg0: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(arg0: MenuItem): Boolean {
                return updateFilter(null)
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_add).isEnabled = preferenceFile != null && preferenceFile!!.isValidPreferenceFile
        menu.findItem(R.id.action_add).setIcon(if (preferenceFile != null && preferenceFile!!.isValidPreferenceFile) R.drawable.ic_action_add else R.drawable.ic_action_add_disabled)
        val sortAlpha = menu.findItem(R.id.action_sort_alpha)
        val sortType = menu.findItem(R.id.action_sort_type)
        sortAlpha.isChecked = false
        sortType.isChecked = false
        if (PreferencesActivity.preferenceSortType == PreferenceSortType.ALPHANUMERIC) {
            sortAlpha.isChecked = true
        } else if (PreferencesActivity.preferenceSortType == PreferenceSortType.TYPE_AND_ALPHANUMERIC) {
            sortType.isChecked = true
        }
        menu.findItem(R.id.action_restore_file).isVisible = mListener != null && mListener!!.canRestoreFile(mFile)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_int -> {
                showPrefDialog(PreferenceType.INT)
                return true
            }
            R.id.action_add_boolean -> {
                showPrefDialog(PreferenceType.BOOLEAN)
                return true
            }
            R.id.action_add_string -> {
                showPrefDialog(PreferenceType.STRING)
                return true
            }
            R.id.action_add_float -> {
                showPrefDialog(PreferenceType.FLOAT)
                return true
            }
            R.id.action_add_long -> {
                showPrefDialog(PreferenceType.LONG)
                return true
            }
            R.id.action_add_stringset -> {
                showPrefDialog(PreferenceType.STRINGSET)
                return true
            }
            R.id.action_edit_file -> {
                if (preferenceFile == null) {
                    if (activity != null) {
                        activity!!.finish()
                    }
                }
                val intent = Intent(activity, FileEditorActivity::class.java)
                intent.putExtra(ARG_ICON_URI, mIconUri)
                intent.putExtra(ARG_FILE, mFile)
                intent.putExtra(ARG_PACKAGE_NAME, mPackageName)
                startActivityForResult(intent, CODE_EDIT_FILE)
                return true
            }
            R.id.action_sort_alpha -> {
                setSortType(PreferenceSortType.ALPHANUMERIC)
                return true
            }
            R.id.action_sort_type -> {
                setSortType(PreferenceSortType.TYPE_AND_ALPHANUMERIC)
                return true
            }
            R.id.action_backup_file -> {
                if (mListener != null) {
                    mListener!!.onBackupFile(mFile)
                }
                return true
            }
            R.id.action_restore_file -> {
                restoreBackup()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun restoreBackup() {
        if (mListener != null) {
            mListener!!.getBackups(mFile)?.let { RestoreDialogFragment.show(this, fragmentManager!!, mFile!!, it) }
        }
    }

    private fun updateFilter(s: String?): Boolean {
        val filter = if (!TextUtils.isEmpty(s)) s!!.trim { it <= ' ' } else null
        val adapter: PreferenceAdapter? = gridView?.adapter as PreferenceAdapter? ?: return false
        adapter?.setFilter(filter)
        adapter?.filter?.filter(filter)
        return true
    }

    private fun setSortType(type: PreferenceSortType) {
        if (PreferencesActivity.preferenceSortType != type) {
            PreferencesActivity.preferenceSortType = type
            if (activity != null) {
                activity!!.invalidateOptionsMenu()
                PreferenceManager.getDefaultSharedPreferences(activity).edit().putInt(PreferencesActivity.KEY_SORT_TYPE, type.ordinal).apply()
            }

            if (gridView!!.adapter != null && preferenceFile != null) {
                preferenceFile!!.updateSort()
                (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_EDIT_FILE && resultCode == AppCompatActivity.RESULT_OK) {
            loadingView!!.visibility = View.VISIBLE
            gridView!!.visibility = View.GONE

            if (activity != null) {
                val fadeInAnim = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
                if (fadeInAnim != null) {
                    loadingView!!.startAnimation(fadeInAnim)
                }
                val fadeOutAnim = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)
                if (fadeOutAnim != null) {
                    gridView!!.startAnimation(fadeOutAnim)
                }
            }
            launchTask()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showPrefDialog(type: PreferenceType, editMode: Boolean = false, key: String? = null, obj: Any? = null) {
        val newFragment = PreferenceDialog.newInstance(type, editMode, key, obj)
        newFragment.setTargetFragment(this, ("Fragment:" + mFile!!).hashCode())
        val fm = fragmentManager
        if (fm != null) {
            newFragment.show(fm, "$mFile#$key")
        }
    }

    fun addPrefKeyValue(previousKey: String?, newKey: String?, value: Any?, editMode: Boolean) {
        if (preferenceFile == null) {
            return
        }
        preferenceFile!!.add(previousKey, newKey, value, editMode)
        Utils.savePreferences(preferenceFile, mFile!!, mPackageName!!, activity!!)
        (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
    }

    fun deletePref(key: String) {
        if (preferenceFile == null) {
            return
        }
        preferenceFile!!.removeValue(key)
        Utils.savePreferences(preferenceFile, mFile!!, mPackageName!!, activity!!)
        (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnPreferenceFragmentInteractionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnPreferenceFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    internal fun updateListView(p: PreferenceFile?, animate: Boolean) {
        if (activity == null || activity!!.isFinishing) {
            return
        }
        if (p == null) {
            activity!!.finish()
            return
        }
        preferenceFile = p
        emptyViewText!!.setText(if (preferenceFile!!.isValidPreferenceFile) R.string.empty_preference_file_valid else R.string.empty_preference_file_invalid)
        loadingView!!.visibility = View.GONE
        gridView!!.visibility = View.VISIBLE

        if (animate) {
            if (activity != null) {
                val fadeOut = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)
                if (fadeOut != null) {
                    loadingView!!.startAnimation(fadeOut)
                }
                val fadeIn = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
                if (fadeIn != null) {
                    gridView!!.startAnimation(fadeIn)
                }
            }
        }

        gridView!!.adapter = PreferenceAdapter(activity!!, this)
        gridView!!.emptyView = emptyViewText
        gridView!!.setOnItemClickListener { _, _, arg2, _ ->

            val item = gridView!!.adapter.getItem(arg2) as Entry<String, Any>
            val type = PreferenceType.fromObject(item.value)
            if (type == PreferenceType.UNSUPPORTED) {
                Toast.makeText(activity, R.string.preference_unsupported, Toast.LENGTH_SHORT).show()
            } else {
                showPrefDialog(type, true, item.key, item.value)
            }
        }
        gridView!!.setMultiChoiceModeListener(object : MultiChoiceModeListener {

            override fun onItemCheckedStateChanged(mode: ActionMode, position: Int, id: Long, checked: Boolean) {
                (gridView!!.adapter as PreferenceAdapter).itemCheckedStateChanged(position, checked)
                @Suppress("DEPRECATION")
                mode.title = Html.fromHtml("<b>" + gridView!!.checkedItemCount + "</b>")
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.action_delete -> {
                        (gridView!!.adapter as PreferenceAdapter).deleteSelection()
                        Utils.savePreferences(preferenceFile, mFile!!, mPackageName!!, activity!!)
                        (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
                        mode.finish()
                        return true
                    }
                    R.id.action_select_all -> {
                        val check = gridView!!.checkedItemCount != gridView!!.count
                        for (i in 0 until gridView!!.count) {
                            gridView!!.setItemChecked(i, check)
                        }
                        return true
                    }
                    else -> return false
                }
            }

            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater = mode.menuInflater
                inflater?.inflate(R.menu.cab, menu)
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                (gridView!!.adapter as PreferenceAdapter).resetSelection()
                activity!!.invalidateOptionsMenu()
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

        })
        activity!!.invalidateOptionsMenu()
    }

    interface OnPreferenceFragmentInteractionListener {

        fun onBackupFile(fullPath: String?)

        fun canRestoreFile(fullPath: String?): Boolean

        fun getBackups(fullPath: String?): List<String>?
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class ParsingTask(private val mFile: String) : AsyncTask<Void, Void, PreferenceFile>() {

        override fun doInBackground(vararg params: Void): PreferenceFile {
            val start = System.currentTimeMillis()
            Log.d(Utils.TAG, "Start reading $mFile")
            val content = Utils.readFile(mFile)
            Log.d(Utils.TAG, "End reading " + mFile + " --> " + (System.currentTimeMillis() - start) + " ms")
            return PreferenceFile.fromXml(content)
        }

        override fun onPostExecute(result: PreferenceFile) {
            super.onPostExecute(result)
            updateListView(result, true)
        }

    }

    companion object {
        private const val CODE_EDIT_FILE = 666

        const val ARG_ICON_URI = "ICON_URI"
        const val ARG_FILE = "FILE"
        const val ARG_PACKAGE_NAME = "PACKAGE_NAME"

        fun newInstance(paramFile: String, paramPackageName: String, paramIconUri: Uri): PreferencesFragment {
            val fragment = PreferencesFragment()
            val args = Bundle()
            args.putParcelable(ARG_ICON_URI, paramIconUri)
            args.putString(ARG_FILE, paramFile)
            args.putString(ARG_PACKAGE_NAME, paramPackageName)
            fragment.arguments = args
            return fragment
        }
    }

}
