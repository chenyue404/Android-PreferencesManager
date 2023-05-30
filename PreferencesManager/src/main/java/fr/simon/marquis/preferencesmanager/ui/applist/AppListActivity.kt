/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.ui.applist

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.AppEntry
import fr.simon.marquis.preferencesmanager.model.EAppTheme
import fr.simon.marquis.preferencesmanager.model.ThemeSettings
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.components.DialogAbout
import fr.simon.marquis.preferencesmanager.ui.components.DialogNoRoot
import fr.simon.marquis.preferencesmanager.ui.components.DialogTheme
import fr.simon.marquis.preferencesmanager.ui.components.ScrollBackUp
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_ICON_URI
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_PACKAGE_NAME
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_TITLE
import fr.simon.marquis.preferencesmanager.ui.preferences.PreferencesActivity
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import fr.simon.marquis.preferencesmanager.util.Utils
import kotlinx.coroutines.launch
import timber.log.Timber

class AppListActivity : ComponentActivity() {

    private val viewModel: AppListViewModel by viewModels()

    private val activityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.run {
            if (uiState.isRootGranted) {
                startTask(this@AppListActivity)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel.checkRoot()

        if (savedInstanceState == null || Utils.previousApps == null) {
            viewModel.run {
                if (uiState.isRootGranted) {
                    startTask(this@AppListActivity)
                }
            }
        }

        Timber.i("onCreate")
        setContent {
            val uiState by viewModel::uiState

            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current
            val scope = rememberCoroutineScope()
            val scrollState = rememberLazyListState()
            val topBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

            var dialogNoRootState by remember { mutableStateOf(false) }
            DialogNoRoot(
                openDialog = dialogNoRootState,
                onPositive = {
                    dialogNoRootState = false
                    (context as Activity).finish()
                }
            )

            // Theme check via preferences.
            val theme = viewModel.themeSettings.themeStream.collectAsState()
            val isDarkTheme = when (theme.value) {
                EAppTheme.AUTO -> isSystemInDarkTheme()
                EAppTheme.DAY -> false
                EAppTheme.NIGHT -> true
            }

            if (!uiState.isRootGranted) {
                LaunchedEffect(Unit) {
                    scope.launch {
                        dialogNoRootState = true
                    }
                }
            }

            AppTheme(isDarkTheme = isDarkTheme) {
                AppListLayout(
                    scrollState = scrollState,
                    scrollBehavior = scrollBehavior,
                    viewModel = viewModel,
                    onClick = { entry ->
                        if (!uiState.isRootGranted) {
                            Timber.e("We don't have root to continue!")
                        } else {
                            val intent = Intent(context, PreferencesActivity::class.java).apply {
                                val pkgName = entry.applicationInfo.packageName
                                putExtra(KEY_ICON_URI, entry.iconUri)
                                putExtra(KEY_PACKAGE_NAME, pkgName)
                                putExtra(KEY_TITLE, entry.label)
                            }

                            activityResult.launch(intent)
                        }
                    },
                    onLongClick = { entry ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        val intent = Intent().apply {
                            val pkgName = entry.applicationInfo.packageName
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            addCategory(Intent.CATEGORY_DEFAULT)
                            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            data = Uri.fromParts("package", pkgName, null)
                        }

                        activityResult.launch(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AppListViewModel,
    themeSettings: ThemeSettings
) {
    val context = LocalContext.current
    val uiState by viewModel::uiState

    var dialogThemeState by remember { mutableStateOf(false) }
    DialogTheme(
        openDialog = dialogThemeState,
        initialSelection = PrefManager.themePreference,
        negativeText = "Cancel",
        onDismiss = { dialogThemeState = false },
        onNegative = { dialogThemeState = false },
        onPositive = {
            themeSettings.theme = EAppTheme.getAppTheme(it)
            PrefManager.themePreference = it
            dialogThemeState = false
        },
        positiveText = "OK",
        title = "Switch Theme"
    )

    var dialogAboutState by remember { mutableStateOf(false) }
    DialogAbout(
        openDialog = dialogAboutState,
        onPositive = { dialogAboutState = false }
    )

    AppBar(
        scrollBehavior = scrollBehavior,
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions = {
            AppListMenu(
                onSearch = { viewModel.setIsSearching(true) },
                onShowSystemApps = {
                    val currentValue = PrefManager.showSystemApps
                    PrefManager.showSystemApps = !currentValue

                    viewModel.startTask(context)
                },
                onSwitchTheme = { dialogThemeState = true },
                onAbout = { dialogAboutState = true }
            )
        },
        textState = viewModel.searchText,
        isSearching = uiState.isSearching,
        onSearchClose = { viewModel.setIsSearching(false) }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AppListLayout(
    scrollState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AppListViewModel,
    onClick: (entry: AppEntry) -> Unit,
    onLongClick: (entry: AppEntry) -> Unit
) {
    val uiState by viewModel::uiState

    Surface {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            floatingActionButton = {
                ScrollUpLayout(scrollState = scrollState)
            },
            floatingActionButtonPosition = FabPosition.Center,
            topBar = {
                AppListAppBar(
                    scrollBehavior = scrollBehavior,
                    viewModel = viewModel,
                    themeSettings = viewModel.themeSettings
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                state = scrollState,
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                val items = uiState.filteredAppList.groupBy { it.headerChar }
                items.forEach { (letter, item) ->
                    stickyHeader {
                        AppEntryHeader(letter = letter)
                    }
                    items(item) { entry ->
                        AppEntryItem(
                            modifier = Modifier.animateItemPlacement(),
                            entry = entry,
                            onClick = { onClick(entry) },
                            onLongClick = { onLongClick(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollUpLayout(
    modifier: Modifier = Modifier,
    scrollState: LazyListState
) {
    val scope = rememberCoroutineScope()
    val showBackUpButton = remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
    }
    ScrollBackUp(
        modifier = modifier,
        enabled = showBackUpButton.value,
        onClicked = {
            scope.launch {
                scrollState.animateScrollToItem(0)
            }
        }
    )
}
