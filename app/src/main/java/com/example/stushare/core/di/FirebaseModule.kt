package com.example.stushare.core.di

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage // 1. Nhớ import thư viện này
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

    // --- 2. THÊM HÀM NÀY ĐỂ SỬA LỖI MISSING BINDING ---
    @Provides
    @Singleton
    fun provideFirebaseStorageInstance(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}