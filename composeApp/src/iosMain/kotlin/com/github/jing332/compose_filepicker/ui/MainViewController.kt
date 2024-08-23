package com.github.jing332.compose_filepicker.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.LocalPlatformContext
import com.github.jing332.compose_filepicker.ComposeApp
import com.github.jing332.compose_filepicker.LocalStorageLauncher
import com.github.jing332.compose_filepicker.LocalStoragePermission
import com.github.jing332.compose_filepicker.StorageLauncher
import com.github.jing332.compose_filepicker.StoragePermissionUtil
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.MainScope
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeApi::class)
fun MainViewController(): UIViewController {
    val controller = ComposeUIViewController(configure = {
        this.opaque = false
        this.platformLayers = true
    }) {
        val lifecycle = LocalLifecycleOwner.current
        val impl =
            StoragePermissionUtil(LocalPlatformContext.current, lifecycle.lifecycle, MainScope())
        val launcher = StorageLauncher()
        CompositionLocalProvider(LocalStoragePermission provides impl) {
            CompositionLocalProvider(LocalStorageLauncher provides launcher) {
                ComposeApp()
            }
        }
    }.apply {
        this.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark
        this.automaticallyAdjustsScrollViewInsets = false
        this.view.backgroundColor = UIColor.whiteColor
    }
    return controller
}
