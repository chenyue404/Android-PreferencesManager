package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.model.ThemeSettingsImpl
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppListState(
    val isLoading: Boolean = false,
    val isRootGranted: Boolean = false,
    val isSearching: Boolean = false,
    val appList: List<AppEntry> = listOf(),
    val filteredAppList: Map<Char, List<AppEntry>> = mapOf()
)

class AppListViewModel : ViewModel() {

    private var _showSplashScreen = MutableStateFlow(true)
    val showSplashScreen = _showSplashScreen.asStateFlow()

    var themeSettings: ThemeSettingsImpl = ThemeSettingsImpl()
        private set

    private val _uiState = MutableStateFlow(AppListState())
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

    fun getShell(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val hasShell = Shell.getShell()
            _showSplashScreen.value = !hasShell.isRoot

            val hasRoot = Shell.isAppGrantedRoot() ?: false
            _uiState.update { it.copy(isRootGranted = hasRoot) }

            if (hasRoot) {
                startTask(context)
            }
        }
    }

    fun setIsSearching(value: Boolean) {
        _uiState.update { it.copy(isSearching = value) }
    }

    private fun searchText(value: String) {
        val isSearching = uiState.value.isSearching && searchText.value.text.isNotEmpty()
        val list = if (isSearching) {
            uiState.value.appList.filter {
                it.label.contains(value, true) || it.packageName.contains(value, true)
            }
        } else {
            uiState.value.appList
        }

        val groupedList = list.groupBy { it.headerChar }
        _uiState.update { it.copy(filteredAppList = groupedList) }
    }

    fun startTask(context: Context) {
        viewModelScope.executeAsyncTask(
            onPreExecute = {
                _uiState.update { it.copy(isLoading = true) }
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                Utils.getApplications(context)
            },
            onPostExecute = { list ->
                val groupedList = list.groupBy { it.headerChar }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        appList = list,
                        filteredAppList = groupedList
                    )
                }
            },
            onProgressUpdate = {
            }
        )
    }
}
