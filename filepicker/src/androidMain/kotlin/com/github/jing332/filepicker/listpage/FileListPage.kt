package com.github.jing332.filepicker.listpage

import android.view.View
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.platform.LocalView
import com.github.jing332.filepicker.FilePickerConfiguration
import com.github.jing332.filepicker.ViewType
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.back_to_previous_dir
import compose_filepicker.filepicker.generated.resources.file
import compose_filepicker.filepicker.generated.resources.folder
import compose_filepicker.filepicker.generated.resources.folder_24px
import compose_filepicker.filepicker.generated.resources.item_desc
import compose_filepicker.filepicker.generated.resources.question_mark_24px
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileListPage(
    modifier: Modifier = Modifier,
    config: FilePickerConfiguration = FilePickerConfiguration(),
    state: FileListPageState = FileListPageState(),
    file: IFileModel,
    onBack: () -> Unit,
    onEnter: (IFileModel) -> Unit,
    onSelect: (IFileModel) -> Unit,
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
        val scope = rememberCoroutineScope()
        Item(
            isChecked = item.isChecked.value,
            isCheckable = if (item.isBackType) false else item.isCheckable.value,
            icon = {
                if (item.isDirectory) {
                    Icon(
                        painter = painterResource(Res.drawable.folder_24px),
                        contentDescription = stringResource(Res.string.folder),
                        tint = { Color.Black }
                    )
                } else {
                    val fileType = config.fileDetector.detect(item.model)
                    if (fileType == null)
                        Icon(
                            painter = painterResource(Res.drawable.question_mark_24px),
                            contentDescription = stringResource(Res.string.file),
                            tint = { Color.Black }
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
                if(item.isDirectory)return@Item
                scope.launch {
                    onSelect.invoke(item.model)
                }
            },
            onClick = {
                if (item.isBackType) {
                    println("onEnterDirectory 返回上一级")
                    onBack()
                } else if (!hasChecked && !item.isChecked.value && item.isDirectory)
                    onEnter(item.model)
                else if (item.isCheckable.value) {
                    state.selector(item)
                    if(item.isDirectory)return@Item
                    scope.launch {
                        onSelect.invoke(item.model)
                    }
                }
            },
            onLongClick = {
                if (item.isBackType) onBack()
                else if (item.isCheckable.value) {
                    state.selector(item)
                    if(item.isDirectory)return@Item
                    scope.launch {
                        onSelect.invoke(item.model)
                    }
                }
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

class HapticFeedbackHandler(private val view: View) {
    fun performHaptic() {
        view.isHapticFeedbackEnabled = true
        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
    }
}

@Composable
fun getHapticFeedbackHandler(): HapticFeedbackHandler {
    val view = LocalView.current
    return remember {
        HapticFeedbackHandler(view)
    }
}
