package fr.simon.marquis.preferencesmanager.ui.preferences

import android.content.Context
import android.net.Uri
import android.util.Pair
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.simon.marquis.preferencesmanager.model.BackupContainer
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date

data class PreferencesState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val isFavorite: Boolean = false,
    val pkgIcon: Uri? = null,
    val pkgName: String = "",
    val pkgTitle: String = "",
    val restoreData: BackupContainer? = null,
    val tabList: List<TabItem> = listOf(),
)

class PreferencesViewModel : ViewModel() {

    var uiState by mutableStateOf(PreferencesState())
        private set

    private val _searchText = MutableStateFlow(TextFieldValue(""))
    val searchText: MutableStateFlow<TextFieldValue> = _searchText

    fun setPackageInfo(pkgTitle: String, pkgName: String, pkgIcon: Uri?) {
        uiState = uiState.copy(
            pkgTitle = pkgTitle,
            pkgName = pkgName,
            pkgIcon = pkgIcon
        )
    }

    fun setIsSearching(value: Boolean) {
        uiState = uiState.copy(isSearching = value)
    }

    fun clearRestoreData() {
        uiState = uiState.copy(restoreData = null)
    }

    fun getTabsAndPreferences() {
        viewModelScope.executeAsyncTask(
            onPreExecute = {
                uiState = uiState.copy(isLoading = true)
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                val xmlFiles = Utils.findXmlFiles(uiState.pkgName)
                val xmlPreferences = xmlFiles.map { file ->
                    val content = Utils.readFile(file)
                    PreferenceFile.fromXml(content, file)
                }

                Pair(xmlFiles, xmlPreferences)
            },
            onPostExecute = {
                val tabList = it.first.mapIndexed { index, string ->
                    TabItem(pkgName = string, preferenceFile = it.second[index])
                }
                uiState = uiState.copy(tabList = tabList, isLoading = false)
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

        uiState = uiState.copy(restoreData = container)

        hasResult(container.backupList.isNotEmpty())
    }

    fun performFileRestore(
        ctx: Context,
        fileName: String,
        packageName: String,
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
