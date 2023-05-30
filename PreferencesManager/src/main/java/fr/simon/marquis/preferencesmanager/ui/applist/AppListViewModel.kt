package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.model.ThemeSettingsImpl
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

data class AppListState(
    val isLoading: Boolean = false,
    val isRootGranted: Boolean = false,
    val isSearching: Boolean = false,
    val appList: List<AppEntry> = listOf(),
    val filteredAppList: List<AppEntry> = listOf()
)

class AppListViewModel : ViewModel() {

    private var _showSplashScreen = MutableStateFlow(true)
    val showSplashScreen = _showSplashScreen.asStateFlow()

    var themeSettings: ThemeSettingsImpl = ThemeSettingsImpl()
        private set

    var uiState by mutableStateOf(AppListState())
        private set

    private val _searchText = MutableStateFlow(TextFieldValue(""))
    val searchText: MutableStateFlow<TextFieldValue> = _searchText

    init {
        viewModelScope.launch {
            searchText.collectLatest {
                searchText(it.text)
            }
        }
    }

    fun isLoadingComplete() {
        _showSplashScreen.value = false
    }

    fun setIsSearching(value: Boolean) {
        uiState = uiState.copy(isSearching = value)
    }

    fun checkRoot() {
        uiState = uiState.copy(isRootGranted = Shell.isAppGrantedRoot() ?: false)

        Timber.i("Root access is ${uiState.isRootGranted}")
    }

    private fun searchText(value: String) {
        val isSearching = uiState.isSearching && searchText.value.text.isNotEmpty()
        val list = if (isSearching) {
            uiState.appList.filter {
                it.label
                    .lowercase(Locale.getDefault())
                    .contains(value.lowercase(Locale.getDefault()))
            }
        } else {
            uiState.appList
        }

        uiState = uiState.copy(filteredAppList = list)
    }

    fun startTask(context: Context) {
        viewModelScope.executeAsyncTask(
            onPreExecute = {
                uiState = uiState.copy(isLoading = true)
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                Utils.getApplications(context)
            },
            onPostExecute = {
                uiState = uiState.copy(
                    isLoading = false,
                    appList = it,
                    filteredAppList = it
                )
            },
            onProgressUpdate = {
            }
        )
    }
}
