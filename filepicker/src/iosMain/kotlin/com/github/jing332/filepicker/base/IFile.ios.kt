package com.github.jing332.filepicker.base

import coil3.Uri
import coil3.pathSegments
import coil3.toUri
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSInputStream
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.NSNumber
import platform.Foundation.NSOutputStream
import platform.Foundation.NSURL
import platform.Foundation.NSURLUbiquitousItemIsDownloadedKey
import platform.Foundation.appendBytes
import platform.Foundation.getBytes
import platform.Foundation.inputStreamWithFileAtPath
import platform.Foundation.outputStreamToFileAtPath
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSInteger
import platform.posix.uint8_tVar


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
    return this.toUri()
}

abstract interface StreamImpl {
    fun close()
}


//ios 实现
@OptIn(ExperimentalForeignApi::class)
actual abstract class InputStreamImpl : StreamImpl {
    actual abstract fun read(): Int

    actual open fun read(b: ByteArray): Int {
        return read(b, 0, b.size)
    }

    actual open fun read(b: ByteArray, off: Int, len: Int): Int = 0

    actual open fun skip(n: Long): Long = 0

    actual open fun available(): Int = 0

    actual override fun close() {
    }
}


actual abstract class OutputStreamImpl : StreamImpl {
    actual abstract fun write(b: Int)
    actual open fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    actual open fun write(b: ByteArray, off: Int, len: Int) {
    }

    actual open fun flush() {
    }

    actual override fun close() {
    }

}

@OptIn(ExperimentalForeignApi::class)
class FileInputStream(private val file: FileImpl) : InputStreamImpl() {
    private val inputStream = NSInputStream.inputStreamWithFileAtPath(file.getAbsolutePath())!!

    init {
        inputStream.open()
    }

    private var isEnded = false

    private var skipBuffer = 0L

    override fun read(): Int {
        val buffer = nativeHeap.alloc<uint8_tVar>()
        println("file: ${file.getAbsolutePath()}  ${file.length()} ${file.lastModified()} ${file.getName()}")
        val bytesRead = inputStream.read(buffer.ptr, 1024u)
        try {
            println("bytesRead: $bytesRead")
            return (if (bytesRead > 0) {
                buffer.value.toInt()
            } else -1).apply {
                if (this <= 0) {
                    isEnded = true
                }
            }
        } finally {
            nativeHeap.free(buffer.ptr)
        }
    }

    @Suppress("unchecked_cast")
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        memScoped {
            val buffer = b.refTo(off).getPointer(this) as CPointer<uint8_tVar>
            val bytesRead: NSInteger = inputStream.read(buffer, len.toULong())
            return bytesRead.toInt().apply {
                if (this <= 0) {
                    isEnded = true
                }
            }
        }
    }

    override fun read(b: ByteArray): Int {
        val skip = skipBuffer.toInt()
        skipBuffer = 0
        return read(b, skip, b.size).apply {
            if (this <= 0) {
                isEnded = true
            }
        }
    }

    override fun skip(n: Long): Long {
        this.skipBuffer += n
        return this.skipBuffer
    }

    override fun available(): Int {
        return if (isEnded.not() && inputStream.hasBytesAvailable) {
            read()
        } else 0
    }

    override fun close() {
        inputStream.close()
    }
}

@OptIn(ExperimentalForeignApi::class)
class FileOutputStream(file: FileImpl) : OutputStreamImpl() {
    private val outputStream = NSOutputStream.outputStreamToFileAtPath(file.getAbsolutePath(), true)

    init {
        outputStream.open()
    }

    override fun write(b: Int) {
        val buffer = byteArrayOf(b.toByte())
        write(buffer, 0, 1)
    }

    override fun write(b: ByteArray) {
        write(b, 0, b.size)
    }


    override fun write(b: ByteArray, off: Int, len: Int) {
        if (outputStream.hasSpaceAvailable.not()) return
        memScoped {
            val buffer = b.refTo(off).getPointer(this) as CPointer<ByteVar>
            outputStream.write(buffer.reinterpret(), len.toULong())
        }
    }

    override fun flush() {
        // NSOutputStream does not have a direct flush method
        // Ensure the stream is open and write data accordingly
    }

    override fun close() {
        outputStream.close()
    }
}


inline fun <T : StreamImpl> T.use(block: (T) -> Unit) {
    try {
        block(this)
    } finally {
        this.close()
    }
}

actual inline fun InputStreamImpl.useImpl(block: (InputStreamImpl) -> Unit) {
    this.use(block)
}

actual inline fun OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> Unit) {
    this.use(block)
}

@OptIn(ExperimentalForeignApi::class)
actual class FileImpl actual constructor(path: String) {
    private val filePath: String = path
    actual fun isDirectory(): Boolean {
        val isDirectory = nativeHeap.alloc<BooleanVar>()
        val exists =
            NSFileManager.defaultManager.fileExistsAtPath(filePath, isDirectory = isDirectory.ptr)
        return exists && isDirectory.value
    }

    actual fun list(): Array<String>? {
        return NSFileManager.defaultManager.contentsOfDirectoryAtPath(filePath, error = null)?.let {
            val list = mutableListOf<String>()
            for (i in 0 until it.count()) {
                val str = (it.get(i) as? String)
                if (str != null) {
                    if (str == ".DS_Store") continue
                    list.add(str)
                }
            }
            return list.toTypedArray()
        }
    }


    actual fun lastModified(): Long {
        return NSFileManager.defaultManager.attributesOfItemAtPath(filePath, error = null)?.let {
            (it[NSFileModificationDate] as? NSDate)?.timeIntervalSince1970?.toLong() ?: 0L
        } ?: 0L
    }

    actual fun length(): Long {

        println(
            "filePath:::$filePath  ${
                NSFileManager.defaultManager.fileExistsAtPath(
                    filePath,
                    isDirectory = null
                )
            }"
        )
        val attr = NSFileManager.defaultManager.attributesOfItemAtPath(filePath, error = null)
        println("attr:::$attr")
        return attr?.let {
            (it[NSFileSize] as? NSNumber)?.longValue ?: 0L
        } ?: 0L
    }

    actual fun listFiles(): Array<FileImpl>? {
        return list()?.map {
            FileImpl(path = "$filePath/$it")
        }?.toTypedArray()
    }

    actual fun mkdir(): Boolean {
        return NSFileManager.defaultManager.createDirectoryAtPath(
            filePath,
            withIntermediateDirectories = false,
            attributes = null,
            error = null
        )
    }

    actual fun createNewFile(): Boolean {
        return NSFileManager.defaultManager.createFileAtPath(
            filePath,
            contents = null,
            attributes = null
        )
    }

    actual fun getAbsolutePath(): String {
        return filePath
    }

    actual fun getName(): String {
        return toUri().pathSegments.last()
    }

    private fun Uri.toNativeUri(): NSURL {
        return NSURL(string = this.path!!)
    }

    private fun NSURL.toCoilUri(): Uri {
        return this.absoluteString?.toUri()!!
    }

    fun toUri(): Uri {
        return NSURL(fileURLWithPath = filePath).toCoilUri()
    }
}

actual inline fun FileImpl.isLocalFile(): Boolean {
    val values = NSURL(fileURLWithPath = this.getAbsolutePath()).resourceValuesForKeys(
        listOf(NSURLUbiquitousItemIsDownloadedKey), null
    )
    val isDownloaded = values?.get(NSURLUbiquitousItemIsDownloadedKey) as? Boolean ?: false
    return isDownloaded
}

actual class ByteArrayOutputStreamImpl actual constructor() : OutputStreamImpl() {
    private val nsDate = NSMutableData()
    actual fun toByteArray(): ByteArray {
        val buffer = ByteArray(nsDate.length.toInt())
        memScoped {
            nsDate.getBytes(buffer.refTo(0).getPointer(this), NSMakeRange(0u, nsDate.length))
            val byteArray = buffer.copyOf(nsDate.length.toInt())
            return byteArray
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        memScoped {
            val pointer = b.refTo(0).getPointer(this) + off
            nsDate.appendBytes(pointer, len.toULong())
        }
    }

    actual override fun write(b: Int) {
    }
}