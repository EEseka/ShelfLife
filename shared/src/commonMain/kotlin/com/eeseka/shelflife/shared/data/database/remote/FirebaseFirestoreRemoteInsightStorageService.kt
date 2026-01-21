package com.eeseka.shelflife.shared.data.database.remote

import com.eeseka.shelflife.shared.data.database.util.FirebaseFirestoreConflictException
import com.eeseka.shelflife.shared.data.database.util.FirebaseFirestoreNotFoundException
import com.eeseka.shelflife.shared.data.database.util.createStorageFile
import com.eeseka.shelflife.shared.data.dto.InsightItemSerializable
import com.eeseka.shelflife.shared.data.mappers.toDomain
import com.eeseka.shelflife.shared.data.mappers.toSerializable
import com.eeseka.shelflife.shared.domain.database.remote.RemoteInsightStorageService
import com.eeseka.shelflife.shared.domain.insight.InsightItem
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.util.DataError
import com.eeseka.shelflife.shared.domain.util.EmptyResult
import com.eeseka.shelflife.shared.domain.util.Result
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Smart Firebase Firestore + Storage implementation of RemoteStorageService.
 *
 * This service is SMART - it automatically handles:
 * 1. Detecting local file paths in imageUrl/thumbnailUrl
 * 2. Uploading those files to Firebase Storage
 * 3. Replacing local paths with download URLs
 * 4. Saving to Firestore with the processed URLs
 *
 * ViewModels don't need to worry about Storage vs Firestore - just pass the InsightItem!
 */
class FirebaseFirestoreRemoteInsightStorageService(
    private val shelfLifeLogger: ShelfLifeLogger
) : RemoteInsightStorageService {
    companion object {
        private const val BASE_COLLECTION_PATH = "users"
        private const val SUB_COLLECTION_PATH = "insight"
        private const val STORAGE_BASE_PATH = "insight_images"
    }

    override suspend fun createInsightItem(
        userId: String,
        insightItem: InsightItem
    ): Result<InsightItem, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val insightCollection =
                Firebase.firestore.collection(BASE_COLLECTION_PATH).document(userId)
                    .collection(SUB_COLLECTION_PATH)

            val insightExists = insightCollection.document(insightItem.id).get().exists

            if (insightExists) {
                throw FirebaseFirestoreConflictException("Insight item already exists")
            }

            // SMART: Auto-upload images to Storage if they're local paths
            val processedItem = uploadImagesIfNeeded(userId, insightItem)

            insightCollection.document(processedItem.id).set(processedItem.toSerializable())
            processedItem
        }
    }

    override suspend fun getInsightItems(userId: String): Result<List<InsightItem>, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val insightCollection =
                Firebase.firestore.collection(BASE_COLLECTION_PATH).document(userId)
                    .collection(SUB_COLLECTION_PATH)

            val snapshot = insightCollection.get()

            snapshot.documents.map { it.data<InsightItemSerializable>().toDomain() }
        }
    }

    override suspend fun getInsightItem(
        userId: String,
        insightItemId: String
    ): Result<InsightItem, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val document = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH).document(insightItemId)
                .get()

            if (!document.exists) {
                throw FirebaseFirestoreNotFoundException("Insight item not found")
            }

            document.data<InsightItemSerializable>().toDomain()
        }
    }

    override suspend fun updateInsightItem(
        userId: String,
        insightItem: InsightItem
    ): Result<InsightItem, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val document = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH).document(insightItem.id)

            val existingDoc = document.get()
            if (!existingDoc.exists) {
                throw FirebaseFirestoreNotFoundException("Insight item not found")
            }

            val oldItem = existingDoc.data<InsightItemSerializable>().toDomain()

            // SMART: Upload new images if they're local paths
            val processedItem = uploadImagesIfNeeded(userId, insightItem)

            // SMART: Delete old images from Storage if they were replaced
            deleteOldImagesIfReplaced(oldItem, processedItem)

            document.set(processedItem.toSerializable())
            processedItem
        }
    }

    override suspend fun deleteInsightItem(
        userId: String,
        insightItemId: String
    ): EmptyResult<DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val document = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH).document(insightItemId)

            val existingDoc = document.get()
            if (!existingDoc.exists) {
                throw FirebaseFirestoreNotFoundException("Insight item not found")
            }

            val item = existingDoc.data<InsightItemSerializable>().toDomain()

            // SMART: Delete images from Storage before deleting document
            deleteImageFromStorage(item.imageUrl)
            document.delete()
        }
    }

    override suspend fun deleteAllInsightItems(
        userId: String
    ): EmptyResult<DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val collection = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH)

            val snapshot = collection.get()
            val documents = snapshot.documents

            if (documents.isEmpty()) return@safeFirebaseFirestoreCall

            // Process in chunks of 500 (Firestore Batch Limit)
            documents.chunked(500).forEach { batchDocuments ->
                val batch = Firebase.firestore.batch()

                for (document in batchDocuments) {
                    // Try to delete the associated image
                    try {
                        // We convert to domain just to get the imageUrl
                        val item = document.data<InsightItemSerializable>().toDomain()
                        // Fire-and-forget image deletion (don't await)
                        deleteImageFromStorage(item.imageUrl)
                    } catch (_: Exception) {
                        // If parsing fails (corrupted data), just log it.
                        // We DO NOT want to stop the document deletion.
                        shelfLifeLogger.warn("Skipping image deletion for corrupted item: ${document.id}")
                    }

                    // Queue Document Deletion (ALWAYS happens, even if image deletion failed)
                    batch.delete(document.reference)
                }

                // Commit the batch (1 Network Call per 500 items)
                batch.commit()
            }
        }
    }

    /**
     * Detects local file paths and uploads them to Firebase Storage.
     * Returns InsightItem with download URLs instead of local paths.
     */
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun uploadImagesIfNeeded(
        userId: String,
        insightItem: InsightItem
    ): InsightItem {
        val newImageUrl = if (insightItem.imageUrl != null && isLocalPath(insightItem.imageUrl)) {
            uploadImageToStorage(
                userId = userId,
                insightItemId = insightItem.id,
                localPath = insightItem.imageUrl,
                fileName = "image_${Uuid.random().toHexString()}.jpg"
            )
        } else {
            insightItem.imageUrl
        }
        return insightItem.copy(imageUrl = newImageUrl)
    }

    /**
     * Uploads a local file to Firebase Storage and returns the download URL.
     *
     * Note: The localPath is expected to be a valid file URI/path that can be used
     * to create a File object. The platform-specific File creation happens here.
     * On Android: content:// or file:// URIs
     * On iOS: file:// URLs or NSURLs converted to string
     */
    private suspend fun uploadImageToStorage(
        userId: String,
        insightItemId: String,
        localPath: String,
        fileName: String
    ): String {
        val storageRef = Firebase.storage.reference
            .child(STORAGE_BASE_PATH)
            .child(userId)
            .child(insightItemId)
            .child(fileName)

        // Upload to Storage using platform-specific File creation
        val file = createStorageFile(localPath)
        storageRef.putFile(file)

        return storageRef.getDownloadUrl()
    }

    /**
     * Deletes old images from Storage if they were replaced with new ones.
     */
    private suspend fun deleteOldImagesIfReplaced(
        oldItem: InsightItem,
        newItem: InsightItem
    ) {
        // Delete old main image if it was replaced
        if (oldItem.imageUrl != null &&
            oldItem.imageUrl != newItem.imageUrl &&
            !isLocalPath(oldItem.imageUrl)
        ) {
            deleteImageFromStorage(oldItem.imageUrl)
        }
    }

    /**
     * Deletes an image from Firebase Storage given its download URL.
     */
    private suspend fun deleteImageFromStorage(downloadUrl: String?) {
        if (downloadUrl == null) return

        // Only attempt to delete if it's actually hosted by us!
        // This prevents trying to delete OpenFoodFacts URLs
        if (!downloadUrl.contains("firebasestorage.googleapis.com")) {
            return
        }

        try {
            val storagePath = extractFirebaseStoragePath(downloadUrl) ?: return
            val storageRef = Firebase.storage.reference(storagePath)
            storageRef.delete()
        } catch (e: Exception) {
            // Log but don't fail the operation if image deletion fails coz it doesn't concern user
            shelfLifeLogger.error("Failed to delete image from Storage: $downloadUrl", e)
        }
    }

    /**
     * Extracts the storage path from a Firebase Storage download URL.
     * Example: "https://...googleapis.com/.../o/insight_images%2FuserId%2F..." → "insight_images/userId/..."
     * Could also be in pantry_images as when moved to insights, I don't delete it from storage but rather use it
     */
    private fun extractFirebaseStoragePath(downloadUrl: String): String? {
        val startIndex = downloadUrl.indexOf("/o/") + 3
        if (startIndex < 3) return null

        val endIndex = downloadUrl.indexOf("?", startIndex)
        val encodedPath = if (endIndex != -1) {
            downloadUrl.substring(startIndex, endIndex)
        } else {
            downloadUrl.substring(startIndex)
        }

        return decodeFirebasePath(encodedPath)
    }

    /**
     * Decodes URL-encoded Firebase Storage path.
     */
    private fun decodeFirebasePath(encodedPath: String): String {
        return encodedPath
            .replace("%2F", "/")
            .replace("%20", " ")
            .replace("%2B", "+")
            .replace("%3A", ":")
    }

    /**
     * Checks if a URL is a local file path (not an http/https URL).
     */
    private fun isLocalPath(url: String): Boolean {
        return !url.startsWith("http://") && !url.startsWith("https://")
    }
}