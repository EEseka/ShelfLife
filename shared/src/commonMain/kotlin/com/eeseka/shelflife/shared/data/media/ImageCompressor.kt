package com.eeseka.shelflife.shared.data.media

expect class ImageCompressor {
    suspend fun compress(contentPath: String, thresholdBytes: Long): String?
}