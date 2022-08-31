@file:OptIn(ExperimentalMaterial3Api::class)

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
import android.text.*
import android.text.style.ForegroundColorSpan
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import fr.simon.marquis.preferencesmanager.ui.components.AppBar
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_FILE
import fr.simon.marquis.preferencesmanager.ui.preferences.KEY_PACKAGE_NAME
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme
import java.util.regex.Pattern
import timber.log.Timber

class FileEditorActivity : ComponentActivity() {

    private val viewModel: FileEditorViewModel by viewModels()

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
            val uiState by viewModel.uiState

            val topBarState = rememberTopAppBarState()
            val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior(topBarState) }

            LaunchedEffect(Unit) {
                val file = intent.getString(KEY_FILE)
                val fileSeparator = System.getProperty("file.separator")
                val title = file?.let {
                    file.substring(file.lastIndexOf(fileSeparator!!) + 1)
                }
                val pkgName = intent.getString(KEY_PACKAGE_NAME)

                viewModel.setPackageInfo(file, title, pkgName)
                viewModel.setXmlColorTheme(this@FileEditorActivity)
                viewModel.highlightXmlText()
            }

            val windowInset = Modifier
                .statusBarsPadding()
                .windowInsetsPadding(
                    WindowInsets
                        .navigationBars
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                )

            AppTheme {
                Scaffold(
                    modifier = windowInset,
                    topBar = {
                        AppBar(
                            scrollBehavior = scrollBehavior,
                            title = {
                                Text(
                                    text = uiState.title ?: "[Empty Package Name]",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        // TODO: Check if text was ever changed, show dialog if true. Or just exit.

                                        @Suppress("DEPRECATION")
                                        onBackPressed()
                                    }
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                FileEditorMenu(
                                    onSave = { /*TODO*/ },
                                    onFontTheme = { /*TODO*/ },
                                    onFontSize = { /*TODO*/ }
                                )
                            },
                        )
                    }
                ) { paddingValues ->
                    FileEditorLayout(
                        paddingValues = paddingValues,
                    )
                }
            }
        }
    }

    @Suppress("RegExpRedundantEscape")
    companion object {
        private val TAG_START = Pattern.compile("</?[-\\w\\?]+", Pattern.CASE_INSENSITIVE)
        private val TAG_END = Pattern.compile("\\??/?>")
        private val TAG_ATTRIBUTE_NAME = Pattern.compile("\\s(\\w*)\\=")
        private val TAG_ATTRIBUTE_VALUE = Pattern.compile("[a-z\\-]*\\=(\"[^\"]*\")")
        private val TAG_ATTRIBUTE_VALUE_2 = Pattern.compile("[a-z\\-]*\\=(\'[^\']*\')")
        private val COMMENT_START = Pattern.compile("<!--")
        private val COMMENT_END = Pattern.compile("-->")

        private fun generateSpan(source: Spannable, p: Pattern, color: Int) {
            val matcher = p.matcher(source)
            var start: Int
            var end: Int
            while (matcher.find()) {
                start = matcher.start()
                end = matcher.end()
                if (start != end) {
                    source.setSpan(
                        ForegroundColorSpan(color),
                        matcher.start(),
                        matcher.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }
}

@Composable
private fun FileEditorLayout(
    paddingValues: PaddingValues,
    text: String = "",
) {
    var editText by remember { mutableStateOf("Hello") }
    Column(modifier = Modifier.padding(paddingValues)) {
        TextField(
            modifier = Modifier.fillMaxSize(),
            value = editText,
            onValueChange = { editText = it },
        )
    }
}

@Preview
@Composable
private fun Preview_FileEditorLayout() {
    AppTheme {
        FileEditorLayout(paddingValues = PaddingValues())
    }
}
