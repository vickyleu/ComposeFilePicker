package com.github.jing332.compose_filepicker

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.PlatformContext
import coil3.compose.LocalPlatformContext
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import com.github.jing332.compose_filepicker.ui.LocalToaster
import com.github.jing332.compose_filepicker.ui.showToast
import com.github.jing332.compose_filepicker.ui.theme.ComposefilepickerTheme
import com.github.jing332.filepicker.base.ByteArrayOutputStreamImpl
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.base.RandomAccessFileImpl
import com.github.jing332.filepicker.base.sync
import com.github.jing332.filepicker.utils.forceGC
import compose_filepicker.composeapp.generated.resources.Res
import io.ktor.client.HttpClient
import io.ktor.client.plugins.Charsets
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.head
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.cancel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.collections.Map
import kotlin.collections.copyOf
import kotlin.collections.find
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.sumOf
import kotlin.collections.toMap
import kotlin.collections.toMutableList
import kotlin.collections.toMutableMap
import kotlin.math.max
import kotlin.math.roundToInt

val LocalProgressiveLoadingDialog = compositionLocalOf(structuralEqualityPolicy()) {
    ProgressState()
}

open class ProgressState() {
    var isShow by mutableStateOf(false)
        private set
    var message by mutableStateOf("")
        private set

    private var callback by mutableStateOf({ it: Boolean -> })

    fun show(message: String) {
        reset()
        this.message = message
        this.isShow = true
    }

    fun showImmediately(message: String) {
        this.message = message
        this.isShow = true
    }

    private fun reset() {
        this.message = ""
        this.callback = {}
    }

    fun show(message: String, callback: (Boolean) -> Unit) {
        reset()
        this.message = message
        this.callback = callback
        this.isShow = true
    }

    fun dismiss(confirm: Boolean = true) {
        this.isShow = false
        callback.invoke(confirm)
        reset()
    }
}

@Composable
fun ComposeApp() {
    ComposefilepickerTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            if (false) {
                val toaster = rememberToasterState()
                val progressiveLoading by remember { mutableStateOf(ProgressState()) }
                val client = remember {
                    HttpClient().config {
                        install(UserAgent) {
                            this.agent = "compose ktor"
                        }
                        Charsets {
                            // Allow to use `UTF_8`.
                            register(io.ktor.utils.io.charsets.Charsets.UTF_8)
                            // Allow to use `ISO_8859_1` with quality 0.1.
                            register(io.ktor.utils.io.charsets.Charsets.ISO_8859_1, quality = 0.1f)
                            // Specify Charset to send request(if no charset in request headers).
                            sendCharset = io.ktor.utils.io.charsets.Charsets.UTF_8
                            // Specify Charset to receive response(if no charset in response headers).
                            responseCharsetFallback = io.ktor.utils.io.charsets.Charsets.UTF_8
                        }
                    }
                }
                val scope = rememberCoroutineScope()
                CompositionLocalProvider(LocalProgressiveLoadingDialog provides progressiveLoading) {
                    CompositionLocalProvider(LocalToaster provides toaster) {
                        Toaster(
                            toaster,
                            alignment = Alignment.Center
                        )

                        val launcher = LocalStorageLauncher.current!!
                        val context = LocalPlatformContext.current
                        LaunchedEffect(Unit) {
                            client.downloadFileImpl(context, scope, launcher = launcher,
                                url = "http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4",
                                toaster = toaster, progressive = progressiveLoading, callback = {

                                })
                        }
                        progressive(progressiveLoading)
                    }
                }
            } else {
                FilePickerScreen(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    onSelectFile = {
                        // 创建一个缓冲区
                        val buffer = ByteArray(1024)
                        // 读取字节数
                        var bytesRead: Int
                        val inputStream = it.inputStream()
                        // 读取整个流,将receiver保存到一个ByteArray中
                        val totalByteArray = ByteArrayOutputStreamImpl()
                        while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                            // 处理读取的数据
                            val receiver: ByteArray = buffer.copyOf(bytesRead)
                            totalByteArray.write(receiver)
                        }
                    }
                )
            }

        }
    }
}


@Composable
private fun progressive(progressiveLoading: ProgressState) {
    var showingProgressiveAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        snapshotFlow { progressiveLoading.isShow }
            .distinctUntilChanged()
            .collect {
                if (it) {
                    withContext(Dispatchers.IO) {
                        delay(300)
                        showingProgressiveAnimation = true
                    }
                }
            }
    }
    val zoomProgressiveAnim by animateFloatAsState(
        targetValue = if (showingProgressiveAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = EaseInOutCubic)
    )

    if (progressiveLoading.isShow) {
        Dialog(
            onDismissRequest = {
                showingProgressiveAnimation = false
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                with(LocalDensity.current) {
                    val value = progressiveLoading.message
                    // Draw a rectangle shape with rounded corners inside the dialog
                    Box(
                        Modifier
                            .scale(zoomProgressiveAnim)
                            .alpha(zoomProgressiveAnim)
                            .wrapContentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .background(
                                    Color(0xFF000000).copy(alpha = 0.68f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clip(
                                    RoundedCornerShape(6.dp)
                                )
                                .requiredSizeIn(
                                    minWidth = 126.dp,
                                    minHeight = 20.dp,
                                    maxWidth = 300.dp,
                                    maxHeight = 280.dp
                                )
                                .wrapContentSize()
                                .padding(horizontal = 21.dp, vertical = 15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .wrapContentSize(),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = value,
                                    fontSize = 16.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


internal fun Res.lottie(name: String): String {
    return resource("files/$name.json")
}

internal fun Res.resource(path: String): String {
    return "composeResources/compose_filepicker.composeapp.generated.resources/$path"
}


@Composable
private fun RadioButton(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(role = Role.RadioButton) {
            onCheckedChange(true)
        }) {
        androidx.compose.material3.RadioButton(selected = checked, onClick = null)
        Text(
            text = text, modifier = Modifier
                .padding(start = 4.dp)
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun CheckBox(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable(role = Role.Checkbox) {
            onCheckedChange(!checked)
        }) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(
            text = text, modifier = Modifier
                .padding(start = 4.dp)
                .padding(vertical = 8.dp)
        )
    }
}


@OptIn(ExperimentalCoroutinesApi::class)
fun HttpClient.downloadFileImpl(
    context: PlatformContext,
    scope: CoroutineScope,
    launcher: StorageLauncher,
    url: String, toaster: ToasterState, progressive: ProgressState, callback: (FileImpl) -> Unit
) {
    scope.launch {
        withContext(Dispatchers.IO) {
            progressive.show("正在下载文件")
            // 获取文件大小
            val (totalSize, fileName) = this@downloadFileImpl.head(url).let {
                println(
                    "文件大小:${
                        it.headers.entries().joinToString { "${it.key}:${it.value}" }
                    }"
                )
                // 文件大小
                val length = it.headers["Content-Length"]?.toLong() ?: return@let null
                // 文件名
                var fileName = it.headers["Content-Disposition"]?.let { disposition ->
                    disposition.split(";").find { it.contains("filename=") }
                        ?.substringAfter("filename=")
                        ?.trim('"')
                } ?: url.substringAfterLast("/")
                if (fileName.contains("-aliyun-")) {
                    if (fileName.split("-aliyun-").size == 2) {
                        val first = fileName.split("-aliyun-")[0]
                        val second = fileName.split("-aliyun-")[1]
                        if (first.contains(".") && second.contains(".")) {
                            val suffix = first.substringAfterLast(".")
                            val suffix2 = first.substringAfterLast(".")
                            if (suffix == suffix2) {//后缀完全一样,说明是阿里云的多余后缀
                                val index = fileName.lastIndexOf("-aliyun-")
                                fileName = fileName.substring(0, index)
                            }
                        }
                    }
                }
                println("文件大小:$length  文件名:$fileName")
                length to fileName
            }
                ?: return@withContext kotlin.run {
                    progressive.dismiss()
                    toaster.showToast("文件大小获取失败")
                }
            // 创建文件
            val createTempFile: FileImpl =
                createTempFile(context, launcher, key = url, name = fileName)
                    ?: return@withContext kotlin.run {
                        progressive.dismiss()
                        toaster.showToast("文件创建失败")
                    }
            if (createTempFile.exists()) {
                val fileLength = createTempFile.length()
                if (fileLength == totalSize) {
                    progressive.dismiss()
                    println("文件已存在:${createTempFile.getAbsolutePath()} ${createTempFile.length()}  ${totalSize}")
                    callback.invoke(createTempFile)
                    return@withContext
                }
                createTempFile.delete()
            }
            createTempFile.getParentFile()!!.mkdirs()
            createTempFile.createNewFile()
            var splitCount = 4
            // 每个线程下载的块大小
            var chunkSize = (totalSize.toDouble() / splitCount.toDouble()).toLong()
                .coerceAtLeast(1024 * 1024 * 3)
            if (chunkSize > totalSize) {
                chunkSize = totalSize
            }
            splitCount = totalSize.toDouble().div(chunkSize).roundToInt()
            println("文件大小:$totalSize  文件名:$fileName  分块大小:$chunkSize  分块数:$splitCount")
            // 开启4个线程下载
            val completer = CompletableDeferred<Unit>()
            val tasks = mutableListOf<Job>()
            val progressCount =
                kotlin.collections.List(splitCount) { 0 to 0L }.toMutableList().toMap()
                    .toMutableMap()
            val boundCheckRange =
                kotlin.collections.List(splitCount) { 0 to (0L to 0L) }.toMutableList().toMap()
                    .toMutableMap()
            scope.launch {
                withContext(Dispatchers.IO) {

                    val accessFile = RandomAccessFileImpl(createTempFile)
                    // 因为是分段下载,所有进度更新需要统计多个线程的进度
                    val previousChunkProgress = mutableMapOf<Int, Long>()


                    val ranges = (0 until splitCount).map { index ->
                        previousChunkProgress[index] = 0
                        val start = index * chunkSize
                        val end = if (index == splitCount - 1) {
                            totalSize - 1
                        } else {
                            (index + 1) * chunkSize - 1
                        }
                        boundCheckRange[index] = (start to end)
                        Triple(index, start, end)
                    }
                    val bufferedLock = Mutex() // 锁定以确保线程安全的缓存写入

                    val progressLock = Mutex() // 锁定以确保线程安全的进度更新

                    var maxProgress = 0
                    val downloadFlows = ranges.map { (index, start, end) ->
                        flow {
                            val downloadCompleter = this@downloadFileImpl.downloadFile(
                                url = url,
                                index = index,
                                start = start, end = end,
                                accessFile = accessFile, mutex = bufferedLock,
                                scope = scope,
                                rangeMap = boundCheckRange
                            ) { count -> // 0~100
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        progressLock.withLock {
                                            progressCount[index] = count
                                            maxProgress =
                                                max(maxProgress, ((progressCount.values.sumOf { it }
                                                    .toDouble() / totalSize.toDouble()) * 100).toInt())
                                            progressive.showImmediately("正在下载文件${maxProgress}%")
                                        }
                                    }
                                }
                            }
                            emit(index to downloadCompleter)
                        }.flowOn(Dispatchers.IO)
                    }
                    val completers = downloadFlows
                        .asFlow()
                        .flattenMerge()
                        .flowOn(Dispatchers.IO)
                        .mapNotNull { (index, downloadCompleter) ->
                            if (completer.isCompleted.not()) {
                                launch {
                                    withContext(Dispatchers.IO) {
                                        downloadCompleter.await()
                                        forceGC()
                                    }
                                }
                            } else {
                                downloadCompleter.cancel()
                                null
                            }
                        }
                        .map {
                            it
                        }.toList()
                    // 等待所有的Completers完成
                    completers.joinAll()
                    forceGC()
                    scope.launch {
                        bufferedLock.withLock {
                            accessFile.close()
                            completer.complete(kotlin.Unit)
                        }
                    }
                }
            }
            try {
                completer.await()
                forceGC()
                progressive.dismiss()
                callback.invoke(createTempFile)
            } catch (e: Exception) {
                progressive.dismiss()
                e.printStackTrace()
                tasks.forEach { it.cancel() }
                toaster.showToast("文件下载失败")
            }
        }
    }
}


internal suspend fun HttpResponse.use(block: suspend (HttpResponse) -> Unit): Unit {
    try {
        block(this@use)
    } finally {
        this.call.client.close()
    }
}

internal suspend fun HttpStatement.use(block: suspend (HttpResponse) -> Unit): Unit {
    this@use.execute {
        block(it)
//        it.call.client.close()
        it
    }
}


@OptIn(InternalAPI::class)
suspend fun HttpClient.downloadFile(
    url: String,
    index: Int,
    start: Long,
    end: Long,
    accessFile: RandomAccessFileImpl,
    mutex: Mutex, scope: CoroutineScope,
    rangeMap: Map<Int, Pair<Long, Long>>,
    onProgress: (count: Long) -> Unit
): CompletableDeferred<Unit> {
    val completer = CompletableDeferred<Unit>()
    scope.launch {
        withContext(Dispatchers.IO) {
            try {
                this@downloadFile.prepareGet {
                    url(url)
                    headers {
                        append(HttpHeaders.Range, "bytes=$start-$end")
                    }
                    onDownload { bytesSentTotal, contentLength ->
                        onProgress(bytesSentTotal)
                    }
                }.use { response ->
                    if (response.status.value == 206) {
                        val channel = response.bodyAsChannel()
                        var remainingBytes = (end - start) + 1
                        var bytesRead: Int
                        // 使用 start 作为文件的写入位置
                        var startPosition = start

                        val innerMutex = Mutex()
                        // 从 response.content 读取数据，并写入文件
                        while (remainingBytes > 0 && channel.isClosedForRead.not()) {
                            val buffer = ByteArray(8192)
                            bytesRead = channel.readAvailable(
                                dst = buffer,
                                offset = 0,
                                length = minOf(remainingBytes.toInt(), buffer.size)
                            )
                            if (bytesRead == -1) break // 读取完毕
                            // 确保写入正确的偏移量
                            if (bytesRead > 0) {
                                val sp = startPosition
                                var br = bytesRead
                                // 如果预计写入位置加上读取的字节数超过了end
                                if ((sp + br) > end + 1) {
                                    br = (end + 1 - sp).toInt()
                                }
                                if (sp < start || (sp + br) > (end + 1)) {
                                    break
                                }
                                if (br > 0) {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            mutex.withLock { //外层保证一个文件不会同时修改,按顺序写入,不要搞乱offset跳转
                                                innerMutex.withLock {//同一个线程内完成标识,只要没锁就是下载完成了
                                                    accessFile.writeAtOffset(
                                                        data = buffer,
                                                        offset = sp,
                                                        length = br
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                remainingBytes -= bytesRead
                                startPosition += bytesRead
                            } else {
                                break
                            }
                        }
                        channel.cancel()
//                        GC.collect()
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                // 循环查询5次,判断mutex每50ms是否是有锁的,有锁则等待500ms重新开始查询,无锁五次以后直接执行
                                waitingByMutex(innerMutex) {
                                    if (remainingBytes == 0L) {
                                        scope.launch {
                                            mutex.withLock {
                                                accessFile.sync()
                                            }
                                        }
                                        completer.complete(Unit)
                                    } else {
                                        completer.completeExceptionally(Exception("Download failed"))
                                    }
                                }
                            }
                        }
                    } else {
                        completer.completeExceptionally(Exception("Download failed"))
                    }
                }
            } catch (e: Exception) {
                accessFile.close()
                e.printStackTrace()
                completer.completeExceptionally(e)
            }
        }
    }
    return completer
}

private suspend fun waitingByMutex(mutex: Mutex, function: () -> Unit) {
    var lockAcquired = false
    repeat(5) { // 最多查询5次
        if (mutex.isLocked) { // 检查mutex是否被锁住
            delay(50) // 如果被锁住，则等待100ms
        } else {
            lockAcquired = true // 如果没有锁住，设置标志位
            return@repeat // 直接跳出循环
        }
    }
    if (!lockAcquired) {
        delay(500) // 如果5次检查都被锁住，则等待500ms再执行
        waitingByMutex(mutex, function)
    } else {
        function.invoke()
    }
}


