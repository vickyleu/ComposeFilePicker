package com.github.jing332.filepicker.filetype

import kotlinx.cinterop.memScoped
import platform.CoreFoundation.CFStringRef
import platform.CoreServices.UTTypeCopyPreferredTagWithClass
import platform.CoreServices.UTTypeCreatePreferredIdentifierForTag
import platform.CoreServices.kUTTagClassFilenameExtension
import platform.CoreServices.kUTTagClassMIMEType
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSString

//  iOS 中通过文件名获取文件类型
actual fun getMineType(name: String): String {
    val fileExtension = name.substringAfterLast('.', "")
    memScoped {
        val cf = CFBridgingRetain(fileExtension) as CFStringRef?
        try {
            val UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, cf, null)
                ?: return "application/octet-stream"
            val utiCf = CFBridgingRetain(UTI) as CFStringRef?
            try {
                val mimeType = UTTypeCopyPreferredTagWithClass(utiCf, kUTTagClassMIMEType)
                val mimeTypeCf = CFBridgingRetain(mimeType) as NSString?
                return mimeTypeCf?.toString() ?: "application/octet-stream"
            } finally {
                CFBridgingRelease(utiCf)
            }
        } finally {
            CFBridgingRelease(cf)
        }
    }
}