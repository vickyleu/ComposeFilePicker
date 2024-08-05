package com.github.jing332.compose_filepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import com.github.jing332.filepicker.model.IFileModel
import com.github.jing332.filepicker.startPickerHandler

object SelectMode {
    const val FILE = 0
    const val FOLDER = 1
    const val ALL = 2
}

@Composable
fun FilePickerScreen(
    modifier: Modifier = Modifier,
    onSelectFile: (IFileModel) -> Unit
) {
    val context = LocalPlatformContext.current

    Column(modifier) {
        val scope = rememberCoroutineScope()
        startPickerHandler(scope) {
            onSelectFile.invoke(it)
        }
    }
}