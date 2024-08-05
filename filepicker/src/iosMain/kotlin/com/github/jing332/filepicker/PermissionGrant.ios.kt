package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Composable
actual fun PermissionGrant() {
}

actual class SecurityFromFilePickerException : Exception()

// 获取iOS系统(File/文件/档案)中的路径
actual fun getExternalStorageDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val documentsDirectory = urls.first() as NSURL
    return documentsDirectory.path!!
}