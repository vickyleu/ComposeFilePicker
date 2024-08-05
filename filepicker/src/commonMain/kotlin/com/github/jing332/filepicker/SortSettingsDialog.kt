package com.github.jing332.filepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.by_date
import compose_filepicker.filepicker.generated.resources.by_name
import compose_filepicker.filepicker.generated.resources.by_size
import compose_filepicker.filepicker.generated.resources.by_type
import compose_filepicker.filepicker.generated.resources.cancel
import compose_filepicker.filepicker.generated.resources.ok
import compose_filepicker.filepicker.generated.resources.reverse_sorting
import compose_filepicker.filepicker.generated.resources.sort_by
import org.jetbrains.compose.resources.stringResource

object SortType {
    const val NAME = 0
    const val SIZE = 1
    const val DATE = 2
    const val TYPE = 3
}

data class SortConfig(
    val sortBy: Int = SortType.NAME,
    val reverse: Boolean = false
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortSettingsDialog(
    onDismissRequest: () -> Unit,
    sortConfig: SortConfig,
    onConfirm: (SortConfig) -> Unit = {}
) {
    var config by remember { mutableStateOf(sortConfig) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(Res.string.sort_by))
        }, text = {
            val strings = remember {
                listOf(
                    Res.string.by_name,
                    Res.string.by_size,
                    Res.string.by_date,
                    Res.string.by_type
                )
            }
            Column {
                FlowRow(maxItemsInEachRow = 2) {
                    for (i in (0..3)) {
                        Row(
                            Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .clickable(role = Role.RadioButton) {
                                    config = config.copy(sortBy = i)
                                }
                                .minimumInteractiveComponentSize()
                        ) {
                            RadioButton(selected = config.sortBy == i, onClick = null)
                            Text(text = stringResource(strings[i]))
                        }
                    }
                }
                Row(
                    Modifier
                        .padding(top = 8.dp)
                        .clickable(role = Role.Checkbox) {
                            config = config.copy(reverse = !config.reverse)
                        }
                        .minimumInteractiveComponentSize()) {
                    Checkbox(checked = config.reverse, onCheckedChange = null)
                    Text(text = stringResource(Res.string.reverse_sorting))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(config)
                onDismissRequest()
            }) {
                Text(text = stringResource(Res.string.ok))
            }
        }, dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(Res.string.cancel))
            }
        }
    )
}
