package com.github.jing332.filepicker.model

import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.base.inputStream
import com.github.jing332.filepicker.base.isLocalFile
import com.github.jing332.filepicker.base.outputStream
import com.github.jing332.filepicker.base.resolve


data class NormalFile(
    private val file: FileImpl,
) : IFileModel() {
    override val name: String
        get() = file.getName()
    override val path: String
        get() = file.getAbsolutePath()
    override val isDirectory: Boolean
        get() = file.isDirectory()
    override val fileCount: Int
        get() = file.list()?.size ?: 0
    override val time: Long
        get() = file.lastModified()
    override val size: Long
        get() = file.length()

    override val isLocalFile: Boolean
        get() = file.isLocalFile()

    override fun files(): List<IFileModel> {
        return file.listFiles()?.map { NormalFile(it) } ?: emptyList()
    }

    override fun createDirectory(name: String): IFileModel {
        val f = file.resolve(name)
        f.mkdir()
        return NormalFile(f)
    }

    override fun createFile(name: String): IFileModel {
        val f = file.resolve(name)
        f.createNewFile()
        return NormalFile(f)
    }

    override fun inputStream() = file.inputStream()
    override fun outputStream() = file.outputStream()
}