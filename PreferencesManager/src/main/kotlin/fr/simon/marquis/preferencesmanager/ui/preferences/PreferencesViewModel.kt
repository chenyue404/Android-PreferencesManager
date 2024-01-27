package fr.simon.marquis.preferencesmanager.ui.preferences

import android.content.Context
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.util.Utils
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class PreferencesState(
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val pkgIcon: Uri? = null,
    val pkgName: String = "",
    val pkgTitle: String = "",
    val restoreData: BackupContainer? = null,
    val tabList: List<String> = listOf(),
    val currentPage: PreferenceFile? = null
)

class PreferencesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PreferencesState())
    val uiState = _uiState.asStateFlow()

    private val _searchText = MutableStateFlow(TextFieldValue(""))
    val searchText: MutableStateFlow<TextFieldValue> = _searchText

    init {
        viewModelScope.launch {
            searchText.collectLatest {
                searchText(it.text)
            }
        }
    }

    // TODO search
    private fun searchText(value: String) {
        val isSearching = uiState.value.isSearching && searchText.value.text.isNotEmpty()
        if (isSearching) {
            val list = uiState.value.currentPage?.list?.filter {
                it.key.lowercase().contains(value.lowercase()) ||
                    it.value.toString().lowercase().contains(value.lowercase())
            }
            _uiState.value.currentPage?.setList(list.orEmpty())
        } else {
            uiState.value.currentPage?.list.orEmpty()
        }
    }

    fun setPackageInfo(pkgTitle: String, pkgName: String, pkgIcon: Uri?) {
        _uiState.update {
            it.copy(
                pkgTitle = pkgTitle,
                pkgName = pkgName,
                pkgIcon = pkgIcon
            )
        }
    }

    fun isSearching() {
        _uiState.update { it.copy(isSearching = true) }
    }

    fun isNotSearching() {
        _uiState.update { it.copy(isSearching = false) }
    }

    fun clearRestoreData() {
        _uiState.update { it.copy(restoreData = null) }
    }

    fun getTabsAndPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }

            val list = Utils.findXmlFiles(uiState.value.pkgName)

            _uiState.update {
                it.copy(
                    tabList = list,
                    isLoading = false
                )
            }
        }
    }

    fun backupFile(context: Context, pkgName: String, file: String) {
        viewModelScope.launch {
            val date = Date()
            val result = Utils.backupFile(context, date.time, pkgName, file)
            Timber.d("Backup: $result")
        }
    }

    fun findFilesToRestore(
        context: Context,
        file: String,
        hasResult: (value: Boolean) -> Unit = {}
    ) {
        val container = Utils.getBackups(context, file)

        Timber.d("Restore has ${container.backupList.size} items")

        _uiState.update { it.copy(restoreData = container) }

        hasResult(container.backupList.isNotEmpty())
    }

    fun performFileRestore(
        ctx: Context,
        fileName: String,
        packageName: String
    ) {
        val result = Utils.restoreFile(ctx, fileName, packageName)

        Timber.d("File Restore: $result")
        getTabsAndPreferences()
    }

    fun deleteFile(context: Context, fileName: String, pkgName: String) {
        val result = Utils.deleteFile(fileName)

        Timber.d("File Delete: $result")
        findFilesToRestore(context, pkgName)
    }

    fun currentPage(page: PreferenceFile) {
        _uiState.update { it.copy(currentPage = page) }
    }
}
