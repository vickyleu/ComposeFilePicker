package com.github.jing332.compose_filepicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.github.jing332.compose_filepicker.ui.theme.ComposefilepickerTheme
import com.github.jing332.filepicker.base.ByteArrayOutputStreamImpl

@Composable
fun ComposeApp() {
    ComposefilepickerTheme {
        Surface {
            FilePickerScreen(
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
                        println("bytesRead:${receiver.size}  ${it.name} ")
                    }
                    println("${it.name}  totalByteArray:${totalByteArray.toByteArray().size}")
                }
            )
        }
    }
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