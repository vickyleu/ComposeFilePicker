package com.github.jing332.filepicker.listpage

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.github.jing332.filepicker.FilePickerConfiguration
import com.github.jing332.filepicker.ViewType
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.back_to_previous_dir
import compose_filepicker.filepicker.generated.resources.file
import compose_filepicker.filepicker.generated.resources.folder
import compose_filepicker.filepicker.generated.resources.item_desc
import org.jetbrains.compose.resources.stringResource


@Composable
fun FileListPage(
    modifier: Modifier = Modifier,
    config: FilePickerConfiguration = FilePickerConfiguration(),
    state: FileListPageState = FileListPageState(),

    file: IFileModel,
    onBack: () -> Unit,
    onEnter: (IFileModel) -> Unit,
) {
    val hasChecked by rememberUpdatedState(newValue = state.hasChecked())


    val handler = getHapticFeedbackHandler()


    LaunchedEffect(key1 = file) {
        if (state.items.isEmpty()) {
            state.config = config
            state.file = file
            state.updateFiles(file)
        }
    }

    LaunchedEffect(key1 = hasChecked) {
        if (hasChecked) handler.performHaptic()
    }

    @Composable
    fun itemContent(item: FileItem) {
        if (!item.isVisible.value) return
        Item(
            isChecked = item.isChecked.value,
            isCheckable = if (item.isBackType) false else item.isCheckable.value,
            icon = {
                if (item.isDirectory) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(Res.string.folder)
                    )
                } else {
                    val fileType = config.fileDetector.detect(item.model)
                    if (fileType == null)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(Res.string.file)
                        )
                    else
                        fileType.IconContent()
                }
            },
            title = item.name,
            subtitle = if (item.isBackType)
                stringResource(Res.string.back_to_previous_dir)
            else
                "${item.fileLastModified.value} | " +
                        if (item.isDirectory) stringResource(
                            Res.string.item_desc,
                            item.fileCount.intValue
                        )
                        else item.fileSize.value,
            onCheckedChange = { _ ->
                state.selector(item)
            },
            onClick = {
                if (item.isBackType)
                    onBack()
                else if (!hasChecked && !item.isChecked.value && item.isDirectory)
                    onEnter(item.model)
                else if (item.isCheckable.value) state.selector(item)
            },
            onLongClick = {
                if (item.isBackType) onBack()
                else if (item.isCheckable.value) state.selector(item)
            },
            gridType = state.viewType == ViewType.GRID
        )
    }

    if (state.viewType == ViewType.GRID)
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Fixed(2),
            state = state.gridState
        ) {
            itemsIndexed(state.items, key = { _, item -> item.key }) { _, item ->
                itemContent(item = item)
            }
        }
    else
        LazyColumn(
            modifier = modifier,
            state = state.listState
        ) {
            itemsIndexed(state.items, key = { _, item -> item.key }) { _, item ->
                itemContent(item = item)
            }
        }
}

@Composable
expect fun getHapticFeedbackHandler(): HapticFeedbackHandler

expect class HapticFeedbackHandler {
    fun performHaptic()
}
