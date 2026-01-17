package com.eeseka.shelflife.shared.data.export

expect class FileExporter {
    suspend fun exportFile(fileName: String, content: String): Boolean
}