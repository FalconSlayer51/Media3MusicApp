package com.example.spotify_clone.di

import android.app.Application
import android.content.Context
import com.example.spotify_clone.data.repository.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideMusicRepository(@ApplicationContext context: Context): MusicRepository {
        return MusicRepository(context)
    }
}