package com.example.stushare.core.di

import android.content.Context
import androidx.room.Room
import com.example.stushare.core.data.db.AppDatabase
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.NotificationDao // ⭐️ IMPORT
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.repository.* // Import hết repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // ⭐️ IMPORT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

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

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    // ⭐️ 1. CUNG CẤP NOTIFICATION DAO
    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    // ⭐️ 2. CẬP NHẬT: THÊM FIREBASE CHO DOCUMENT REPO (QUAN TRỌNG)
    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        apiService: ApiService,
        settingsRepository: SettingsRepository,
        // Inject thêm 2 cái này:
        storage: FirebaseStorage,
        firestore: FirebaseFirestore
    ): DocumentRepository {
        // Truyền đủ 5 tham số cho Impl
        return DocumentRepositoryImpl(documentDao, apiService, settingsRepository, storage, firestore)
    }

    // ⭐️ 3. CUNG CẤP NOTIFICATION REPOSITORY
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao
    ): NotificationRepository {
        return NotificationRepositoryImpl(notificationDao)
    }

    @Provides
    @Singleton
    fun provideRequestRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): RequestRepository {
        return RequestRepositoryImpl(firestore, firebaseAuth)
    }
}