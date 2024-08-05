package com.github.jing332.filepicker.filetype

import java.net.URLConnection

actual fun getMineType(name: String): String {
   return URLConnection.getFileNameMap().getContentTypeFor(name) ?: ""
}