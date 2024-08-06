package com.github.jing332.filepicker.base

import coil3.Uri
import okio.Buffer
import okio.Sink
import okio.Source
import okio.Timeout


expect abstract class InputStreamImpl {
    abstract fun read(): Int
    fun read(b: ByteArray): Int
    fun read(b: ByteArray, off: Int, len: Int): Int
    fun skip(n: Long): Long
    fun available(): Int
    fun close()
}

expect fun InputStreamImpl.source(): Source


expect class ByteArrayOutputStreamImpl() : OutputStreamImpl {
    fun toByteArray(): ByteArray
    override fun write(b: Int)
}

@Suppress("UNUSED")
expect class FileSource(inputStream: InputStreamImpl) : Source{
    override fun close()
    override fun read(sink: Buffer, byteCount: Long): Long
    override fun timeout(): Timeout
}



@Suppress("UNUSED")
class FileSink(private val outputStream: OutputStreamImpl) : Sink {
    override fun write(source: okio.Buffer, byteCount: Long) {
        val byteArray = ByteArray(byteCount.toInt())
        source.readFully(byteArray)
        outputStream.write(byteArray)
    }

    override fun flush() {
        outputStream.flush()
    }

    override fun timeout(): Timeout = Timeout.NONE

    override fun close() {
        outputStream.close()
    }
}

expect abstract class OutputStreamImpl {
    abstract fun write(b: Int)
    fun write(b: ByteArray)
    fun write(b: ByteArray, off: Int, len: Int)
    fun flush()
    fun close()

}

expect inline fun InputStreamImpl.useImpl(block: (InputStreamImpl) -> Unit)
expect inline fun OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> Unit)

expect class FileImpl(path: String) {
    fun isDirectory(): Boolean
    fun list(): Array<String>?
    fun lastModified(): Long
    fun length(): Long
    fun listFiles(): Array<FileImpl>?
    fun mkdir(): Boolean
    fun createNewFile(): Boolean
    fun getAbsolutePath(): String
    fun getName(): String
}

expect fun FileImpl.uri(): Uri

expect fun FileImpl.isLocalFile(): Boolean
expect fun FileImpl.inputStream(): InputStreamImpl
expect fun FileImpl.outputStream(): OutputStreamImpl
expect fun FileImpl.resolve(relative: String): FileImpl