package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIWindow

internal actual fun String.formatImpl(vararg args: Any): String {
    var returnString = ""
    val regEx = "%[\\d|.]*[sdf]|[%]".toRegex()
    val singleFormats = regEx.findAll(this).map {
        it.groupValues.first()
    }.asSequence().toList()
    val newStrings = this.split(regEx)
    for (i in 0 until args.count()) {
        val arg = args[i]
        returnString += when (arg) {
            is Double -> {
                NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Double)
            }

            is Int -> {
                NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Int)
            }

            else -> {
                NSString.stringWithFormat(newStrings[i] + "%@", args[i])
            }
        }
    }
    return returnString
}


@Composable
actual fun startPickerHandler(
    scope: CoroutineScope,
    callback: ((IFileModel) -> Unit),
) {
    (UIApplication.sharedApplication.windows.first() as UIWindow).apply {
        val handler = DocumentPickerHandler(scope)
//        handler.setModalPresentationStyle(UIModalPresentationFullScreen)
        rootViewController?.presentViewController(handler, true) {
            handler.pickDocument {
                println("DocumentPickerHandler: $it")
                callback.invoke(it)
            }
        }
    }
}