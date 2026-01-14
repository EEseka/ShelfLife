package com.eeseka.shelflife.shared.domain.media

interface ImageCompressionService {
    suspend fun compress(contentPath: String, thresholdBytes: Long = 200 * 1024L): String?
}