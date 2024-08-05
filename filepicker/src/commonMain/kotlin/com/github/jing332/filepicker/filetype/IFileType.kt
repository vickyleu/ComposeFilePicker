package com.github.jing332.filepicker.filetype

import androidx.compose.runtime.Composable
import com.github.jing332.filepicker.model.IFileModel

abstract class IFileType {
    open val name: String = ""

    open fun verify(model: IFileModel, mimeType: String): Boolean = false
    fun verify(model: IFileModel): Boolean =
        verify(model, getMineType(model.name))

    @Composable
    open fun IconContent() {
    }

    fun String.fileExtContains(vararg names: String): Boolean {
        for (name in names) {
            if (this.endsWith(".$name", ignoreCase = true))
                return true
        }

        return false
    }
}

expect fun getMineType(name: String): String