package fr.simon.marquis.preferencesmanager.ui.editor

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import fr.simon.marquis.preferencesmanager.util.Utils

data class FileEditorState(
    val textChanged: Boolean = false,
    val file: String? = null,
    val title: String? = null,
    val pkgName: String? = null,
    val editText: String? = null,
    val fontTheme: EFontTheme? = null,
    val fontSize: EFontSize? = null,
    val xmlColorTheme: XmlColorTheme? = null,
)

class FileEditorViewModel : ViewModel() {

    private val _uiState = mutableStateOf(FileEditorState())
    val uiState: State<FileEditorState> = _uiState

    fun setTextChanged() {
        _uiState.value = uiState.value.copy(textChanged = true)
    }

    fun setPackageInfo(file: String?, title: String?, pkgName: String?) {
        _uiState.value = uiState.value.copy(
            file = file,
            title = title,
            pkgName = pkgName,
            editText = Utils.readFile(file!!)
        )

        setFontStyle()
    }

    fun setXmlColorTheme(context: Context) {
        val theme = XmlColorTheme.createTheme(context, uiState.value.fontTheme!!)
    }

    fun setFontStyle() {
        val size = PrefManager.keyFontSize.toString()
        val theme = PrefManager.keyFontTheme.toString()
        setFontSize(size)
        setFontTheme(theme)
    }

    fun setFontSize(value: String) {
        _uiState.value = uiState.value.copy(
            fontSize = EFontSize.valueOf(value)
        )
    }

    fun setFontTheme(value: String) {
        _uiState.value = uiState.value.copy(
            fontTheme = EFontTheme.valueOf(value),
        )
    }

    fun highlightXmlText() {
    }
}
