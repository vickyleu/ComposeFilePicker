package com.github.jing332.filepicker.filetype

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringGetCStringPtr
import platform.CoreFoundation.CFStringRef
import platform.CoreServices.UTTypeCopyPreferredTagWithClass
import platform.CoreServices.UTTypeCreatePreferredIdentifierForTag
import platform.CoreServices.kUTTagClassFilenameExtension
import platform.CoreServices.kUTTagClassMIMEType
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSString
import platform.Foundation.create

//  iOS 中通过文件名获取文件类型
@OptIn(BetaInteropApi::class)
@Suppress("UNCHECKED_CAST")
actual fun getMineType(name: String): String {
    val fileExtension = name.substringAfterLast('.', "")
    memScoped {
        val cfExtension = CFBridgingRetain(NSString.create(string=fileExtension)) as CFStringRef?
        var UTI:CFStringRef?=null
        try {
            UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, cfExtension, null)
                ?: return "application/octet-stream"
            val mimeType = UTTypeCopyPreferredTagWithClass(UTI, kUTTagClassMIMEType)
            return mimeType?.let {
                CFStringGetCStringPtr(it, 0u)?.toKString()
            } ?: "application/octet-stream"
        } finally {
            UTI?.let { CFRelease(it) }  // Ensure UTI is released
            cfExtension?.let { CFRelease(it) }  // Ensure cfExtension is released
        }
    }
}