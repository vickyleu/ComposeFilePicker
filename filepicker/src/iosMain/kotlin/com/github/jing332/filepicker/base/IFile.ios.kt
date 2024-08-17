package com.github.jing332.filepicker.base

import coil3.Uri
import coil3.pathSegments
import coil3.toUri
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.BooleanVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import okio.IOException
import okio.Sink
import okio.Source
import okio.Timeout
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSFileHandle
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
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.getBytes
import platform.Foundation.inputStreamWithFileAtPath
import platform.Foundation.outputStreamToFileAtPath
import platform.Foundation.readDataOfLength
import platform.Foundation.synchronizeFile
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSInteger
import platform.darwin.NSUInteger


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

@Suppress("unused")
actual fun FileImpl.sink(): Sink {
    return FileSink(this.outputStream())
}

@Suppress("unused")
actual fun InputStreamImpl.source(): Source {
    return FileSource(this)
}

actual class FileSource actual constructor(private val inputStream: InputStreamImpl) :
    Source {
    @Throws(IOException::class)
    actual override fun read(sink: okio.Buffer, byteCount: Long): Long {
        val byteArray = ByteArray(byteCount.toInt())
        val bytesRead = inputStream.read(byteArray)
        return if (bytesRead == -1) {
            -1
        } else {
            sink.write(byteArray, 0, bytesRead)
            bytesRead.toLong()
        }
    }

    actual override fun timeout(): Timeout = Timeout.NONE

    @Throws(IOException::class)
    actual override fun close() {
        inputStream.close()
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
        memScoped {
            val uByteArray = UByteArray(1024)
            uByteArray.usePinned {
                val bytesRead =
                    inputStream.read(it.addressOf(0), uByteArray.size.toULong())
                return (if (bytesRead > 0) {
                    bytesRead.toInt()
                } else -1).apply {
                    if (this <= 0) {
                        isEnded = true
                    }
                }
            }


        }
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        memScoped {
            println("file: ${file.getAbsolutePath()}  ${file.length()} ${file.lastModified()} ${file.getName()}  len:$len off:$off")
            try {
                if (len - off <= 0) {
                    return -1
                }
                // 创建一个 UByteArray
                val ubyteBuffer = UByteArray(len)
                // 获取指向 UByteArray 的指针
                // 要获取稳定的C指针,必须在pin固定后操作,否则会导致指针失效
                ubyteBuffer.usePinned { pinnedArray ->
                    val ubuffer = pinnedArray.addressOf(0).reinterpret<UByteVar>()
                    println("pinnedArray:ptr:${ubuffer}")
                    val bytesRead: NSInteger = inputStream.read(ubuffer, len.toULong())
                    return bytesRead.toInt().apply {
                        if (this <= 0) {
                            isEnded = true
                        } else {
                            // 将读取到的内容复制回 ByteArray
                            for (i in 0 until bytesRead.toInt()) {
                                b[off + i] = ubyteBuffer[i].toByte()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return -1
            }
        }
    }

    override fun read(b: ByteArray): Int {
        val skip = skipBuffer.toInt()
        skipBuffer = 0
        println("file: ${file.getAbsolutePath()}  ${file.length()} ${file.lastModified()} ${file.getName()}")
        println("skip: $skip b.size${b.size}")
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
        skipBuffer = 0
        inputStream.close()
    }
}

@OptIn(BetaInteropApi::class)
@Suppress("unused")
fun ByteArray.toNSData(): NSData = memScoped {
    if (this@toNSData.isEmpty()) {
        NSData()
    } else {
        this@toNSData.usePinned {
            val size = this@toNSData.size.toULong().convert<NSUInteger>()
            NSData.create(bytes = it.addressOf(0), length = size)
        }
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
            if (len - off <= 0) {
                return
            }
            try {
                // 创建一个 UByteArray
                val ubyteBuffer = UByteArray(len - off)
                // 将 ByteArray 的内容复制到 UByteArray 中
                for (i in ubyteBuffer.indices) {
                    ubyteBuffer[i] = b[off + i].toUByte()
                }
                // 获取指向 UByteArray 的固定指针,这样可以保证指针不会失效
                ubyteBuffer.usePinned { pinnedArray ->
                    // 获取指向 UByteArray 的指针
                    val ubuffer = pinnedArray.addressOf(0).reinterpret<UByteVar>()
                    println("pinnedArray:ptr:${ubuffer}")
                    val bytesWrite: NSInteger = outputStream.write(ubuffer, (len - off).toULong())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

@Suppress("unused")
actual inline fun InputStreamImpl.useImpl(block: (InputStreamImpl) -> Unit) {
    this.use(block)
}

@Suppress("unused")
actual inline fun OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> Unit) {
    this.use(block)
}

@OptIn(ExperimentalForeignApi::class)
actual class FileImpl {
    private lateinit var path: String

    private val fileManager = NSFileManager.defaultManager
    actual fun exists(): Boolean {
        return fileManager.fileExistsAtPath(filePath, isDirectory = null)
    }

    actual fun delete(): Boolean {
        return fileManager.removeItemAtPath(filePath, error = null)
    }

    actual constructor(path: String) {
        this.path = path
    }

    actual constructor(parent: String, child: String) : this(path = "$parent/$child") {
//        this.path = "$parent/$child"
        println("this.path::${this.path}")
    }

    actual constructor(parent: FileImpl, child: String) : this(
        parent = parent.getAbsolutePath(),
        child = child
    ) {
//        this.path = "${parent.getAbsolutePath()}/$child"
        println("this.path::${this.path}")
    }

    actual fun getParentFile(): FileImpl? {
        val parentPath = getParent()
        return parentPath?.let { FileImpl(it) }
    }

    actual fun getParent(): String? {
        val parentPath = filePath.removeSuffix(getName()).ifEmpty { null }
        return parentPath
    }


    private val filePath: String by lazy {
        if (::path.isInitialized.not()) throw RuntimeException("path is not initialized")
        path
    }

    actual fun isDirectory(): Boolean {
        val isDirectory = nativeHeap.alloc<BooleanVar>()
        val exists = fileManager.fileExistsAtPath(filePath, isDirectory = isDirectory.ptr)
        return exists && isDirectory.value
    }

    actual fun list(): Array<String>? {
        return fileManager.contentsOfDirectoryAtPath(filePath, error = null)?.let {
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
        return fileManager.attributesOfItemAtPath(filePath, error = null)?.let {
            (it[NSFileModificationDate] as? NSDate)?.timeIntervalSince1970?.toLong() ?: 0L
        } ?: 0L
    }

    actual fun length(): Long {
        val attr = fileManager.attributesOfItemAtPath(filePath, error = null)
        return attr?.let {
            (it[NSFileSize] as? NSNumber)?.longValue ?: 0L
        } ?: 0L
    }

    actual fun listFiles(): Array<FileImpl>? {
        return list()?.map {
            FileImpl(path = "$filePath/$it")
        }?.toTypedArray()
    }

    actual fun mkdirs(): Boolean {
        return fileManager.createDirectoryAtPath(
            filePath,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
    }

    actual fun mkdir(): Boolean {
        return fileManager.createDirectoryAtPath(
            filePath,
            withIntermediateDirectories = false,
            attributes = null,
            error = null
        )
    }

    actual fun createNewFile(): Boolean {
        return fileManager.createFileAtPath(
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

actual fun FileImpl.isLocalFile(): Boolean {
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
            buffer.usePinned { pinnedArray ->
                val ptr = pinnedArray.addressOf(0)
                nsDate.getBytes(ptr, NSMakeRange(0u, nsDate.length))
                val byteArray = buffer.copyOf(nsDate.length.toInt())
                return byteArray
            }
        }
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        memScoped {
            b.usePinned { pinnedArray ->
                val ptr = pinnedArray.addressOf(off)
                nsDate.appendBytes(ptr, len.toULong())
            }
//            val pointer = b.refTo(0).getPointer(this) + off
        }
    }

    actual override fun write(b: Int) {
    }
}


@OptIn(BetaInteropApi::class)
@Suppress("unused")
actual class RandomAccessFileImpl {

    private val file: FileImpl
    private lateinit var fileReadingHandle: NSFileHandle
    private lateinit var fileWritingHandle: NSFileHandle

    actual constructor(filePath: String) {
        file = FileImpl(filePath)
        fileReadingHandle = NSFileHandle.fileHandleForReadingAtPath(filePath)!!
        fileWritingHandle = NSFileHandle.fileHandleForWritingAtPath(filePath)!!
    }

    actual constructor(file: FileImpl) {
        this.file = file
        fileReadingHandle = NSFileHandle.fileHandleForReadingAtPath(file.getAbsolutePath())!!
        fileWritingHandle = NSFileHandle.fileHandleForWritingAtPath(file.getAbsolutePath())!!
    }

    actual constructor(file: FileImpl, mode: String) {
        this.file = file
        when (mode) {
            "r" -> fileReadingHandle =
                NSFileHandle.fileHandleForReadingAtPath(file.getAbsolutePath())!!

            "rw" -> {
                fileReadingHandle =
                    NSFileHandle.fileHandleForReadingAtPath(file.getAbsolutePath())!!
                fileWritingHandle =
                    NSFileHandle.fileHandleForWritingAtPath(file.getAbsolutePath())!!
            }

            "w" -> fileWritingHandle =
                NSFileHandle.fileHandleForWritingAtPath(file.getAbsolutePath())!!

            "rws" -> {
                fileReadingHandle =
                    NSFileHandle.fileHandleForReadingAtPath(file.getAbsolutePath())!!
                fileWritingHandle =
                    NSFileHandle.fileHandleForWritingAtPath(file.getAbsolutePath())!!
            }

            else -> Unit
        }
    }


    actual fun writeAtOffset(data: ByteArray, offset: Long, length: Int) {
        if (::fileWritingHandle.isInitialized.not()) return
        if(data.size < length){
           return
        }
        fileWritingHandle.seekToOffset(offset.toULong(), null)
        memScoped {
            data.usePinned {
                val ptr = it.addressOf(0)
                fileWritingHandle.writeData(NSData.create(ptr, length.toULong()), null)

            }
//            val buffer = data.refTo(0).getPointer(this)
//            fileWritingHandle.writeData(NSData.create(buffer, length.toULong()), null)
        }
    }

    actual fun readAtOffset(offset: Long, length: Int): ByteArray {
        if (::fileReadingHandle.isInitialized.not()) return ByteArray(0)
        fileReadingHandle.seekToOffset(offset.toULong(), null)
        fileReadingHandle.readDataOfLength(length.toULong()).let {
            val data = it
            if(data.length.toInt()<=0){
                return ByteArray(0)
            }
            memScoped {
                val buffer = ByteArray(it.length.toInt())
                buffer.usePinned {
                    val ptr = it.addressOf(0)
                    data.getBytes(ptr, NSMakeRange(0u, data.length))
                    return buffer
                }
            }
        }
    }

    actual fun getFileLength(): Long {
        return file.length()
    }

    actual fun close() {
        if (::fileReadingHandle.isInitialized) {
            fileReadingHandle.closeFile()
        }
        if (::fileWritingHandle.isInitialized) {
            fileWritingHandle.synchronizeFile()
            fileWritingHandle.closeFile()
        }
    }

    actual fun toFile(): FileImpl {
        return file
    }

}