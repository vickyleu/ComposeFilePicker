package com.github.jing332.filepicker.base

import androidx.core.net.toUri
import coil3.Uri
import coil3.toCoilUri
import java.io.FileInputStream
import java.io.FileOutputStream


actual typealias FileImpl = java.io.File
actual typealias InputStreamImpl = java.io.InputStream
actual typealias OutputStreamImpl = java.io.OutputStream

actual inline fun InputStreamImpl.useImpl(block: (InputStreamImpl) -> Unit){
    this.use(block)
}
actual inline fun OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> Unit){
    this.use(block)
}

actual fun FileImpl.resolve(relative: String): FileImpl {
    return this.resolve(relative)
}

actual inline fun FileImpl.inputStream(): InputStreamImpl {
    return FileInputStream(this)
}

actual inline fun FileImpl.outputStream(): OutputStreamImpl {
    return FileOutputStream(this)
}

actual inline fun FileImpl.uri(): Uri {
    return this.toUri().toCoilUri()
}

actual inline fun FileImpl.isLocalFile()=true

actual typealias ByteArrayOutputStreamImpl = java.io.ByteArrayOutputStream