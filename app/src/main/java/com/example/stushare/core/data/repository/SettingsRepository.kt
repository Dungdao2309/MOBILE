// File: core/data/repository/SettingsRepository.kt

package com.example.stushare.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey // Khai báo key cho String
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Định nghĩa DataStore cho toàn ứng dụng
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Định nghĩa Key cho Tên người dùng
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
    }

    // --- READ (Đọc) ---
    // Exposed ra Flow để ViewModel có thể lắng nghe
    val userName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_NAME] ?: "Tên Sinh Viên" // Giá trị mặc định
        }

    // --- WRITE (Ghi) ---
    // Hàm suspend để ghi tên người dùng
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USER_NAME] = name
        }
    }
}