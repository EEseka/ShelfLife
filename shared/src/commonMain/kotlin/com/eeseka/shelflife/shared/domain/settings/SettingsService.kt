package com.eeseka.shelflife.shared.domain.settings

import com.eeseka.shelflife.shared.domain.auth.User
import kotlinx.coroutines.flow.Flow

interface SettingsService {
    val theme: Flow<AppTheme>
    val hasSeenOnboarding: Flow<Boolean>
    val cachedUser: Flow<User?>
    val notificationPreferences: Flow<NotificationPreferences>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setOnboardingCompleted()
    suspend fun saveUser(user: User?) // Pass null to "Clear" (Logout)
    suspend fun setNotificationPreferences(preferences: NotificationPreferences)
    suspend fun clearUserPreferences()
}