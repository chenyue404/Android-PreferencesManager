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
import fr.simon.marquis.preferencesmanager.util.PrefManager
import fr.simon.marquis.preferencesmanager.util.XmlUtils.readMapXml
import fr.simon.marquis.preferencesmanager.util.XmlUtils.writeMapXml
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import org.xmlpull.v1.XmlPullParserException

class PreferenceFile(val file: String) {

    private var preferences: MutableMap<String, Any> = hashMapOf()

    var list: MutableList<MutableMap.MutableEntry<String, Any>> = mutableListOf()
        private set

    var isValidPreferenceFile = true
        private set

    val isValid: Boolean
        get() {
            try {
                readMapXml(ByteArrayInputStream(toXml().toByteArray()))
            } catch (e: Exception) {
                return false
            }

            return true
        }

    private fun setPreferences(map: MutableMap<String, Any>) {
        preferences = map
        list = ArrayList(preferences.entries)
        updateSort()
    }

    fun toXml(): String {
        val out = ByteArrayOutputStream()

        try {
            writeMapXml(preferences, out)
        } catch (ignored: XmlPullParserException) {
            // Ignored
        } catch (ignored: IOException) {
            // Ignored
        }

        return out.toString()
    }

    fun setList(mList: MutableList<MutableMap.MutableEntry<String, Any>>) {
        this.list = mList
        preferences = HashMap()
        for ((key, value) in mList) {
            preferences[key] = value
        }
        updateSort()
    }

    private fun updateValue(key: String, value: Any) {
        list.find { it.key == key }?.setValue(value)
        preferences[key] = value
        updateSort()
    }

    fun removeValue(key: String) {
        preferences.remove(key)
        list.find { it.key == key }?.let { list.remove(it) }
    }

    private fun createAndAddValue(key: String, value: Any) {
        list.add(0, AbstractMap.SimpleEntry(key, value))
        preferences[key] = value
        updateSort()
    }

    fun add(previousKey: String, newKey: String, value: Any, editMode: Boolean) {
        if (TextUtils.isEmpty(newKey)) {
            return
        }

        if (!editMode) {
            if (preferences.containsKey(newKey)) {
                updateValue(newKey, value)
            } else {
                createAndAddValue(newKey, value)
            }
        } else {
            if (newKey == previousKey) {
                updateValue(newKey, value)
            } else {
                removeValue(previousKey)

                if (preferences.containsKey(newKey)) {
                    updateValue(newKey, value)
                } else {
                    createAndAddValue(newKey, value)
                }
            }
        }
    }

    private fun updateSort() {
        val sortType = PrefManager.keySortType
        val comparator = PreferenceComparator(EPreferencesSort.values()[sortType])
        Collections.sort(list, comparator)
    }

    companion object {
        /**
         * @param xml The xml content as a string
         * @param file The file path of that xml preference file
         */
        fun fromXml(xml: String, file: String): PreferenceFile {
            val preferenceFile = PreferenceFile(file)

            // Check for empty files
            if (TextUtils.isEmpty(xml) || xml.trim().isEmpty()) {
                return preferenceFile
            }

            try {
                val inputStream: InputStream = ByteArrayInputStream(xml.toByteArray())
                val map = readMapXml(inputStream)
                inputStream.close()
                if (map != null) {
                    preferenceFile.setPreferences(map)
                    return preferenceFile
                }
            } catch (ignored: XmlPullParserException) {
                // Ignored
            } catch (ignored: IOException) {
                // Ignored
            }

            preferenceFile.isValidPreferenceFile = false

            return preferenceFile
        }
    }
}
