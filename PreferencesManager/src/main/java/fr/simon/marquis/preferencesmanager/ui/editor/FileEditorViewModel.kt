package fr.simon.marquis.preferencesmanager.ui.editor

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.simon.marquis.preferencesmanager.model.EFontSize
import fr.simon.marquis.preferencesmanager.model.EFontTheme
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import fr.simon.marquis.preferencesmanager.util.Utils
import java.util.*
import kotlinx.coroutines.launch
import timber.log.Timber

data class FileEditorState(
    val textChanged: Boolean = false,
    val file: String? = null,
    val title: String? = null,
    val pkgName: String? = null,
    val editText: String? = null,
    val fontSize: EFontSize = EFontSize.getBySize(PrefManager.keyFontSize),
    val fontTheme: EFontTheme = EFontTheme.getByTheme(PrefManager.keyFontTheme),
    val xmlColorTheme: XmlColorTheme? = null,
)

class FileEditorViewModel : ViewModel() {

    var uiState by mutableStateOf(FileEditorState())
        private set

    init {
        val xmlColorTheme = XmlColorTheme.createTheme(uiState.fontTheme)
        uiState = uiState.copy(xmlColorTheme = xmlColorTheme)
    }

    fun setTextChanged(value: String) {
        uiState = uiState.copy(textChanged = true, editText = value)
    }

    fun setPackageInfo(file: String?, title: String?, pkgName: String?) {
        uiState = uiState.copy(
            file = file,
            title = title,
            pkgName = pkgName,
            editText = Utils.readFile(file!!)
        )
    }

    fun setFontSize(value: EFontSize) {
        uiState = uiState.copy(fontSize = value)

        PrefManager.keyFontSize = uiState.fontSize.size
    }

    fun setFontTheme(value: EFontTheme) {
        val xmlColorTheme = XmlColorTheme.createTheme(value)
        uiState = uiState.copy(fontTheme = value, xmlColorTheme = xmlColorTheme)

        PrefManager.keyFontTheme = uiState.fontTheme.ordinal
    }

    fun saveChanges(context: Context): Boolean {
        val file = uiState.file!!
        val pkgName = uiState.pkgName!!
        val pref = PreferenceFile.fromXml(uiState.editText ?: "")

        backupFile(context, pkgName, file)

        return Utils.savePreferences(context, pref, file, pkgName)
    }

    private fun backupFile(context: Context, pkgName: String, file: String) {
        viewModelScope.launch {
            val date = Date()
            val result = Utils.backupFile(context, date.time, pkgName, file)
            Timber.d("Backup: $result")
        }
    }
}
