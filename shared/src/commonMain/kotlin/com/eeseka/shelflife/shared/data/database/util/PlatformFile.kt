package com.eeseka.shelflife.shared.data.database.util

import dev.gitlive.firebase.storage.File

/**
 * Helper function to create a Firebase Storage File from a string path.
 * This is implemented per-platform since File construction is platform-specific.
 * 
 * - Android: Pass content:// or file:// URI strings
 * - iOS: Pass file:// URL strings or NSURL string representations
 */
expect fun createStorageFile(path: String): File