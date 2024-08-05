package com.github.jing332.filepicker.listpage

expect class DateFormatterKmp( pattern: String) {

    fun format(time: Long): String
}
