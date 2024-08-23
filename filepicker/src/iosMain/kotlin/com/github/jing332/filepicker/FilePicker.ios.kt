package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.github.jing332.filepicker.model.IFileModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import platform.Foundation.NSString
import platform.Foundation.stringWithFormat
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
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
    close: () -> Unit
) {
    val controller = remember { mutableStateOf<UIViewController?>(null) }
    LaunchedEffect(Unit) {
        (UIApplication.sharedApplication.windows.first() as UIWindow).apply {
            rootViewController?.apply {
                controller.value = this
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { controller.value }
            .distinctUntilChanged()
            .filterNotNull()
            .collect {
                val handler = DocumentPickerHandler(scope)
                handler.pickDocument(it) {
                    println("DocumentPickerHandler: $it")
                    close.invoke()
                    if (it != null) {
                        callback.invoke(it)
                    }
                    controller.value = null
                }
            }
    }

}