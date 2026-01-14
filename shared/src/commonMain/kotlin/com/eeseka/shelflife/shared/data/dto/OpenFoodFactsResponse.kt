package com.eeseka.shelflife.shared.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenFoodFactsResponse(
    // The API returns "status": 1 if found, 0 if not found
    @SerialName("status") val status: Int, 
    @SerialName("status_verbose") val statusVerbose: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("product") val product: OpenFoodFactsProduct? = null
)

@Serializable
data class OpenFoodFactsProduct(
    @SerialName("_id") val id: String? = null,
    @SerialName("product_name") val productName: String? = null,
    // Sometimes they use generic name
    @SerialName("generic_name") val genericName: String? = null, 
    @SerialName("brands") val brands: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("image_front_small_url") val smallImageUrl: String? = null,
    
    @SerialName("quantity") val quantityString: String? = null, // e.g. "500g"
    
    // Insights
    @SerialName("nutriscore_grade") val nutriScore: String? = null,
    @SerialName("nova_group") val novaGroup: Int? = null,
    @SerialName("ecoscore_grade") val ecoScore: String? = null,
    
    // Allergens come as a comma-separated string usually, or hierarchy list
    @SerialName("allergens_tags") val allergensTags: List<String> = emptyList(),
    @SerialName("labels_tags") val labelsTags: List<String> = emptyList(),
    
    @SerialName("nutriments") val nutriments: OffNutriments? = null
)

@Serializable
data class OffNutriments(
    @SerialName("energy-kcal_100g") val calories: Double? = null,
    @SerialName("sugars_100g") val sugar: Double? = null,
    @SerialName("fat_100g") val fat: Double? = null,
    @SerialName("proteins_100g") val proteins: Double? = null
)