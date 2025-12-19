package com.eeseka.shelflife.onboarding.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.onboarding.presentation.components.OnboardingControls
import com.eeseka.shelflife.onboarding.presentation.components.OnboardingPageContent
import com.eeseka.shelflife.onboarding.presentation.model.OnboardingPageUi
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.presentation.permissions.Permission
import com.eeseka.shelflife.shared.presentation.permissions.rememberPermissionController
import com.eeseka.shelflife.shared.presentation.util.DeviceConfiguration
import com.eeseka.shelflife.shared.presentation.util.currentDeviceConfiguration
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.onboarding.generated.resources.Res
import shelflife.feature.onboarding.generated.resources.get_notified_before_your_food_expires_so_you_can_cook_it_in_time
import shelflife.feature.onboarding.generated.resources.instantly_add_items_with_the_barcode_scanner_we_handle_the_details
import shelflife.feature.onboarding.generated.resources.never_miss_a_date
import shelflife.feature.onboarding.generated.resources.scan_and_forget
import shelflife.feature.onboarding.generated.resources.stop_wasting_money
import shelflife.feature.onboarding.generated.resources.track_your_food

private const val ANIMATION_FOOD_WASTE = "food_waste.json"
private const val ANIMATION_BARCODE_SCAN = "barcode_scan.json"
private const val ANIMATION_NOTIFICATION = "notification.json"

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun OnboardingScreen(
    onAction: (OnboardingAction) -> Unit
) {
    val config = currentDeviceConfiguration()

    val permissionController = rememberPermissionController()

    val pages = listOf(
        OnboardingPageUi(
            title = stringResource(Res.string.stop_wasting_money),
            description = stringResource(Res.string.track_your_food),
            animationFileName = ANIMATION_FOOD_WASTE
        ),
        OnboardingPageUi(
            title = stringResource(Res.string.scan_and_forget),
            description = stringResource(Res.string.instantly_add_items_with_the_barcode_scanner_we_handle_the_details),
            animationFileName = ANIMATION_BARCODE_SCAN
        ),
        OnboardingPageUi(
            title = stringResource(Res.string.never_miss_a_date),
            description = stringResource(Res.string.get_notified_before_your_food_expires_so_you_can_cook_it_in_time),
            animationFileName = ANIMATION_NOTIFICATION
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }

    // If we are NOT on the first page, INTERCEPT the back button.
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            // Instead of closing the app, scroll back one page
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    val onOnboardingButtonClick: () -> Unit = {
        if (!isProcessing) {
            if (pagerState.currentPage < pages.size - 1) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            } else {
                isProcessing = true
                scope.launch {
                    runCatching {
                        permissionController.requestPermission(Permission.NOTIFICATIONS)
                    }
                    // Navigate REGARDLESS of the outcome or crash above.
                    // Navigation will happen automatically via MainViewModel's reactive state
                    onAction(OnboardingAction.OnGetStartedClick)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing), // Handle notches/status bars safely
        contentAlignment = Alignment.Center
    ) {
        when (config) {
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                // 1. Mobile Landscape: Split View (Row)
                Row(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Content (Pager)
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { pageIndex ->
                        OnboardingPageContent(
                            page = pages[pageIndex],
                            isLandscape = true
                        )
                    }
                    // Right: Controls
                    Column(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OnboardingControls(
                            currentPage = pagerState.currentPage,
                            pageSize = pages.size,
                            onOnboardingButtonClick = onOnboardingButtonClick,
                            isProcessing = isProcessing
                        )
                    }
                }
            }

            else -> {
                // 2. Mobile Portrait & Tablets: Vertical Stack
                // For Tablets, we constrain width to 600.dp to prevent stretching
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(
                            if (config.isWideScreen) Modifier.widthIn(max = 600.dp)
                            else Modifier.fillMaxWidth()
                        )
                        .padding(24.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { pageIndex ->
                        OnboardingPageContent(page = pages[pageIndex])
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OnboardingControls(
                        currentPage = pagerState.currentPage,
                        pageSize = pages.size,
                        onOnboardingButtonClick = onOnboardingButtonClick,
                        isProcessing = isProcessing
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    ShelfLifeTheme {
        OnboardingScreen(
            onAction = {}
        )
    }
}