package com.eeseka.shelflife.shared.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.eeseka.shelflife.shared.data.dto.NotificationPreferencesSerializable
import com.eeseka.shelflife.shared.data.dto.UserSerializable
import com.eeseka.shelflife.shared.data.mappers.toDomain
import com.eeseka.shelflife.shared.data.mappers.toSerializable
import com.eeseka.shelflife.shared.domain.auth.User
import com.eeseka.shelflife.shared.domain.logging.ShelfLifeLogger
import com.eeseka.shelflife.shared.domain.settings.AppTheme
import com.eeseka.shelflife.shared.domain.settings.NotificationPreferences
import com.eeseka.shelflife.shared.domain.settings.SettingsService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DataStoreSettingsService(
    private val shelfLifeLogger: ShelfLifeLogger,
    private val dataStore: DataStore<Preferences>
) : SettingsService {
    companion object Companion {
        // Key strings
        private const val KEY_THEME_STRING = "app_theme"
        private const val KEY_HAS_SEEN_ONBOARDING_STRING = "has_seen_onboarding"
        private const val KEY_USER_CACHE_STRING = "user_cache_json"
        private const val KEY_NOTIFICATION_PREFERENCES_STRING = "notification_preferences"

        // Preference keys
        private val KEY_THEME = stringPreferencesKey(KEY_THEME_STRING)
        private val KEY_HAS_SEEN_ONBOARDING = booleanPreferencesKey(KEY_HAS_SEEN_ONBOARDING_STRING)
        private val KEY_USER_CACHE = stringPreferencesKey(KEY_USER_CACHE_STRING)
        private val KEY_NOTIFICATION_PREFERENCES =
            stringPreferencesKey(KEY_NOTIFICATION_PREFERENCES_STRING)
    }

    // JSON Config (Lenient is safer for future-proofing)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val theme: Flow<AppTheme> = dataStore.data.map { prefs ->
        val themeName = prefs[KEY_THEME]
        try {
            if (themeName != null) enumValueOf<AppTheme>(themeName) else AppTheme.SYSTEM
        } catch (e: IllegalArgumentException) {
            // Fallback if data is corrupted or unknown theme value
            shelfLifeLogger.warn("Unknown theme value: $themeName - more details: ${e.message}")
            AppTheme.SYSTEM
        }
    }

    override val hasSeenOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_HAS_SEEN_ONBOARDING] ?: false
    }

    override val cachedUser: Flow<User?> = dataStore.data.map { prefs ->
        val userJson = prefs[KEY_USER_CACHE]
        if (userJson.isNullOrBlank()) {
            null
        } else {
            try {
                json.decodeFromString<UserSerializable>(userJson).toDomain()
            } catch (e: Exception) {
                // If JSON format changes in future update, don't crash the app. Just return null.
                // This also handles cases where user data is corrupted.
                shelfLifeLogger.warn("Error decoding user cache: ${e.message}")
                null
            }
        }
    }
    override val notificationPreferences: Flow<NotificationPreferences> =
        dataStore.data.map { prefs ->
            val jsonString = prefs[KEY_NOTIFICATION_PREFERENCES]
            if (jsonString.isNullOrBlank()) {
                NotificationPreferences()
            } else {
                try {
                    json.decodeFromString<NotificationPreferencesSerializable>(jsonString)
                        .toDomain()
                } catch (e: Exception) {
                    shelfLifeLogger.error("Error decoding notification prefs: ${e.message}", e)
                    NotificationPreferences()
                }
            }
        }

    override suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[KEY_HAS_SEEN_ONBOARDING] = true
        }
    }

    override suspend fun saveUser(user: User?) {
        dataStore.edit { prefs ->
            if (user == null) {
                // Logout Scenario: Clear the cache
                prefs.remove(KEY_USER_CACHE)
            } else {
                try {
                    val dto = user.toSerializable()
                    prefs[KEY_USER_CACHE] = json.encodeToString(dto)
                } catch (e: Exception) {
                    shelfLifeLogger.error("Error serializing user: ${e.message}", e)
                }
            }
        }
    }

    override suspend fun setNotificationPreferences(preferences: NotificationPreferences) {
        dataStore.edit { prefs ->
            try {
                val dto = preferences.toSerializable()
                prefs[KEY_NOTIFICATION_PREFERENCES] = json.encodeToString(dto)
            } catch (e: Exception) {
                shelfLifeLogger.error("Error serializing notification prefs: ${e.message}", e)
            }
        }
    }

    override suspend fun clearUserPreferences() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_NOTIFICATION_PREFERENCES)
            prefs.remove(KEY_THEME)
        }
    }
}