package com.github.jing332.filepicker.base

import coil3.Uri


expect abstract class InputStreamImpl {
    abstract fun read(): Int
    fun read(b: ByteArray): Int
    fun read(b: ByteArray, off: Int, len: Int): Int
    fun skip(n: Long): Long
    fun available(): Int
    fun close()

}

expect class ByteArrayOutputStreamImpl(): OutputStreamImpl {
    fun toByteArray(): ByteArray
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

expect class FileImpl {
    constructor(path: String)

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

expect inline fun FileImpl.uri(): Uri

expect inline fun FileImpl.isLocalFile(): Boolean
expect inline fun FileImpl.inputStream(): InputStreamImpl
expect inline fun FileImpl.outputStream(): OutputStreamImpl
expect fun FileImpl.resolve(relative: String): FileImpl