package com.github.jing332.filepicker.base

import androidx.core.net.toUri
import coil3.Uri
import coil3.toCoilUri
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import java.io.FileInputStream
import java.io.FileOutputStream


actual typealias FileImpl = java.io.File
actual typealias InputStreamImpl = java.io.InputStream
actual typealias OutputStreamImpl = java.io.OutputStream

@Suppress("unused")
actual fun InputStreamImpl.source(): Source {
    return this.source()
}

@Suppress("unused")
actual fun FileImpl.sink(): Sink {
    return this.sink(append = false)
}


actual class FileSource actual constructor(inputStream: InputStreamImpl) :
    Source by inputStream.source()

@Suppress("unused")
actual inline fun InputStreamImpl.useImpl(block: (InputStreamImpl) -> Unit) {
    this.use(block)
}

@Suppress("unused")
actual inline fun OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> Unit) {
    this.use(block)
}

actual fun FileImpl.resolve(relative: String): FileImpl {
    return this.resolve(relative)
}

actual fun FileImpl.inputStream(): InputStreamImpl {
    return FileInputStream(this)
}

actual fun FileImpl.outputStream(): OutputStreamImpl {
    return FileOutputStream(this)
}

@Suppress("unused")
actual fun FileImpl.uri(): Uri {
    return this.toUri().toCoilUri()
}

actual fun FileImpl.isLocalFile() = true

actual typealias ByteArrayOutputStreamImpl = java.io.ByteArrayOutputStream

@Suppress("unused")
actual class RandomAccessFileImpl {
    private val file: FileImpl
    private val randomAccessFile: java.io.RandomAccessFile

    actual constructor(filePath: String) {
        file = FileImpl(filePath)
        randomAccessFile = java.io.RandomAccessFile(filePath, "rw")
    }

    actual constructor(file: FileImpl) {
        this.file = file
        randomAccessFile = java.io.RandomAccessFile(file, "rw")
    }

    actual constructor(file: FileImpl, mode: String) {
        this.file = file
        randomAccessFile = java.io.RandomAccessFile(file, mode)
    }

    actual fun writeAtOffset(data: ByteArray, offset: Long, length: Int) {
        randomAccessFile.seek(offset)
        randomAccessFile.write(data, 0, length)
    }

    actual fun readAtOffset(offset: Long, length: Int): ByteArray {
        randomAccessFile.seek(offset)
        val buffer = ByteArray(length)
        randomAccessFile.read(buffer)
        return buffer
    }

    actual fun getFileLength(): Long {
        return randomAccessFile.length()
    }

    actual fun close() {
        if(isClosed)return
        randomAccessFile.close()
        isClosed=true
    }

    actual fun toFile(): FileImpl {
        return file
    }

    private var isClosed = false

    fun syncInternal() {
        if(isClosed)return
        randomAccessFile.fd.sync()
    }

}

actual fun RandomAccessFileImpl.sync() {
    syncInternal()
}