package com.eeseka.shelflife.pantry.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eeseka.shelflife.pantry.presentation.util.CameraPermissionGuard
import com.eeseka.shelflife.shared.presentation.util.DeviceConfiguration
import com.eeseka.shelflife.shared.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.ncgroup.kscan.BarcodeFormats
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerController
import org.ncgroup.kscan.ScannerView
import shelflife.feature.pantry.generated.resources.Res
import shelflife.feature.pantry.generated.resources.align_barcode_within_frame
import shelflife.feature.pantry.generated.resources.camera_access_description
import shelflife.feature.pantry.generated.resources.camera_access_required
import shelflife.feature.pantry.generated.resources.close
import shelflife.feature.pantry.generated.resources.open_settings
import shelflife.feature.pantry.generated.resources.scan_product
import shelflife.feature.pantry.generated.resources.toggle_torch

@Composable
fun PantryScannerSheet(
    onDismiss: () -> Unit,
    onBarcodeDetected: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    val deviceConfig = currentDeviceConfiguration()
    val isMobilePortrait = deviceConfig == DeviceConfiguration.MOBILE_PORTRAIT
    val isMobileLandscape = deviceConfig == DeviceConfiguration.MOBILE_LANDSCAPE

    Surface(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPermissionGuard(
                onPermissionDenied = { openSettings ->
                    // Fallback UI when permission is denied
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.camera_access_required),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.camera_access_description),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = openSettings) {
                            Text(stringResource(Res.string.open_settings))
                        }
                    }
                }
            ) {
                // --- PERMISSION GRANTED: SHOW SCANNER ---
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val scannerController = remember { ScannerController() }

                    // Camera Feed
                    ScannerView(
                        codeTypes = listOf(
                            BarcodeFormats.FORMAT_EAN_13,
                            BarcodeFormats.FORMAT_EAN_8,
                            BarcodeFormats.FORMAT_UPC_A,
                            BarcodeFormats.FORMAT_UPC_E
                        ),
                        scannerUiOptions = null,
                        scannerController = scannerController,
                        modifier = Modifier.fillMaxSize()
                    ) { result ->
                        when (result) {
                            is BarcodeResult.OnSuccess -> {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                onBarcodeDetected(result.barcode.data)
                            }

                            is BarcodeResult.OnFailed -> {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
                            }

                            BarcodeResult.OnCanceled -> onDismiss()
                        }
                    }

                    // UX Overlay (Scrim + Brackets + Laser)
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val boxSize = 280.dp
                        val boxSizePx = with(LocalDensity.current) { boxSize.toPx() }
                        val screenHeightPx = constraints.maxHeight.toFloat()

                        // Calculate exact bottom of the scan box relative to screen center
                        val boxBottomY = (screenHeightPx / 2) + (boxSizePx / 2)

                        // Adjust text offset depending on slider position so they don't clash
                        val textPadding = if (isMobilePortrait) 16.dp else 24.dp
                        val textOffsetY =
                            with(LocalDensity.current) { boxBottomY.toDp() + textPadding }

                        ScannerOverlay(modifier = Modifier.fillMaxSize(), scanBoxSize = boxSize)

                        // Helper Text
                        Text(
                            text = stringResource(Res.string.align_barcode_within_frame),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset { IntOffset(0, textOffsetY.roundToPx()) }
                        )
                    }

                    // Controls
                    // Torch (Top Right)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(16.dp)
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(
                                    if (scannerController.torchEnabled) HapticFeedbackType.ToggleOff
                                    else HapticFeedbackType.ToggleOn
                                )
                                scannerController.setTorch(!scannerController.torchEnabled)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (scannerController.torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = stringResource(Res.string.toggle_torch)
                            )
                        }
                    }

                    // Adaptive Zoom Slider
                    if (isMobilePortrait) {
                        // Mobile Portrait: Horizontal Slider at Bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .padding(bottom = 32.dp, start = 48.dp, end = 48.dp)
                                .fillMaxWidth()
                        ) {
                            Slider(
                                value = scannerController.zoomRatio,
                                onValueChange = { scannerController.setZoom(it) },
                                valueRange = 1f..scannerController.maxZoomRatio,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    } else {
                        // Landscape / Tablet: Vertical Slider at Right Edge
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .windowInsetsPadding(WindowInsets.safeDrawing)
                                .padding(end = 12.dp)
                                .fillMaxHeight(0.6f)
                                .width(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Slider(
                                value = scannerController.zoomRatio,
                                onValueChange = { scannerController.setZoom(it) },
                                valueRange = 1f..scannerController.maxZoomRatio,
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationZ = 270f
                                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                                    }
                                    .fillMaxWidth()
                                    .width(200.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
            // Close Button (Top Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape)
            ) {
                IconButton(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, stringResource(Res.string.close))
                }
            }

            if (!isMobileLandscape) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(top = 16.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.scan_product),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay(
    modifier: Modifier = Modifier,
    scanBoxSize: Dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val boxSizePx = scanBoxSize.toPx()

        val left = (canvasWidth - boxSizePx) / 2
        val top = (canvasHeight - boxSizePx) / 2
        val right = left + boxSizePx
        val bottom = top + boxSizePx

        // Dimmed Scrim
        val scrimColor = Color.Black.copy(alpha = 0.5f)
        drawRect(color = scrimColor, topLeft = Offset.Zero, size = Size(canvasWidth, top))
        drawRect(
            color = scrimColor,
            topLeft = Offset(0f, bottom),
            size = Size(canvasWidth, canvasHeight - bottom)
        )
        drawRect(color = scrimColor, topLeft = Offset(0f, top), size = Size(left, boxSizePx))
        drawRect(
            color = scrimColor,
            topLeft = Offset(right, top),
            size = Size(canvasWidth - right, boxSizePx)
        )

        // Corner Brackets
        val cornerLength = 30.dp.toPx()
        val cornerWidth = 4.dp.toPx()
        val cornerColor = Color.White
        val cornerRadius = 12.dp.toPx()
        val stroke = Stroke(width = cornerWidth, cap = StrokeCap.Round)

        // Top Left
        drawPath(
            path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)
            },
            color = cornerColor,
            style = stroke
        )
        // Top Right
        drawPath(
            path = Path().apply {
                moveTo(right - cornerLength, top)
                lineTo(right - cornerRadius, top)
                quadraticTo(right, top, right, top + cornerRadius)
                lineTo(right, top + cornerLength)
            },
            color = cornerColor,
            style = stroke
        )
        // Bottom Left
        drawPath(
            path = Path().apply {
                moveTo(left, bottom - cornerLength)
                lineTo(left, bottom - cornerRadius)
                quadraticTo(left, bottom, left + cornerRadius, bottom)
                lineTo(left + cornerLength, bottom)
            },
            color = cornerColor,
            style = stroke
        )
        // Bottom Right
        drawPath(
            path = Path().apply {
                moveTo(right - cornerLength, bottom)
                lineTo(right - cornerRadius, bottom)
                quadraticTo(right, bottom, right, bottom - cornerRadius)
                lineTo(right, bottom - cornerLength)
            },
            color = cornerColor,
            style = stroke
        )

        // Laser
        val laserY = top + (boxSizePx * animatedProgress)
        drawLine(
            color = Color.Red,
            start = Offset(left + 20f, laserY),
            end = Offset(right - 20f, laserY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
