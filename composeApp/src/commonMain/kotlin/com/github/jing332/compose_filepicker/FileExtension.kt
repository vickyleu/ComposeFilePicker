package com.github.jing332.compose_filepicker

import androidx.compose.runtime.ProvidableCompositionLocal
import coil3.PlatformContext
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.base.isFile
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.ForwardingSource
import okio.IOException
import okio.Sink
import okio.Source
import okio.buffer
import okio.use

expect class StorageLauncher {

}

expect val LocalStorageLauncher: ProvidableCompositionLocal<StorageLauncher?>

// sink 写入下载到的新数据.. sink是一个文件, channel是下载的数据
fun ForwardingSink.writeChannel(channel: ByteReadChannel, start: Long, end: Long) {

}
expect suspend fun createTempFile(
    context: PlatformContext, launcher: StorageLauncher,
    key: String = "",
    name: String = "temp"
): FileImpl?


internal fun compareBeforeIsEqualOrLargeThanAfterVersion(
    before: String,
    after: String
): Boolean {
    val afterArray = after.split(".").map { it.toIntOrNull() ?: 0 }.toMutableList()
    val beforeArray = before.split(".").map { it.toIntOrNull() ?: 0 }.toMutableList()
    val maxArrayLength = maxOf(afterArray.size, beforeArray.size)
    if (maxArrayLength == 0) return false
    for (i in 0 until maxArrayLength - afterArray.size) {
        afterArray.add(0)
    }
    for (i in 0 until maxArrayLength - beforeArray.size) {
        beforeArray.add(0)
    }
    for (i in 0 until maxArrayLength) {
        val af = afterArray[i]
        val bf = beforeArray[i]
        if (bf > af) {
            return true
        } else if (bf < af) {
            return false
        }
    }
    return afterArray.joinToString(".") == beforeArray.joinToString(".")
}

class ProgressSource(
    delegate: Source,
    private val totalBytes: Long,
    private val onProgress: (bytesWritten: Long, totalBytes: Long) -> Unit
) : ForwardingSource(delegate) {
    private var bytesWritten = 0L

    override fun read(sink: Buffer, byteCount: Long): Long {
        bytesWritten += byteCount
        onProgress(bytesWritten, totalBytes)
        return byteCount
    }
}

open class ForwardingSink(
    /** [Sink] to which this instance is delegating. */
    private val delegate: Sink,
) : Sink {
    // TODO 'Sink by delegate' once https://youtrack.jetbrains.com/issue/KT-23935 is fixed.

    @Throws(IOException::class)
    override fun write(source: Buffer, byteCount: Long) = delegate.write(source, byteCount)

    @Throws(IOException::class)
    override fun flush() = delegate.flush()

    override fun timeout() = delegate.timeout()

    @Throws(IOException::class)
    override fun close() = delegate.close()
}

private const val OKIO_RECOMMENDED_BUFFER_SIZE: Int = 8192

@Suppress("NAME_SHADOWING")
suspend fun ByteReadChannel.readFully(sink: Sink) {
    val channel = this
    sink.buffer().use { sink ->
        while (!channel.isClosedForRead) {
            // TODO: Allocating a new packet on every copy isn't great. Find a faster way to move bytes.
            val packet = channel.readRemaining(OKIO_RECOMMENDED_BUFFER_SIZE.toLong())
            while (!packet.exhausted()) {
                sink.write(packet.readBytes())
            }
        }
    }
}

fun Source.toByteReadChannel(): ByteReadChannel {
    val channel = ByteChannel(autoFlush = true)
    GlobalScope.launch {
        withContext(Dispatchers.IO) {
            var bytesRead: Int
            val buffer = ByteArray(OKIO_RECOMMENDED_BUFFER_SIZE)
            val bufferedSource = buffer()
            while (bufferedSource.read(buffer).also { bytesRead = it } != -1) {
                channel.writeFully(buffer, startIndex = 0, endIndex = bytesRead)
            }
            channel.close(null)
        }
    }
    return channel
}

fun FileImpl.test(){
    this.isFile
}
