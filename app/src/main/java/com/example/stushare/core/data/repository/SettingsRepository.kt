// File: core/data/repository/SettingsRepository.kt
// (Phiên bản hoàn chỉnh đã cập nhật Caching VÀ Recent Searches)

package com.example.stushare.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
// ⭐️ IMPORT THÊM stringSetPreferencesKey ⭐️
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    // Định nghĩa Keys (bao gồm tất cả keys mới)
    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val LAST_REFRESH_TIMESTAMP = longPreferencesKey("last_refresh_timestamp")
        // ⭐️ KEY MỚI CHO TÌM KIẾM GẦN ĐÂY ⭐️
        val RECENT_SEARCHES = stringSetPreferencesKey("recent_searches")
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

    // --- REFRESH CACHING (Cải tiến 1 - Giữ nguyên) ---
    val lastRefreshTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_REFRESH_TIMESTAMP] ?: 0L
        }

    suspend fun updateLastRefreshTimestamp() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_REFRESH_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    // --- ⭐️ LOGIC MỚI CHO TÌM KIẾM GẦN ĐÂY (Cải tiến 3) ⭐️ ---

    /**
     * Lấy danh sách các từ khóa đã lưu, trả về tối đa 5 từ.
     */
    val recentSearches: Flow<List<String>> = context.dataStore.data
        .map { preferences ->
            // Đọc Set<String> từ DataStore, nếu rỗng thì tạo Set rỗng
            val searchesSet = preferences[PreferencesKeys.RECENT_SEARCHES] ?: emptySet()
            // Chuyển Set thành List và đảo ngược (để từ mới nhất lên đầu)
            searchesSet.toList().reversed()
        }

    /**
     * Thêm một từ khóa mới vào danh sách đã lưu.
     * Chỉ giữ lại 5 từ khóa mới nhất.
     */
    suspend fun addRecentSearch(query: String) {
        context.dataStore.edit { preferences ->
            // 1. Lấy danh sách cũ (hoặc tạo mới)
            val oldSet = preferences[PreferencesKeys.RECENT_SEARCHES] ?: emptySet()

            // 2. Tạo danh sách mới, xóa từ khóa cũ nếu trùng, và thêm từ mới
            val newSet = oldSet.toMutableSet()
            newSet.remove(query) // Xóa query cũ (nếu có)
            newSet.add(query)    // Thêm query mới (vào cuối)

            // 3. Giới hạn chỉ lưu 5 từ khóa (lấy 5 từ cuối cùng)
            val tempList = newSet.toList()
            val limitedList = if (tempList.size > 5) {
                tempList.subList(tempList.size - 5, tempList.size)
            } else {
                tempList
            }
            val finalSet = limitedList.toSet()

            // 4. Lưu lại vào DataStore
            preferences[PreferencesKeys.RECENT_SEARCHES] = finalSet
        }
    }

    /**
     * (TÙY CHỌN - Bổ sung) Xóa lịch sử tìm kiếm.
     */
    suspend fun clearRecentSearches() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RECENT_SEARCHES] = emptySet()
        }
    }
}