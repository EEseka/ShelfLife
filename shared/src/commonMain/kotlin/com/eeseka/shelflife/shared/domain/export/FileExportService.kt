package com.eeseka.shelflife.shared.domain.export

interface FileExportService {
    suspend fun exportFile(fileName: String, content: String): Boolean
}