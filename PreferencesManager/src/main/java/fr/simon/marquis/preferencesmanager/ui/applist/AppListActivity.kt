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
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package fr.simon.marquis.preferencesmanager.ui.applist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.components.DialogAbout
import fr.simon.marquis.preferencesmanager.ui.components.DialogNoRoot
import fr.simon.marquis.preferencesmanager.ui.components.DialogTheme
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import fr.simon.marquis.preferencesmanager.util.Utils
import kotlinx.coroutines.launch
import timber.log.Timber

class AppListActivity : ComponentActivity() {

    private val viewModel: AppListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        viewModel.checkRoot()

        if (savedInstanceState == null || Utils.previousApps == null) {
            viewModel.run {
                if (viewModel.uiState.value.isRootGranted)
                    startTask(this@AppListActivity)
            }
        }

        Timber.i("onCreate")
        setContent {
            val uiState by viewModel.uiState

            val scope = rememberCoroutineScope()
            val topBarState = rememberTopAppBarState()
            val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topBarState) }

            val windowInset = Modifier
                .statusBarsPadding()
                .windowInsetsPadding(
                    WindowInsets
                        .navigationBars
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                )

            val dialogNoRootState = rememberMaterialDialogState()
            DialogNoRoot(dialogState = dialogNoRootState)

            if (!uiState.isRootGranted) {
                LaunchedEffect(Unit) {
                    scope.launch {
                        dialogNoRootState.show()
                    }
                }
            }

            AppTheme {
                Scaffold(
                    modifier = windowInset,
                    topBar = {
                        AppListAppBar(scrollBehavior = scrollBehavior, viewModel = viewModel)
                    }
                ) { paddingValues ->
                    AppListLayout(
                        paddingValues = paddingValues,
                        scrollBehavior = scrollBehavior,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppListAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AppListViewModel,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState

    // TODO live theme change
    val dialogThemeState = rememberMaterialDialogState()
    DialogTheme(dialogState = dialogThemeState) {}

    // TODO using AndroidView
    val dialogAboutState = rememberMaterialDialogState()
    DialogAbout(dialogState = dialogAboutState)

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
                onSwitchTheme = { dialogThemeState.show() },
                onAbout = { dialogAboutState.show() }
            )
        },
        textState = viewModel.searchText,
        isSearching = uiState.isSearching,
        onSearchClose = { viewModel.setIsSearching(false) }
    )
}

@Composable
fun AppListLayout(
    paddingValues: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AppListViewModel,

) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val uiState by viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = scrollState,
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                val items = uiState.filteredAppList.groupBy { it.headerChar }
                items.forEach { (letter, item) ->
                    stickyHeader {
                        AppEntryHeader(
                            modifier = Modifier, // .animateItemPlacement(),
                            letter = letter
                        )
                    }
                    items(item) { entry ->
                        AppEntryItem(
                            modifier = Modifier.animateItemPlacement(),
                            entry = entry,
                            onClick = { viewModel.launchPreference(context, entry) }
                        )
                    }
                }
            }
        }

        val showBackUpButton = remember {
            derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
        }
        ScrollBackUp(
            modifier = Modifier
                .navigationBarsPadding()
                .align(Alignment.BottomCenter),
            enabled = showBackUpButton.value,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }
        )
    }
}
