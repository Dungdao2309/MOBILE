package com.example.stushare.core.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestoreInstance(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthInstance(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageInstance(): FirebaseStorage {
        // ⭐️ CẢI TIẾN QUAN TRỌNG:
        // Thay vì dùng getInstance() mặc định, ta truyền trực tiếp URL bucket vào.
        // URL này lấy từ dòng "storage_bucket" trong file google-services.json của bạn.
        // Định dạng chuẩn: gs://<tên-bucket>
        return FirebaseStorage.getInstance("gs://stushare-cf343.firebasestorage.app")
    }
}