package com.github.jing332.filepicker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.model.NormalFile
import kotlinx.coroutines.CoroutineScope

actual fun String.formatImpl(vararg args: Any): String {
    return this.format(*args)
}

actual fun navControllerSetup(navController: NavHostController) {
    navController.enableOnBackPressed(false)
}

actual fun getPlatform(): Platform {
    return Platform.ANDROID
}

@Composable
actual fun startPickerHandler(scope: CoroutineScope, callback: (NormalFile) -> Unit) {
}