package com.eeseka.shelflife.shared.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.eeseka.shelflife.shared.R.string.export_pantry_data_chooser_title
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class FileExporter(private val context: Context, private val logger: ShelfLifeLogger) {
    actual suspend fun exportFile(fileName: String, content: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachePath = File(context.cacheDir, "exports")
                cachePath.mkdirs()
                val file = File(cachePath, fileName)
                file.writeText(content)

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val title = context.getString(export_pantry_data_chooser_title)
                val chooserIntent = Intent.createChooser(shareIntent, title)
                chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooserIntent)

                true // Success
            } catch (e: Exception) {
                logger.error("File Export Failed", e)
                false // Failed
            }
        }
    }
}