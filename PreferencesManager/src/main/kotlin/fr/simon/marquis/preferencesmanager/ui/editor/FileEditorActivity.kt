package fr.simon.marquis.preferencesmanager.ui.editor

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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.model.EAppTheme
import fr.simon.marquis.preferencesmanager.model.EFontTheme
import fr.simon.marquis.preferencesmanager.model.XmlColorTheme
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.components.DialogSaveChanges
import fr.simon.marquis.preferencesmanager.ui.components.NavigationBack
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_FILE
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_PACKAGE_NAME
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import fr.simon.marquis.preferencesmanager.util.PrefManager
import kotlinx.coroutines.launch
import timber.log.Timber

class FileEditorActivity : ComponentActivity() {

    private val viewModel: FileEditorViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )

        val intent = intent.extras
        if (intent == null) {
            finish()
            return
        }

        Timber.i("onCreate")
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            fun showSnackBar(@StringRes id: Int) {
                scope.launch {
                    snackbarHostState.showSnackbar(getString(id))
                }
            }

            val context = LocalContext.current
            val topBarState = rememberTopAppBarState()
            val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

            LaunchedEffect(Unit) {
                val file = intent.getString(KEY_FILE)
                val fileSeparator = System.getProperty("file.separator")
                val title = file?.let {
                    file.substring(file.lastIndexOf(fileSeparator!!) + 1)
                }
                val pkgName = intent.getString(KEY_PACKAGE_NAME)
                viewModel.setPackageInfo(file, title, pkgName)
            }

            var saveChangesState by remember { mutableStateOf(false) }
            DialogSaveChanges(
                openDialog = saveChangesState,
                onPositive = {
                    if (viewModel.saveChanges(this@FileEditorActivity)) {
                        showSnackBar(R.string.save_success)
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        showSnackBar(R.string.save_fail)
                    }
                    saveChangesState = false
                },
                onNegative = {
                    onBackPressedDispatcher.onBackPressed()
                    saveChangesState = false
                },
                onCancel = {
                    saveChangesState = false
                }
            )

            val isDarkTheme = when (EAppTheme.getAppTheme(PrefManager.themePreference)) {
                EAppTheme.AUTO -> isSystemInDarkTheme()
                EAppTheme.DAY -> false
                EAppTheme.NIGHT -> true
            }
            AppTheme(isDarkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.imePadding(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        AppBar(
                            scrollBehavior = scrollBehavior,
                            title = {
                                Text(
                                    text = uiState.title.orEmpty(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                NavigationBack {
                                    if (uiState.textChanged) {
                                        saveChangesState = true
                                        return@NavigationBack
                                    }

                                    onBackPressedDispatcher.onBackPressed()
                                }
                            },
                            actions = {
                                FileEditorMenu(
                                    onSave = {
                                        if (!uiState.textChanged) {
                                            showSnackBar(R.string.toast_no_changes)
                                            return@FileEditorMenu
                                        }

                                        val saveChanges = viewModel.saveChanges(context)
                                        if (saveChanges) {
                                            showSnackBar(R.string.save_success)
                                        } else {
                                            showSnackBar(R.string.save_fail)
                                        }
                                    },
                                    onFontTheme = viewModel::setFontTheme,
                                    onFontSize = viewModel::setFontSize
                                )
                            }
                        )
                    }
                ) { paddingValues ->
                    // Weird bottom padding workaround:
                    // https://issuetracker.google.com/issues/249727298
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                            .systemBarsPadding(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        FileEditorLayout(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            xmlColorTheme = uiState.xmlColorTheme!!,
                            textSize = uiState.fontSize.size,
                            text = uiState.editText.orEmpty(),
                            onValueChange = viewModel::setTextChanged
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileEditorLayout(
    modifier: Modifier = Modifier,
    xmlColorTheme: XmlColorTheme,
    textSize: Int,
    text: String,
    onValueChange: (String) -> Unit
) {
    // NOTE: there is a bug that when there is a lot of text, the performance tanks
    BasicTextField(
        modifier = modifier,
        textStyle = TextStyle.Default.copy(fontSize = textSize.sp),
        value = text,
        onValueChange = onValueChange,
        visualTransformation = XmlTransformation(xmlColorTheme)
    )
}

@Preview
@Composable
private fun Preview_FileEditorLayout() {
    val xmlColorTheme = XmlColorTheme.createTheme(EFontTheme.ECLIPSE)

    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        FileEditorLayout(
            xmlColorTheme = xmlColorTheme,
            textSize = PrefManager.keyFontSize,
            text = stringResource(id = R.string.about_body),
            onValueChange = {}
        )
    }
}
