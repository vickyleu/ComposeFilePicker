package com.github.jing332.filepicker.base

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual class DateFormatterKmp actual constructor(pattern: String) {
    private val formatter = NSDateFormatter().apply {
        this.dateFormat = pattern
    }

    actual fun format(time: Long): String {
        return formatter.stringFromDate(NSDate((time * 1000).toDouble()))
    }

}