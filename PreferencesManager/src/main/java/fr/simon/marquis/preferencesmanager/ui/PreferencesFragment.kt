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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AbsListView.MultiChoiceModeListener
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.model.PreferenceSortType
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import fr.simon.marquis.preferencesmanager.ui.PreferencesActivity.Companion.preferenceSortType
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
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

    private var resultFileEdit = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            loadingView!!.hide()
            gridView!!.hide()

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mIconUri = it.getParcelable(ARG_ICON_URI)
            mFile = it.getString(ARG_FILE)
            mPackageName = it.getString(ARG_PACKAGE_NAME)
        }

        retainInstance = true
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        lifecycleScope.executeAsyncTask(
            onPreExecute = {
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                val start = System.currentTimeMillis()
                Log.d(Utils.TAG, "Start reading $mFile")
                val content = Utils.readFile(mFile!!)
                val ms = System.currentTimeMillis() - start
                Log.d(Utils.TAG, "End reading $mFile --> $ms ms")
                PreferenceFile.fromXml(content)
            },
            onPostExecute = {
                updateListView(it, true)
            },
            onProgressUpdate = {
            }
        )
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
        val addEnabled = preferenceFile != null && preferenceFile!!.isValidPreferenceFile
        val addIcon = if (addEnabled)
            R.drawable.ic_action_add
        else
            R.drawable.ic_action_add_disabled

        menu.findItem(R.id.action_add).isEnabled = addEnabled
        menu.findItem(R.id.action_add).setIcon(addIcon)
        val sortAlpha = menu.findItem(R.id.action_sort_alpha)
        val sortType = menu.findItem(R.id.action_sort_type)
        sortAlpha.isChecked = false
        sortType.isChecked = false
        if (preferenceSortType == PreferenceSortType.ALPHANUMERIC) {
            sortAlpha.isChecked = true
        } else if (preferenceSortType == PreferenceSortType.TYPE_AND_ALPHANUMERIC) {
            sortType.isChecked = true
        }
        val restoreVisibility = mListener != null && mListener!!.canRestoreFile(mFile)
        menu.findItem(R.id.action_restore_file).isVisible = restoreVisibility
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
                        activity?.finish()
                    }
                }
                val intent = Intent(activity, FileEditorActivity::class.java)
                intent.putExtra(ARG_ICON_URI, mIconUri)
                intent.putExtra(ARG_FILE, mFile)
                intent.putExtra(ARG_PACKAGE_NAME, mPackageName)
                resultFileEdit.launch(intent)
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
            mListener!!.getBackups(mFile)?.let {
                RestoreDialogFragment.show(this, parentFragmentManager, mFile!!, it)
            }
        }
    }

    private fun updateFilter(s: String?): Boolean {
        val filter = if (!TextUtils.isEmpty(s)) s!!.trim { it <= ' ' } else null
        val adapter: PreferenceAdapter = gridView?.adapter as PreferenceAdapter? ?: return false
        adapter.setFilter(filter)
        adapter.filter.filter(filter)
        return true
    }

    private fun setSortType(type: PreferenceSortType) {
        if (PreferencesActivity.preferenceSortType != type) {
            PreferencesActivity.preferenceSortType = type
            activity?.invalidateOptionsMenu()
            PreferenceManager.getDefaultSharedPreferences(requireActivity())
                .edit()
                .putInt(PreferencesActivity.KEY_SORT_TYPE, type.ordinal)
                .apply()

            if (gridView!!.adapter != null && preferenceFile != null) {
                preferenceFile!!.updateSort()
                (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
            }
        }
    }

    @Suppress("Unchecked_Cast")
    private fun showPrefDialog(
        type: PreferenceType,
        editMode: Boolean = false,
        editKey: String? = null,
        obj: Any? = null
    ) {

        // This is hacky :(
        // TODO StringSet

        val mPreferenceType = PreferenceType.valueOf(type.name)
        var keyValue: Any? = null
        var mEditValue: Any? = null

        if (editMode) {
            when (type) {
                PreferenceType.BOOLEAN -> keyValue = obj as Boolean
                PreferenceType.FLOAT -> keyValue = obj as Float
                PreferenceType.INT -> keyValue = obj as Int
                PreferenceType.LONG -> keyValue = obj as Long
                PreferenceType.STRING -> keyValue = obj as String
                PreferenceType.STRINGSET -> {
                    val objArray = (keyValue as Set<String>).toTypedArray()
                    val stringArray = arrayOfNulls<String>(objArray.size)
                    for (i in stringArray.indices) {
                        stringArray[i] = objArray[i]
                    }
                    keyValue = stringArray
                }
                PreferenceType.UNSUPPORTED -> {
                    // Nothing
                }
            }
        }

        when (mPreferenceType) {
            PreferenceType.BOOLEAN -> mEditValue = keyValue
            PreferenceType.FLOAT -> mEditValue = keyValue
            PreferenceType.INT -> mEditValue = keyValue
            PreferenceType.LONG -> mEditValue = keyValue
            PreferenceType.STRING -> mEditValue = keyValue
            PreferenceType.STRINGSET -> mEditValue = keyValue
            PreferenceType.UNSUPPORTED -> {
            }
        }

        val dialog = MaterialDialog(requireActivity())
            .title(if (editMode) type.dialogTitleEdit else type.dialogTitleAdd)
            .customView(R.layout.dialog_layout)
            .positiveButton(if (editMode) R.string.dialog_update else R.string.dialog_add) {
                val view = it.getCustomView()
                val editable = view.findViewById<TextInputEditText>(R.id.key_edit_text)
                var key = ""
                if (editable != null) {
                    key = editable.text.toString()
                }

                var value: Any? = null
                when (mPreferenceType) {
                    PreferenceType.BOOLEAN ->
                        value = it.getCustomView()
                            .findViewById<Switch>(R.id.value_boolean).isChecked
                    PreferenceType.INT ->
                        value = it.getCustomView()
                            .findViewById<TextInputEditText>(R.id.value_edit_text)
                            .text.toString().toInt()
                    PreferenceType.STRING ->
                        value = it.getCustomView()
                            .findViewById<TextInputEditText>(R.id.value_edit_text)
                            .text.toString()
                    PreferenceType.FLOAT ->
                        value = it.getCustomView()
                            .findViewById<TextInputEditText>(R.id.value_edit_text)
                            .text.toString().toFloat()
                    PreferenceType.LONG ->
                        value = it.getCustomView()
                            .findViewById<TextInputEditText>(R.id.value_edit_text)
                            .text.toString().toLong()
                    PreferenceType.STRINGSET -> {
                        // val set = HashSet<String>()
                        // val container = mValue as LinearLayout?
                        // for (i in 0 until container!!.childCount) {
                        //    set.add((((container.getChildAt(i) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1) as EditText).text.toString())
                        // }
                        // value = set
                    }
                    PreferenceType.UNSUPPORTED -> {
                    }
                }
                addPrefKeyValue(editKey, key, value, editMode)
            }
            .negativeButton(R.string.dialog_cancel)

        // Init Values
        if (editMode) {
            dialog.getCustomView()
                .findViewById<TextInputEditText>(R.id.key_edit_text)
                .setText(editKey)

            when (mPreferenceType) {
                PreferenceType.BOOLEAN -> {
                    dialog.getCustomView()
                        .findViewById<TextInputLayout>(R.id.value_input_layout)
                        .visibility = View.GONE
                    dialog.getCustomView()
                        .findViewById<Switch>(R.id.value_boolean)
                        .visibility = View.VISIBLE
                    dialog.getCustomView()
                        .findViewById<Switch>(R.id.value_boolean)
                        .isChecked = mEditValue as Boolean
                }
                PreferenceType.FLOAT,
                PreferenceType.INT,
                PreferenceType.LONG,
                PreferenceType.STRING -> {
                    dialog.getCustomView()
                        .findViewById<TextInputEditText>(R.id.value_edit_text)
                        .setText(mEditValue.toString())
                }
                PreferenceType.STRINGSET -> {
                    //    val array = mEditValue as Array<String>?
                    //    for (anArray in array!!) {
                    //        addStringSetEntry(false, anArray)
                    //    }
                }
                PreferenceType.UNSUPPORTED -> {
                }
            }
        } else {
            when (mPreferenceType) {
                PreferenceType.BOOLEAN -> {
                    with(dialog.getCustomView()) {
                        findViewById<TextInputEditText>(R.id.value_edit_text).visibility = View.GONE
                        findViewById<Switch>(R.id.value_boolean).visibility = View.VISIBLE
                        findViewById<Switch>(R.id.value_boolean).isChecked = true
                    }
                }
                // PreferenceType.STRINGSET -> if ((mValue as LinearLayout).childCount == 0) {
                //    addStringSetEntry(false, null)
                // }
                else -> {
                }
            }
        }

        if (editMode) {
            @Suppress("DEPRECATION")
            dialog.neutralButton(R.string.dialog_suppr) {
                deletePref(editKey!!)
            }
        }

        if (mPreferenceType == PreferenceType.STRINGSET) {
            dialog.getCustomView()
                .findViewById<Button>(R.id.dialog_add).apply {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        // addStringSetEntry(true, null)
                    }
                }
        }

        dialog.show()
    }

    private fun addPrefKeyValue(
        previousKey: String?,
        newKey: String?,
        value: Any?,
        editMode: Boolean
    ) {
        if (preferenceFile == null) {
            return
        }
        preferenceFile!!.add(previousKey, newKey, value, editMode)
        Utils.savePreferences(preferenceFile, mFile!!, mPackageName!!, requireActivity())
        (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
    }

    private fun deletePref(key: String) {
        if (preferenceFile == null) {
            return
        }
        preferenceFile!!.removeValue(key)
        Utils.savePreferences(preferenceFile, mFile!!, mPackageName!!, requireActivity())
        (gridView!!.adapter as PreferenceAdapter).notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as OnPreferenceFragmentInteractionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(
                "$context must implement OnPreferenceFragmentInteractionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    internal fun updateListView(p: PreferenceFile?, animate: Boolean) {

        if (activity == null || requireActivity().isFinishing) {
            return
        }

        if (p == null) {
            requireActivity().finish()
            return
        }

        preferenceFile = p
        emptyViewText!!.setText(
            if (preferenceFile!!.isValidPreferenceFile)
                R.string.empty_preference_file_valid
            else
                R.string.empty_preference_file_invalid
        )
        loadingView!!.hide()
        gridView!!.show()

        if (animate) {
            if (activity != null) {
                loadingView!!.startAnimation(
                    AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)
                )
                gridView!!.startAnimation(
                    AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
                )
            }
        }

        gridView!!.adapter = PreferenceAdapter(requireActivity(), this)
        gridView!!.emptyView = emptyViewText
        gridView!!.setOnItemClickListener { _, _, arg2, _ ->

            val item = gridView!!.adapter.getItem(arg2) as Entry<*, *>
            val type = PreferenceType.fromObject(item.value!!)
            if (type == PreferenceType.UNSUPPORTED) {
                Toast.makeText(activity, R.string.preference_unsupported, Toast.LENGTH_SHORT).show()
            } else {
                showPrefDialog(type, true, item.key as String, item.value)
            }
        }
        gridView!!.setMultiChoiceModeListener(object : MultiChoiceModeListener {

            override fun onItemCheckedStateChanged(
                mode: ActionMode,
                position: Int,
                id: Long,
                checked: Boolean
            ) {
                (gridView!!.adapter as PreferenceAdapter).itemCheckedStateChanged(position, checked)
                @Suppress("DEPRECATION")
                mode.title = Html.fromHtml("<b>" + gridView!!.checkedItemCount + "</b> selected")
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
                (activity as PreferencesActivity).supportActionBar?.hide()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                (activity as PreferencesActivity).supportActionBar?.show()
                (gridView!!.adapter as PreferenceAdapter).resetSelection()
                activity!!.invalidateOptionsMenu()
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }
        })
        requireActivity().invalidateOptionsMenu()
    }

    interface OnPreferenceFragmentInteractionListener {

        fun onBackupFile(fullPath: String?)

        fun canRestoreFile(fullPath: String?): Boolean

        fun getBackups(fullPath: String?): List<String>?
    }

    companion object {
        const val ARG_ICON_URI = "ICON_URI"
        const val ARG_FILE = "FILE"
        const val ARG_PACKAGE_NAME = "PACKAGE_NAME"

        fun newInstance(
            paramFile: String,
            paramPackageName: String,
            paramIconUri: Uri
        ): PreferencesFragment {
            val fragment = PreferencesFragment()
            val args = Bundle().apply {
                putParcelable(ARG_ICON_URI, paramIconUri)
                putString(ARG_FILE, paramFile)
                putString(ARG_PACKAGE_NAME, paramPackageName)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
