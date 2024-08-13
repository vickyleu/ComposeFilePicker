package com.github.jing332.filepicker.filetype

import kotlinx.cinterop.memScoped
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreServices.UTTypeCopyPreferredTagWithClass
import platform.CoreServices.UTTypeCreatePreferredIdentifierForTag
import platform.CoreServices.kUTTagClassFilenameExtension
import platform.CoreServices.kUTTagClassMIMEType
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSString

//  iOS 中通过文件名获取文件类型
/*@Suppress("UNCHECKED_CAST")
actual fun getMineType(name: String): String {
    val fileExtension = name.substringAfterLast('.', "")
    memScoped {
        val cf = CFBridgingRetain(fileExtension) as CFStringRef?
        try {
            val UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, cf, null)
                ?: return "application/octet-stream"
            val utiCf = CFBridgingRetain(UTI) as CFStringRef?
            try {
                if(utiCf!=null){
                    val mimeType = UTTypeCopyPreferredTagWithClass(utiCf, kUTTagClassMIMEType)
                    val mimeTypeCf = CFBridgingRetain(mimeType) as NSString?
                    return mimeTypeCf?.toString() ?: "application/octet-stream"
                }else{
                    return "application/octet-stream"
                }
            }catch (e: Exception){
                e.printStackTrace()
                return "application/octet-stream"
            } finally {
                CFBridgingRelease(utiCf)
            }
        } finally {
            CFBridgingRelease(cf)
        }
    }
}*/

@Suppress("UNCHECKED_CAST")
actual fun getMineType(name: String): String {
    val fileExtension = name.substringAfterLast('.', "")
    memScoped {
        val cfExtension = CFBridgingRetain(fileExtension as NSString) as CFStringRef?
        var UTI:CFStringRef?=null
        try {
            UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, cfExtension, null)
                ?: return "application/octet-stream"
            val mimeType = UTTypeCopyPreferredTagWithClass(UTI, kUTTagClassMIMEType)
            return mimeType?.let {
                (it as NSString).toString()
            } ?: "application/octet-stream"
        } finally {
            UTI?.let { CFRelease(it) }  // Ensure UTI is released
            cfExtension?.let { CFRelease(it) }  // Ensure cfExtension is released
        }
    }
}