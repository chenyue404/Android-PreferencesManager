package fr.simon.marquis.preferencesmanager.model

import androidx.annotation.StringRes
import fr.simon.marquis.preferencesmanager.R

enum class EFontTheme {
    ECLIPSE, GOOGLE, ROBOTICKET, NOTEPAD, NETBEANS;

    companion object {
        fun getByTheme(value: Int): EFontTheme {
            values().forEach {
                if (it.ordinal == value)
                    return it
            }

            throw IllegalArgumentException("$value is not a valid EFontTheme")
        }
    }
}

enum class EFontSize(val size: Int) {
    EXTRA_SMALL(10), SMALL(13), MEDIUM(16), LARGE(20), EXTRA_LARGE(24);

    companion object {
        fun getBySize(value: Int): EFontSize {
            values().forEach {
                if (it.size == value)
                    return it
            }

            throw IllegalArgumentException("$value is not a valid EFontSize")
        }
    }
}

enum class ScrollButtonVisibility {
    Visible,
    Gone,
}

enum class EPreferencesAdd {
    INTEGER, STRING, BOOLEAN, FLOAT, LONG, STRINGSET
}

enum class EPreferencesOverflow {
    EDIT, /* SHORTCUT, */ BACKUP, RESTORE
}

enum class EPreferencesSort {
    ALPHANUMERIC, TYPE_AND_ALPHANUMERIC
}

enum class EPreferenceEditType(
    @StringRes val addTitle: Int,
    @StringRes val editTitle: Int,
) {
    UNSUPPORTED(0, 0),
    BOOLEAN(R.string.title_add_boolean, R.string.title_edit_boolean),
    STRING(R.string.title_add_string, R.string.title_edit_string),
    INT(R.string.title_add_int, R.string.title_edit_int),
    FLOAT(R.string.title_add_float, R.string.title_edit_float),
    LONG(R.string.title_add_long, R.string.title_edit_long),
    STRINGSET(R.string.title_add_stringset, R.string.title_edit_stringset)
    ;

    companion object {
        fun fromObject(obj: Any): EPreferenceEditType {
            return when (obj) {
                is String -> STRING
                is Int -> INT
                is Long -> LONG
                is Float -> FLOAT
                is Boolean -> BOOLEAN
                is Set<*> -> STRINGSET
                else -> UNSUPPORTED
            }
        }
    }
}
