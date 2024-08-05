package com.github.jing332.compose_filepicker.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.github.jing332.compose_filepicker.ComposeApp
import platform.UIKit.UIColor
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused")
@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    val controller = ComposeUIViewController() {
        ComposeApp()
    }.apply {
        this.overrideUserInterfaceStyle = UIUserInterfaceStyle.UIUserInterfaceStyleDark
        this.automaticallyAdjustsScrollViewInsets = false
        this.view.backgroundColor = UIColor.whiteColor
    }
    return controller
}
