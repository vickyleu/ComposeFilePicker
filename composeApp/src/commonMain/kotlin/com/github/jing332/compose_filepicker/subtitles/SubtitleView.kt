package org.uooc.compose.ui.view.subtitles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.jing332.compose_filepicker.subtitles.SubtitlesModel

@Composable
fun SubtitleView(
    subtitles: SnapshotStateList<SubtitlesModel>,
    currentTime: State<Int>, // 当前播放时间，毫秒
    languageType: State<Int>, // 语言类型
    fontSize: State<TextUnit> = mutableStateOf(13.sp)
) {
    val subtitleModel = remember { mutableStateOf<SubtitlesModel?>(null) }

    // 更新字幕
    LaunchedEffect(currentTime.value) {
        subtitleModel.value = searchSub(subtitles, currentTime.value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 中文字幕显示
        if (languageType.value == LANGUAGE_TYPE_CHINA || languageType.value == LANGUAGE_TYPE_BOTH) {
            Text(
                text = subtitleModel.value?.contextC ?: "无中文字幕", // 避免空字幕
                fontSize = fontSize.value,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        // 英文字幕显示
        if (languageType.value == LANGUAGE_TYPE_ENGLISH || languageType.value == LANGUAGE_TYPE_BOTH) {
            Text(
                text = subtitleModel.value?.contextE ?: "No English subtitle", // 避免空字幕
                fontSize = fontSize.value
            )
        }
    }
}

// 二分查找字幕函数
private fun searchSub(list: List<SubtitlesModel>, key: Int): SubtitlesModel? {
    var start = 0
    var end = list.size - 1

    while (start <= end) {
        val middle = (start + end) / 2

        // 如果 key 在 middle 之前
        if (key < list[middle].star) {
            end = middle - 1
        }
        // 如果 key 在 middle 之后
        else if (key > list[middle].end) {
            start = middle + 1
        }
        // 如果 key 在 middle 范围内
        else {
            return list[middle]
        }
    }
    return null // 未找到
}


// 定义语言类型常量
const val LANGUAGE_TYPE_CHINA = 0
const val LANGUAGE_TYPE_ENGLISH = 1
const val LANGUAGE_TYPE_BOTH = 2
const val LANGUAGE_TYPE_NONE = 3
