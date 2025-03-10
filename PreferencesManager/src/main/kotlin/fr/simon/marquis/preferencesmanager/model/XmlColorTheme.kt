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

import androidx.compose.ui.graphics.Color
import fr.simon.marquis.preferencesmanager.ui.theme.*

class XmlColorTheme(
    private val xmlTag: Color,
    private val xmlAttributeName: Color,
    private val xmlAttributeValue: Color,
    private val xmlComment: Color,
    private val xmlValue: Color,
    private val xmlDefault: Color
) {
    enum class ColorTagEnum {
        TAG, ATTR_NAME, ATTR_VALUE, COMMENT, VALUE, DEFAULT
    }

    fun getColor(type: ColorTagEnum): Color {
        return when (type) {
            ColorTagEnum.TAG -> xmlTag
            ColorTagEnum.ATTR_NAME -> xmlAttributeName
            ColorTagEnum.ATTR_VALUE -> xmlAttributeValue
            ColorTagEnum.COMMENT -> xmlComment
            ColorTagEnum.VALUE -> xmlValue
            ColorTagEnum.DEFAULT -> xmlDefault
        }
    }

    companion object {
        fun createTheme(theme: EFontTheme): XmlColorTheme {
            return when (theme) {
                EFontTheme.ECLIPSE -> {
                    XmlColorTheme(
                        xmlEclipseTag,
                        xmlEclipseAttributeName,
                        xmlEclipseAttributeValue,
                        xmlEclipseComment,
                        xmlEclipseValue,
                        xmlEclipseDefault
                    )
                }
                EFontTheme.GOOGLE -> {
                    XmlColorTheme(
                        xmlGoogleTag,
                        xmlGoogleAttributeName,
                        xmlGoogleAttributeValue,
                        xmlGoogleComment,
                        xmlGoogleValue,
                        xmlGoogleDefault
                    )
                }
                EFontTheme.ROBOTICKET -> {
                    XmlColorTheme(
                        xmlRoboticketTag,
                        xmlRoboticketAttributeName,
                        xmlRoboticketAttributeValue,
                        xmlRoboticketComment,
                        xmlRoboticketValue,
                        xmlRoboticketDefault
                    )
                }
                EFontTheme.NOTEPAD -> {
                    XmlColorTheme(
                        xmlNotepadTag,
                        xmlNotepadAttributeName,
                        xmlNotepadAttributeValue,
                        xmlNotepadComment,
                        xmlNotepadValue,
                        xmlNotepadDefault
                    )
                }
                EFontTheme.NETBEANS -> {
                    XmlColorTheme(
                        xmlNetbeansTag,
                        xmlNetbeansAttributeName,
                        xmlNetbeansAttributeValue,
                        xmlNetbeansComment,
                        xmlNetbeansValue,
                        xmlNetbeansDefault
                    )
                }
            }
        }
    }
}
