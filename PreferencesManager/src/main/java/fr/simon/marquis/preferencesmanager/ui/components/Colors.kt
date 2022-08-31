package fr.simon.marquis.preferencesmanager.ui.components

import androidx.compose.ui.graphics.Color

/* Colors for Preference Entry Items */
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
