package com.github.jing332.filepicker.model

import com.github.jing332.filepicker.base.InputStreamImpl
import com.github.jing332.filepicker.base.OutputStreamImpl


abstract class IFileModel {
    open val name: String = ""
    open val path: String = ""
    open val isDirectory: Boolean = false
    open val isLocalFile: Boolean = true
    open val fileCount: Int = 0
    open val time: Long = 0
    open val size: Long = 0

    open fun createDirectory(name: String): IFileModel = throw NotImplementedError()
    open fun createFile(name: String): IFileModel = throw NotImplementedError()
    open fun inputStream(): InputStreamImpl = throw NotImplementedError()
    open fun outputStream(): OutputStreamImpl = throw NotImplementedError()
    open fun files(): List<IFileModel> = emptyList()
    open fun fileCountWithFilter(fileFilter: com.github.jing332.filepicker.FileFilter):Int{
        var fileCount = 0
        files().forEach {
            if (fileFilter.accept(it)) {
                fileCount++
            }
        }
        return fileCount
    }
}
