package com.eeseka.shelflife.auth.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.auth.generated.resources.Res
import shelflife.feature.auth.generated.resources.apple_logo
import shelflife.feature.auth.generated.resources.google_logo
import shelflife.feature.auth.generated.resources.please_wait

@Composable
fun SocialAuthButton(
    text: String,
    icon: Painter,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = !isLoading,
    loadingText: String = stringResource(Res.string.please_wait),
    iconTint: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Standard touch target height
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(targetState = isLoading) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconTint
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (isLoading) loadingText else text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SocialAuthButtonPreview() {
    ShelfLifeTheme {
        SocialAuthButton(
            text = "Continue with Google",
            icon = painterResource(Res.drawable.google_logo),
            isLoading = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SocialAuthButtonApplePreview() {
    ShelfLifeTheme {
        SocialAuthButton(
            text = "Continue with Apple",
            icon = painterResource(Res.drawable.apple_logo),
            isLoading = false,
            onClick = {}
        )
    }
}

