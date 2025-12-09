package com.eeseka.shelflife.shared.design_system.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import shelflife.shared.generated.resources.Res
import shelflife.shared.generated.resources.manrope_bold
import shelflife.shared.generated.resources.manrope_light
import shelflife.shared.generated.resources.manrope_medium
import shelflife.shared.generated.resources.manrope_regular

@Composable
fun getAppTypography(): Typography {
    val manrope = FontFamily(
        Font(Res.font.manrope_light, FontWeight.Light),
        Font(Res.font.manrope_regular, FontWeight.Normal),
        Font(Res.font.manrope_medium, FontWeight.Medium),
        Font(Res.font.manrope_bold, FontWeight.Bold)
    )

    val baseline = Typography()

    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = manrope),
        displayMedium = baseline.displayMedium.copy(fontFamily = manrope),
        displaySmall = baseline.displaySmall.copy(fontFamily = manrope),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = manrope),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = manrope),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = manrope),
        titleLarge = baseline.titleLarge.copy(fontFamily = manrope),
        titleMedium = baseline.titleMedium.copy(fontFamily = manrope),
        titleSmall = baseline.titleSmall.copy(fontFamily = manrope),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = manrope),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = manrope),
        bodySmall = baseline.bodySmall.copy(fontFamily = manrope),
        labelLarge = baseline.labelLarge.copy(fontFamily = manrope),
        labelMedium = baseline.labelMedium.copy(fontFamily = manrope),
        labelSmall = baseline.labelSmall.copy(fontFamily = manrope),
    )
}