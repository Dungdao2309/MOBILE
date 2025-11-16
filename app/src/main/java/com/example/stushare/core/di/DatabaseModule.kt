package com.example.stushare.core.di

import android.content.Context
import androidx.room.Room
import com.example.stushare.core.data.db.AppDatabase
import com.example.stushare.core.data.db.DocumentDao
import com.example.stushare.core.data.db.RequestDao
// Import các lớp cần thiết
import com.example.stushare.core.data.network.models.ApiService
import com.example.stushare.core.data.repository.DocumentRepository
import com.example.stushare.core.data.repository.DocumentRepositoryImpl
import com.example.stushare.core.data.repository.RequestRepository
import com.example.stushare.core.data.repository.RequestRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. Cung cấp AppDatabase Singleton
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext, // Dùng applicationContext
            AppDatabase::class.java,
            "stushare_database" // Tên database
        )
            .fallbackToDestructiveMigration() // Cần thiết khi tăng version
            .build()
    }

    // 2. Cung cấp các DAO (Hilt sẽ tự động lấy AppDatabase từ hàm trên)
    @Provides
    fun provideDocumentDao(database: AppDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideRequestDao(database: AppDatabase): RequestDao {
        return database.requestDao()
    }

    // 3. BỔ SUNG: Cung cấp DocumentRepository
    // Hilt sẽ tự lấy DocumentDao (từ hàm 2) và ApiService (từ NetworkModule)
    @Provides
    @Singleton
    fun provideDocumentRepository(
        documentDao: DocumentDao,
        apiService: ApiService
    ): DocumentRepository {
        return DocumentRepositoryImpl(documentDao, apiService)
    }

    // 4. BỔ SUNG: Cung cấp RequestRepository (Đây là hàm giải quyết lỗi của bạn)
    @Provides
    @Singleton
    fun provideRequestRepository(
        requestDao: RequestDao,
        apiService: ApiService
    ): RequestRepository {
        return RequestRepositoryImpl(requestDao, apiService)
    }
}