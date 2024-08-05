package com.github.jing332.filepicker.model

import coil3.PlatformContext
import com.github.jing332.filepicker.base.InputStreamImpl
import com.github.jing332.filepicker.base.OutputStreamImpl


/*actual class PlatformDocumentFileImpl actual constructor(
    private val context: PlatformContext,
    val file: DocumentFileImpl
) :
    IFileModel() {
    override val name: String
        get() = file.name ?: ""

    override val path: String
        get() = file.uri.toString()

    override val isDirectory: Boolean
        get() = file.isDirectory

    override val size: Long
        get() = file.length()

    override val time: Long
        get() = file.lastModified()

    override fun files(): List<IFileModel> {
        return file.listFiles().map { PlatformDocumentFileImpl(context, it) }
    }

    override fun inputStream(): InputStreamImpl {
        return context.contentResolver.openInputStream(file.uri)!!
    }

    override fun outputStream(): OutputStreamImpl {
        return context.contentResolver.openOutputStream(file.uri)!!
    }

    override fun createFile(name: String): IFileModel {
        val f = file.createFile("", name)
        return PlatformDocumentFileImpl(context, f!!)
    }

    override fun createDirectory(name: String): IFileModel {
        val f = file.createDirectory(name)
        return PlatformDocumentFileImpl(context, f!!)
    }
}

actual typealias DocumentFileImpl = DocumentFile*/
