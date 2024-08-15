package com.github.jing332.compose_filepicker.ui

import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.ui.window.ComposeUIViewController
import com.github.jing332.compose_filepicker.ComposeApp
import kotlinx.cinterop.ExperimentalForeignApi
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
        ComposeApp()
    }.apply {
        this.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark
        this.automaticallyAdjustsScrollViewInsets = false
        this.view.backgroundColor = UIColor.whiteColor
    }
    return controller
}
