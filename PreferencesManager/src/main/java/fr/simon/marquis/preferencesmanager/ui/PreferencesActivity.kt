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
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import org.json.JSONArray

import java.util.ArrayList
import java.util.Date

import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.PreferenceSortType
import fr.simon.marquis.preferencesmanager.ui.PreferencesFragment.OnPreferenceFragmentInteractionListener
import fr.simon.marquis.preferencesmanager.util.Utils
import kotlinx.android.synthetic.main.activity_preferences.*

class PreferencesActivity : AppCompatActivity(), OnPreferenceFragmentInteractionListener, RestoreDialogFragment.OnRestoreFragmentInteractionListener {

    private var mViewPager: ViewPager? = null
    private var mLoadingView: View? = null
    private var mEmptyView: View? = null

    private var iconUri: Uri? = null
    private var files: List<String>? = null
    private var pkgName: String? = null
    private var title: String? = null

    private var backupContainer: BackupContainer? = null

    private var findFilesAndBackupsTask: FindFilesAndBackupsTask? = null

    private var launchedFromShortcut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(App.theme.theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent = intent.extras
        if (intent == null) {
            finish()
            return
        }

        val index = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_SORT_TYPE, 0)
        preferenceSortType = PreferenceSortType.values()[index]

        mViewPager = findViewById(R.id.pager)
        mLoadingView = findViewById(R.id.loadingView)
        mEmptyView = findViewById(R.id.emptyView)

        iconUri = intent.getParcelable(KEY_ICON_URI)
        pkgName = intent.getString(EXTRA_PACKAGE_NAME)
        title = intent.getString(EXTRA_TITLE)
        launchedFromShortcut = intent.getBoolean(EXTRA_SHORTCUT, false)

        //Custom toolbar
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_package_name)
        val toolbarSubTitle = findViewById<TextView>(R.id.toolbar_package)
        val image = findViewById<ImageView>(R.id.toolbar_icon)

        val appName = findViewById<LinearLayout>(R.id.toolbar_app_name)
        appName.visibility = View.GONE

        toolbarTitle.isSelected = true
        toolbarSubTitle.isSelected = true

        toolbarTitle.text = title
        toolbarSubTitle.text = pkgName

        Glide.with(this)
                .load(iconUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_launcher)
                )
                .into(image)


        if (savedInstanceState == null) {
            findFilesAndBackupsTask = FindFilesAndBackupsTask(pkgName!!)
            findFilesAndBackupsTask!!.execute()
        } else {
            try {
                val tmp = ArrayList<String>()
                val array = JSONArray(savedInstanceState.getString(KEY_FILES))
                for (i in 0 until array.length()) {
                    tmp.add(array.getString(i))
                }
                updateFindFiles(tmp)
                updateFindBackups(Utils.getBackups(applicationContext, pkgName!!))
            } catch (e: Exception) {
                findFilesAndBackupsTask = FindFilesAndBackupsTask(pkgName!!)
                findFilesAndBackupsTask!!.execute()
            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (files != null) {
            val json = JSONArray()
            for (file in files!!) {
                json.put(file)
            }
            outState.putString(KEY_FILES, json.toString())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.preferences_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val fav = Utils.isFavorite(pkgName!!, this)
        val itemFav = menu.findItem(R.id.action_fav)
        itemFav?.setIcon(if (fav) R.drawable.ic_action_star_10 else R.drawable.ic_action_star_0)?.setTitle(if (fav) R.string.action_unfav else R.string.action_fav)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (launchedFromShortcut) {
                    val i = Intent(this, AppListActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(i)
                }
                finish()
                return true
            }
            R.id.action_fav -> {
                Utils.setFavorite(pkgName!!, !Utils.isFavorite(pkgName!!, this), this)
                invalidateOptionsMenu()
            }
            R.id.action_shortcut -> {
                createShortcut()
                Toast.makeText(this, R.string.toast_shortcut, Toast.LENGTH_SHORT).show()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createShortcut() {
        val shortcutIntent = Intent(this, PreferencesActivity::class.java)
        shortcutIntent.putExtra(EXTRA_PACKAGE_NAME, pkgName)
        shortcutIntent.putExtra(EXTRA_TITLE, title)
        shortcutIntent.putExtra(EXTRA_SHORTCUT, true)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val addIntent = Intent()
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher))
        addIntent.action = INSTALL_SHORTCUT
        sendBroadcast(addIntent)
    }

    override fun onBackupFile(fullPath: String?) {
        val backup = Date().time.toString()
        backupContainer!!.put(fullPath!!, backup)

        //TODO: asynctask
        if (Utils.backupFile(backup, fullPath, this)) {
            Utils.saveBackups(this, pkgName!!, backupContainer!!)
            Toast.makeText(this, R.string.toast_backup_success, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.toast_backup_fail, Toast.LENGTH_SHORT).show()
        }
    }

    override fun canRestoreFile(fullPath: String?): Boolean {
        return backupContainer != null && backupContainer!!.contains(fullPath!!)
    }

    override fun getBackups(fullPath: String?): List<String>? {
        return if (backupContainer == null) emptyList() else backupContainer!![fullPath!!]
    }

    override fun onRestoreFile(backup: String, fullPath: String?): String {
        Log.d(TAG, String.format("onRestoreFile(%s, %s)", backup, fullPath))
        if (Utils.restoreFile(this, backup, fullPath!!, pkgName!!)) {
            Toast.makeText(this, R.string.file_restored, Toast.LENGTH_SHORT).show()
        }
        return Utils.readFile(fullPath)
    }

    override fun onDeleteBackup(backup: String, fullPath: String): List<String>? {
        backupContainer!!.remove(fullPath, backup)
        deleteFile(backup)
        Utils.saveBackups(this, pkgName!!, backupContainer!!)
        invalidateOptionsMenu()
        return backupContainer!![fullPath]
    }

    private fun updateFindFiles(tmp: List<String>) {
        files = tmp
        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, files!!)
        mViewPager!!.adapter = mSectionsPagerAdapter
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

        if (files == null || files!!.isEmpty()) {
            if (fadeIn != null) {
                mEmptyView!!.startAnimation(fadeIn)
            }
            mEmptyView!!.visibility = View.VISIBLE
            if (fadeOut != null) {
                mLoadingView!!.startAnimation(fadeOut)
            }
            mLoadingView!!.visibility = View.GONE
        } else {
            mEmptyView!!.visibility = View.GONE
            mLoadingView!!.visibility = View.GONE
            if (fadeIn != null) {
                mViewPager!!.startAnimation(fadeIn)
            }
            mViewPager!!.visibility = View.VISIBLE
        }
    }

    private fun updateFindBackups(b: BackupContainer) {
        backupContainer = b
    }

    internal inner class SectionsPagerAdapter(fm: FragmentManager, private val mFiles: List<String>) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return PreferencesFragment.newInstance(mFiles[position], pkgName!!, iconUri!!)
        }

        override fun getCount(): Int {
            return mFiles.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return Utils.extractFileName(mFiles[position])
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class FindFilesAndBackupsTask(private val mPackageName: String) : AsyncTask<Void, Void, Pair<List<String>, BackupContainer>>() {

        override fun doInBackground(vararg params: Void): Pair<List<String>, BackupContainer> {
            return Pair.create(Utils.findXmlFiles(mPackageName), Utils.getBackups(applicationContext, mPackageName))
        }

        override fun onPostExecute(result: Pair<List<String>, BackupContainer>) {
            updateFindFiles(result.first)
            updateFindBackups(result.second)
            super.onPostExecute(result)
        }
    }

    override fun onDestroy() {
        if (findFilesAndBackupsTask != null) {
            findFilesAndBackupsTask!!.cancel(true)
        }
        super.onDestroy()
    }

    companion object {

        private val TAG = PreferencesActivity::class.java.simpleName
        const val KEY_SORT_TYPE = "KEY_SORT_TYPE"
        const val EXTRA_PACKAGE_NAME = "EXTRA_PACKAGE_NAME"
        const val KEY_ICON_URI = "KEY_ICON_URI"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val KEY_FILES = "KEY_FILES"
        private const val INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
        private const val EXTRA_SHORTCUT = "EXTRA_SHORTCUT"
        var preferenceSortType = PreferenceSortType.TYPE_AND_ALPHANUMERIC
    }
}
