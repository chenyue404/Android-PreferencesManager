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
package fr.simon.marquis.preferencesmanager.model

import android.text.TextUtils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap

class BackupContainer {

    private val backups: MutableMap<String, MutableList<String>>

    val isEmpty: Boolean
        get() = backups.isEmpty()

    init {
        this.backups = HashMap()
    }

    fun toJSON(): JSONArray {
        val array = JSONArray()
        val entries = backups.entries
        for ((key, value) in entries) {
            val obj = JSONObject()
            val arrayBackups = JSONArray(value)
            try {
                obj.put(KEY_BACKUPS, arrayBackups)
                obj.put(KEY_FILE, key)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            if (arrayBackups.length() > 0) {
                array.put(obj)
            }
        }
        return array
    }

    fun put(key: String, value: String) {
        if (backups.containsKey(key)) {
            backups[key]!!.add(value)
        } else {
            val list = ArrayList<String>()
            list.add(value)
            backups[key] = list
        }
    }

    fun remove(key: String, value: String) {
        if (backups.containsKey(key)) {
            val list = backups[key]
            list!!.remove(value)
            if (list.isEmpty()) {
                backups.remove(key)
            }
        }
    }

    operator fun contains(key: String): Boolean {
        return backups.containsKey(key)
    }

    operator fun get(key: String): List<String>? {
        return backups[key]
    }

    companion object {

        private const val KEY_FILE = "FILE"
        private const val KEY_BACKUPS = "BACKUPS"

        fun fromJSON(filesArray: JSONArray): BackupContainer {
            val container = BackupContainer()
            for (i in 0 until filesArray.length()) {
                val obj = filesArray.optJSONObject(i)
                if (obj != null) {
                    val file = obj.optString(KEY_FILE)
                    val backupsArray = obj.optJSONArray(KEY_BACKUPS)
                    if (backupsArray != null) {
                        for (j in 0 until backupsArray.length()) {
                            val backup = backupsArray.optString(j)
                            if (!TextUtils.isEmpty(backup)) {
                                container.put(file, backup)
                            }
                        }
                    }
                }
            }
            return container
        }
    }
}
