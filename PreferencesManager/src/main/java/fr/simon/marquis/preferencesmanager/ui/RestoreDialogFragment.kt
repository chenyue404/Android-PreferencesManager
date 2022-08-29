// /*
// * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
// package fr.simon.marquis.preferencesmanager.ui
//
// import android.annotation.SuppressLint
// import android.app.Dialog
// import android.content.Context
// import android.os.Bundle
// import android.text.TextUtils
// import android.view.LayoutInflater
// import android.view.View
// import android.view.ViewGroup
// import android.widget.AdapterView
// import android.widget.ListView
// import androidx.fragment.app.DialogFragment
// import androidx.fragment.app.FragmentManager
// import fr.simon.marquis.preferencesmanager.R
// import fr.simon.marquis.preferencesmanager.model.PreferenceFile
// import java.util.ArrayList
// import org.json.JSONArray
// import org.json.JSONException
//
// class RestoreDialogFragment : DialogFragment(), AdapterView.OnItemClickListener {
//
//    private var listener: OnRestoreFragmentInteractionListener? = null
//    private var backups: MutableList<String>? = null
//    private var mFullPath: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            mFullPath = it.getString(ARG_FULL_PATH)
//            backups = ArrayList()
//            try {
//                val array = JSONArray(it.getString(ARG_BACKUPS))
//                for (i in 0 until array.length()) {
//                    val backup = array.optString(i)
//                    if (!TextUtils.isEmpty(backup)) {
//                        backups!!.add(backup)
//                    }
//                }
//            } catch (ignore: JSONException) {
//            }
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        if (activity == null) {
//            return null
//        }
//        @SuppressLint("InflateParams")
//        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_restore, null)!!
//        val listView = view.findViewById<ListView>(R.id.listView)
//        listView.adapter = RestoreAdapter(requireActivity(), this, backups, listener!!, mFullPath!!)
//        listView.onItemClickListener = this
//        return view
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.setTitle(R.string.pick_restore)
//        return dialog
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            listener = context as OnRestoreFragmentInteractionListener?
//        } catch (e: ClassCastException) {
//            throw ClassCastException("$context must implement OnRestoreFragmentInteractionListener")
//        }
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        listener = null
//    }
//
//    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
//        if (listener != null) {
//            val data = listener!!.onRestoreFile(backups!![position], mFullPath)
//            val fragment = targetFragment as PreferencesFragment?
//            fragment?.updateListView(PreferenceFile.fromXml(data), true)
//            dismiss(fragmentManager)
//        }
//    }
//
//    fun noMoreBackup() {
//        dismiss(fragmentManager)
//        val fragment = targetFragment as PreferencesFragment?
//        if (fragment != null && fragment.activity != null) {
//            fragment.requireActivity().invalidateOptionsMenu()
//        }
//    }
//
//    interface OnRestoreFragmentInteractionListener {
//        fun onRestoreFile(backup: String, fullPath: String?): String
//
//        fun onDeleteBackup(backup: String, fullPath: String): List<String>?
//    }
//
//    companion object {
//
//        private const val TAG = "RestoreDialogFragment"
//        private const val ARG_FULL_PATH = "FULL_PATH"
//        private const val ARG_BACKUPS = "BACKUPS"
//
//        fun show(
//            target: PreferencesFragment,
//            fm: FragmentManager,
//            fullPath: String,
//            backups: List<String>
//        ) {
//            dismiss(fm)
//            newInstance(fullPath, backups).apply {
//                setTargetFragment(target, "Fragment:$fullPath".hashCode())
//                show(fm, TAG)
//            }
//        }
//
//        private fun newInstance(fullPath: String, backups: List<String>): RestoreDialogFragment {
//            val dialog = RestoreDialogFragment()
//            val array = JSONArray(backups)
//            val args = Bundle().apply {
//                putString(ARG_FULL_PATH, fullPath)
//                putString(ARG_BACKUPS, array.toString())
//            }
//            dialog.arguments = args
//            return dialog
//        }
//
//        private fun dismiss(fm: FragmentManager?) {
//            find(fm!!)?.dismiss()
//        }
//
//        private fun find(fm: FragmentManager): RestoreDialogFragment? {
//            return fm.findFragmentByTag(TAG) as RestoreDialogFragment?
//        }
//    }
// }
