package com.github.jing332.filepicker

import android.widget.Toast
import coil3.PlatformContext

actual fun showToast(context: PlatformContext, msg: String) {
    Toast
        .makeText(context, msg, Toast.LENGTH_SHORT)
        .show()
}