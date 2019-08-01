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
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import java.util.*

class PreferenceDialog : DialogFragment() {

    private var mKey: EditText? = null
    private var mValue: View? = null

    private var mPreferenceType: PreferenceType? = null
    private var mEditMode: Boolean = false
    private var mEditKey: String? = null
    private var mEditValue: Any? = null

    private var mBtnOK: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = arguments ?: return

        mPreferenceType = PreferenceType.valueOf(b.getString(KEY_TYPE)!!)
        mEditMode = b.getBoolean(KEY_EDIT_MODE)
        mEditKey = b.getString(KEY_EDIT_KEY)

        when (mPreferenceType) {
            PreferenceType.BOOLEAN -> mEditValue = b.getBoolean(KEY_EDIT_VALUE)
            PreferenceType.FLOAT -> mEditValue = b.getFloat(KEY_EDIT_VALUE)
            PreferenceType.INT -> mEditValue = b.getInt(KEY_EDIT_VALUE)
            PreferenceType.LONG -> mEditValue = b.getLong(KEY_EDIT_VALUE)
            PreferenceType.STRING -> mEditValue = b.getString(KEY_EDIT_VALUE)
            PreferenceType.STRINGSET -> mEditValue = b.getStringArray(KEY_EDIT_VALUE)
            PreferenceType.UNSUPPORTED -> {
            }
        }

        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // remove the background of the regular Dialog
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        val view = buildView()

        initValues()

        createValidator()

        // first validate to correctly disable the OK button
        validate()

        return view
    }

    private fun initValues() {
        if (mEditMode) {
            mKey!!.setText(mEditKey)
            when (mPreferenceType) {
                PreferenceType.BOOLEAN -> (mValue as CompoundButton).isChecked = (mEditValue as Boolean?)!!
                PreferenceType.FLOAT, PreferenceType.INT, PreferenceType.LONG, PreferenceType.STRING -> (mValue as EditText).setText(mEditValue!!.toString())
                PreferenceType.STRINGSET -> {
                    val array = mEditValue as Array<String>?
                    for (anArray in array!!) {
                        addStringSetEntry(false, anArray)
                    }
                }
                PreferenceType.UNSUPPORTED -> {
                }
            }
        } else {
            when (mPreferenceType) {
                PreferenceType.BOOLEAN -> (mValue as CompoundButton).isChecked = true
                PreferenceType.STRINGSET -> if ((mValue as LinearLayout).childCount == 0) {
                    addStringSetEntry(false, null)
                }
                else -> {
                }
            }
        }
    }

    private fun addStringSetEntry(changeFocus: Boolean, value: String?) {
        if (activity == null) {
            return
        }

        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = mValue as LinearLayout?
        @SuppressLint("InflateParams")
        val item = inflater.inflate(R.layout.row_stringset, null) as LinearLayout
        layout?.addView(item)
        val editText = (item.getChildAt(0) as ViewGroup).getChildAt(1) as EditText
        val child = item.getChildAt(1)
        child?.setOnClickListener {
            if ((mValue as LinearLayout).childCount > 0) {
                (mValue as LinearLayout).removeView(item)
            } else {
                val childRoot = (mValue as ViewGroup).getChildAt(0)
                if (childRoot != null) {
                    val childEditText = (childRoot as ViewGroup).getChildAt(0) as EditText
                    childEditText.text = null
                }
            }
            validate()
        }

        if (changeFocus) {
            editText.requestFocus()
        }

        editText.setText(value)
    }

    private fun createValidator() {
        val textWatcher = object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                validate()
            }
        }

        mKey!!.addTextChangedListener(textWatcher)

        when (mPreferenceType) {
            PreferenceType.STRING,
            PreferenceType.FLOAT,
            PreferenceType.LONG,
            PreferenceType.INT -> (mValue as EditText).addTextChangedListener(textWatcher)
            PreferenceType.BOOLEAN,
            PreferenceType.STRINGSET -> {
            }
            PreferenceType.UNSUPPORTED -> {
            }
        }
    }

    private fun generateTitle(): String {
        return getString(if (mEditMode) mPreferenceType!!.dialogTitleEdit else mPreferenceType!!.dialogTitleAdd)
    }

    private fun buildView(): View? {
        if (activity == null) {
            return null
        }
        val layout = mPreferenceType!!.getDialogLayout(mEditMode)
        val view = activity!!.layoutInflater.inflate(layout, null)!!
        view.setBackgroundResource(R.color.gray)

        mKey = view.findViewById(R.id.key)
        mValue = view.findViewById(R.id.value)
        (view.findViewById<View>(R.id.title) as TextView).text = generateTitle()

        val mBtnKO = view.findViewById<Button>(R.id.btnKO)
        mBtnKO.setOnClickListener { dismiss() }
        mBtnOK = view.findViewById(R.id.btnOK)
        mBtnOK!!.setOnClickListener {
            performOK()
            dismiss()
        }
        if (mEditMode) {
            val mBtnSuppr = view.findViewById<Button>(R.id.btnSUPPR)
            mBtnSuppr.setOnClickListener {
                performSuppr()
                dismiss()
            }
        }
        if (mPreferenceType == PreferenceType.STRINGSET) {
            val mBtnAddEntrySet = view.findViewById<Button>(R.id.action_add_stringset_entry)
            mBtnAddEntrySet.setOnClickListener { addStringSetEntry(true, null) }
        }
        return view
    }

    private fun performSuppr() {
        val fragment = targetFragment as PreferencesFragment? ?: return
        fragment.deletePref(mEditKey!!)
    }

    private fun performOK() {
        val fragment = targetFragment as PreferencesFragment? ?: return

        if (validate()) {
            val editable = mKey!!.text
            var key = ""
            if (editable != null) {
                key = editable.toString()
            }

            var value: Any? = null

            when (mPreferenceType) {
                PreferenceType.BOOLEAN -> value = (mValue as CompoundButton).isChecked
                PreferenceType.INT -> value = (mValue as EditText).text.toString().toInt()
                PreferenceType.STRING -> value = (mValue as EditText).text.toString()
                PreferenceType.FLOAT -> value = (mValue as EditText).text.toString().toFloat()
                PreferenceType.LONG -> value = (mValue as EditText).text.toString().toLong()
                PreferenceType.STRINGSET -> {
                    val set = HashSet<String>()
                    val container = mValue as LinearLayout?
                    for (i in 0 until container!!.childCount) {
                        set.add((((container.getChildAt(i) as ViewGroup).getChildAt(0) as ViewGroup).getChildAt(1) as EditText).text.toString())
                    }
                    value = set
                }
                PreferenceType.UNSUPPORTED -> {
                }
            }
            fragment.addPrefKeyValue(mEditKey, key, value, mEditMode)
        }
    }

    private fun validate(): Boolean {
        val editable = mKey!!.text
        var key = ""
        if (editable != null) {
            key = editable.toString().trim { it <= ' ' }
        }
        val keyValid = !TextUtils.isEmpty(key)
        var valueValid = false
        try {
            when (mPreferenceType) {
                PreferenceType.BOOLEAN, PreferenceType.STRINGSET, PreferenceType.STRING -> valueValid = true
                PreferenceType.FLOAT -> {
                    val f = java.lang.Float.parseFloat((mValue as EditText).text.toString().trim { it <= ' ' })
                    valueValid = !java.lang.Float.isInfinite(f) && !java.lang.Float.isNaN(f)
                }
                PreferenceType.LONG -> {
                    java.lang.Long.parseLong((mValue as EditText).text.toString().trim { it <= ' ' })
                    valueValid = true
                }
                PreferenceType.INT -> {
                    Integer.parseInt((mValue as EditText).text.toString().trim { it <= ' ' })
                    valueValid = true
                }
                PreferenceType.UNSUPPORTED -> {
                }
            }
        } catch (e: NumberFormatException) {
            valueValid = false
        }

        mBtnOK!!.isEnabled = keyValid && valueValid
        return keyValid && valueValid
    }

    companion object {
        private const val KEY_TYPE = "KEY_TYPE"
        private const val KEY_EDIT_MODE = "KEY_EDIT_MODE"
        private const val KEY_EDIT_KEY = "KEY_EDIT_KEY"
        private const val KEY_EDIT_VALUE = "KEY_EDIT_VALUE"

        fun newInstance(type: PreferenceType, editMode: Boolean, editKey: String?, editValue: Any?): PreferenceDialog {
            val frag = PreferenceDialog()
            val args = Bundle()
            args.putString(KEY_TYPE, type.name)
            args.putBoolean(KEY_EDIT_MODE, editMode)
            args.putString(KEY_EDIT_KEY, editKey)
            if (editMode) {
                when (type) {
                    PreferenceType.BOOLEAN -> args.putBoolean(KEY_EDIT_VALUE, editValue as Boolean)
                    PreferenceType.FLOAT -> args.putFloat(KEY_EDIT_VALUE, editValue as Float)
                    PreferenceType.INT -> args.putInt(KEY_EDIT_VALUE, editValue as Int)
                    PreferenceType.LONG -> args.putLong(KEY_EDIT_VALUE, editValue as Long)
                    PreferenceType.STRING -> args.putString(KEY_EDIT_VALUE, editValue as String)
                    PreferenceType.STRINGSET -> {
                        val objArray = (editValue as Set<String>).toTypedArray()
                        val stringArray = arrayOfNulls<String>(objArray.size)
                        for (i in stringArray.indices) {
                            stringArray[i] = objArray[i]
                        }
                        args.putStringArray(KEY_EDIT_VALUE, stringArray)
                    }
                    PreferenceType.UNSUPPORTED -> {
                    }
                }
            }
            frag.arguments = args
            return frag
        }
    }
}