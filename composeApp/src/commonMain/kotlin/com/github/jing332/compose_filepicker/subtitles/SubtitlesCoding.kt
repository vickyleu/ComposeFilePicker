package com.github.jing332.compose_filepicker.subtitles

import com.github.jing332.filepicker.base.BufferedInputStreamImpl
import com.github.jing332.filepicker.base.BufferedReaderImpl
import com.github.jing332.filepicker.base.CharsetImpl
import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.base.FileInputStream
import com.github.jing332.filepicker.base.InputStreamReaderImpl
import com.github.jing332.filepicker.base.StandardCharsetsImplObj
import com.github.jing332.filepicker.base.byteArrayToStringWithEncoding
import com.github.jing332.filepicker.base.inputStream
import com.github.jing332.filepicker.base.isFile
import com.github.jing332.filepicker.base.sink
import com.github.jing332.filepicker.base.source
import com.github.jing332.filepicker.base.useImpl
import kotlinx.io.IOException
import okio.Path.Companion.toPath
import okio.buffer
import okio.use


/**
 * @description 用于解析字幕
 */
/**
 * @description 用于解析字幕
 */
/*
object SubtitlesCoding {
    private const val ONE_SECOND = 1000
    private const val ONE_MINUTE = 60 * ONE_SECOND
    private const val ONE_HOUR = 60 * ONE_MINUTE

    private val timePattern = Regex("""\d{2}:\d{2}:\d{2},\d{3} --> \d{2}:\d{2}:\d{2},\d{3}""")

    */
/**
     * 读取本地文件
     *
     * @param path 文件路径
     * @return 解析后的字幕列表
     *//*

    fun readFile(subtitlesFile: FileImpl): List<SubtitlesModel> {
        val list = mutableListOf<SubtitlesModel>()
        if (!subtitlesFile.exists() || subtitlesFile.isDirectory()) {
            println("File does not exist or is not a file")
            return list
        }
        val inputStream =  subtitlesFile.inputStream()
            try {
                val charset = detectCharset(subtitlesFile)
                // 转成okio的Source
                val source: Source = inputStream.source()
                val bufferedSource = source.buffer()
                try {
//                    InputStreamReader
                    bufferedSource.readUtf8Line()?.let { totalLine ->
                        totalLine.split("\n").forEach { line ->
                            timePattern.find(line)?.let {
                                val times = it.value.split(" --> ")
                                if (times.size == 2) {
                                    val startTime = getTime(times[0])
                                    val endTime = getTime(times[1])
                                    val contextC = bufferedSource.readUtf8Line() ?: ""
                                    val contextE = bufferedSource.readUtf8Line() ?: ""
                                    list.add(SubtitlesModel(startTime, endTime, contextC, contextE))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }catch (e: IOException) {
                e.printStackTrace()
            }finally {
                inputStream.close()
            }
        return list
    }

    private fun getTime(timeString: String): Int {
        val parts = timeString.split(":")
        return parts[0].toInt() * ONE_HOUR +
                parts[1].toInt() * ONE_MINUTE +
                parts[2].substringBefore(',').toInt() * ONE_SECOND +
                parts[2].substringAfter(',').toInt()
    }

    // 用于检测文件编码
    // 用于检测文件编码
    private fun detectCharset(file: FileImpl): String { // 返回编码类型
        file.inputStream().useImpl { inputStream ->
            val source: Source = inputStream.source()
            val bufferedSource = source.buffer()
            val bom = ByteArray(4)
            // 读取BOM
            bufferedSource.readFully(bom)
            return when {
                bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() -> "UTF-16LE"
                bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte() -> "UTF-16BE"
                bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte() -> "UTF-8"
                else -> "UTF-8" // 默认假设为UTF-8
            }
        }
    }

    inline fun <T> InputStreamImpl.useImpl(block: (InputStreamImpl) -> T):T{
        try {
           return block(this)
        } finally {
            close()
        }
    }
}
*/

object Log{
    fun e(tag: String, msg: String){
        println("$tag: $msg")
    }
}

/**
 * @description 用于解析字幕
 */
object SubtitlesCoding {
    /**
     * 一秒=1000毫秒
     */
    private const val oneSecond = 1000
    private const val oneMinute = 60 * oneSecond
    private const val oneHour = 60 * oneMinute

    /**
     * 正则表达式，判断是否是时间的格式
     */
    private val equalStringExpress = Regex("""\d{2}:\d{2}:\d{2},\d{3} --> \d{2}:\d{2}:\d{2},\d{3}""")

    /*// 简单的编码检测方法，实际项目中可使用第三方库，如 juniversalchardet
    @Throws(IOException::class)
    private fun detectCharset1(file: FileImpl): CharsetImpl {
        BufferedInputStreamImpl(FileInputStream(file)).useImpl { bis ->
            val bom = ByteArray(4)
            bis.mark(4)
            bis.read(bom, 0, 4)
            bis.reset()
            return when {
                bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() -> StandardCharsetsImplObj.UTF_16LE // UTF-16LE BOM
                bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte() -> StandardCharsetsImplObj.UTF_16BE // UTF-16BE BOM
                bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte() -> StandardCharsetsImplObj.UTF_8 // UTF-8 BOM
                else -> StandardCharsetsImplObj.UTF_8 // 默认假设是UTF-8
            }
        }
    }*/

    // 用于检测文件编码
    private fun detectCharset(file: FileImpl): CharsetImpl { // 返回编码类型
        // 读取文件的前4个字节来检测BOM
        file.inputStream().source().buffer().use { bufferedSource ->
            val bom: ByteArray = bufferedSource.readByteArray(4)  // 读取前4字节
//            bufferedSource.seek(0) // 回到文件开头，确保后续读取从头开始
            return when {
                // UTF-16LE BOM
                bom.size >= 2 && bom[0] == 0xFF.toByte() && bom[1] == 0xFE.toByte() -> {
                    StandardCharsetsImplObj.UTF_16LE
                }
                // UTF-16BE BOM
                bom.size >= 2 && bom[0] == 0xFE.toByte() && bom[1] == 0xFF.toByte() -> {
                    StandardCharsetsImplObj.UTF_16BE
                }
                // UTF-8 BOM
                bom.size >= 3 && bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte() -> {
                    StandardCharsetsImplObj.UTF_8
                }
                // 默认使用 UTF-8
                else -> StandardCharsetsImplObj.UTF_8
            }
        }
    }

    /**
     * 读取本地文件
     *
     * @param path
     */
    /*fun readFile(path: String): List<SubtitlesModel> {
        val list = mutableListOf<SubtitlesModel>()
        val subtitlesFile = FileImpl(path)
        var inReader: BufferedReaderImpl? = null

        if (!subtitlesFile.exists() || !subtitlesFile.isFile) {
            Log.e("vicky:", "open file fail")
            return list
        }

        try {
            // 检测文件编码
            val detectedCharset = detectCharset(subtitlesFile)
            val inputStream = FileInputStream(subtitlesFile)
            inReader = BufferedReaderImpl(InputStreamReaderImpl(inputStream, detectedCharset))

            var line: String?
            while (inReader.readLine().also { line = it } != null) {
                val timeArr = mutableListOf<String>()
                val matcher = equalStringExpress.findAll(line!!)
                for (matchResult in matcher) {
                    // `groupValues` 包含正则表达式匹配的结果
                    timeArr.add(matchResult.groupValues[0])
                }
                // 通常有两个，开始时间 结束时间
                if (timeArr.size == 2) {
                    val sm = SubtitlesModel()
                    // 填充开始时间数据
                    sm.star = getTime(timeArr[0])
                    // 填充结束时间数据
                    sm.end = getTime(timeArr[1])
                    // 填充中文数据
                    sm.contextC = inReader.readLine()
                    // 填充英文数据
                    sm.contextE = inReader.readLine()
                    // 当前字幕的节点位置
                    sm.node = list.size + 1
                    list.add(sm)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inReader?.close()
        }

        return list
    }*/


    // 读取 SRT 文件内容并解析
    fun readSrtFile(filePath: String): List<SubtitlesModel> {
        val list = mutableListOf<SubtitlesModel>()
        val subtitlesFile = FileImpl(filePath)
        if (!subtitlesFile.exists() || !subtitlesFile.isFile) {
            Log.e("vicky:", "open file fail")
            return list
        }
        // 检测文件编码
        val charset = detectCharset(subtitlesFile)
        // 使用检测到的编码读取文件内容
        subtitlesFile.inputStream().source().buffer().use { bufferedSource ->
            // 读取字节数组
            val byteArray = bufferedSource.readByteArray()
            // 使用 NSData 和 NSString 处理编码
            val fileContent = byteArrayToStringWithEncoding(byteArray, charset)
            val lines = fileContent.split("\n")
            var index = 0
            while (index < lines.size) {
                val timeArr = mutableListOf<String>()

                // 跳过空行
                while (index < lines.size && lines[index].trim().isEmpty()) {
                    index++
                }

                // 检测时间行
                if (index < lines.size) {
                    val line = lines[index].trim()
                    if (equalStringExpress.matches(line)) {
                        timeArr.addAll(line.split(" --> "))
                        if (timeArr.size == 2) {
                            val sm = SubtitlesModel()
                            // 填充开始时间
                            sm.star = getTime(timeArr[0])
                            // 填充结束时间
                            sm.end = getTime(timeArr[1])
                            // 填充字幕内容 (中文和英文)
                            sm.contextC = if (index + 1 < lines.size) lines[index + 1].trim() else ""
                            sm.contextE = if (index + 2 < lines.size) lines[index + 2].trim() else ""
                            sm.node = list.size + 1
                            list.add(sm)

                            index += 3 // 跳过当前的时间行和两行字幕
                        }
                    }
                }
            }
        }
        return list
    }

    /**
     * @param line
     * @return 字幕所在的时间节点
     * @description 将String类型的时间转换成int的时间类型
     */
    private fun getTime(line: String): Int {
        return try {
            (line.substring(0, 2).toInt() * oneHour // 时
                    + line.substring(3, 5).toInt() * oneMinute // 分
                    + line.substring(6, 8).toInt() * oneSecond // 秒
                    + line.substring(9, line.length).toInt()) // 毫秒
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            -1
        }
    }
}
