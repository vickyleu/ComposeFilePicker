package com.github.jing332.filepicker.model


abstract class IFileModel {
    open val name: String = ""
    open val path: String = ""
    open val isDirectory: Boolean = false
    open val fileCount: Int = 0
    open val time: Long = 0
    open val size: Long = 0

    open fun createDirectory(name: String): IFileModel = throw NotImplementedError()
    open fun createFile(name: String): IFileModel = throw NotImplementedError()
    open fun inputStream(): InputStream = throw NotImplementedError()
    open fun outputStream(): OutputStream = throw NotImplementedError()

    open fun files(): List<IFileModel> = emptyList()
}

expect class InputStream {
    fun read(): Int
    fun read(b: ByteArray): Int
    fun read(b: ByteArray, off: Int, len: Int): Int
    fun skip(n: Long): Long
    fun available(): Int
    fun close()
    fun use(block: (OutputStream) -> Unit)
}
expect class OutputStream {
    fun write(b: Int)
    fun write(b: ByteArray)
    fun write(b: ByteArray, off: Int, len: Int)
    fun flush()
    fun close()
    fun use(block: (OutputStream) -> Unit)
}