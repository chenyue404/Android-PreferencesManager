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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.xmlpull.v1.XmlPullParserException

data class PreferenceItem(
    val key: String,
    var value: Any,
    val isSelected: Boolean = false
)

class PreferenceFile(val file: String) {

    var list = mutableListOf<PreferenceItem>()
        private set

    private val _filteredList = MutableStateFlow(mutableListOf<PreferenceItem>())
    val filteredList = _filteredList.asStateFlow()

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

    private fun setPreferences(map: List<PreferenceItem>?) {
        list = map.orEmpty().toMutableList()
        _filteredList.update {
            list
        } // Set the filtered list too.
        updateSort()
    }

    fun toXml(): String {
        val out = ByteArrayOutputStream()

        try {
            writeMapXml(list.associate { it.key to it.value }, out)
        } catch (ignored: XmlPullParserException) {
            // Ignored
        } catch (ignored: IOException) {
            // Ignored
        }

        return out.toString()
    }

    fun setList(list: List<PreferenceItem>) {
        _filteredList.update { list.toMutableList() }
        updateSort()
    }

    private fun updateValue(key: String, value: Any) {
        list.find { it.key == key }?.value = value
        updateSort()
    }

    fun removeValue(key: String) {
        list.removeIf { it.key == key }
    }

    private fun createAndAddValue(key: String, value: Any) {
        list.add(PreferenceItem(key, value))
        updateSort()
    }

    fun add(previousKey: String, newKey: String, value: Any, editMode: Boolean) {
        if (TextUtils.isEmpty(newKey)) {
            return
        }

        if (!editMode) {
            if (list.any { it.key == newKey }) {
                updateValue(newKey, value)
            } else {
                createAndAddValue(newKey, value)
            }
        } else {
            if (newKey == previousKey) {
                updateValue(newKey, value)
            } else {
                removeValue(previousKey)

                if (list.any { it.key == newKey }) {
                    updateValue(newKey, value)
                } else {
                    createAndAddValue(newKey, value)
                }
            }
        }
    }

    private fun updateSort() {
        val sortType = PrefManager.keySortType
        when (EPreferencesSort.entries[sortType]) {
            EPreferencesSort.ALPHANUMERIC -> {
                list.sortBy { it.key }
            }
            EPreferencesSort.TYPE_AND_ALPHANUMERIC -> {
                list.sortWith(
                    compareBy({ it.value.javaClass.name }, { it.key })
                )
            }
        }
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
                val map = readMapXml(inputStream)?.map {
                    PreferenceItem(it.key, it.value)
                }
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
