package com.eeseka.shelflife.shared.data.database.util

import dev.gitlive.firebase.storage.File
import platform.Foundation.NSURL

actual fun createStorageFile(path: String): File {
    val url = NSURL.URLWithString(path) ?: throw IllegalArgumentException("Invalid file path: $path")
    return File(url)
}
