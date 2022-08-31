package fr.simon.marquis.preferencesmanager.ui

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

import android.os.Build.VERSION
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorTagEnum.*
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme.ColorThemeEnum
import fr.simon.marquis.preferencesmanager.model.XmlFontSize
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_FILE
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_PACKAGE_NAME
import fr.simon.marquis.preferencesmanager.util.Utils
import java.util.regex.Pattern

class FileEditorActivity : AppCompatActivity(), TextWatcher {

    private var mXmlFontSize: XmlFontSize? = null
    private var mColorTheme: ColorThemeEnum? = null
    private var mXmlColorTheme: XmlColorTheme? = null

    private var mFile: String? = null
    private var mTitle: String? = null
    private var mPackageName: String? = null
    private var mEditText: EditText? = null
    private var mHasContentChanged: Boolean = false
    private var mNeedUpdateOnActivityFinish = false

    private val onBackPressed = {
        if (mHasContentChanged) {
            showSavePopup()
        }
    }

    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        setContentView(R.layout.activity_file_editor)

        if (VERSION.SDK_INT >= 33) {
            val priority = OnBackInvokedDispatcher.PRIORITY_DEFAULT
            onBackInvokedDispatcher.registerOnBackInvokedCallback(priority) {
                onBackPressed
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        onBackPressed
                    }
                }
            )
        }

        val intent = intent.extras
        if (intent == null) {
            finish()
            return
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mEditText = findViewById(R.id.editText)
        mFile = intent.getString(KEY_FILE)
        mTitle = extractFileName(mFile!!)
        mPackageName = intent.getString(KEY_PACKAGE_NAME)

        // Hack to prevent EditText to request focus when the Activity is created
        mEditText!!.post {
            mEditText!!.isFocusable = true
            mEditText!!.isFocusableInTouchMode = true
        }

        if (arg0 == null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            mEditText!!.setText(Utils.readFile(mFile!!))
            mColorTheme = ColorThemeEnum.valueOf(
                prefs.getString(
                    KEY_COLOR_THEME,
                    ColorThemeEnum.ECLIPSE.name
                )!!
            )
            setXmlFontSize(
                XmlFontSize.generateSize(
                    prefs.getInt(
                        KEY_FONT_SIZE,
                        XmlFontSize.MEDIUM.size
                    )
                )
            )
        } else {
            mHasContentChanged = arg0.getBoolean(KEY_HAS_CONTENT_CHANGED, false)
            mNeedUpdateOnActivityFinish = arg0.getBoolean(KEY_NEED_UPDATE_ON_ACTIVITY_FINISH, false)
            if (mNeedUpdateOnActivityFinish) {
                setResult(RESULT_OK)
            }
            mColorTheme = ColorThemeEnum.valueOf(arg0.getString(KEY_COLOR_THEME)!!)
            setXmlFontSize(XmlFontSize.generateSize(arg0.getInt(KEY_FONT_SIZE)))
        }

        mXmlColorTheme = XmlColorTheme.createTheme(this, mColorTheme!!)

        updateTitle()
        invalidateOptionsMenu()

        highlightXMLText(mEditText!!.text)

        mEditText!!.clearFocus()
    }

    override fun onResume() {
        mEditText!!.addTextChangedListener(this)
        super.onResume()
    }

    override fun onPause() {
        mEditText!!.removeTextChangedListener(this)
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_HAS_CONTENT_CHANGED, mHasContentChanged)
        outState.putString(KEY_COLOR_THEME, mColorTheme!!.name)
        outState.putInt(KEY_FONT_SIZE, mXmlFontSize!!.size)
        outState.putBoolean(KEY_NEED_UPDATE_ON_ACTIVITY_FINISH, mNeedUpdateOnActivityFinish)
        super.onSaveInstanceState(outState)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val saveIcon = if (mHasContentChanged)
            R.drawable.ic_action_save
        else
            R.drawable.ic_action_save_disabled

        menu.findItem(R.id.action_save).setEnabled(mHasContentChanged).setIcon(saveIcon)
        menu.findItem(R.id.action_theme_eclipse).isChecked = false
        menu.findItem(R.id.action_theme_google).isChecked = false
        menu.findItem(R.id.action_theme_roboticket).isChecked = false
        menu.findItem(R.id.action_theme_notepad).isChecked = false
        menu.findItem(R.id.action_theme_netbeans).isChecked = false

        when (mColorTheme) {
            ColorThemeEnum.ECLIPSE ->
                menu.findItem(R.id.action_theme_eclipse).isChecked = true
            ColorThemeEnum.GOOGLE ->
                menu.findItem(R.id.action_theme_google).isChecked = true
            ColorThemeEnum.ROBOTICKET ->
                menu.findItem(R.id.action_theme_roboticket).isChecked = true
            ColorThemeEnum.NOTEPAD ->
                menu.findItem(R.id.action_theme_notepad).isChecked = true
            ColorThemeEnum.NETBEANS ->
                menu.findItem(R.id.action_theme_netbeans).isChecked = true
            else -> Unit
        }

        menu.findItem(R.id.action_size_extra_small).isChecked = false
        menu.findItem(R.id.action_size_small).isChecked = false
        menu.findItem(R.id.action_size_medium).isChecked = false
        menu.findItem(R.id.action_size_large).isChecked = false
        menu.findItem(R.id.action_size_extra_large).isChecked = false

        when (mXmlFontSize) {
            XmlFontSize.EXTRA_SMALL ->
                menu.findItem(R.id.action_size_extra_small).isChecked = true
            XmlFontSize.SMALL ->
                menu.findItem(R.id.action_size_small).isChecked = true
            XmlFontSize.MEDIUM ->
                menu.findItem(R.id.action_size_medium).isChecked = true
            XmlFontSize.LARGE ->
                menu.findItem(R.id.action_size_large).isChecked = true
            XmlFontSize.EXTRA_LARGE ->
                menu.findItem(R.id.action_size_extra_large).isChecked = true
            else -> Unit
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.file_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (mHasContentChanged) {
                showSavePopup()
            } else {
                finish()
            }
            R.id.action_save -> save()
            R.id.action_theme_eclipse -> setXmlColorTheme(ColorThemeEnum.ECLIPSE)
            R.id.action_theme_google -> setXmlColorTheme(ColorThemeEnum.GOOGLE)
            R.id.action_theme_roboticket -> setXmlColorTheme(ColorThemeEnum.ROBOTICKET)
            R.id.action_theme_notepad -> setXmlColorTheme(ColorThemeEnum.NOTEPAD)
            R.id.action_theme_netbeans -> setXmlColorTheme(ColorThemeEnum.NETBEANS)
            R.id.action_size_extra_small -> setXmlFontSize(XmlFontSize.EXTRA_SMALL)
            R.id.action_size_small -> setXmlFontSize(XmlFontSize.SMALL)
            R.id.action_size_medium -> setXmlFontSize(XmlFontSize.MEDIUM)
            R.id.action_size_large -> setXmlFontSize(XmlFontSize.LARGE)
            R.id.action_size_extra_large -> setXmlFontSize(XmlFontSize.EXTRA_LARGE)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun extractFileName(s: String): String? {
        val fileSeparator = System.getProperty("file.separator")
        return if (TextUtils.isEmpty(s)) {
            null
        } else s.substring(s.lastIndexOf(fileSeparator!!) + 1)
    }

    private fun setXmlFontSize(size: XmlFontSize) {
        if (mXmlFontSize != size) {
            mXmlFontSize = size
            invalidateOptionsMenu()
            mEditText!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, mXmlFontSize!!.size.toFloat())

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(KEY_FONT_SIZE, mXmlFontSize!!.size)
                .apply()
        }
    }

    private fun setXmlColorTheme(theme: ColorThemeEnum) {
        if (mColorTheme != theme) {
            mColorTheme = theme
            mXmlColorTheme = XmlColorTheme.createTheme(this, mColorTheme!!)
            invalidateOptionsMenu()
            highlightXMLText(mEditText!!.text)

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(KEY_COLOR_THEME, mColorTheme!!.name)
                .apply()
        }
    }

    private fun save(): Boolean {
        val editable = mEditText!!.text
        val preferences = editable?.toString() ?: ""
        val pref = PreferenceFile.fromXml(preferences)
        return if (Utils.savePreferences(pref, mFile!!, mPackageName!!, this)) {
            mNeedUpdateOnActivityFinish = true
            setResult(RESULT_OK)
            Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()
            hideSoftKeyboard(mEditText!!)
            mHasContentChanged = false
            updateTitle()
            invalidateOptionsMenu()
            true
        } else {
            Toast.makeText(this, R.string.save_fail, Toast.LENGTH_SHORT).show()
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun updateTitle() {
        val content = if (mHasContentChanged) "<font color='#33b5e5'><b>&#9679;</b></font> " else ""
        val str = Html.fromHtml(content + mTitle!!)
        val actionBar = actionBar
        if (actionBar != null) {
            actionBar.title = str
        }
    }

    private fun clearSpans(source: Spannable) {
        val toRemoveSpans = source.getSpans(0, source.length, ForegroundColorSpan::class.java)
        for (toRemoveSpan in toRemoveSpans) {
            source.removeSpan(toRemoveSpan)
        }
    }

    private fun highlightXMLText(source: Spannable) {
        clearSpans(source)
        generateSpan(source, TAG_START, mXmlColorTheme!!.getColor(TAG))
        generateSpan(source, TAG_END, mXmlColorTheme!!.getColor(TAG))
        generateSpan(source, TAG_ATTRIBUTE_VALUE, mXmlColorTheme!!.getColor(ATTR_VALUE))
        generateSpan(source, TAG_ATTRIBUTE_VALUE_2, mXmlColorTheme!!.getColor(ATTR_VALUE))
        generateSpan(source, TAG_ATTRIBUTE_NAME, mXmlColorTheme!!.getColor(ATTR_NAME))
        generateSpan(source, COMMENT_START, mXmlColorTheme!!.getColor(COMMENT))
        generateSpan(source, COMMENT_END, mXmlColorTheme!!.getColor(COMMENT))
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun afterTextChanged(s: Editable) {
        if (!mHasContentChanged) {
            mHasContentChanged = true
            updateTitle()
            invalidateOptionsMenu()
        }
        highlightXMLText(mEditText!!.text)
    }

    private fun showSavePopup() {
        TODO("Re-implement save popup")
//        MaterialDialog(this).show {
//            title(text = mTitle)
//            message(R.string.popup_edit_message)
//            icon(R.drawable.ic_action_edit)
//            positiveButton(R.string.yes) {
//                if (save())
//                    finish()
//            }
//            negativeButton(R.string.no) {
//                finish()
//            }
//        }
    }

    @Suppress("RegExpRedundantEscape")
    companion object {
        private val TAG_START = Pattern.compile("</?[-\\w\\?]+", Pattern.CASE_INSENSITIVE)
        private val TAG_END = Pattern.compile("\\??/?>")
        private val TAG_ATTRIBUTE_NAME = Pattern.compile("\\s(\\w*)\\=")
        private val TAG_ATTRIBUTE_VALUE = Pattern.compile("[a-z\\-]*\\=(\"[^\"]*\")")
        private val TAG_ATTRIBUTE_VALUE_2 = Pattern.compile("[a-z\\-]*\\=(\'[^\']*\')")
        private val COMMENT_START = Pattern.compile("<!--")
        private val COMMENT_END = Pattern.compile("-->")

        private const val KEY_HAS_CONTENT_CHANGED = "HAS_CONTENT_CHANGED"
        private const val KEY_COLOR_THEME = "KEY_COLOR_THEME"
        private const val KEY_FONT_SIZE = "KEY_FONT_SIZE"
        private const val KEY_NEED_UPDATE_ON_ACTIVITY_FINISH = "NEED_UPDATE_ON_ACTIVITY_FINISH"

        private fun generateSpan(source: Spannable, p: Pattern, color: Int) {
            val matcher = p.matcher(source)
            var start: Int
            var end: Int
            while (matcher.find()) {
                start = matcher.start()
                end = matcher.end()
                if (start != end) {
                    source.setSpan(
                        ForegroundColorSpan(color),
                        matcher.start(),
                        matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }
}
