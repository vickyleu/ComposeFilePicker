package com.github.jing332.filepicker.filetype

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.text_fields_24px
import org.jetbrains.compose.resources.painterResource

object TextType : IFileType() {
    override val name: String = "Text"

    override fun verify(model: IFileModel, mimeType: String): Boolean {
        return mimeType.startsWith("text")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun IconContent() {
        Icon(painter = painterResource(Res.drawable.text_fields_24px),
            tint = { Color.Black },
            contentDescription = name)
    }

}