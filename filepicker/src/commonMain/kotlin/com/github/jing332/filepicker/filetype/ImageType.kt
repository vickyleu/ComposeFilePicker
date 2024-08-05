package com.github.jing332.filepicker.filetype

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.monochrome_photos_24px
import org.jetbrains.compose.resources.painterResource

object ImageType : IFileType() {
    override fun verify(model: IFileModel, mimeType: String): Boolean {
        return mimeType.startsWith("image")
    }

    @Composable
    override fun IconContent() {
        Icon(
            painter = painterResource(Res.drawable.monochrome_photos_24px),
            contentDescription = "Image"
        )
    }
}