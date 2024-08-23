@file:OptIn(ExperimentalForeignApi::class)

package com.github.jing332.compose_filepicker

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_MD5
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA1

actual fun ByteArray.md5(): ByteArray {
    val data = this
    return ByteArray(CC_MD5_DIGEST_LENGTH).also { bytes: ByteArray ->
        bytes.usePinned { digestPin ->
            data.usePinned { dataPin ->
                CC_MD5(
                    data = dataPin.addressOf(0),
                    len = data.size.toUInt(),
                    md = digestPin.addressOf(0).reinterpret()
                )
            }
        }
    }
}

fun ByteArray.sha1Digest(): ByteArray {
    val sha1Digest = ByteArray(CC_MD5_DIGEST_LENGTH)
    sha1Digest.usePinned { digestPin ->
        this.usePinned { dataPin ->
            CC_SHA1(dataPin.addressOf(0), this.size.toUInt(), digestPin.addressOf(0).reinterpret())
        }
    }
    return sha1Digest
}