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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import kotlinx.coroutines.launch
import timber.log.Timber
import ua.hospes.lazygrid.LazyGridState
import ua.hospes.lazygrid.items
import ua.hospes.lazygrid.rememberLazyGridState

class AppListActivity : ComponentActivity() {

    private val viewModel: AppListViewModel by viewModels()

    private val activityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.run {
            if (uiState.value.isRootGranted) {
                startTask(this@AppListActivity)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.showSplashScreen.value
        }

        viewModel.getShell(this@AppListActivity)

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        Timber.i("onCreate")
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current

            val scrollState = rememberLazyGridState()
            val topBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

            var dialogNoRootState by remember(uiState.isRootGranted) {
                mutableStateOf(!uiState.isRootGranted)
            }
            DialogNoRoot(
                openDialog = dialogNoRootState,
                onPositive = {
                    dialogNoRootState = false
                    (context as Activity).finish()
                }
            )

            // Theme check via preferences.
            val theme by viewModel.themeSettings.themeStream.collectAsState()
            val isDarkTheme = when (theme) {
                EAppTheme.AUTO -> isSystemInDarkTheme()
                EAppTheme.DAY -> false
                EAppTheme.NIGHT -> true
            }

            AppTheme(isDarkTheme = isDarkTheme) {
                val haptic = LocalHapticFeedback.current

                AppListLayout(
                    windowSizeClass = windowSizeClass,
                    scrollState = scrollState,
                    scrollBehavior = scrollBehavior,
                    viewModel = viewModel,
                    onClick = { entry ->
                        if (uiState.isRootGranted) {
                            Intent(context, PreferencesActivity::class.java).apply {
                                val pkgName = entry.applicationInfo.packageName
                                putExtra(KEY_ICON_URI, entry.iconUri)
                                putExtra(KEY_PACKAGE_NAME, pkgName)
                                putExtra(KEY_TITLE, entry.label)
                            }.also { intent -> activityResult.launch(intent) }
                        } else {
                            Timber.e("We don't have root to continue!")
                        }
                    },
                    onLongClick = { entry ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                        val pkgName = entry.applicationInfo.packageName
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", pkgName, null)
                            addCategory(Intent.CATEGORY_DEFAULT)
                            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        }.also { intent -> activityResult.launch(intent) }
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var dialogThemeState by remember { mutableStateOf(false) }
    DialogTheme(
        openDialog = dialogThemeState,
        initialSelection = PrefManager.themePreference,
        negativeText = "Cancel",
        positiveText = "OK",
        title = "Switch Theme",
        onDismiss = { dialogThemeState = false },
        onNegative = { dialogThemeState = false },
        onPositive = {
            themeSettings.theme = EAppTheme.getAppTheme(it)
            PrefManager.themePreference = it
            dialogThemeState = false
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppListLayout(
    windowSizeClass: WindowSizeClass,
    scrollState: LazyGridState,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AppListViewModel,
    onClick: (entry: AppEntry) -> Unit,
    onLongClick: (entry: AppEntry) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Set the grid size if were Phone, Tablet, or Expanded.
    val gridSize by remember(windowSizeClass.widthSizeClass) {
        val size = when (windowSizeClass.widthSizeClass) {
            WindowWidthSizeClass.Compact -> 1
            WindowWidthSizeClass.Medium,
            WindowWidthSizeClass.Expanded -> 2

            else -> 1
        }
        mutableIntStateOf(size)
    }

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
            ua.hospes.lazygrid.LazyVerticalGrid(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                state = scrollState,
                columns = ua.hospes.lazygrid.GridCells.Fixed(gridSize),
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                uiState.filteredAppList.forEach { (letter, item) ->
                    stickyHeader {
                        AppEntryHeader(letter = letter)
                    }
                    items(item) { entry ->
                        AppEntryItem(
                            modifier = Modifier.animateItemPlacement(),
                            entry = entry,
                            onClick = onClick,
                            onLongClick = onLongClick
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
    scrollState: LazyGridState
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
