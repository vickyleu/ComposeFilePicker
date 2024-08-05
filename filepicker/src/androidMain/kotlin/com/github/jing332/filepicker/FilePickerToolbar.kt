package com.github.jing332.filepicker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.cancel
import compose_filepicker.filepicker.generated.resources.grid
import compose_filepicker.filepicker.generated.resources.list
import compose_filepicker.filepicker.generated.resources.more_options
import compose_filepicker.filepicker.generated.resources.new_folder
import compose_filepicker.filepicker.generated.resources.ok
import compose_filepicker.filepicker.generated.resources.refresh
import compose_filepicker.filepicker.generated.resources.search
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource

private const val TAG = "FilePicker"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BasicToolbar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    title: @Composable () -> Unit,
    actions: @Composable() (RowScope.() -> Unit) = {},
) {
    Column(modifier) {
        TopAppBar(title = title, navigationIcon = navigationIcon, actions = actions)
    }
}

private object Navigation {
    const val MAIN = 0
    const val SEARCH = 1
    const val SELECT = 2
}

@Composable
fun FilePickerToolbar(
    modifier: Modifier,
    title: String,
    sortConfig: SortConfig,
    onSortConfigChange: (SortConfig) -> Unit,
    viewType: Int,
    onSwitchViewType: (Int) -> Unit,
    selectedCount: Int,
    closeSearch: MutableState<Boolean>,
    onSearch: (Int, String) -> Unit,
    onRefresh: () -> Unit,
) {
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var searchType by rememberSaveable { mutableIntStateOf(SearchType.ALL) }

    if (closeSearch.value) {
        showSearch = false
        closeSearch.value = false
        onSearch(SearchType.ALL, "")
        searchText = ""
    }

    BackHandler(showSearch) { closeSearch.value = true }
    Column {
        Crossfade(targetState = selectedCount > 0, label = "") { selectMode ->
            BasicToolbar(modifier = modifier,
                title = {
                    Crossfade(targetState = showSearch, label = "") { search ->
                        if (search) {
                            LaunchedEffect(Unit) {
                                var lastType = 0
                                var lastText = ""
                                while (coroutineContext.isActive) {
                                    delay(500)
                                    if (lastType == searchType && lastText == searchText) continue
                                    onSearch(searchType, searchText)

                                    lastType = searchType
                                    lastText = searchText
                                }
                            }
                            SearchTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                type = searchType,
                                onTypeChange = { searchType = it },
                                onClose = { closeSearch.value = true }
                            )
                        } else
                            Text(text = title, maxLines = 1)
                    }
                },
                actions = {
                    var showSortConfigDialog by remember { mutableStateOf(false) }
                    if (showSortConfigDialog)
                        SortSettingsDialog(
                            onDismissRequest = { showSortConfigDialog = false },
                            sortConfig = sortConfig,
                            onConfirm = onSortConfigChange
                        )
                    AnimatedVisibility(visible = !showSearch) {
                        Row {
                            IconButton(onClick = { showSearch = true }) {
                                Icon(Icons.Default.Search, stringResource(Res.string.search))
                            }
                        }
                    }

                    var showOptions by rememberSaveable { mutableStateOf(false) }
                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(Res.string.more_options))
                        DropdownMenu(
                            expanded = showOptions,
                            onDismissRequest = { showOptions = false }) {
                            RadioDropdownMenuItem(
                                text = {
                                    Text(stringResource(Res.string.list))
                                },

                                checked = viewType == ViewType.LIST,
                                onClick = {
                                    showOptions = false
                                    onSwitchViewType(if (viewType == ViewType.LIST) ViewType.GRID else ViewType.LIST)
                                }
                            )
                            HorizontalDivider(Modifier.fillMaxWidth())
                            RadioDropdownMenuItem(
                                text = {
                                    Text(stringResource(Res.string.grid))
                                },
                                checked = viewType == ViewType.GRID,
                                onClick = {
                                    showOptions = false
                                    onSwitchViewType(if (viewType == ViewType.LIST) ViewType.GRID else ViewType.LIST)
                                }
                            )

                            HorizontalDivider(Modifier.fillMaxWidth())
                            /*DropdownMenuItem(
                                text = { Text(stringResource(Res.string.sort_by)) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Send, null) },
                                onClick = {
                                    showOptions = false
                                    showSortConfigDialog = true
                                }
                            )*/

                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.refresh)) },
                                leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                onClick = {
                                    showOptions = false
                                    onRefresh()
                                }
                            )
                        }
                    }
                })
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
                .shadow(4.dp)
        )
    }
}

@Composable
internal fun RadioDropdownMenuItem(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    checked: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        modifier = modifier
            .semantics {
                role = Role.RadioButton
                selected = checked

                selectableGroup()
            },
        text = text,
        onClick = onClick,
        leadingIcon = {
            if (checked)
                Icon(Icons.Default.CheckCircle, null)
            else
                Icon(Icons.Default.CheckCircle, null)
        }
    )
}

@Composable
private fun NewFolderDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.new_folder)) },
        text = {
            OutlinedTextField(value = text, onValueChange = { text = it })
        },
        confirmButton = {
            TextButton(
                enabled = text.isNotBlank(),
                onClick = {
                    onConfirm(text)
                    onDismissRequest()
                }) {
                Text(stringResource(Res.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}