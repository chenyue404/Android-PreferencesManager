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

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.BackupContainerInfo
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.Collator
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber

fun <T : Any> getParcelable(intent: Bundle, key: String?, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        intent.getParcelable(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        intent.getParcelable(key)
    }
}

fun <P, R> CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit,
    doInBackground: suspend (suspend (P) -> Unit) -> R,
    onPostExecute: (R) -> Unit,
    onProgressUpdate: (P) -> Unit
) = launch {
    onPreExecute()

    val result = withContext(Dispatchers.IO) {
        doInBackground {
            withContext(Dispatchers.Main) {
                onProgressUpdate(it)
            }
        }
    }
    onPostExecute(result)
}

object Utils {

    private val TAG: String = Utils::class.java.simpleName

    private const val CMD_FIND_XML_FILES = "find /data/data/%s -type f -name \\*.xml"
    private const val CMD_CHOWN = "chown %s.%s \"%s\""
    private const val CMD_CAT_FILE = "cat \"%s\""
    private const val CMD_CP = "cp \"%s\" \"%s\""
    private const val TMP_FILE = ".temp"
    private val LINE_SEPARATOR = System.getProperty("line.separator")

    var previousApps: ArrayList<AppEntry>? = null
        private set

    private var favorites: HashSet<String>? = null

    // Get a list of all installed applications.
    fun getApplications(ctx: Context): ArrayList<AppEntry> {
        val pm = ctx.packageManager
        if (pm == null) {
            previousApps = ArrayList()
        } else {
            var appsInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val flags = ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                pm.getInstalledApplications(flags)
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }

            if (appsInfo.isEmpty()) {
                appsInfo = ArrayList()
            }

            val entries = ArrayList<AppEntry>(appsInfo.size)
            appsInfo.forEach { app ->
                if (PrefManager.showSystemApps || app.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    entries.add(AppEntry(app, ctx))
                }
            }

            val comparator = object : Comparator<AppEntry> {
                private val sCollator = Collator.getInstance()

                init {
                    // Ignore case and accents
                    sCollator.strength = Collator.SECONDARY
                }

                override fun compare(obj1: AppEntry, obj2: AppEntry): Int =
                    sCollator.compare(obj1.sortingValue, obj2.sortingValue)
            }

            Collections.sort(entries, comparator)
            previousApps = ArrayList(entries)
        }
        Timber.tag(TAG).d("Applications: %s", previousApps!!.toTypedArray().contentToString())
        return previousApps!!
    }

    private fun updateApplicationInfo(packageName: String, favorite: Boolean) {
        Timber.tag(TAG).d("updateApplicationInfo(%s, %s)", packageName, favorite)
        previousApps?.forEach { app ->
            if (app.applicationInfo.packageName == packageName) {
                app.setFavorite(favorite)
                return
            }
        }
    }

    fun setFavorite(packageName: String, favorite: Boolean) {
        Timber.tag(TAG).d("setFavorite(%s, %s)", packageName, favorite)
        initFavorites()

        if (favorite) {
            favorites!!.add(packageName)
        } else {
            favorites!!.remove(packageName)
        }

        if (favorites!!.isEmpty()) {
            PrefManager.clearFavorites()
        } else {
            PrefManager.favorites = JSONArray(favorites).toString()
        }

        updateApplicationInfo(packageName, favorite)
    }

    fun isFavorite(packageName: String): Boolean {
        initFavorites()
        return favorites!!.contains(packageName)
    }

    private fun initFavorites() {
        if (favorites == null) {
            favorites = HashSet()

            val preferencesFavorite = PrefManager.favorites
            if (preferencesFavorite != null) {
                try {
                    val array = JSONArray(preferencesFavorite)
                    for (i in 0 until array.length()) {
                        favorites!!.add(array.optString(i))
                    }
                } catch (e: JSONException) {
                    Timber.tag(TAG).e(e, "error parsing JSON")
                }
            }
        }
    }

    fun findXmlFiles(packageName: String): List<String> {
        Timber.tag(TAG).d(packageName, "findXmlFiles(%s)")

        val stdout: List<String> = ArrayList()
        val stderr: List<String> = ArrayList()
        Shell.cmd(String.format(CMD_FIND_XML_FILES, packageName)).to(stdout, stderr).exec()

        return stdout
    }

    fun readFile(file: String): String {
        Timber.tag(TAG).d(file, "readFile(%s)")
        val sb = StringBuilder()
        val lines = ArrayList<String>()
        Shell.cmd(String.format(CMD_CAT_FILE, file)).to(lines).exec()

        lines.forEach { line ->
            sb.append(line)
            sb.append(LINE_SEPARATOR)
        }

        return sb.toString()
    }

    fun getBackups(ctx: Context, packageName: String): BackupContainer {
        val fileDir = ctx.externalCacheDir

        val container = BackupContainer(packageName, mutableListOf())

        Timber.d("Package Name: $packageName")

        fileDir?.listFiles()?.forEach {
            if (it.isDirectory) {
                return@forEach
            }

            val currentFile = it.name.split(" ")
            if (packageName.contains(currentFile[1]) && packageName.contains(currentFile[2])) {
                container.backupList.add(
                    BackupContainerInfo(
                        backupDate = currentFile[0],
                        backupFile = it.absolutePath,
                        backupXmlName = currentFile[2],
                        size = it.length()
                    )
                )
            }
        }

        return container
    }

    fun backupFile(ctx: Context, date: Long, pkgName: String, fileName: String): Boolean {
        val fileDir = ctx.externalCacheDir
        val name = fileName.substringAfterLast("/")
        val destination = File(fileDir, "$date $pkgName $name")

        Timber.tag(TAG).d("backupFile(%s, %s)", date, name)
        val job = Shell.cmd(String.format(CMD_CP, fileName, destination.absolutePath)).exec()

        Timber.tag(TAG).d("backupFile --> %s", destination)
        return job.isSuccess
    }

    fun restoreFile(ctx: Context, fileName: String, packageName: String): Boolean {
        Timber.tag(TAG).d("restoreFile(%s, %s)", fileName, packageName)
        val backupFile = File(fileName)
        val job = Shell.cmd(String.format(CMD_CP, backupFile.absolutePath, fileName)).exec()

        if (!fixUserAndGroupId(ctx, fileName, packageName)) {
            Timber.tag(TAG).e("Error fixUserAndGroupId")
            return false
        }

        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .killBackgroundProcesses(packageName)

        Timber.tag(TAG).d("restoreFile --> $fileName")
        return job.isSuccess
    }

    fun deleteFile(fileName: String): Boolean {
        Timber.tag(TAG).d("deleteFile(%s)", fileName)
        val deleteFile = File(fileName)

        if (deleteFile.isDirectory) {
            Timber.w("Tried to delete a folder.")
            return false
        }

        return deleteFile.delete()
    }

    fun savePreferences(
        ctx: Context,
        preferenceFile: PreferenceFile?,
        file: String,
        packageName: String
    ): Boolean {
        Timber.tag(TAG).d("savePreferences(%s, %s)", file, packageName)
        if (preferenceFile == null) {
            Timber.tag(TAG).e("Error preferenceFile is null")
            return false
        }

        if (!preferenceFile.isValid) {
            Timber.tag(TAG).e("Error preferenceFile is not valid")
            return false
        }

        val preferences = preferenceFile.toXml()
        if (TextUtils.isEmpty(preferences)) {
            Timber.tag(TAG).e("Error preferences is empty")
            return false
        }

        val tmpFile = File(ctx.filesDir, TMP_FILE)
        try {
            val outputStreamWriter =
                OutputStreamWriter(ctx.openFileOutput(TMP_FILE, Context.MODE_PRIVATE))
            outputStreamWriter.write(preferences)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "Error writing temporary file")
            return false
        }

        Shell.cmd(String.format(CMD_CP, tmpFile.absolutePath, file)).exec()

        if (!fixUserAndGroupId(ctx, file, packageName)) {
            Timber.tag(TAG).e("Error fixUserAndGroupId")
            return false
        }

        if (!tmpFile.delete()) {
            Timber.tag(TAG).e("Error deleting temporary file")
        }

        (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .killBackgroundProcesses(packageName)

        Timber.tag(TAG).d("Preferences correctly updated")
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
        Timber.tag(TAG).d("fixUserAndGroupId(%s, %s)", file, packageName)
        val uid: String
        val pm = ctx.packageManager ?: return false
        try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val flags = ApplicationInfoFlags.of(0)
                pm.getApplicationInfo(packageName, flags)
            } else {
                @Suppress("DEPRECATION")
                pm.getApplicationInfo(packageName, 0)
            }
            uid = appInfo.uid.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.tag(TAG).e(e, "error while getting uid")
            return false
        }

        if (TextUtils.isEmpty(uid)) {
            Timber.tag(TAG).d("uid is undefined")
            return false
        }

        Shell.cmd(String.format(CMD_CHOWN, uid, uid, file)).exec()
        return true
    }
}
