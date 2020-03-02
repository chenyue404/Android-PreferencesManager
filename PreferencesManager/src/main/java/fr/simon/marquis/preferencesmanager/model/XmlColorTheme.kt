package fr.simon.marquis.preferencesmanager.model

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

import android.content.Context
import androidx.core.content.ContextCompat
import fr.simon.marquis.preferencesmanager.R

class XmlColorTheme(
        context: Context,
        xmlTag: Int,
        xmlAttributeName: Int,
        xmlAttributeValue: Int,
        xmlComment: Int,
        xmlValue: Int,
        xmlDefault: Int
) {

    private val tag: Int = ContextCompat.getColor(context, xmlTag)
    private val attributeName: Int = ContextCompat.getColor(context, xmlAttributeName)
    private val attributeValue: Int = ContextCompat.getColor(context, xmlAttributeValue)
    private val comment: Int = ContextCompat.getColor(context, xmlComment)
    private val value: Int = ContextCompat.getColor(context, xmlValue)
    private val defaultColor: Int = ContextCompat.getColor(context, xmlDefault)

    enum class ColorThemeEnum {
        ECLIPSE, GOOGLE, ROBOTICKET, NOTEPAD, NETBEANS
    }

    enum class ColorTagEnum {
        TAG, ATTR_NAME, ATTR_VALUE, COMMENT, VALUE, DEFAULT
    }

    fun getColor(type: ColorTagEnum): Int {
        return when (type) {
            ColorTagEnum.TAG -> tag
            ColorTagEnum.ATTR_NAME -> attributeName
            ColorTagEnum.ATTR_VALUE -> attributeValue
            ColorTagEnum.COMMENT -> comment
            ColorTagEnum.VALUE -> value
            ColorTagEnum.DEFAULT -> defaultColor
        }
    }

    companion object {

        fun createTheme(context: Context, theme: ColorThemeEnum): XmlColorTheme? {
            return when (theme) {
                ColorThemeEnum.ECLIPSE ->
                    XmlColorTheme(
                            context,
                            R.color.xml_eclipse_tag,
                            R.color.xml_eclipse_attribute_name,
                            R.color.xml_eclipse_attribute_value,
                            R.color.xml_eclipse_comment,
                            R.color.xml_eclipse_value,
                            R.color.xml_eclipse_default
                    )
                ColorThemeEnum.GOOGLE ->
                    XmlColorTheme(
                            context,
                            R.color.xml_google_tag,
                            R.color.xml_google_attribute_name,
                            R.color.xml_google_attribute_value,
                            R.color.xml_google_comment,
                            R.color.xml_google_value,
                            R.color.xml_google_default
                    )
                ColorThemeEnum.NETBEANS ->
                    XmlColorTheme(
                            context,
                            R.color.xml_netbeans_tag,
                            R.color.xml_netbeans_attribute_name,
                            R.color.xml_netbeans_attribute_value,
                            R.color.xml_netbeans_comment,
                            R.color.xml_netbeans_value,
                            R.color.xml_netbeans_default
                    )
                ColorThemeEnum.NOTEPAD ->
                    XmlColorTheme(
                            context,
                            R.color.xml_notepad_tag,
                            R.color.xml_notepad_attribute_name,
                            R.color.xml_notepad_attribute_value,
                            R.color.xml_notepad_comment,
                            R.color.xml_notepad_value,
                            R.color.xml_notepad_default
                    )
                ColorThemeEnum.ROBOTICKET ->
                    XmlColorTheme(
                            context,
                            R.color.xml_roboticket_tag,
                            R.color.xml_roboticket_attribute_name,
                            R.color.xml_roboticket_attribute_value,
                            R.color.xml_roboticket_comment,
                            R.color.xml_roboticket_value,
                            R.color.xml_roboticket_default
                    )
            }
        }
    }
}
