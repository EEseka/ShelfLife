package com.eeseka.shelflife.pantry.presentation.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSProcessInfo
import platform.Foundation.isiOSAppOnMac
import platform.darwin.sel_registerName

@OptIn(ExperimentalForeignApi::class)
actual fun isAppleSiliconMac(): Boolean {
    val processInfo = NSProcessInfo.processInfo

    if (processInfo.respondsToSelector(sel_registerName("isiOSAppOnMac"))) {
        return processInfo.isiOSAppOnMac()
    }

    return false
}