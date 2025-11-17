// File: core/data/repository/SettingsRepository.kt
// (Đã cập nhật logic Caching)

package com.example.stushare.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey // ⭐️ IMPORT MỚI
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Định nghĩa DataStore (giữ nguyên)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Định nghĩa Keys (Thêm key mới)
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        // ⭐️ THÊM KEY MỚI:
        val LAST_REFRESH_TIMESTAMP = longPreferencesKey("last_refresh_timestamp")
    }

    // --- USER NAME (Giữ nguyên) ---
    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "Tên Sinh Viên"
        }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USER_NAME] = name
        }
    }

    // --- REFRESH CACHING (Logic mới) ---

    /**
     * ⭐️ HÀM MỚI (READ):
     * Lấy mốc thời gian lần cuối làm mới dữ liệu.
     * Trả về 0L nếu là lần đầu tiên (buộc phải làm mới).
     */
    val lastRefreshTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_REFRESH_TIMESTAMP] ?: 0L
        }

    /**
     * ⭐️ HÀM MỚI (WRITE):
     * Cập nhật mốc thời gian làm mới về "ngay bây giờ".
     */
    suspend fun updateLastRefreshTimestamp() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_REFRESH_TIMESTAMP] = System.currentTimeMillis()
        }
    }
}