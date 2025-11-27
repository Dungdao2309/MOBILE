package com.example.stushare.core.di

import android.content.Context
import androidx.room.Room
import com.example.stushare.core.data.db.AppDatabase
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.NotificationDao
import com.example.stushare.core.data.db.UserDao // ⭐️ Import mới
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
            .fallbackToDestructiveMigration() // Tự động xóa DB cũ nếu tăng version
            .build()
    }

    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    // ⭐️ 1. THÊM: Cung cấp UserDao
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        apiService: ApiService,
        settingsRepository: SettingsRepository,
        storage: FirebaseStorage,
        firestore: FirebaseFirestore
    ): DocumentRepository {
        return DocumentRepositoryImpl(documentDao, apiService, settingsRepository, storage, firestore)
    }

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

    // ⭐️ 2. THÊM: Cung cấp LeaderboardRepository
    @Provides
    @Singleton
    fun provideLeaderboardRepository(
        userDao: UserDao,
        documentDao: DocumentDao,
        firestore: FirebaseFirestore
    ): LeaderboardRepository {
        return LeaderboardRepositoryImpl(userDao, documentDao, firestore)
    }
}