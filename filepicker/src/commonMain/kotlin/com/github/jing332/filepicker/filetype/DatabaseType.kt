package com.github.jing332.filepicker.filetype

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.database_24px
import org.jetbrains.compose.resources.painterResource

object DatabaseType : IFileType() {
    override fun verify(model: IFileModel, mimeType: String): Boolean {
        return model.name.fileExtContains("db")
    }

    @Composable
    override fun IconContent() {
        Icon(painter = painterResource(Res.drawable.database_24px), contentDescription = "Database")
    }
}