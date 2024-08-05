package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.github.jing332.filepicker.model.NormalFile
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

actual fun String.formatImpl(vararg args: Any): String {
    // ios 中字符串格式化
    return when (args.size) {
        0 -> NSString.stringWithFormat(this)
        1 -> NSString.stringWithFormat(this, args[0])
        2 -> NSString.stringWithFormat(this, args[0], args[1])
        3 -> NSString.stringWithFormat(this, args[0], args[1], args[2])
        4 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3])
        5 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4])
        6 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5])
        7 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6])
        8 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7])
        9 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8])
        else -> error("Too many arguments.")
    }
}

actual fun navControllerSetup(navController: NavHostController) {
    //
}

actual fun getPlatform(): Platform {
    return Platform.IOS
}
@Composable
actual fun startPickerHandler(scope: CoroutineScope, callback: (NormalFile) -> Unit) {
    (UIApplication.sharedApplication.windows.first() as UIWindow).apply {
        val handler = DocumentPickerHandler(scope)
        rootViewController?.presentViewController(handler, true) {
            handler.pickDocument {
                println("DocumentPickerHandler: $it")
                callback.invoke(it)
            }
        }
    }
}