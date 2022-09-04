package fr.simon.marquis.preferencesmanager.ui.theme

import androidx.compose.ui.graphics.Color

val headerBlue = Color(0xFF33B5E5)

/**
 * New themes can be found here: https://eclipse-color-themes.web.app/
 * Source: https://github.com/eclipse-color-theme/eclipse-color-theme/issues/290
 */

/* Default theme from eclipse */
val xmlEclipseTag = Color(0xFF3F7F7F)
val xmlEclipseAttributeName = Color(0xFF7F007F)
val xmlEclipseAttributeValue = Color(0xFF2A00FF)
val xmlEclipseComment = Color(0xFF3F5FBF)
val xmlEclipseValue = Color(0xFF000000)
val xmlEclipseDefault = Color(0xFF000000)

/* Prettyprint from http://developer.android.com */
val xmlGoogleTag = Color(0xFF000088)
val xmlGoogleAttributeName = Color(0xFF882288)
val xmlGoogleAttributeValue = Color(0xFF008800)
val xmlGoogleComment = Color(0xFF880000)
val xmlGoogleValue = Color(0xFF000000)
val xmlGoogleDefault = Color(0xFF000000)

/* Roboticket from http://eclipsecolorthemes.org/?view=theme&id=93 */
val xmlRoboticketTag = Color(0xFFB05A65)
val xmlRoboticketAttributeName = Color(0xFF566874)
val xmlRoboticketAttributeValue = Color(0xFF317ECC)
val xmlRoboticketComment = Color(0xFFAD95AF)
val xmlRoboticketValue = Color(0xFF585858)
val xmlRoboticketDefault = Color(0xFF585858)

/* Notepad++ from http://eclipsecolorthemes.org/?view=theme&id=91 */
val xmlNotepadTag = Color(0xFF000080)
val xmlNotepadAttributeName = Color(0xFF800080)
val xmlNotepadAttributeValue = Color(0xFF808080)
val xmlNotepadComment = Color(0xFF008000)
val xmlNotepadValue = Color(0xFF008000)
val xmlNotepadDefault = Color(0xFF008000)

/* Netbeans 6++ from http://eclipsecolorthemes.org/?view=theme&id=259 */
val xmlNetbeansTag = Color(0xFF001A49)
val xmlNetbeansAttributeName = Color(0xFF009900)
val xmlNetbeansAttributeValue = Color(0xFFCE7B00)
val xmlNetbeansComment = Color(0xFFB7B7B7)
val xmlNetbeansValue = Color(0xFF000000)
val xmlNetbeansDefault = Color(0xFF000000)

/* Preference Card Colors */
private val boolean = Color(0xFF7e3794)
private val float = Color(0xFF3f5ca9)
private val int = Color(0xFFdd4330)
private val long = Color(0xFF11a9cc)
private val string = Color(0xFF0f9d58)
private val stringSet = Color(0xFFf4b400)
private val unsupported = Color(0xFF000000)

fun getColorFromObjet(obj: Any): Color {
    return when (obj) {
        is Boolean -> boolean
        is Float -> float
        is Int -> int
        is Long -> long
        is Set<*> -> stringSet
        is String -> string
        else -> unsupported
    }
}
