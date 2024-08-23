package com.github.jing332.filepicker.utils

import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

@OptIn(NativeRuntimeApi::class)
actual fun forceGC() {
    println("Collecting...")
    GC.collect()
}