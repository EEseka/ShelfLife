package com.eeseka.shelflife.shared.data.database.remote

import com.eeseka.shelflife.shared.data.database.util.FirebaseFirestoreConflictException
import com.eeseka.shelflife.shared.data.database.util.FirebaseFirestoreNotFoundException
import com.eeseka.shelflife.shared.data.database.util.createStorageFile
import com.eeseka.shelflife.shared.data.dto.PantryItemSerializable
import com.eeseka.shelflife.shared.data.mappers.toDomain
import com.eeseka.shelflife.shared.data.mappers.toSerializable
import com.eeseka.shelflife.shared.domain.database.remote.RemotePantryStorageService
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
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
 * ViewModels don't need to worry about Storage vs Firestore - just pass the PantryItem!
 */
class FirebaseFirestoreRemotePantryStorageService(
    private val shelfLifeLogger: ShelfLifeLogger
) : RemotePantryStorageService {
    companion object {
        private const val BASE_COLLECTION_PATH = "users"
        private const val SUB_COLLECTION_PATH = "pantry"
        private const val STORAGE_BASE_PATH = "pantry_images"
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createPantryItem(
        userId: String,
        pantryItem: PantryItem
    ): Result<PantryItem, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val pantryCollection =
                Firebase.firestore.collection(BASE_COLLECTION_PATH).document(userId)
                    .collection(SUB_COLLECTION_PATH)

            val pantryExists = pantryCollection.document(pantryItem.id).get().exists

            if (pantryExists) {
                throw FirebaseFirestoreConflictException("Pantry item already exists")
            }

            // SMART: Auto-upload images to Storage if they're local paths
            val processedItem = uploadImagesIfNeeded(userId, pantryItem)

            pantryCollection.document(processedItem.id).set(processedItem.toSerializable())
            processedItem
        }
    }

    override suspend fun getPantryItems(userId: String): Result<List<PantryItem>, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val pantryCollection =
                Firebase.firestore.collection(BASE_COLLECTION_PATH).document(userId)
                    .collection(SUB_COLLECTION_PATH)

            val snapshot = pantryCollection.get()

            snapshot.documents.map { it.data<PantryItemSerializable>().toDomain() }
        }
    }

    override suspend fun getPantryItem(
        userId: String,
        pantryItemId: String
    ): Result<PantryItem, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val document = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH).document(pantryItemId)
                .get()

            if (!document.exists) {
                throw FirebaseFirestoreNotFoundException("Pantry item not found")
            }

            document.data<PantryItemSerializable>().toDomain()
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun updatePantryItem(
        userId: String,
        pantryItem: PantryItem
    ): Result<PantryItem, DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val document = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH).document(pantryItem.id)

            val existingDoc = document.get()
            if (!existingDoc.exists) {
                throw FirebaseFirestoreNotFoundException("Pantry item not found")
            }

            val oldItem = existingDoc.data<PantryItemSerializable>().toDomain()

            // SMART: Upload new images if they're local paths
            val processedItem = uploadImagesIfNeeded(userId, pantryItem)

            // SMART: Delete old images from Storage if they were replaced
            deleteOldImagesIfReplaced(oldItem, processedItem)

            document.set(processedItem.toSerializable())
            processedItem
        }
    }

    override suspend fun deletePantryItem(
        userId: String,
        pantryItemId: String,
        deleteImage: Boolean
    ): EmptyResult<DataError.RemoteStorage> {
        return safeFirebaseFirestoreCall(shelfLifeLogger) {
            val document = Firebase.firestore
                .collection(BASE_COLLECTION_PATH).document(userId)
                .collection(SUB_COLLECTION_PATH).document(pantryItemId)

            val existingDoc = document.get()
            if (!existingDoc.exists) {
                throw FirebaseFirestoreNotFoundException("Pantry item not found")
            }

            if (deleteImage) {
                val item = existingDoc.data<PantryItemSerializable>().toDomain()

                // SMART: Delete images from Storage before deleting document
                deleteImageFromStorage(item.imageUrl)
                deleteImageFromStorage(item.thumbnailUrl)
            }

            document.delete()
        }
    }

    /**
     * Detects local file paths and uploads them to Firebase Storage.
     * Returns PantryItem with download URLs instead of local paths.
     */
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun uploadImagesIfNeeded(
        userId: String,
        pantryItem: PantryItem
    ): PantryItem {
        val newImageUrl = if (pantryItem.imageUrl != null && isLocalPath(pantryItem.imageUrl)) {
            uploadImageToStorage(
                userId = userId,
                pantryItemId = pantryItem.id,
                localPath = pantryItem.imageUrl,
                fileName = "image_${Uuid.random().toHexString()}.jpg"
            )
        } else {
            pantryItem.imageUrl
        }

        val newThumbnailUrl =
            if (pantryItem.thumbnailUrl != null && isLocalPath(pantryItem.thumbnailUrl)) {
                uploadImageToStorage(
                    userId = userId,
                    pantryItemId = pantryItem.id,
                    localPath = pantryItem.thumbnailUrl,
                    fileName = "thumbnail_${Uuid.random().toHexString()}.jpg"
                )
            } else {
                pantryItem.thumbnailUrl
            }

        return pantryItem.copy(
            imageUrl = newImageUrl,
            thumbnailUrl = newThumbnailUrl
        )
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
        pantryItemId: String,
        localPath: String,
        fileName: String
    ): String {
        val storageRef = Firebase.storage.reference
            .child(STORAGE_BASE_PATH)
            .child(userId)
            .child(pantryItemId)
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
        oldItem: PantryItem,
        newItem: PantryItem
    ) {
        // Delete old main image if it was replaced
        if (oldItem.imageUrl != null &&
            oldItem.imageUrl != newItem.imageUrl &&
            !isLocalPath(oldItem.imageUrl)
        ) {
            deleteImageFromStorage(oldItem.imageUrl)
        }

        // Delete old thumbnail if it was replaced
        if (oldItem.thumbnailUrl != null &&
            oldItem.thumbnailUrl != newItem.thumbnailUrl &&
            !isLocalPath(oldItem.thumbnailUrl)
        ) {
            deleteImageFromStorage(oldItem.thumbnailUrl)
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
     * Example: "https://...googleapis.com/.../o/pantry_images%2FuserId%2F..." → "pantry_images/userId/..."
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
