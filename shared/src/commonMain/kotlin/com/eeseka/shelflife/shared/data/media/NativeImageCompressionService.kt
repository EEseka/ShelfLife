package com.eeseka.shelflife.shared.data.media

import com.eeseka.shelflife.shared.domain.media.ImageCompressionService

class NativeImageCompressionService(
    private val imageCompressor: ImageCompressor
) : ImageCompressionService {
    override suspend fun compress(contentPath: String, thresholdBytes: Long): String? {
        return imageCompressor.compress(contentPath, thresholdBytes)
    }
}