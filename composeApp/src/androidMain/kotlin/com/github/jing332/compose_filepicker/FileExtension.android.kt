package com.github.jing332.compose_filepicker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.Lifecycle
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// FileImpl alias for File
actual suspend fun createTempFile(
    context: PlatformContext,
    launcher: StorageLauncher,
    key: String, name: String
): FileImpl? {
    val completer = CompletableDeferred<FileImpl?>()
    withContext(Dispatchers.IO) {
        // 需要检查文件写入权限是否存在,不存在就请求权限,还需要判断Android 11 以上的权限
        // 检查是否有写入权限
        val isGranted = when {
            scopeStorageCheck() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Environment.isExternalStorageManager()
                } else {
                    context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                }
            }

            else -> {
                context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            }
        }
        // 定义长期保留的文件目录
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val fileName = "temp_${key.md5()}/${name.ifEmpty { ".tmp" }}"
        val file = File(directory, fileName)
        if (isGranted) {
            completer.complete(file)
        } else {
            fun permissionCall() {
                launcher.launchPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    if (it) {
                        completer.complete(file)
                    } else {
                        completer.complete(null)
                    }
                }
            }

            @RequiresApi(Build.VERSION_CODES.R)
            fun scopeCall(intent: Intent) {
                launcher.launchScope(intent) {
                    if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Environment.isExternalStorageManager()
                        } else {
                            false
                        }
                    ) {
                        completer.complete(file)
                    } else {
                        completer.complete(null)
                    }
                }
            }
            if (scopeStorageCheck()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = resolveAllFileAccessInfo(context)
                    if (intent != null) {
                        scopeCall(intent)
                    } else {
                        permissionCall()
                    }
                } else {
                    permissionCall()
                }
            } else {
                permissionCall()
            }
        }
    }
    return completer.await()
}

@RequiresApi(Build.VERSION_CODES.R)
private fun resolveAllFileAccessInfo(context: Context): Intent? {
    val packageManager = context.packageManager
    val intentWrap = try {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.addCategory("android.intent.category.DEFAULT")
        intent.data = Uri.parse("package:${context.packageName}")
        intent
    } catch (e: Exception) {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
        intent
    }
    val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.resolveActivity(
            intentWrap,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.resolveActivity(intentWrap, 0)?.let {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            intent
        }
    }
    return if (resolveInfo != null) intentWrap else null
}

private fun scopeStorageCheck(): Boolean {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q).let {
        if (it) {
            true
        } else {
            false
        }
    }
}


actual val LocalStorageLauncher = compositionLocalOf<StorageLauncher?>(structuralEqualityPolicy()) {
    null
}

actual class StorageLauncher(
    activity: ComponentActivity,
    private val lifecycle: Lifecycle,
    private val scope: CoroutineScope
) {
    private val scopeFlow = MutableStateFlow<ActivityResult?>(null)
    private val permissionFlow = MutableStateFlow<Boolean?>(null)

    private val scopeLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            scope.launch {
                scopeFlow.tryEmit(result)
            }
        }
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            scope.launch {
                permissionFlow.tryEmit(isGranted)
            }
        }

    fun launchScope(intent: Intent, onResult: (ActivityResult) -> Unit) {
        var job: Job? = null
        job = scopeFlow
            .filterNotNull()
            .onEach { result ->
                onResult(result)
                job?.cancel()
                job = null
            }
            .launchIn(scope)
        scopeLauncher.launch(intent)
        job?.invokeOnCompletion { throwable ->
            if (throwable != null) {
                println("Job was cancelled due to: $throwable")
            }
        }
    }

    fun launchPermission(permission: String, onResult: (Boolean) -> Unit) {
        var job: Job? = null
        job = permissionFlow
            .filterNotNull()
            .onEach { result ->
                onResult(result)
                job?.cancel()
                job = null
            }
            .launchIn(scope)
        permissionLauncher.launch(permission)
        job?.invokeOnCompletion { throwable ->
            if (throwable != null) {
                println("Job was cancelled due to: $throwable")
            }
        }
    }

}
