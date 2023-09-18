package fr.simon.marquis.preferencesmanager.model

enum class EAppTheme {
    AUTO, DAY, NIGHT;

    companion object {
        fun getAppTheme(value: Int): EAppTheme = entries.find { it.ordinal == value }!!
    }
}

enum class EFontTheme {
    ECLIPSE, GOOGLE, ROBOTICKET, NOTEPAD, NETBEANS;

    companion object {
        fun getByTheme(value: Int): EFontTheme = entries.find { it.ordinal == value }!!
    }
}

enum class EFontSize(val size: Int) {
    EXTRA_SMALL(10), SMALL(13), MEDIUM(16), LARGE(20), EXTRA_LARGE(24);

    companion object {
        fun getBySize(value: Int): EFontSize = entries.find { it.size == value }!!
    }
}

enum class ScrollButtonVisibility {
    Visible,
    Gone
}

enum class EPreferencesAdd {
    INTEGER, STRING, BOOLEAN, FLOAT, LONG, STRINGSET
}

enum class EPreferencesOverflow {
    EDIT, FAV, BACKUP, RESTORE
}

enum class EPreferencesSort {
    ALPHANUMERIC, TYPE_AND_ALPHANUMERIC
}
