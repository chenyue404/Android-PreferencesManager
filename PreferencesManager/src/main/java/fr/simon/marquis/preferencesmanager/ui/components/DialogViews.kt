package fr.simon.marquis.preferencesmanager.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.simon.marquis.preferencesmanager.R
import fr.simon.marquis.preferencesmanager.ui.theme.AppTheme

@Composable
internal fun CustomViewChoiceItem(
    item: String,
    index: Int,
    selected: Boolean,
    enabled: Boolean,
    onSelect: (index: Int) -> Unit,
    onDelete: (index: Int) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = {
                if (enabled) {
                    onSelect(index)
                }
            },
            enabled = enabled
        )
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(32.dp)
        )
        Text(
            item,
            modifier = Modifier.weight(1f),
            color = if (enabled) {
                MaterialTheme.colors.onSurface
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
            },
            style = MaterialTheme.typography.body1,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(32.dp)
        )
        IconButton(
            onClick = {
                if (enabled) {
                    onDelete(index)
                }
            },
            enabled = enabled
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }
}

@Composable
private fun DialogViewTextValue(
    modifier: Modifier = Modifier,
    isEdit: Boolean,
    editKey: String? = null,
    editValue: String? = null,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    if (isEdit && editKey == null && editValue == null) {
        throw NullPointerException("Preference KeyValue was null on edit")
    }

    var textKey by remember { mutableStateOf(editKey ?: "") }
    var textValue by remember { mutableStateOf(editValue ?: "") }

    Column(
        modifier = modifier.padding(top = 5.dp, bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textKey,
            onValueChange = { textKey = it },
            label = { Text(text = stringResource(id = R.string.hint_key)) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = textValue,
            onValueChange = { textValue = it },
            label = { Text("Label") }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
            Spacer(modifier = Modifier.width(10.dp))
            if (isEdit) {
                OutlinedButton(onClick = onDelete) {
                    Text(text = stringResource(id = R.string.dialog_delete))
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            OutlinedButton(onClick = onUpdate) {
                val textId = if (isEdit) R.string.dialog_update else R.string.dialog_add
                Text(text = stringResource(id = textId))
            }
        }
    }
}

@Composable
internal fun DialogViewBooleanValue(
    modifier: Modifier = Modifier,
    isEdit: Boolean,
    editKey: String? = null,
    editValue: Boolean? = null,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    if (isEdit && editKey == null && editValue == null) {
        throw java.lang.NullPointerException("Preference KeyValue was null on edit")
    }

    var textKey by remember { mutableStateOf(editKey ?: "") }
    var textValue by remember { mutableStateOf(editValue ?: false) }

    Column(
        modifier = modifier.padding(top = 5.dp, bottom = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textKey,
            onValueChange = { textKey = it },
            label = { Text(text = stringResource(id = R.string.hint_key)) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "False")
            Spacer(modifier = Modifier.width(6.dp))
            Switch(
                checked = textValue,
                onCheckedChange = { textValue = it }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "True")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.dialog_cancel))
            }
            Spacer(modifier = Modifier.width(10.dp))
            if (isEdit) {
                OutlinedButton(onClick = onDelete) {
                    Text(text = stringResource(id = R.string.dialog_delete))
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            OutlinedButton(onClick = onUpdate) {
                val textId = if (isEdit) R.string.dialog_update else R.string.dialog_add
                Text(text = stringResource(id = textId))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_RestoreItem() {
    AppTheme(isSystemInDarkTheme()) {
        CustomViewChoiceItem(
            item = "00/00/00 12:34:56PM",
            index = 1,
            selected = true,
            enabled = true,
            onDelete = {},
            onSelect = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_DialogViewStringValue_Edit() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        DialogViewTextValue(
            isEdit = true,
            editKey = "Some Cool Key",
            editValue = "Some Cool Value",
            onUpdate = {},
            onDelete = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_DialogViewStringValue_New() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        DialogViewTextValue(
            isEdit = false,
            onUpdate = {},
            onDelete = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_DialogViewBooleanValue_Edit() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        DialogViewBooleanValue(
            isEdit = true,
            editKey = "Some Cool Key",
            editValue = true,
            onUpdate = {},
            onDelete = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_DialogViewBooleanValue_New() {
    AppTheme(isDarkTheme = isSystemInDarkTheme()) {
        DialogViewBooleanValue(
            isEdit = false,
            onUpdate = {},
            onDelete = {},
            onCancel = {}
        )
    }
}
