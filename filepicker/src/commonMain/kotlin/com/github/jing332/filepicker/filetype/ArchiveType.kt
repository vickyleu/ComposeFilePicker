package com.github.jing332.filepicker.filetype

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.folder_zip_24px
import org.jetbrains.compose.resources.painterResource

object ArchiveType : IFileType() {
    override fun verify(model: IFileModel, mimeType: String): Boolean {
        return model.name.fileExtContains("zip", "rar", "7z", "tar", "gz", "bz2", "xz")
    }

    @Composable
    override fun IconContent() {
        Icon(
            painter = painterResource(Res.drawable.folder_zip_24px),
            contentDescription = "Archive"
        )
    }
}