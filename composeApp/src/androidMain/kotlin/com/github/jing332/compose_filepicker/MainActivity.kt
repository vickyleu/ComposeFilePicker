package com.github.jing332.compose_filepicker

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        setContent {
            ComposeApp()
        }
    }
}