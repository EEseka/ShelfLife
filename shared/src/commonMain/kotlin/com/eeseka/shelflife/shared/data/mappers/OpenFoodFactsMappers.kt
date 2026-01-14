package com.eeseka.shelflife.shared.data.mappers

import com.eeseka.shelflife.shared.data.dto.OpenFoodFactsProduct
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.domain.pantry.StorageLocation
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
fun OpenFoodFactsProduct.toDomain(barcode: String): PantryItem {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Parse Name (Prefer product_name, fallback to generic)
    val name = this.productName ?: this.genericName ?: ""

    return PantryItem(
        id = Uuid.random().toString(),
        barcode = barcode,
        name = name,
        brand = this.brands,
        imageUrl = this.imageUrl,
        thumbnailUrl = this.smallImageUrl,

        // Defaulting quantity to 1.0 pcs as we can't easily parse "500g" without regex logic
        quantity = 1.0,
        quantityUnit = "",
        packagingSize = this.quantityString,

        // API doesn't know these, so we set "Today" as default for the UI to edit
        expiryDate = today,
        purchaseDate = today,
        openDate = null,
        storageLocation = StorageLocation.PANTRY,

        // Insights
        nutriScore = this.nutriScore?.uppercase(),
        novaGroup = this.novaGroup,
        ecoScore = this.ecoScore?.uppercase(),

        // Clean up tags (remove "en:" prefix common in OFF api)
        allergens = this.allergensTags.map { it.removePrefix("en:").replace("-", " ") },
        labels = this.labelsTags.map { it.removePrefix("en:").replace("-", " ") },

        // Nutrition (OFF often returns Doubles, domain might expect Int/Double)
        caloriesPer100g = this.nutriments?.calories?.toInt(),
        sugarPer100g = this.nutriments?.sugar,
        fatPer100g = this.nutriments?.fat,
        proteinPer100g = this.nutriments?.proteins
    )
}