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

expect abstract class ReaderImpl
expect abstract class CharsetImpl

expect fun CharsetImpl.forName(charsetName: String): CharsetImpl
@Suppress("unused")
expect final class StandardCharsetsImpl {
    companion object {
        val UTF_8: CharsetImpl
        val US_ASCII: CharsetImpl
        val ISO_8859_1: CharsetImpl
        val UTF_16: CharsetImpl
        val UTF_16BE: CharsetImpl
        val UTF_16LE: CharsetImpl
    }
}


@Suppress("unused")
expect class InputStreamReaderImpl : ReaderImpl {
    constructor(`in`: InputStreamImpl)
    constructor(`in`: InputStreamImpl, charsetName: String)
    constructor(`in`: InputStreamImpl, charset: CharsetImpl)

    fun read(): Int
    fun ready(): Boolean
    fun getEncoding(): String
    fun read(cbuf: CharArray, offset: Int, length: Int): Int
    fun close()

}


expect fun InputStreamImpl.source(): Source
expect fun FileImpl.sink(): Sink


expect class ByteArrayOutputStreamImpl() : OutputStreamImpl {
    fun toByteArray(): ByteArray
    override fun write(b: Int)
}

@Suppress("UNUSED")
expect class FileSource(inputStream: InputStreamImpl) : Source {
    override fun close()
    override fun read(sink: Buffer, byteCount: Long): Long
    override fun timeout(): Timeout
}


@Suppress("UNUSED")
class FileSink(private val outputStream: OutputStreamImpl) : Sink {
    override fun write(source: Buffer, byteCount: Long) {
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

expect class FileImpl {

    constructor(path: String)
    constructor(parent: String, child: String)
    constructor(parent: FileImpl, child: String)

    fun isDirectory(): Boolean
    fun list(): Array<String>?
    fun lastModified(): Long
    fun length(): Long
    fun listFiles(): Array<FileImpl>?
    fun mkdir(): Boolean
    fun createNewFile(): Boolean
    fun getAbsolutePath(): String
    fun getName(): String

    fun exists(): Boolean

    fun mkdirs(): Boolean

    fun delete(): Boolean

    fun getParentFile(): FileImpl?

    fun getParent(): String?

}


expect class RandomAccessFileImpl {
    constructor(filePath: String)
    constructor(file: FileImpl)
    constructor(file: FileImpl, mode: String)

    fun writeAtOffset(data: ByteArray, offset: Long, length: Int)
    fun readAtOffset(offset: Long, length: Int): ByteArray
    fun getFileLength(): Long
    fun close()

    fun toFile(): FileImpl
}

expect fun RandomAccessFileImpl.sync()


expect fun FileImpl.uri(): Uri

expect fun FileImpl.isLocalFile(): Boolean
expect fun FileImpl.inputStream(): InputStreamImpl
expect fun FileImpl.outputStream(): OutputStreamImpl
expect fun FileImpl.resolve(relative: String): FileImpl