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
import fr.simon.marquis.preferencesmanager.ui.PreferencesActivity
import fr.simon.marquis.preferencesmanager.util.XmlUtils
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class PreferenceFile private constructor() {

    var isValidPreferenceFile = true
        private set

    private var mPreferences: MutableMap<Any, Any>? = null
    private var mList: MutableList<MutableMap.MutableEntry<Any, Any>>? = null

    var list: MutableList<MutableMap.MutableEntry<Any, Any>>
        get() {
            if (mList == null) {
                mList = ArrayList()
            }
            return mList!!
        }
        set(mList) {
            this.mList = mList
            this.mPreferences = HashMap()
            for ((key, value) in mList) {
                mPreferences!![key] = value
            }

            updateSort()
        }

    val isValid: Boolean
        get() {
            try {
                XmlUtils.readMapXml(ByteArrayInputStream(toXml().toByteArray()))
            } catch (e: Exception) {
                return false
            }

            return true
        }

    init {
        mPreferences = HashMap()
    }

    private fun setPreferences(map: HashMap<Any, Any>?) {
        mPreferences = map
        mList = ArrayList(mPreferences!!.entries)
        updateSort()
    }

    fun toXml(): String {
        val out = ByteArrayOutputStream()
        try {
            XmlUtils.writeMapXml(mPreferences, out)
        } catch (ignored: XmlPullParserException) {
        } catch (ignored: IOException) {
        }

        return out.toString()
    }

    private fun updateValue(key: String, value: Any) {
        for (entry in mList!!) {
            if (entry.key == key) {
                entry.setValue(value)
                break
            }
        }
        mPreferences!![key] = value
        updateSort()
    }

    fun removeValue(key: String) {
        mPreferences!!.remove(key)
        for (entry in mList!!) {
            if (entry.key == key) {
                mList!!.remove(entry)
                break
            }
        }
    }

    private fun createAndAddValue(key: String, value: Any) {
        mList!!.add(0, AbstractMap.SimpleEntry(key, value))
        mPreferences!![key] = value
        updateSort()
    }

    fun add(previousKey: String?, newKey: String?, value: Any?, editMode: Boolean) {
        if (newKey.isNullOrBlank()) {
            return
        }

        if (!editMode) {
            if (mPreferences!!.containsKey(newKey)) {
                updateValue(newKey, value!!)
            } else {
                createAndAddValue(newKey, value!!)
            }
        } else {
            if (newKey == previousKey) {
                updateValue(newKey, value!!)
            } else {
                removeValue(previousKey!!)

                if (mPreferences!!.containsKey(newKey)) {
                    updateValue(newKey, value!!)
                } else {
                    createAndAddValue(newKey, value!!)
                }
            }
        }
    }

    fun updateSort() {
        Collections.sort(list, PreferenceComparator(PreferencesActivity.preferenceSortType))
    }

    companion object {

        fun fromXml(xml: String): PreferenceFile {
            val preferenceFile = PreferenceFile()

            // Check for empty files
            if (TextUtils.isEmpty(xml) || xml.trim { it <= ' ' }.isEmpty()) {
                return preferenceFile
            }

            try {
                val `in` = ByteArrayInputStream(xml.toByteArray())
                val map = XmlUtils.readMapXml(`in`)
                `in`.close()

                preferenceFile.setPreferences(map)

                return preferenceFile

            } catch (ignored: XmlPullParserException) {
            } catch (ignored: IOException) {
            }

            preferenceFile.isValidPreferenceFile = false
            return preferenceFile
        }
    }

}
