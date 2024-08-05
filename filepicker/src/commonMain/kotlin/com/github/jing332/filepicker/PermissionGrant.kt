package com.github.jing332.filepicker

import androidx.compose.runtime.Composable

@Composable
expect fun PermissionGrant()



expect class SecurityFromFilePickerException : Exception


expect fun getExternalStorageDirectory(): String