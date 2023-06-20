package fr.simon.marquis.preferencesmanager.ui.preferences

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.PreferenceFile
import fr.simon.marquis.preferencesmanager.model.PreferenceType
import fr.simon.marquis.preferencesmanager.ui.components.DialogPreference
import fr.simon.marquis.preferencesmanager.ui.components.EmptyView
import fr.simon.marquis.preferencesmanager.util.Utils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PreferenceFragment(
    preferencePath: String,
    pkgName: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    val preference by remember(preferencePath) {
        val content = Utils.readFile(preferencePath)
        val file = PreferenceFile.fromXml(content, preferencePath)
        mutableStateOf(file)
    }

    // TODO implement edit dialogs
    var preferenceItemDialog by remember { mutableStateOf(false) }
    var preferenceItemType by remember { mutableStateOf(PreferenceType.UNSUPPORTED) }
    DialogPreference(
        openDialog = preferenceItemDialog,
        preferenceType = preferenceItemType,
        confirmButton = { key, value ->
            preferenceItemDialog = false
        },
        deleteButton = { key ->
            // TODO crashes since the list size changed. Hoist more of the work higher.
            preference.removeValue(key)
            Utils.savePreferences(context, preference, preference.file, pkgName)
            preferenceItemDialog = false
        },
        dismissButton = {
        }
    )

    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        Box(contentAlignment = Alignment.Center) {
            EmptyView(
                isEmpty = preference.list.isEmpty(),
                emptyMessage = stringResource(id = R.string.empty_preference_file_valid)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = scrollState,
                contentPadding = WindowInsets.navigationBars.asPaddingValues()
            ) {
                items(items = preference.list, key = { it.key }) { item ->
                    PreferencesEntryItem(
                        modifier = Modifier.animateItemPlacement(),
                        item = item,
                        onClick = {
                            // onClick
                            val type = PreferenceType.fromObject(it.value).apply {
                                key = it.key
                                value = it.value
                                isEdit = true
                            }
                            preferenceItemType = type
                            preferenceItemDialog = true
                        },
                        onLongClick = {
                            // onLongClick
                        }
                    )
                }
            }
        }
    }
}
