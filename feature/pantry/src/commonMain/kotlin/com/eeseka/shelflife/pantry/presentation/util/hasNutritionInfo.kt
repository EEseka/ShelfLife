package com.eeseka.shelflife.pantry.presentation.util

import com.eeseka.shelflife.shared.domain.pantry.PantryItem

fun PantryItem.hasNutritionInfo(): Boolean =
    caloriesPer100g != null || sugarPer100g != null || fatPer100g != null || proteinPer100g != null