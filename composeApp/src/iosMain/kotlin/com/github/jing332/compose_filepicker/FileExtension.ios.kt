package com.github.jing332.compose_filepicker

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual suspend fun createTempFile(
    context: PlatformContext,
    launcher: StorageLauncher,
    key: String, name: String
): FileImpl? {
    val completer = CompletableDeferred<FileImpl?>()
    withContext(Dispatchers.IO) {
        // 定义长期保留的文件目录
        val directory:String = (NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() ?: return@withContext completer.complete(null) )as String
        val fileName = "temp_${key.md5()}/${name.ifEmpty { ".tmp" }}"
        val file = FileImpl(directory,fileName)
        completer.complete(file)
    }
    return completer.await()
}

actual val LocalStorageLauncher = compositionLocalOf<StorageLauncher?>(structuralEqualityPolicy()) {
    StorageLauncher()
}

actual class StorageLauncher {

}