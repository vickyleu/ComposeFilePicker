package com.github.jing332.filepicker.listpage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

 class HapticFeedbackHandler {
    //  iOS 中触感反馈
    private val feedbackGenerator =
        UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)

    init {
        feedbackGenerator.prepare()
    }

     fun performHaptic() {
        feedbackGenerator.impactOccurred()
    }
}

@Composable
 fun getHapticFeedbackHandler(): HapticFeedbackHandler {
    return remember { HapticFeedbackHandler() }
}