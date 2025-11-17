package com.example.stushare.core.di

import android.content.Context
import androidx.room.Room
import com.example.stushare.core.data.db.AppDatabase
import com.example.stushare.core.data.db.DocumentDao
// ⭐️ XÓA: import com.example.stushare.core.data.db.RequestDao
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.repository.DocumentRepositoryImpl
import com.example.stushare.core.data.repository.RequestRepository
import com.example.stushare.core.data.repository.RequestRepositoryImpl
import com.example.stushare.core.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
// ⭐️ IMPORT THÊM:
import com.google.firebase.firestore.FirebaseFirestore

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. Cung cấp AppDatabase (Chỉ còn Document)
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "stushare_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // 2. Cung cấp DocumentDao (Giữ nguyên)
    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    // ⭐️ HÀM provideRequestDao() ĐÃ BỊ XÓA ⭐️

    // 3. Cung cấp DocumentRepository (Giữ nguyên)
    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        apiService: ApiService,
        settingsRepository: SettingsRepository
    ): DocumentRepository {
        return DocumentRepositoryImpl(documentDao, apiService, settingsRepository)
    }

    // 4. Cung cấp RequestRepository (⭐️ ĐÃ CẬP NHẬT ⭐️)
    @Provides
    @Singleton
    fun provideRequestRepository(
        // ⭐️ THAY ĐỔI: Inject Firestore (từ FirebaseModule)
        firestore: FirebaseFirestore
        // ⭐️ XÓA: requestDao: RequestDao
        // ⭐️ XÓA: apiService: ApiService
    ): RequestRepository {
        // ⭐️ THAY ĐỔI: Trả về Impl mới
        return RequestRepositoryImpl(firestore)
    }
}