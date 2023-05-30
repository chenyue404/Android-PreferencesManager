package fr.simon.marquis.preferencesmanager.ui.preferences

import android.content.Context
import android.net.Uri
import android.util.Pair
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
import java.util.Date
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
    val tabList: List<TabItem> = listOf()
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

    private fun searchText(value: String) {
//        val isSearching = uiState.value.isSearching && searchText.value.text.isNotEmpty()
//        val list = if (isSearching) {
//            uiState.value.appList.filter {
//                it.label
//                    .lowercase(Locale.getDefault())
//                    .contains(value.lowercase(Locale.getDefault()))
//            }
//        } else {
//            uiState.value.appList
//        }
//
//        _uiState.update { it.copy(filteredAppList = list) }
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

    fun setIsSearching(value: Boolean) {
        _uiState.update { it.copy(isSearching = value) }
    }

    fun clearRestoreData() {
        _uiState.update { it.copy(restoreData = null) }
    }

    fun getTabsAndPreferences() {
        viewModelScope.executeAsyncTask(
            onPreExecute = {
                _uiState.update { it.copy(isLoading = true) }
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                val xmlFiles = Utils.findXmlFiles(uiState.value.pkgName)
                val xmlPreferences = xmlFiles.map { file ->
                    val content = Utils.readFile(file)
                    PreferenceFile.fromXml(content, file)
                }

                Pair(xmlFiles, xmlPreferences)
            },
            onPostExecute = { pair ->
                val tabList = pair.first.mapIndexed { index, string ->
                    TabItem(pkgName = string, preferenceFile = pair.second[index])
                }
                _uiState.update { it.copy(tabList = tabList, isLoading = false) }
            },
            onProgressUpdate = {
            }
        )
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
}
