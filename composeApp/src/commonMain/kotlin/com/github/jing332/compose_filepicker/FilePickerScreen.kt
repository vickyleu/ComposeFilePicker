package com.github.jing332.compose_filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import com.github.jing332.filepicker.FilePicker
import com.github.jing332.filepicker.FilePickerConfiguration
import com.github.jing332.filepicker.model.IFileModel
import com.github.jing332.filepicker.rememberFilePickerState
import com.github.jing332.filepicker.showToast
import okio.internal.commonAsUtf8ToByteArray

object SelectMode {
    const val FILE = 0
    const val FOLDER = 1
    const val ALL = 2
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerScreen(
    modifier: Modifier = Modifier, filename: String? = null,
    onlyShowDir: Boolean,
    selectMode: Int,
    singleSelect: Boolean,
) {
    val context = LocalPlatformContext.current
    var showSelectedList by remember { mutableStateOf<List<IFileModel>?>(null) }
    if (showSelectedList != null) {
        val list = showSelectedList!!
        ModalBottomSheet(onDismissRequest = { showSelectedList = null }) {
            LazyColumn {
                items(list) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = it.name)
                        Text(text = it.path)
                    }
                }
            }
        }
    }

    fun saveFileCallback(file: IFileModel, name: String): Boolean {
        file.createFile(name).outputStream().use {
            it.write("Hello, World!".commonAsUtf8ToByteArray())
        }
        showToast(context, "Save successful.")
        return true
    }

    Column(modifier) {
        FilePicker(
            state = rememberFilePickerState(saveFilename = filename ?: ""),
            config = FilePickerConfiguration(
                fileFilter = { if (onlyShowDir) it.isDirectory else true },
                fileSelector = { checkedList, check ->
                    val pass = when (selectMode) {
                        SelectMode.FILE -> !check.isDirectory
                        SelectMode.FOLDER -> check.isDirectory
                        else -> true
                    }

                    if (pass) {
                        if (singleSelect) {
                            listOf(check)
                        } else {
                            checkedList + check
                        }
                    } else {
                        showToast(
                            context,
                            "Only ${if (selectMode == SelectMode.FILE) "file" else "directory"} can be selected.",
                        )
                        checkedList
                    }
                }
            ),
            onConfirmSelect = {
                showSelectedList = it
            },
            onSaveFile = if (filename != null) ::saveFileCallback else null
        )
    }
}