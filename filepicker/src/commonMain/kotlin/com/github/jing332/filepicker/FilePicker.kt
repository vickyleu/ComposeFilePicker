package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import kotlinx.coroutines.CoroutineScope

private const val TAG = "FilePicker"


@Composable
expect fun startPickerHandler(
    scope: CoroutineScope,
    callback: ((IFileModel) -> Unit),
)

expect fun String.formatImpl(vararg args: Any): String
