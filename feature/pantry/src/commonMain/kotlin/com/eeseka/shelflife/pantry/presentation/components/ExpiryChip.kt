package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.util.CalendarUnit
import com.eeseka.shelflife.pantry.presentation.util.ExpiryState
import org.jetbrains.compose.resources.stringResource
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

@Composable
fun ExpiryChip(state: ExpiryState) {
    val (text, containerColor, contentColor) = when (state) {
        is ExpiryState.Expired -> {
            val textString = when (state.unit) {
                CalendarUnit.YEAR -> {
                    if (state.value == 1) stringResource(Res.string.expired_one_year_ago)
                    else stringResource(Res.string.expired_years_ago, state.value)
                }
                CalendarUnit.MONTH -> {
                    if (state.value == 1) stringResource(Res.string.expired_one_month_ago)
                    else stringResource(Res.string.expired_months_ago, state.value)
                }
                CalendarUnit.DAY -> {
                    stringResource(Res.string.expired_days_ago, state.value)
                }
            }
            Triple(
                textString,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.onError
            )
        }

        ExpiryState.Yesterday -> {
            Triple(
                stringResource(Res.string.expired_yesterday),
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.onError
            )
        }

        ExpiryState.Today -> {
            Triple(
                stringResource(Res.string.expires_today),
                MaterialTheme.colorScheme.errorContainer,
                MaterialTheme.colorScheme.onErrorContainer
            )
        }

        is ExpiryState.Urgent -> {
            val resId =
                if (state.days == 1) Res.string.expires_in_one_day else Res.string.expires_in_days
            Triple(
                stringResource(resId, state.days),
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        is ExpiryState.Warning -> {
            Triple(
                stringResource(Res.string.expires_in_days, state.days),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        is ExpiryState.Safe -> {
            val textString = when (state.unit) {
                CalendarUnit.YEAR -> {
                    if (state.value == 1) stringResource(Res.string.expires_in_one_year)
                    else stringResource(Res.string.expires_in_years, state.value)
                }

                CalendarUnit.MONTH -> {
                    if (state.value == 1) stringResource(Res.string.expires_in_one_month)
                    else stringResource(Res.string.expires_in_months, state.value)
                }

                CalendarUnit.DAY -> {
                    if (state.value == 1) stringResource(Res.string.expires_in_one_day)
                    else stringResource(Res.string.expires_in_days, state.value)
                }
            }

            Triple(
                textString,
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.defaultMinSize(minHeight = 20.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}