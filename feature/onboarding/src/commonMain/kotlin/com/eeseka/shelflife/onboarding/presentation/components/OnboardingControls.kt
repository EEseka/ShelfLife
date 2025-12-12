package com.eeseka.shelflife.onboarding.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import shelflife.feature.onboarding.generated.resources.Res
import shelflife.feature.onboarding.generated.resources.get_started
import shelflife.feature.onboarding.generated.resources.next

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingControls(
    currentPage: Int,
    pageSize: Int,
    isProcessing: Boolean,
    onOnboardingButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingPageIndicator(
            pageSize = pageSize,
            currentPage = currentPage,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OnboardingPageButton(
            onClick = onOnboardingButtonClick,
            enabled = !isProcessing,
            text = if (currentPage == pageSize - 1)
                stringResource(Res.string.get_started)
            else
                stringResource(Res.string.next),
            modifier = Modifier.height(56.dp)
        )
    }
}