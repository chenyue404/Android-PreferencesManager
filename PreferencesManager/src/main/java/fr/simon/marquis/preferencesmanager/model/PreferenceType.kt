package fr.simon.marquis.preferencesmanager.model

import fr.simon.marquis.preferencesmanager.R

enum class PreferenceType(
    val dialogTitleAdd: Int,
    val dialogTitleEdit: Int,
    var key: String,
    var value: Any?
) {

    BOOLEAN(R.string.title_add_boolean, R.string.title_edit_boolean, "", false),
    FLOAT(R.string.title_add_float, R.string.title_edit_float, "", 0F),
    INT(R.string.title_add_int, R.string.title_edit_int, "", 0),
    LONG(R.string.title_add_long, R.string.title_edit_long, "", 0L),
    STRING(R.string.title_add_string, R.string.title_edit_string, "", ""),
    STRINGSET(R.string.title_add_stringset, R.string.title_edit_stringset, "", setOf<Any>()),
    UNSUPPORTED(0, 0, "", null);

    var isEdit: Boolean = false

    companion object {
        fun fromObject(obj: Any?): PreferenceType {
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
