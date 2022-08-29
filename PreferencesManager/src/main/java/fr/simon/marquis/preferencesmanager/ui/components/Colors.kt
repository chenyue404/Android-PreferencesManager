package fr.simon.marquis.preferencesmanager.ui.components

import androidx.compose.ui.graphics.Color

/* Colors for Preference Entry Items */
val boolean = Color(0xFF7e3794)
val float = Color(0xFF3f5ca9)
val int = Color(0xFFdd4330)
val long = Color(0xFF11a9cc)
val string = Color(0xFF0f9d58)
val stringSet = Color(0xFFf4b400)
val unsupported = Color(0xFF000000)

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
