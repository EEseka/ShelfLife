package com.eeseka.shelflife.shared.data.export

import com.eeseka.shelflife.shared.domain.export.FileExportService

class NativeFileExportService(
    private val fileExporter: FileExporter
) : FileExportService {
    override suspend fun exportFile(fileName: String, content: String): Boolean {
        return fileExporter.exportFile(fileName, content)
    }
}