package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_ICON_URI
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_PACKAGE_NAME
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_TITLE
import fr.simon.marquis.preferencesmanager.ui.preferences.PreferencesActivity
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
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

    private val _uiState = mutableStateOf(AppListState())
    val uiState: State<AppListState> = _uiState

    private val _searchText = MutableStateFlow(TextFieldValue(""))
    val searchText: MutableStateFlow<TextFieldValue> = _searchText

    init {
        viewModelScope.launch {
            searchText.collectLatest {
                searchText(it.text)
            }
        }
    }

    fun setIsSearching(value: Boolean) {
        _uiState.value = uiState.value.copy(isSearching = value)
    }

    fun checkRoot() {
        _uiState.value = uiState.value.copy(isRootGranted = Shell.isAppGrantedRoot() ?: false)

        Timber.i("Root access is ${uiState.value.isRootGranted}")
    }

    private fun searchText(value: String) {
        val isSearching = uiState.value.isSearching && searchText.value.text.isNotEmpty()
        val list = if (isSearching) {
            uiState.value.appList.filter {
                it.label
                    .lowercase(Locale.getDefault())
                    .contains(value.lowercase(Locale.getDefault()))
            }
        } else {
            uiState.value.appList
        }

        _uiState.value = uiState.value.copy(filteredAppList = list)
    }

    fun startTask(context: Context) {
        viewModelScope.executeAsyncTask(
            onPreExecute = {
                _uiState.value = uiState.value.copy(isLoading = true)
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                Utils.getApplications(context)
            },
            onPostExecute = {
                _uiState.value = uiState.value.copy(
                    isLoading = false,
                    appList = it,
                    filteredAppList = it
                )
            },
            onProgressUpdate = {
            }
        )
    }

    fun launchPreference(context: Context, app: AppEntry) {
        if (!uiState.value.isRootGranted) {
            Timber.e("We don't have root to continue!")
        } else {
            val intent = Intent(context, PreferencesActivity::class.java).apply {
                putExtra(KEY_ICON_URI, app.iconUri)
                putExtra(KEY_PACKAGE_NAME, app.applicationInfo.packageName)
                putExtra(KEY_TITLE, app.label)
            }

            context.startActivity(intent)
        }
    }

    fun launchAppSettings(context: Context, app: AppEntry) {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            data = Uri.fromParts("package", app.applicationInfo.packageName, null)
        }

        context.startActivity(intent)
    }
}
