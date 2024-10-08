package com.github.jing332.filepicker

import com.github.jing332.filepicker.base.FileImpl
import com.github.jing332.filepicker.model.NormalFile
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFStringGetCString
import platform.CoreFoundation.CFStringGetLength
import platform.CoreFoundation.CFStringGetMaximumSizeForEncoding
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreServices.kUTTypeAudio
import platform.CoreServices.kUTTypePDF
import platform.CoreServices.kUTTypeRTF
import platform.CoreServices.kUTTypeVideo
import platform.Foundation.NSError
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSThread
import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerMode
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController
import platform.darwin.NSObject


val compressionTypes = listOf(
    "public.zip-archive",       // zip
    "com.rarlab.rar-archive",   // rar
    "org.7-zip.7-zip-archive",  // 7z
//    "com.apple.binhex-archive", // binhex
    "public.tar-archive",       // tar
    "org.gnu.gnu-zip-archive",  // gzip
    "org.gnu.gnu-tar-archive",  // tar.gz
//    "com.apple.macbinary-archive" // macbinary
)

val designAndCadFileTypes = listOf(
    "com.adobe.photoshop-image",       // Photoshop (.psd)
    "com.bohemiancoding.sketch.drawing", // Sketch (.sketch)
    "com.autodesk.dwg",                // AutoCAD (.dwg)
    "com.adobe.illustrator.ai-image",  // Illustrator (.ai)
    "com.adobe.pdf",                   // PDF (.pdf)
    "com.corel.coreldraw",             // CorelDRAW (.cdr)
    "com.adobe.indesign-document",     // InDesign (.indd)
    "public.eps",                      // EPS (.eps)
    "org.khronos.collada.digital-asset" // COLLADA (.dae)
)

@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
@ExportObjCClass
class DocumentPickerHandler(private val scope: CoroutineScope) :NSObject(), UIDocumentPickerDelegateProtocol {
    private lateinit var callback: ((NormalFile?) -> Unit)

    private fun CFStringRef.toKString(): String {
        val length = CFStringGetLength(this)
        val maxSize = CFStringGetMaximumSizeForEncoding(length, kCFStringEncodingUTF8)
        val buffer = ByteArray(maxSize.toInt())
        return memScoped {
            buffer.usePinned {
                val ptr = it.addressOf(0)
                if (CFStringGetCString(this@toKString, ptr, maxSize, kCFStringEncodingUTF8)) {
                    buffer.toKString()
                } else {
                    throw IllegalArgumentException("Failed to convert CFString to String")
                }
            }
        }
    }


    fun pickDocument(controller: UIViewController,callback: (NormalFile?) -> Unit) {
        this.callback = callback
        val documentTypes = listOf(
            kUTTypePDF,
            kUTTypeRTF,
            "public.plain-text",
//            kUTTypePlainText,
            kUTTypeVideo,
            kUTTypeAudio,
            "com.microsoft.word.doc", /* doc */
            "org.openxmlformats.wordprocessingml.document", /* docx */
            "com.microsoft.powerpoint.ppt", /* ppt */
            "org.openxmlformats.presentationml.presentation", /* pptx */
            "com.microsoft.excel.xls", /* xls */
            "org.openxmlformats.spreadsheetml.sheet",
            /* xlsx */
//            kUTTypeData, // For doc, docx, ppt, pptx, xls, xlsx
            *compressionTypes.toTypedArray(), // For compressed files like zip
            *designAndCadFileTypes.toTypedArray(), // For design files like ps, ai, sketch, dwg, pdf, cdr, indd, eps, dae
        ).mapNotNull {
            @Suppress("UNCHECKED_CAST")
            ((it as? CFStringRef)?.toKString()) ?: it?.toString()
        }
        val documentPicker = UIDocumentPickerViewController(
            documentTypes = documentTypes, inMode = UIDocumentPickerMode.UIDocumentPickerModeImport
        )
        documentPicker.delegate = this
        documentPicker.setModalPresentationStyle(UIModalPresentationFullScreen)
        controller.presentModalViewController(documentPicker, animated = true)
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController, didPickDocumentAtURL: NSURL
    ) {
        val url = didPickDocumentAtURL
        var newUrl = url
        // Create file URL to temporary folder
        var tempURL = NSURL(fileURLWithPath = NSTemporaryDirectory())
        // Append filename (name+extension) to URL
        tempURL = tempURL.URLByAppendingPathComponent(url.lastPathComponent!!)!!
        try {
            // If file with same name exists remove it (replace file with new one)
            if (NSFileManager.defaultManager.fileExistsAtPath(tempURL.path!!)) {
                NSFileManager.defaultManager.removeItemAtPath(tempURL.path!!, error = null)
            }
            // Move file from app_id-Inbox to tmp/filename
            NSFileManager.defaultManager.moveItemAtPath(
                url.path!!, toPath = tempURL.path!!, error = null
            )

            newUrl = tempURL
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val file = NormalFile(FileImpl(newUrl.path!!))

        scope.launch {
            withContext(Dispatchers.Main) {
                //关掉当前controller
                controller.dismissViewControllerAnimated(true, null)
                callback.invoke(file)
            }
            /*withContext(Dispatchers.IO) {
                if (file.isLocalFile.not()) {
                    println("不是本地文件, 开始下载")
                    file.downloadFile()
                } else {
                    println("是本地文件")

                }
            }*/
        }
    }

    override fun documentPicker(
        controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>
    ) {
        val url = (didPickDocumentsAtURLs.firstOrNull() as? NSURL) ?: return run {
            controller.dismissViewControllerAnimated(true, null)
        }
        var newUrl = url
        // Create file URL to temporary folder
        var tempURL = NSURL(fileURLWithPath = NSTemporaryDirectory())
        // Apend filename (name+extension) to URL
        tempURL = tempURL.URLByAppendingPathComponent(url.lastPathComponent!!)!!
        try {
            // If file with same name exists remove it (replace file with new one)
            if (NSFileManager.defaultManager.fileExistsAtPath(tempURL.path!!)) {
                NSFileManager.defaultManager.removeItemAtPath(tempURL.path!!, error = null)
            }
            // Move file from app_id-Inbox to tmp/filename
            NSFileManager.defaultManager.moveItemAtPath(
                url.path!!, toPath = tempURL.path!!, error = null
            )
            newUrl = tempURL
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val file = NormalFile(FileImpl(newUrl.path!!))
        // CloudKit Container ID
        /*val containerIdentifier = "iCloud.compose"
        val urlUbiquity =
            NSFileManager.defaultManager.URLForUbiquityContainerIdentifier(containerIdentifier)
        if (urlUbiquity != null) {
            NSFileManager.defaultManager.contentsOfDirectoryAtPath(urlUbiquity.path!!, error = null)
                ?.apply {
                    println("iCloud: $this")
                    val list = mutableListOf<String>()
                    for (i in 0 until this.count()) {
                        val str = (this[i] as? String)
                        if (str != null) {
                            if (str == ".DS_Store") continue
                            list.add(str)
                        }
                    }
                    list.toTypedArray().forEach {
                        NSFileManager.defaultManager.contentsOfDirectoryAtPath(
                            "$urlUbiquity/$it",
                            error = null
                        )?.apply {
                            println("iCloud: $this")
                            val list = mutableListOf<String>()
                            for (i in 0 until this.count()) {
                                val str = (this[i] as? String)
                                if (str != null) {
                                    if (str == ".DS_Store") continue
                                    list.add(str)
                                }
                            }
                            list.toTypedArray().forEach {
                                NSFileManager.defaultManager.contentsOfDirectoryAtPath(
                                    "$urlUbiquity/$it/$it",
                                    error = null
                                )?.apply {
                                    println("iCloud: $this")
                                }
                            }
                        }
                    }
                }
        }*/
        scope.launch {
            withContext(Dispatchers.Main) {
                println("是本地文件")
                //关掉当前controller
                controller.dismissViewControllerAnimated(true, null)
                callback.invoke(file)
            }
            /* withContext(Dispatchers.IO) {
                 if (file.isLocalFile.not()) {
                     println("不是本地文件, 开始下载")
                     file.downloadFile()
                 } else {

                 }
             }*/
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        //关掉当前controller
        controller.dismissViewControllerAnimated(true, null)
        callback.invoke(null)
    }

    private suspend fun NormalFile.downloadFile() {
        val url = NSURL(fileURLWithPath = this.path)
        try {
            url.startAccessingSecurityScopedResource() //TODO fucking idiot apis, always return false
            val localURL = downloadICloudFileIfNeeded()
            if (localURL != null && NSFileManager.defaultManager.fileExistsAtPath(localURL.path!!)) {
                println("文件下载完成: ${localURL.path}")
            } else {
                println("文件下载失败")
            }
        } finally {
            url.stopAccessingSecurityScopedResource()
        }

    }

    private suspend fun NormalFile.downloadICloudFileIfNeeded(): NSURL? {
        val coordinator = NSFileCoordinator()
        val error = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
        try {
            val originUrl = NSURL(fileURLWithPath = this.path)
            if (NSFileManager.defaultManager.fileExistsAtPath(this.path, isDirectory = null)) {
                println("文件已经存在磁盘上了")
                withContext(Dispatchers.Main) {
                    this@DocumentPickerHandler.callback.invoke(this@downloadICloudFileIfNeeded)
                }
                return originUrl
            } else {
                println("文件未在磁盘上")
                var isDownloading = true
                coordinator.coordinateReadingItemAtURL(
                    originUrl, options = 0u, error = error.ptr
                ) { url ->
                    if (url != null) {
                        /**
                         * NSDictionary *attributes = [NSFileManager.defaultManager attributesOfItemAtPath:newURL.path error:nil];
                         *     if(NSOrderedSame == [attributes.fileModificationDate compare:self.fileModificationDate]) {
                         *       return; // no content change
                         *     }
                         */
                        val fileManager = NSFileManager.defaultManager
                        val exists = fileManager.fileExistsAtPath(url.path!!, isDirectory = null)
                        if (exists) {
                            isDownloading = false
                        } else {
                            println("开始下载文件 $url")
                            val errorDownload = nativeHeap.alloc<ObjCObjectVar<NSError?>>()
                            fileManager.startDownloadingUbiquitousItemAtURL(url, errorDownload.ptr)
                            if (errorDownload.value != null) {
                                println("下载文件失败 ${errorDownload.value?.localizedDescription}")
                                isDownloading = false
                                this@DocumentPickerHandler.callback.invoke(null)
                                return@coordinateReadingItemAtURL
                            }
                        }
                    }
                }
                if (error.value != null) {
                    println("下载文件失败 ${error.value?.localizedDescription}")
                    isDownloading = false
                    this@DocumentPickerHandler.callback.invoke(null)
                    return null
                }
                // 等待文件下载完成
                while (this.isLocalFile.not() && isDownloading) {
                    NSThread.sleepForTimeInterval(0.5)
                    println("等待文件下载完成 ${this.path}")
                }
                if (this.isLocalFile) {
                    println("文件下载完成 ${this.path}")
                    withContext(Dispatchers.Main) {
                        this@DocumentPickerHandler.callback.invoke(this@downloadICloudFileIfNeeded)
                    }
                    return originUrl
                } else {
                    return null
                }
            }
        } finally {
            nativeHeap.free(error.ptr)
        }
    }


}
