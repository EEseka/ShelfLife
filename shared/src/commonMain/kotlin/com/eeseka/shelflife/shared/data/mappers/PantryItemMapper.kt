package com.eeseka.shelflife.shared.data.mappers

import com.eeseka.shelflife.shared.data.database.local.entities.PantryItemEntity
import com.eeseka.shelflife.shared.data.dto.PantryItemSerializable
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import kotlinx.datetime.LocalDate

fun PantryItemSerializable.toDomain(): PantryItem {
    return PantryItem(
        id = id,
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        packagingSize = packagingSize,
        expiryDate = LocalDate.parse(expiryDate),
        purchaseDate = LocalDate.parse(purchaseDate),
        openDate = openDate?.let { LocalDate.parse(it) },
        storageLocation = StorageLocation.valueOf(storageLocation),
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore,
        allergens = allergens,
        labels = labels,
        caloriesPer100g = caloriesPer100g,
        sugarPer100g = sugarPer100g,
        fatPer100g = fatPer100g,
        proteinPer100g = proteinPer100g
    )
}

fun PantryItem.toSerializable(): PantryItemSerializable {
    return PantryItemSerializable(
        id = id,
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        packagingSize = packagingSize,
        expiryDate = expiryDate.toString(),
        purchaseDate = purchaseDate.toString(),
        openDate = openDate?.toString(),
        storageLocation = storageLocation.name,
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore,
        allergens = allergens,
        labels = labels,
        caloriesPer100g = caloriesPer100g,
        sugarPer100g = sugarPer100g,
        fatPer100g = fatPer100g,
        proteinPer100g = proteinPer100g
    )
}

fun PantryItemEntity.toDomain(): PantryItem {
    return PantryItem(
        id = id,
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        packagingSize = packagingSize,
        // Convert Epoch Days (Long) back to LocalDate
        expiryDate = LocalDate.fromEpochDays(expiryDate.toInt()),
        purchaseDate = LocalDate.fromEpochDays(purchaseDate.toInt()),
        openDate = openDate?.let { LocalDate.fromEpochDays(it.toInt()) },
        storageLocation = try {
            StorageLocation.valueOf(storageLocation)
        } catch (e: Exception) {
            e.printStackTrace()
            StorageLocation.PANTRY // Fallback
        },
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore,
        // Convert CSV String back to List
        allergens = if (allergens.isBlank()) emptyList() else allergens.split(","),
        labels = if (labels.isBlank()) emptyList() else labels.split(","),
        caloriesPer100g = caloriesPer100g,
        sugarPer100g = sugarPer100g,
        fatPer100g = fatPer100g,
        proteinPer100g = proteinPer100g
    )
}

fun PantryItem.toEntity(): PantryItemEntity {
    return PantryItemEntity(
        id = id,
        barcode = barcode,
        name = name,
        brand = brand,
        imageUrl = imageUrl,
        thumbnailUrl = thumbnailUrl,
        quantity = quantity,
        quantityUnit = quantityUnit,
        packagingSize = packagingSize,
        // Convert LocalDate to Epoch Days (Int -> Long)
        expiryDate = expiryDate.toEpochDays(),
        purchaseDate = purchaseDate.toEpochDays(),
        openDate = openDate?.toEpochDays(),
        storageLocation = storageLocation.name,
        updatedAt = updatedAt,
        nutriScore = nutriScore,
        novaGroup = novaGroup,
        ecoScore = ecoScore,
        // Convert List to CSV String
        allergens = allergens.joinToString(","),
        labels = labels.joinToString(","),
        caloriesPer100g = caloriesPer100g,
        sugarPer100g = sugarPer100g,
        fatPer100g = fatPer100g,
        proteinPer100g = proteinPer100g
    )
}
