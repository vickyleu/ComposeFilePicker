package com.github.jing332.filepicker.utils

// ANDROID GC
actual fun forceGC() {
    println("Collecting...")
    System.gc()
}