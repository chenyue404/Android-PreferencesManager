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
package fr.simon.marquis.preferencesmanager.ui.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.*
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.components.DialogPreference
import fr.simon.marquis.preferencesmanager.ui.components.DialogRestore
import fr.simon.marquis.preferencesmanager.ui.components.EmptyView
import fr.simon.marquis.preferencesmanager.ui.editor.FileEditorActivity
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import fr.simon.marquis.preferencesmanager.util.Utils
import fr.simon.marquis.preferencesmanager.util.getParcelable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

const val KEY_ICON_URI = "KEY_ICON_URI"
const val KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME"
const val KEY_TITLE = "KEY_TITLE"
const val KEY_FILE = "KEY_FILE"

class PreferencesActivity : ComponentActivity() {

    private val viewModel: PreferencesViewModel by viewModels()

    private val contract = ActivityResultContracts.StartActivityForResult()
    private var resultFileEdit = registerForActivityResult(contract) {
        viewModel.getTabsAndPreferences()
    }

    @OptIn(
        ExperimentalFoundationApi::class,
        ExperimentalMaterial3Api::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val intent = intent.extras
        if (intent == null) {
            finish()
            return
        }

        Timber.i("onCreate")
        setContent {
            // TODO: Add columns to list if either a tablet or landscape mode.
            // val windowSizeClass = calculateWindowSizeClass(this)

            val context = LocalContext.current
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            fun showSnackBar(@StringRes id: Int) {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(id))
                }
            }

            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { uiState.tabList.size }
            )

            LaunchedEffect(Unit) {
                val pkgTitle = intent.getString(KEY_TITLE)!!
                val pkgName = intent.getString(KEY_PACKAGE_NAME)!!
                val pkgIcon = getParcelable(intent, KEY_ICON_URI, Uri::class.java)
                viewModel.setPackageInfo(pkgTitle, pkgName, pkgIcon)
                viewModel.getTabsAndPreferences()
            }

            LaunchedEffect(pagerState.isScrollInProgress) {
                viewModel.clearRestoreData()
            }

            val isDarkTheme = when (EAppTheme.getAppTheme(PrefManager.themePreference)) {
                EAppTheme.AUTO -> isSystemInDarkTheme()
                EAppTheme.DAY -> false
                EAppTheme.NIGHT -> true
            }

            AppTheme(isDarkTheme = isDarkTheme) {
                var restoreDialogState by remember { mutableStateOf(false) }
                DialogRestore(
                    openDialog = restoreDialogState,
                    container = uiState.restoreData,
                    onNegative = { restoreDialogState = false },
                    onRestore = { fileName ->
                        val pkgName = uiState.pkgName
                        viewModel.performFileRestore(context, fileName, pkgName)
                        restoreDialogState = false
                        showSnackBar(R.string.file_restored)
                    },
                    onDelete = { fileName ->
                        val currentPage = pagerState.currentPage
                        val currentTab = uiState.tabList[currentPage]
                        viewModel.deleteFile(context, fileName, currentTab)
                    }
                )

                // Y'know, it works, so I'm okay with it for now... Could be better
                var preferenceDialogVisible by remember { mutableStateOf(false) }
                var preferenceItem: PreferenceFile? by remember { mutableStateOf(null) }
                var preferenceItemType by remember { mutableStateOf(PreferenceType.UNSUPPORTED) }
                DialogPreference(
                    openDialog = preferenceDialogVisible,
                    preferenceType = preferenceItemType,
                    confirmButton = { prevKey, newKey, value, editMode ->
                        preferenceItem?.add(prevKey, newKey, value, editMode)
                        Utils.savePreferences(
                            ctx = context,
                            preferenceFile = preferenceItem,
                            file = preferenceItem!!.file,
                            packageName = uiState.pkgName
                        )
                        preferenceDialogVisible = false
                        viewModel.getTabsAndPreferences()
                    },
                    deleteButton = { key ->
                        preferenceItem?.removeValue(key)
                        Utils.savePreferences(
                            ctx = context,
                            preferenceFile = preferenceItem,
                            file = preferenceItem!!.file,
                            packageName = uiState.pkgName
                        )
                        preferenceDialogVisible = false
                        viewModel.getTabsAndPreferences()
                    },
                    dismissButton = {
                        preferenceDialogVisible = false
                    }
                )

                Surface {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            PreferencesAppBar(
                                scrollBehavior = null,
                                state = uiState,
                                searchText = viewModel.searchText,
                                onBackPressed = {
                                    onBackPressedDispatcher.onBackPressed()
                                },
                                onAddClicked = {
                                    preferenceItemType = it.apply {
                                        isEdit = false // It seems to retain data
                                    }
                                    preferenceItem = uiState.currentPage
                                    preferenceDialogVisible = true
                                },
                                onOverflowClicked = {
                                    val currentPage = pagerState.currentPage
                                    val currentTab = uiState.tabList[currentPage]

                                    when (it) {
                                        EPreferencesOverflow.EDIT -> editFile(currentTab)
                                        EPreferencesOverflow.FAV -> {
                                            val pkgName = uiState.pkgName
                                            val favorite = Utils.isFavorite(pkgName)
                                            Utils.setFavorite(pkgName, !favorite)
                                        }

                                        EPreferencesOverflow.BACKUP -> {
                                            val pkgName = uiState.pkgName
                                            viewModel.backupFile(context, pkgName, currentTab)
                                            showSnackBar(R.string.toast_backup_success)
                                        }

                                        EPreferencesOverflow.RESTORE -> {
                                            viewModel.findFilesToRestore(context, currentTab) { r ->
                                                if (r) {
                                                    restoreDialogState = true
                                                } else {
                                                    showSnackBar(R.string.empty_restore)
                                                }
                                            }
                                        }
                                    }
                                },
                                onSortClicked = {
                                    PrefManager.keySortType = it.ordinal
                                    viewModel.getTabsAndPreferences()
                                },
                                onIsSearching = viewModel::isSearching,
                                onIsNotSearching = viewModel::isNotSearching
                            )
                        }
                    ) { paddingValues ->
                        val haptic = LocalHapticFeedback.current
                        val topBarState = rememberTopAppBarState()
                        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

                        TabLayout(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize(),
                            scrollBehavior = scrollBehavior,
                            pagerState = pagerState,
                            state = uiState,
                            onPage = {
                                viewModel.currentPage(it)
                            },
                            onClick = { item, preference ->
                                preferenceItemType = PreferenceType.fromObject(item.value).apply {
                                    key = item.key
                                    value = item.value
                                    isEdit = true
                                }
                                preferenceItem = preference
                                preferenceDialogVisible = true
                            },
                            onLongClick = { item, preference ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // TODO: Add cab selection again, or some multi select option.
                            }
                        )
                    }
                }
            }
        }
    }

    private fun editFile(file: String) {
        val intent = Intent(this, FileEditorActivity::class.java).apply {
            putExtra(KEY_FILE, file)
            putExtra(KEY_ICON_URI, viewModel.uiState.value.pkgIcon)
            putExtra(KEY_PACKAGE_NAME, viewModel.uiState.value.pkgName)
        }

        resultFileEdit.launch(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesAppBar(
    scrollBehavior: TopAppBarScrollBehavior?,
    searchText: MutableStateFlow<TextFieldValue>,
    state: PreferencesState,
    onAddClicked: (value: PreferenceType) -> Unit,
    onBackPressed: () -> Unit,
    onOverflowClicked: (value: EPreferencesOverflow) -> Unit,
    onIsSearching: () -> Unit,
    onIsNotSearching: () -> Unit,
    onSortClicked: (value: EPreferencesSort) -> Unit
) {
    AppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                SubcomposeAsyncImage(
                    modifier = Modifier.size(48.dp),
                    model = state.pkgIcon,
                    contentDescription = null,
                    loading = {
                        CircularProgressIndicator()
                    },
                    error = {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = state.pkgTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.pkgName,
                        maxLines = 1,
                        fontSize = 12.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            PreferencesMenu(
                isFavorite = Utils.isFavorite(state.pkgName),
                onSearch = onIsSearching,
                onAddClicked = onAddClicked,
                onOverflowClicked = onOverflowClicked,
                onSortClicked = onSortClicked
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        textState = searchText,
        isSearching = state.isSearching,
        onSearchClose = onIsNotSearching
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TabLayout(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    scrollBehavior: TopAppBarScrollBehavior,
    state: PreferencesState,
    onPage: (page: PreferenceFile) -> Unit,
    onClick: (item: MutableMap.MutableEntry<String, Any>, file: PreferenceFile) -> Unit,
    onLongClick: (item: MutableMap.MutableEntry<String, Any>, file: PreferenceFile) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
            val scope = rememberCoroutineScope()
            if (state.tabList.isEmpty() && !state.isLoading) {
                EmptyView(
                    isEmpty = true,
                    emptyMessage = stringResource(id = R.string.empty_preference_application)
                )
            } else {
                // Tabs

                // TODO: Investigate?
                // Note:
                //  This if statement is a bit arbitrary...
                //  There is an idle game I play with well over 600 preferences. (whyyy)
                //  This composable will crash.
                //  So, let's kinda hack something together to render the 'tab' were in
                if (state.tabList.size < 500) {
                    ScrollableTabRow(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.surface,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.pagerTabIndicatorOffset(
                                    pagerState,
                                    tabPositions
                                )
                            )
                        }
                    ) {
                        state.tabList.forEachIndexed { index, tabItem ->
                            Tab(
                                text = {
                                    val pkgName = tabItem.substringAfterLast("/")
                                    val text = if (pkgName.length > 30) {
                                        "…${pkgName.takeLast(30)}"
                                    } else {
                                        pkgName
                                    }
                                    Text(text = text)
                                },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    Tab(
                        text = {
                            val pkgName = state.tabList[pagerState.currentPage].substringAfterLast(
                                "/"
                            )
                            val text = if (pkgName.length > 30) {
                                "…${pkgName.takeLast(30)}"
                            } else {
                                pkgName
                            }
                            Text(text = text)
                        },
                        selected = true,
                        onClick = {}
                    )
                }

                // Tab Content
                HorizontalPager(state = pagerState, beyondBoundsPageCount = 1) { page ->
                    val currentPage = state.tabList[page]
                    PreferenceFragment(
                        preferenceFile = currentPage,
                        onPage = onPage,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(92.dp))
        }
    }
}
