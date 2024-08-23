package com.github.jing332.compose_filepicker

import java.security.MessageDigest

actual fun ByteArray.md5(): ByteArray {
    val md = MessageDigest.getInstance("MD5")
    return md.digest(this)
}