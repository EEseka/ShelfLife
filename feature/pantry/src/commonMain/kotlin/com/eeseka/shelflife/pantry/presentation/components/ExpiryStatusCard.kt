package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.util.CalendarUnit
import com.eeseka.shelflife.pantry.presentation.util.ExpiryState
import com.eeseka.shelflife.pantry.presentation.util.calculateExpiryState
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.expired_days_ago
import shelflife.feature.pantry.generated.resources.expired_months_ago
import shelflife.feature.pantry.generated.resources.expired_one_month_ago
import shelflife.feature.pantry.generated.resources.expired_one_year_ago
import shelflife.feature.pantry.generated.resources.expired_years_ago
import shelflife.feature.pantry.generated.resources.expired_yesterday
import shelflife.feature.pantry.generated.resources.expires_in_days
import shelflife.feature.pantry.generated.resources.expires_in_months
import shelflife.feature.pantry.generated.resources.expires_in_one_day
import shelflife.feature.pantry.generated.resources.expires_in_one_month
import shelflife.feature.pantry.generated.resources.expires_in_one_year
import shelflife.feature.pantry.generated.resources.expires_in_years
import shelflife.feature.pantry.generated.resources.expires_today
import shelflife.feature.pantry.generated.resources.expiry_date

@Composable
fun ExpiryStatusCard(expiryDate: LocalDate) {
    val expiryState = calculateExpiryState(expiryDate)

    val (statusText, containerColor, contentColor, icon) = when (expiryState) {
        is ExpiryState.Expired -> {
            val textString = when (expiryState.unit) {
                CalendarUnit.YEAR -> {
                    if (expiryState.value == 1) stringResource(Res.string.expired_one_year_ago)
                    else stringResource(Res.string.expired_years_ago, expiryState.value)
                }

                CalendarUnit.MONTH -> {
                    if (expiryState.value == 1) stringResource(Res.string.expired_one_month_ago)
                    else stringResource(Res.string.expired_months_ago, expiryState.value)
                }

                CalendarUnit.DAY -> stringResource(Res.string.expired_days_ago, expiryState.value)
            }

            Quadruple(
                textString,
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer,
                Icons.Default.Warning
            )
        }

        ExpiryState.Yesterday -> Quadruple(
            stringResource(Res.string.expired_yesterday),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.Warning
        )

        ExpiryState.Today -> Quadruple(
            stringResource(Res.string.expires_today),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Default.PriorityHigh
        )

        is ExpiryState.Urgent -> {
            val resId =
                if (expiryState.days == 1) Res.string.expires_in_one_day else Res.string.expires_in_days
            Quadruple(
                stringResource(resId, expiryState.days),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
                Icons.Default.HourglassEmpty
            )
        }

        is ExpiryState.Warning -> Quadruple(
            stringResource(Res.string.expires_in_days, expiryState.days),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Default.AccessTime
        )

        is ExpiryState.Safe -> {
            val textString = when (expiryState.unit) {
                CalendarUnit.YEAR -> if (expiryState.value == 1) stringResource(Res.string.expires_in_one_year) else stringResource(
                    Res.string.expires_in_years,
                    expiryState.value
                )

                CalendarUnit.MONTH -> if (expiryState.value == 1) stringResource(Res.string.expires_in_one_month) else stringResource(
                    Res.string.expires_in_months,
                    expiryState.value
                )

                CalendarUnit.DAY -> {
                    if (expiryState.value == 1) stringResource(Res.string.expires_in_one_day)
                    else stringResource(Res.string.expires_in_days, expiryState.value)
                }
            }
            Quadruple(
                textString,
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Default.Event
            )
        }
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with a subtle background circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(contentColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text Content
            Column {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stringResource(Res.string.expiry_date)}: $expiryDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// By the time I come back to this code after some time all of these would have "expired" 😂
@Preview(showBackground = true)
@Composable
fun ExpiryStatusCardPreview1() {
    ShelfLifeTheme {
        ExpiryStatusCard(LocalDate(2023, 12, 1))
    }
}

@Preview(showBackground = true)
@Composable
fun ExpiryStatusCardPreview2() {
    ShelfLifeTheme {
        ExpiryStatusCard(LocalDate(2028, 12, 1))
    }
}


@Preview(showBackground = true)
@Composable
fun ExpiryStatusCardPreview3() {
    ShelfLifeTheme {
        ExpiryStatusCard(LocalDate(2026, 1, 10))
    }
}


@Preview(showBackground = true)
@Composable
fun ExpiryStatusCardPreview4() {
    ShelfLifeTheme {
        ExpiryStatusCard(LocalDate(2026, 1, 11))
    }
}

@Preview(showBackground = true)
@Composable
fun ExpiryStatusCardPreview5() {
    ShelfLifeTheme {
        ExpiryStatusCard(LocalDate(2026, 1, 14))
    }
}