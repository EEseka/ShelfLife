package com.eeseka.shelflife.shared.data.export

import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual class FileExporter(
    private val logger: ShelfLifeLogger
) {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun exportFile(fileName: String, content: String): Boolean {
        return withContext(Dispatchers.Default) {
            try {
                val tempDir = NSTemporaryDirectory()
                val path = tempDir + fileName
                val url = NSURL.fileURLWithPath(path)

                val nsContent = NSString.create(string = content)
                nsContent.writeToFile(path, true, NSUTF8StringEncoding, null)

                withContext(Dispatchers.Main) {
                    val activityController = UIActivityViewController(
                        activityItems = listOf(url),
                        applicationActivities = null
                    )

                    val rootController =
                        UIApplication.sharedApplication.keyWindow?.rootViewController
                    rootController?.presentViewController(
                        activityController,
                        animated = true,
                        completion = null
                    )
                }
                true // Success
            } catch (e: Exception) {
                logger.error("File Export Failed", e)
                false // Failed
            }
        }
    }
}