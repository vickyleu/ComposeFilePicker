package com.github.jing332.filepicker.listpage

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

actual class HapticFeedbackHandler(private val view: View) {
    actual fun performHaptic() {
        view.isHapticFeedbackEnabled = true
        view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
    }
}

@Composable
actual fun getHapticFeedbackHandler(): HapticFeedbackHandler {
    val view = LocalView.current
    return remember {
        HapticFeedbackHandler(view)
    }
}