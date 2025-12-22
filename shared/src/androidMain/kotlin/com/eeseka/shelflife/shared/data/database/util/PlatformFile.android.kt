package com.eeseka.shelflife.shared.data.database.util

import androidx.core.net.toUri
import dev.gitlive.firebase.storage.File

actual fun createStorageFile(path: String): File {
    return File(path.toUri())
}
