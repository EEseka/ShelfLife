package com.eeseka.shelflife.pantry.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

@Composable
actual fun CameraPermissionGuard(
    onPermissionDenied: @Composable (openSettings: () -> Unit) -> Unit,
    onPermissionGranted: @Composable () -> Unit
) {
    var isGranted by remember { mutableStateOf(false) }
    var isDetermined by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                isGranted = true
                isDetermined = true
            }

            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    isGranted = granted
                    isDetermined = true
                }
            }

            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                isGranted = false
                isDetermined = true
            }

            else -> {
                isGranted = false
                isDetermined = true
            }
        }
    }

    val openSettings = {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null && UIApplication.sharedApplication.canOpenURL(settingsUrl)) {
            UIApplication.sharedApplication.openURL(settingsUrl, emptyMap<Any?, Any?>(), null)
        }
    }

    if (!isDetermined) {
        // Optional: Show a loading spinner while iOS prompts the user
        // For now, we return empty so we don't flash the denied screen
    } else if (isGranted) {
        onPermissionGranted()
    } else {
        onPermissionDenied(openSettings)
    }
}