package com.github.jing332.compose_filepicker.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

val LocalToaster = compositionLocalOf(structuralEqualityPolicy()) {
    ToasterState(
        coroutineScope = CoroutineScope(
            SupervisorJob()
        )
    )
}

fun ToasterState.showToast(msg: String?, type: ToastType = ToastType.Normal) {
    println("message::::$msg")
    val message = msg ?: return
    if (message.isEmpty()) return
    show(
        message = message,
        type = type//[ | Success | Info | Warning | Error],
    )
}