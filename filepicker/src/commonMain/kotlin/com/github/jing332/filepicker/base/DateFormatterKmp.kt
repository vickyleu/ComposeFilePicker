package com.github.jing332.filepicker.base

expect class DateFormatterKmp( pattern: String) {

    fun format(time: Long): String
}