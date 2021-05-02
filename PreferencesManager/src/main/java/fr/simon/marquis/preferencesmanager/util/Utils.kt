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
package fr.simon.marquis.preferencesmanager.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import androidx.preference.PreferenceManager
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.BuildConfig
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*

object Utils {

    val TAG: String = Utils::class.java.simpleName
    private const val FAVORITES_KEY = "FAVORITES_KEY"
    private const val VERSION_CODE_KEY = "VERSION_CODE"
    private const val BACKUP_PREFIX = "BACKUP_"
    private const val PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS"
    private const val CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml"
    private const val CMD_CHOWN = "chown %s.%s \"%s\""
    private const val CMD_CAT_FILE = "cat \"%s\""
    private const val CMD_CP = "cp \"%s\" \"%s\""
    private const val TMP_FILE = ".temp"
    private val FILE_SEPARATOR = System.getProperty("file.separator")
    private val LINE_SEPARATOR = System.getProperty("line.separator")
    private const val PACKAGE_NAME_PATTERN = "^[a-zA-Z_$][\\w$]*(?:\\.[a-zA-Z_$][\\w$]*)*$"

    var previousApps: ArrayList<AppEntry>? = null
        private set

    private var favorites: HashSet<String>? = null

    fun getApplications(ctx: Context): ArrayList<AppEntry> {
        val pm = ctx.packageManager
        if (pm == null) {
            previousApps = ArrayList()
        } else {
            val showSystemApps = isShowSystemApps(ctx)
            var appsInfo: MutableList<ApplicationInfo> =
                pm.getInstalledApplications(PackageManager.GET_META_DATA)

            if (appsInfo.isNullOrEmpty()) {
                appsInfo = ArrayList()
            }

            val entries = ArrayList<AppEntry>(appsInfo.size)
            for (a in appsInfo) {
                if (showSystemApps || a.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    entries.add(AppEntry(a, ctx))
                }
            }

            Collections.sort(entries, MyComparator())
            previousApps = ArrayList(entries)
        }
        Log.d(TAG, "Applications: " + previousApps!!.toTypedArray().contentToString())
        return previousApps!!
    }

    fun setFavorite(packageName: String, favorite: Boolean, ctx: Context) {
        Log.d(TAG, String.format("setFavorite(%s, %s)", packageName, favorite))
        initFavorites(ctx)

        if (favorite) {
            favorites!!.add(packageName)
        } else {
            favorites!!.remove(packageName)
        }

        val ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        if (favorites!!.isEmpty()) {
            ed.remove(FAVORITES_KEY)
        } else {
            ed.putString(FAVORITES_KEY, JSONArray(favorites).toString())
        }

        ed.apply()
        updateApplicationInfo(packageName, favorite)
    }

    private fun updateApplicationInfo(packageName: String, favorite: Boolean) {
        Log.d(TAG, String.format("updateApplicationInfo(%s, %s)", packageName, favorite))
        for (a in previousApps!!) {
            if (a.applicationInfo.packageName == packageName) {
                a.setFavorite(favorite)
                return
            }
        }
    }

    fun isFavorite(packageName: String, ctx: Context): Boolean {
        initFavorites(ctx)
        return favorites!!.contains(packageName)
    }

    private fun initFavorites(ctx: Context) {
        if (favorites == null) {
            favorites = HashSet()

            val sp = PreferenceManager.getDefaultSharedPreferences(ctx)

            if (sp.contains(FAVORITES_KEY)) {
                try {
                    val array = JSONArray(sp.getString(FAVORITES_KEY, "[]"))
                    for (i in 0 until array.length()) {
                        favorites!!.add(array.optString(i))
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "error parsing JSON", e)
                }

            }
        }
    }

    fun isShowSystemApps(ctx: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
            .getBoolean(PREF_SHOW_SYSTEM_APPS, false)
    }

    fun setShowSystemApps(ctx: Context, show: Boolean) {
        Log.d(TAG, String.format("setShowSystemApps(%s)", show))
        val e = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        e.putBoolean(PREF_SHOW_SYSTEM_APPS, show)
        e.apply()
    }

    fun findXmlFiles(packageName: String): List<String> {
        Log.d(TAG, String.format("findXmlFiles(%s)", packageName))

        val stdout: List<String> = ArrayList()
        val stderr: List<String> = ArrayList()
        val what =
            Shell.su(String.format(CMD_FIND_XML_FILES, packageName)).to(stdout, stderr).exec()

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "Out: ${what.out}\n" +
                    "Err: ${what.err}\n" +
                    "Succ: ${what.isSuccess}\n" +
                    "Code: ${what.code}\n" +
                    "stdout: $stdout \n" +
                    "stderr: $stderr \n"
            )

            Log.d(TAG, "files: " + stdout.toTypedArray().contentToString())
        }

        return stdout
    }

    fun readFile(file: String): String {
        Log.d(TAG, String.format("readFile(%s)", file))
        val sb = StringBuilder()
        val lines = ArrayList<String>()
        Shell.su(String.format(CMD_CAT_FILE, file)).to(lines).exec()

        for (line in lines) {
            sb.append(line)
            sb.append(LINE_SEPARATOR)
        }

        return sb.toString()
    }

    fun checkBackups(ctx: Context) {
        Log.d(TAG, "checkBackups")
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        val needToBackport = needToBackport(sp)
        Log.d(TAG, "needToBackport ? $needToBackport")
        saveVersionCode(ctx, sp)
        if (!needToBackport) {
            return
        }
        backportBackups(ctx)
    }

    private fun backportBackups(ctx: Context) {
        Log.d(TAG, "backportBackups")
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = sp.edit()
        val keys = sp.all ?: return

        for ((key, value1) in keys) {
            val value = value1.toString()

            Log.d(TAG, "key: $key")

            if (!key.startsWith(BACKUP_PREFIX) && key.matches(PACKAGE_NAME_PATTERN.toRegex()) && value.contains(
                    "FILE"
                ) && value.contains("BACKUPS")
            ) {
                Log.d(TAG, " need to be updated")
                var array: JSONArray? = null
                try {
                    array = JSONArray(value)
                    for (i in 0 until array.length()) {
                        val container = array.getJSONObject(i)
                        val file = container.getString("FILE")
                        if (!file.startsWith(FILE_SEPARATOR!!)) {
                            container.put("FILE", FILE_SEPARATOR + file)
                        }
                        val backups = container.getJSONArray("BACKUPS")
                        val values = ArrayList<String>(backups.length())
                        for (j in 0 until backups.length()) {
                            values.add(backups.getJSONObject(j).getLong("TIME").toString())
                        }
                        container.put("BACKUPS", JSONArray(values))
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Error trying to backport Backups", e)
                }

                if (array != null) {
                    editor.putString(BACKUP_PREFIX + key, array.toString())
                }
                editor.remove(key)
            }
        }

        editor.apply()
    }

    private fun needToBackport(sp: SharedPreferences): Boolean {
        // 18 was the latest version code release with old Backup system
        return sp.getInt(VERSION_CODE_KEY, 0) <= 18
    }

    private fun saveVersionCode(ctx: Context, sp: SharedPreferences) {
        try {
            //Ignoring for older devices
            @Suppress("DEPRECATION")
            sp.edit().putInt(
                VERSION_CODE_KEY,
                ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionCode
            ).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error trying to save the version code", e)
        }

    }

    fun getBackups(ctx: Context, packageName: String): BackupContainer {
        Log.d(TAG, String.format("getBackups(%s)", packageName))
        val sp = PreferenceManager.getDefaultSharedPreferences(ctx)
        var container: BackupContainer? = null
        try {
            container =
                BackupContainer.fromJSON(JSONArray(sp.getString(BACKUP_PREFIX + packageName, "[]")))
        } catch (ignore: JSONException) {
        }

        if (container == null) {
            container = BackupContainer()
        }
        Log.d(TAG, "backups: " + container.toJSON().toString())
        return container
    }

    fun saveBackups(ctx: Context, packageName: String, container: BackupContainer) {
        Log.d(TAG, String.format("saveBackups(%s, %s)", packageName, container.toJSON().toString()))
        val ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        if (container.isEmpty) {
            ed.remove(BACKUP_PREFIX + packageName)
        } else {
            ed.putString(BACKUP_PREFIX + packageName, container.toJSON().toString())
        }
        ed.apply()
    }

    fun backupFile(backup: String, fileName: String, ctx: Context): Boolean {
        Log.d(TAG, String.format("backupFile(%s, %s)", backup, fileName))
        val destination = File(ctx.filesDir, backup)
        Shell.su(String.format(CMD_CP, fileName, destination.absolutePath)).exec()
        Log.d(TAG, "backupFile --> $destination")
        return true
    }

    fun restoreFile(ctx: Context, backup: String, fileName: String, packageName: String): Boolean {
        Log.d(TAG, String.format("restoreFile(%s, %s, %s)", backup, fileName, packageName))
        val backupFile = File(ctx.filesDir, backup)
        Shell.su(String.format(CMD_CP, backupFile.absolutePath, fileName)).exec()

        if (!fixUserAndGroupId(ctx, fileName, packageName)) {
            Log.e(TAG, "Error fixUserAndGroupId")
            return false
        }

        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(
            packageName
        )

        Log.d(TAG, "restoreFile --> $fileName")
        return true
    }

    fun extractFileName(s: String): String? {
        return if (TextUtils.isEmpty(s)) {
            null
        } else s.substring(s.lastIndexOf(FILE_SEPARATOR!!) + 1)
    }

    fun savePreferences(
        preferenceFile: PreferenceFile?,
        file: String,
        packageName: String,
        ctx: Context
    ): Boolean {
        Log.d(TAG, String.format("savePreferences(%s, %s)", file, packageName))
        if (preferenceFile == null) {
            Log.e(TAG, "Error preferenceFile is null")
            return false
        }

        if (!preferenceFile.isValid) {
            Log.e(TAG, "Error preferenceFile is not valid")
            return false
        }

        val preferences = preferenceFile.toXml()
        if (TextUtils.isEmpty(preferences)) {
            Log.e(TAG, "Error preferences is empty")
            return false
        }

        val tmpFile = File(ctx.filesDir, TMP_FILE)
        try {
            val outputStreamWriter =
                OutputStreamWriter(ctx.openFileOutput(TMP_FILE, Context.MODE_PRIVATE))
            outputStreamWriter.write(preferences)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error writing temporary file", e)
            return false
        }

        Shell.su(String.format(CMD_CP, tmpFile.absolutePath, file)).exec()

        if (!fixUserAndGroupId(ctx, file, packageName)) {
            Log.e(TAG, "Error fixUserAndGroupId")
            return false
        }

        if (!tmpFile.delete()) {
            Log.e(TAG, "Error deleting temporary file")
        }

        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(
            packageName
        )
        Log.d(TAG, "Preferences correctly updated")
        return true
    }

    /**
     * Put User id and Group id back to the corresponding app with this cmd: `chown uid.gid filename`
     *
     * @param ctx         Context
     * @param file        The file to fix
     * @param packageName The packageName of the app
     * @return true if success
     */
    private fun fixUserAndGroupId(ctx: Context, file: String, packageName: String): Boolean {
        Log.d(TAG, String.format("fixUserAndGroupId(%s, %s)", file, packageName))
        val uid: String
        val pm = ctx.packageManager ?: return false
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            uid = appInfo.uid.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "error while getting uid", e)
            return false
        }

        if (TextUtils.isEmpty(uid)) {
            Log.d(TAG, "uid is undefined")
            return false
        }

        Shell.su(String.format(CMD_CHOWN, uid, uid, file)).exec()
        return true
    }
}
