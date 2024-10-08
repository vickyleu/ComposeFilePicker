package com.github.jing332.compose_filepicker

import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.Lifecycle
import coil3.PlatformContext
import kotlinx.coroutines.CoroutineScope


val LocalStoragePermission = compositionLocalOf<StoragePermissionUtil?> {
    null
}


expect class StoragePermissionUtil(
    context: PlatformContext,
    lifecycle: Lifecycle,
    scope: CoroutineScope,
) {
    suspend fun checkCameraPermission(): Boolean
    suspend fun checkStoragePermission(): Boolean
    suspend fun requestStoragePermission(onGranted: () -> Unit, onDenied: () -> Unit)
    suspend fun requestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit)
}

// 跳转到应用设置页面
expect fun PlatformContext.goToAppSetting()