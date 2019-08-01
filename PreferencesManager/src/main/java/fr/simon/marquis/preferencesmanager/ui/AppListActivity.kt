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
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.animateView
import fr.simon.marquis.preferencesmanager.util.hideSoftKeyboard
import kotlinx.android.synthetic.main.activity_app_list.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class AppListActivity : AppCompatActivity() {

    private var mAdapter: AppAdapter? = null
    private var mSearchView: SearchView? = null
    private var task: GetApplicationsTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(App.theme.theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)
        Utils.checkBackups(applicationContext)

        //Custom Toolbar
        setSupportActionBar(toolbar)

        toolbar_app_name.visibility = View.VISIBLE

        listView!!.isDrawingListUnderStickyHeader = false
        listView!!.setOnItemClickListener { _, _, arg2, _ ->
            if (isRootAccessGiven) {
                startPreferencesActivity(mAdapter!!.getItem(arg2) as AppEntry)
            } else {
                checkRoot()
            }
        }

        if (savedInstanceState == null) {
            checkRoot()
        }

        if (savedInstanceState == null || Utils.previousApps == null) {
            startTask()
        } else {
            updateListView(Utils.previousApps)
        }
    }

    private fun checkRoot() {

        isRootAccessGiven = Shell.rootAccess()

        Log.i(TAG, "Root Access: $isRootAccessGiven")

        if (!isRootAccessGiven)
            Utils.displayNoRoot(this@AppListActivity)
    }

    /**
     * Start the PreferencesActivity with supplied AppEntry
     *
     * @param app to browse
     */
    private fun startPreferencesActivity(app: AppEntry) {
        if (!Shell.rootAccess()) {
            Utils.displayNoRoot(this)
        } else {
            val i = Intent(this@AppListActivity, PreferencesActivity::class.java)
            i.putExtra(PreferencesActivity.KEY_ICON_URI, app.iconUri)
            i.putExtra(PreferencesActivity.EXTRA_TITLE, app.label)
            i.putExtra(PreferencesActivity.EXTRA_PACKAGE_NAME, app.applicationInfo.packageName)
            startActivityForResult(i, REQUEST_CODE_PREFERENCES_ACTIVITY)
        }
    }

    /**
     * @return true if a new task is started
     */
    private fun startTask(): Boolean {
        if (task == null || task!!.isCancelled) {
            task = GetApplicationsTask(this)
            task!!.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            return true
        }
        return false
    }

    /**
     * Update ListView with provided apps
     *
     * @param apps List of applications
     */
    private fun updateListView(apps: ArrayList<AppEntry>?) {
        mAdapter = AppAdapter(this, apps!!, emptyView!!)
        listView!!.adapter = mAdapter
        setListState(false)
    }

    /**
     * Switch ListView to loading/loaded state
     *
     * @param loading state to apply
     */
    private fun setListState(loading: Boolean) {
        animateView(loadingView!!, loading, loading)
        animateView(listView!!, !loading, !loading)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PREFERENCES_ACTIVITY) {
            if (mAdapter != null) {
                mAdapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_list_activity, menu)

        val searchItem = menu.findItem(R.id.menu_search)
        mSearchView = searchItem.actionView as SearchView
        mSearchView!!.queryHint = getString(R.string.action_search)
        mSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(s: String): Boolean {
                hideSoftKeyboard(mSearchView!!)
                mSearchView!!.clearFocus()
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                return updateFilter(s)
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return updateFilter(null)
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val show = Utils.isShowSystemApps(this)
        val showItem = menu.findItem(R.id.show_system_apps)
        if (showItem != null) {
            showItem.setTitle(if (show) R.string.hide_system_apps else R.string.show_system_apps)
            showItem.setIcon(if (show) R.drawable.ic_action_show else R.drawable.ic_action_hide)
        }
        val themeItem = menu.findItem(R.id.menu_switch_theme)
        themeItem?.setTitle(App.theme.title)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> aboutDialog()//AboutDialog.show(supportFragmentManager, false)
            R.id.show_system_apps -> {
                Utils.setShowSystemApps(this, !Utils.isShowSystemApps(this))
                if (!startTask()) {
                    Utils.setShowSystemApps(this, !Utils.isShowSystemApps(this))
                }
                invalidateOptionsMenu()
            }
            R.id.menu_switch_theme -> {
                (application as App).switchTheme()
                recreate()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateFilter(s: String?): Boolean {
        val filter = if (!TextUtils.isEmpty(s)) s!!.trim { it <= ' ' } else null
        if (mAdapter == null) {
            return false
        }

        mAdapter!!.setFilter(filter)
        mAdapter!!.filter.filter(filter)
        return true
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class GetApplicationsTask(private val mContext: Context) : AsyncTask<Void, Void, ArrayList<AppEntry>>() {

        override fun onPreExecute() {
            setListState(true)
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void): ArrayList<AppEntry> {
            return Utils.getApplications(mContext)
        }

        override fun onPostExecute(result: ArrayList<AppEntry>) {
            super.onPostExecute(result)
            updateListView(result)
            finishTask()
        }

        private fun finishTask() {
            task = null
        }

        override fun onCancelled() {
            finishTask()
            super.onCancelled()
        }

    }


    companion object {

        private val TAG = AppListActivity::class.java.simpleName

        private const val REQUEST_CODE_PREFERENCES_ACTIVITY = 123
        private var isRootAccessGiven = false
    }

}
