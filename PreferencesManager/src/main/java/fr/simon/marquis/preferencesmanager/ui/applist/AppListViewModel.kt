package fr.simon.marquis.preferencesmanager.ui.applist

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.ui.PreferencesActivity
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.executeAsyncTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class AppListViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isRootGranted = MutableStateFlow(false)
    val isRootGranted: StateFlow<Boolean> = _isRootGranted

    private val _appList = MutableStateFlow(mutableListOf<AppEntry>())
    val appList: StateFlow<MutableList<AppEntry>> = _appList

    fun checkRoot() {
        _isRootGranted.value = Shell.isAppGrantedRoot() ?: false

        Timber.i("Root access is ${_isRootGranted.value}")
    }

    fun startTask(context: Context) {
        viewModelScope.executeAsyncTask(
            onPreExecute = {
                _isLoading.value = true
            },
            doInBackground = { _: suspend (progress: Int) -> Unit ->
                Utils.getApplications(context)
            },
            onPostExecute = {
                _appList.value = it
                _isLoading.value = false
            },
            onProgressUpdate = {
            }
        )
    }

    fun launchPreference(context: Context, app: AppEntry) {
        if (!_isRootGranted.value) {
            Timber.e("We don't have root to continue!")
        } else {
            context.run {
                val intent = Intent(this, PreferencesActivity::class.java).apply {
                    putExtra(PreferencesActivity.KEY_ICON_URI, app.iconUri)
                    putExtra(PreferencesActivity.EXTRA_TITLE, app.label)
                    putExtra(
                        PreferencesActivity.EXTRA_PACKAGE_NAME,
                        app.applicationInfo.packageName
                    )
                }
                startActivity(intent)
            }
        }
    }
}
