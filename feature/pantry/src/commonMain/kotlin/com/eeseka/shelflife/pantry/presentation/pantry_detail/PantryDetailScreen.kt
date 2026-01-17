package com.eeseka.shelflife.pantry.presentation.pantry_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.components.DetailActionFooter
import com.eeseka.shelflife.pantry.presentation.components.DetailChipSection
import com.eeseka.shelflife.pantry.presentation.components.DetailHeader
import com.eeseka.shelflife.pantry.presentation.components.DetailMetadataSection
import com.eeseka.shelflife.pantry.presentation.components.DetailStatsGrid
import com.eeseka.shelflife.pantry.presentation.components.ExpiryStatusCard
import com.eeseka.shelflife.pantry.presentation.components.HealthScoresSection
import com.eeseka.shelflife.pantry.presentation.components.HeroImageSection
import com.eeseka.shelflife.pantry.presentation.components.NutritionInfoPanel
import com.eeseka.shelflife.pantry.presentation.components.previewPantryItem
import com.eeseka.shelflife.pantry.presentation.pantry_list_detail.PantryAction
import com.eeseka.shelflife.pantry.presentation.util.cleanTag
import com.eeseka.shelflife.pantry.presentation.util.hasNutritionInfo
import com.eeseka.shelflife.shared.design_system.components.ShelfLifeScaffold
import com.eeseka.shelflife.shared.design_system.theme.ShelfLifeTheme
import com.eeseka.shelflife.shared.domain.pantry.PantryItem
import com.eeseka.shelflife.shared.presentation.util.ObserveAsEvents
import com.eeseka.shelflife.shared.presentation.util.ShelfLifeSnackbarVisuals
import com.eeseka.shelflife.shared.presentation.util.SnackbarType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.allergens
import shelflife.feature.pantry.generated.resources.back
import shelflife.feature.pantry.generated.resources.barcode_copied
import shelflife.feature.pantry.generated.resources.cancel
import shelflife.feature.pantry.generated.resources.confirm_delete
import shelflife.feature.pantry.generated.resources.delete_item
import shelflife.feature.pantry.generated.resources.deleting
import shelflife.feature.pantry.generated.resources.edit_item
import shelflife.feature.pantry.generated.resources.labels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryDetailScreen(
    item: PantryItem,
    isDeleteLoading: Boolean,
    isMoveLoading: Boolean,
    events: Flow<PantryDetailEvent>,
    onAction: (PantryAction) -> Unit,
    showBackButton: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    ObserveAsEvents(events) { event ->
        when (event) {
            is PantryDetailEvent.Success -> {
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        message = event.message.asStringAsync(),
                        type = SnackbarType.Success,
                        withDismissAction = true
                    )
                )
            }

            is PantryDetailEvent.Error -> {
                snackbarHostState.showSnackbar(
                    ShelfLifeSnackbarVisuals(
                        message = event.message.asStringAsync(),
                        type = SnackbarType.Error,
                        withDismissAction = true
                    )
                )
            }
        }
    }

    ShelfLifeScaffold(
        snackbarHostState = snackbarHostState,
        topBar = {
            PantryDetailTopBar(
                showBackButton = showBackButton,
                onBackClick = { onAction(PantryAction.OnDetailClose) },
                onEditClick = { onAction(PantryAction.OnEditSheetOpen) },
                onDeleteClick = { showDeleteDialog = true }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val screenHeight = maxHeight
                val heroImageHeight = (screenHeight * 0.45f).coerceIn(250.dp, 400.dp)

                HeroImageSection(
                    imageUrl = item.imageUrl,
                    thumbnailUrl = item.thumbnailUrl,
                    contentDescription = item.name,
                    imageHeight = heroImageHeight
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(heroImageHeight - 32.dp))
                    // Main Content (The "Sheet")
                    Column(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                            )
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        DetailHeader(
                            itemName = item.name,
                            itemBrand = item.brand
                        )

                        ExpiryStatusCard(item.expiryDate)

                        // Data Grid (Quantity & Quantity Unit, Location, Purchase Date, Packaging Size, Opened Date)
                        DetailStatsGrid(item)

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Health & Science
                        if (item.nutriScore != null || item.ecoScore != null || item.novaGroup != null) {
                            HealthScoresSection(
                                nutriScore = item.nutriScore,
                                ecoScore = item.ecoScore,
                                novaGroup = item.novaGroup
                            )
                        }

                        if (item.hasNutritionInfo()) {
                            NutritionInfoPanel(
                                caloriesPer100g = item.caloriesPer100g,
                                sugarPer100g = item.sugarPer100g,
                                fatPer100g = item.fatPer100g,
                                proteinPer100g = item.proteinPer100g
                            )
                        }

                        if (item.allergens.isNotEmpty()) {
                            DetailChipSection(
                                title = stringResource(Res.string.allergens),
                                items = item.allergens.map { it.cleanTag() },
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (item.labels.isNotEmpty()) {
                            DetailChipSection(
                                title = stringResource(Res.string.labels),
                                items = item.labels.map { it.cleanTag() },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Technical / Metadata
                        DetailMetadataSection(
                            barcode = item.barcode,
                            openDate = item.openDate,
                            packagingSize = item.packagingSize,
                            showSnackbar = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        ShelfLifeSnackbarVisuals(
                                            getString(resource = Res.string.barcode_copied),
                                            SnackbarType.Info
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
            DetailActionFooter(
                isLoading = isMoveLoading,
                onConsumed = { onAction(PantryAction.OnItemConsumed) },
                onWasted = { onAction(PantryAction.OnItemWasted) },
            )
        }
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    if (!isDeleteLoading) showDeleteDialog = false
                },
                title = { Text(stringResource(Res.string.delete_item)) },
                text = {
                    if (isDeleteLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(Res.string.deleting))
                        }
                    } else {
                        Text(stringResource(Res.string.confirm_delete))
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleteLoading,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            onAction(PantryAction.OnDeleteItem(item.id))
                            showDeleteDialog = false
                        }) {
                        Text(
                            stringResource(Res.string.delete_item),
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = if (!isDeleteLoading) {
                    {
                        TextButton(onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                            showDeleteDialog = false
                        }) {
                            Text(stringResource(Res.string.cancel))
                        }
                    }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryDetailTopBar(
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    TopAppBar(
        title = {},
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onBackClick()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.back)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                onEditClick()
            }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.edit_item)
                )
            }
            IconButton(onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                onDeleteClick()
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.delete_item),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PantryDetailScreenPreview() {
    ShelfLifeTheme {
        PantryDetailScreen(
            item = previewPantryItem,
            isMoveLoading = false,
            isDeleteLoading = false,
            events = emptyFlow(),
            onAction = {},
            showBackButton = true
        )
    }
}