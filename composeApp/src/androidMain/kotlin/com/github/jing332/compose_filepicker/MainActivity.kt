package com.github.jing332.compose_filepicker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.MainScope
import java.util.Locale

class MainActivity : ComponentActivity() {
    @Suppress("DEPRECATION", "unused")
    private fun setLocale(locale: Locale) {
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val impl = StoragePermissionUtil(this, lifecycle, MainScope())
        val launcher = StorageLauncher(this, lifecycle, MainScope())
        setContent {
            CompositionLocalProvider(LocalStoragePermission provides impl) {
                CompositionLocalProvider(LocalStorageLauncher provides launcher) {
                    ComposeApp()
                }
            }
        }
    }
}