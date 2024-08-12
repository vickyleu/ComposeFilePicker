package com.github.jing332.filepicker

import androidx.compose.runtime.Composable


expect class SecurityFromFilePickerException : Exception


expect fun getExternalStorageDirectory(): String