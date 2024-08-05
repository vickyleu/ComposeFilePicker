package com.github.jing332.filepicker.listpage

import java.util.Date
import java.util.Locale

actual class DateFormatterKmp actual constructor(pattern: String) {
    private val formatter = java.text.SimpleDateFormat(pattern, Locale.getDefault())
    actual fun format(time: Long): String {
       return formatter.format(Date(time))
    }

}