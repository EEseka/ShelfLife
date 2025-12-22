package com.eeseka.shelflife.shared.data.mappers

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
