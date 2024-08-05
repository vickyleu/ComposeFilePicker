package com.github.jing332.filepicker.filetype

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.music_cast_24px
import org.jetbrains.compose.resources.painterResource

object AudioType : IFileType() {
    override val name: String = "Audio"

    override fun verify(model: IFileModel, mimeType: String): Boolean {
        return mimeType.startsWith("audio")
    }

    @Composable
    override fun IconContent() {
        Icon(painter = painterResource(Res.drawable.music_cast_24px), contentDescription = name)
    }
}