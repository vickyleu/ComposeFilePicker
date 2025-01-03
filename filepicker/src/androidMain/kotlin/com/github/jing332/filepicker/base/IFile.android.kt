package com.github.jing332.filepicker.base

import androidx.core.net.toUri
import coil3.Uri
import coil3.toCoilUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.Sink
import okio.Source
import okio.sink
import okio.source
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


actual typealias FileImpl = java.io.File

actual typealias Charset = java.nio.charset.Charset
actual typealias CharsetDecoderImpl = java.nio.charset.CharsetDecoder

actual fun byteArrayToStringWithEncoding(byteArray: ByteArray, charset: CharsetImpl): String {
    return String(byteArray, charset.charset) // 使用 Java 提供的 String(byte[], Charset) 构造函数
}


@Suppress("unused")
actual typealias StandardCharsetsImpl = java.nio.charset.StandardCharsets

@Suppress("unused")
actual object StandardCharsetsImplObj {
    actual val UTF_8: CharsetImpl = CharsetImpl(StandardCharsets.UTF_8)
    actual val US_ASCII: CharsetImpl = CharsetImpl(StandardCharsets.US_ASCII)
    actual val ISO_8859_1: CharsetImpl = CharsetImpl(StandardCharsets.ISO_8859_1)
    actual val UTF_16: CharsetImpl = CharsetImpl(StandardCharsets.UTF_16)
    actual val UTF_16BE: CharsetImpl = CharsetImpl(StandardCharsets.UTF_16BE)
    actual val UTF_16LE: CharsetImpl = CharsetImpl(StandardCharsets.UTF_16LE)
}

@Suppress("unused")
internal actual object CharsetImplObj {
    actual fun forName(charsetName: String): Charset = Charset.forName(charsetName)
}


actual typealias BufferedReaderImpl = java.io.BufferedReader
actual typealias FilterInputStreamImpl = java.io.FilterInputStream
actual typealias BufferedInputStreamImpl = java.io.BufferedInputStream
actual typealias ReaderImpl = java.io.Reader
@Suppress("unused")
actual typealias InputStreamReaderImpl = java.io.InputStreamReader
actual typealias InputStreamImpl = java.io.InputStream
actual typealias OutputStreamImpl = java.io.OutputStream
actual typealias FileInputStream = java.io.FileInputStream

//actual abstract class FileInputStream : java.io.FileInputStream{
//    actual constructor(file: FileImpl):super(file)
//
//    override fun read(): Int {
//        return file.inputStream().read()
//    }
//}

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
actual inline fun <T> InputStreamImpl.useImpl(block: (InputStreamImpl) -> T):T {
    return this.use(block)
}

@Suppress("unused")
actual inline fun <T>OutputStreamImpl.useImpl(block: (OutputStreamImpl) -> T):T{
   return this.use(block)
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
    private val writerJob = CoroutineScope(Dispatchers.IO).launch {
        try {
            if(writeChannel == null)return@launch
            if(writeChannel.iterator()==null)return@launch
            for (request in writeChannel) {
                if(request == null)return@launch
                try {
                    writeData(request.data, request.offset, request.length)
                }catch (e:Exception){
                }
            }
        }catch (e:Exception){
        }
    }
    private val scope = CoroutineScope(Dispatchers.IO + writerJob)
    private val mutex = kotlinx.coroutines.sync.Mutex()
    // 写入请求数据类
    private data class WriteRequest(val data: ByteArray, val offset: Long, val length: Int)

    private val writeChannel = Channel<WriteRequest>(capacity = Channel.UNLIMITED)

    // 实际执行写入操作
    private suspend fun writeData(data: ByteArray, offset: Long, length: Int) {
        // NSFileHandle
        if (isClosed) return
        if (data.size < length) return
        mutex.withLock {
            if (isClosed) return
            randomAccessFile.seek(offset)
            randomAccessFile.write(data, 0, length)
        }
    }
    // 添加写入请求
    actual suspend fun writeAtOffsetAsync(data: ByteArray, offset: Long, length: Int) {
        writeChannel.send(WriteRequest(data.copyOf(length), offset, length))
    }


    actual fun writeAtOffset(data: ByteArray, offset: Long, length: Int) {
        if(isClosed) return
        val dataCopy = data.copyOfRange(0, length)
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ensureActive()
                    if (isClosed) return@withContext
                    mutex.withLock {
                        ensureActive()
                        if (isClosed) return@withContext
                        randomAccessFile.seek(offset)
                        randomAccessFile.write(dataCopy, 0, length)
                    }
                }
            }catch (e:Exception){
            }
        }
    }

    actual fun readAtOffset(offset: Long, length: Int): ByteArray {
        if(isClosed) return ByteArray(0)
        randomAccessFile.seek(offset)
        val buffer = ByteArray(length)
        randomAccessFile.read(buffer)
        return buffer
    }

    actual fun getFileLength(): Long {
        return randomAccessFile.length()
    }

    actual fun close() {
        if (isClosed) return
        randomAccessFile.close()
        isClosed = true
        try {
            if (mutex.isLocked) {
                mutex.unlock()
            }
        }catch (e:Exception){
        }
        try {
            writerJob.cancel() // 取消所有正在执行的协程
        }catch (e:Exception){
        }
    }

    actual fun toFile(): FileImpl {
        return file
    }

    private var isClosed = false

    actual fun isClosed(): Boolean {
        return isClosed
    }

    fun syncInternal() {
        if (isClosed) return
        randomAccessFile.fd.sync()
    }

}

actual fun RandomAccessFileImpl.sync() {
    syncInternal()
}