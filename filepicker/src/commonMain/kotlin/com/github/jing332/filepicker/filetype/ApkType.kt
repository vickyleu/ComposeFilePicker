package com.github.jing332.filepicker.filetype

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel
import compose_filepicker.filepicker.generated.resources.Res
import compose_filepicker.filepicker.generated.resources.apk_install_24px
import org.jetbrains.compose.resources.painterResource

object ApkType : IFileType() {
    override fun verify(model: IFileModel, mimeType: String): Boolean {
        return model.name.endsWith(".apk")
    }

    @Composable
    override fun IconContent() {
        Icon(painter = painterResource(Res.drawable.apk_install_24px), contentDescription = "Apk")
    }
}