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
    fun mark(readLimit: Int)
    fun reset()
    fun markSupported(): Boolean
}

expect open class FilterInputStreamImpl : InputStreamImpl {
    private constructor(`in`: InputStreamImpl)
    override fun read(): Int
}

expect fun byteArrayToStringWithEncoding(byteArray: ByteArray, charset: CharsetImpl): String

expect abstract class ReaderImpl{
    private constructor()
    private constructor(lock:Any?)
}

expect abstract class Charset {
    fun name(): String
    final override fun equals(other: Any?): Boolean
    final override fun toString(): String
    final override fun hashCode(): Int
}

internal expect object CharsetImplObj {
    fun forName(charsetName: String): Charset
}

expect abstract class CharsetDecoderImpl

class CharsetImpl internal constructor(val charset: Charset) {
    companion object {
        @Suppress("UNUSED")
        fun forName(charsetName: String): CharsetImpl {
            return CharsetImpl(CharsetImplObj.forName(charsetName))
        }
    }
}


expect class BufferedReaderImpl : ReaderImpl {
    constructor(`in`: ReaderImpl?, sz: Int)
    constructor(`in`: ReaderImpl?)

    fun readLine(): String?

    fun close()
}

expect class BufferedInputStreamImpl : FilterInputStreamImpl {
    constructor(`in`: InputStreamImpl)
    constructor(`in`: InputStreamImpl, size: Int)

    override fun read(): Int
}


expect  class FileInputStream: InputStreamImpl {
    constructor(file: FileImpl)

    override fun read(): Int
}


/**
 * Constant definitions for the standard {@link CharsetImpl Charsets}. These
 * charsets are guaranteed to be available on every implementation of the Kotlin
 * Multiplatform .
 *
 * @see <a href="Charset.html#standard">Standard Charsets</a>
 * @since 2.0.20
 */
@Suppress("unused")
expect final class StandardCharsetsImpl

@Suppress("unused")
expect object StandardCharsetsImplObj {
    val UTF_8: CharsetImpl
    val US_ASCII: CharsetImpl
    val ISO_8859_1: CharsetImpl
    val UTF_16: CharsetImpl
    val UTF_16BE: CharsetImpl
    val UTF_16LE: CharsetImpl
}


@Suppress("unused")
expect class InputStreamReaderImpl : ReaderImpl {
    constructor(`in`: InputStreamImpl)
    constructor(`in`: InputStreamImpl, charsetName: String)

    //    constructor(`in`: InputStreamImpl, cs: CharsetImpl)
    constructor(`in`: InputStreamImpl, dec: CharsetDecoderImpl)

    fun read(): Int
    fun ready(): Boolean
    fun getEncoding(): String
    fun read(cbuf: CharArray, offset: Int, length: Int): Int
    fun close()

}

@Suppress("UNUSED")
fun InputStreamReaderImpl(`in`: InputStreamImpl, cs: CharsetImpl): InputStreamReaderImpl {
    return InputStreamReaderImpl(`in`, cs.charset.name())
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

expect inline fun <T> InputStreamImpl.useImpl(block: (InputStreamImpl) -> T):T
expect inline fun <T> OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> T):T

expect class FileImpl {

    constructor(path: String)
    constructor(parent: String, child: String)
    constructor(parent: FileImpl, child: String)

//    fun isFile(): Boolean

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

@Suppress("UNUSED")
val FileImpl.isFile: Boolean
    get() = isDirectory.not()

@Suppress("UNUSED")
val FileImpl.isDirectory: Boolean
    get() = isDirectory()

@Suppress("UNUSED")
val FileImpl.exists: Boolean
    get() = exists()

@Suppress("UNUSED")
val FileImpl.absolutePath: String
    get() = getAbsolutePath()

@Suppress("UNUSED")
val FileImpl.name: String
    get() = getName()

@Suppress("UNUSED")
val FileImpl.parentFile: FileImpl?
    get() = getParentFile()

@Suppress("UNUSED")
val FileImpl.parent: String?
    get() = getParent()


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