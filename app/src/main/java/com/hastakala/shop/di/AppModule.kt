package com.hastakala.shop.di

import android.content.Context
import com.hastakala.shop.data.AppDao
import com.hastakala.shop.data.AppDatabase
import com.hastakala.shop.data.ShopRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getInstance(context)

    @Provides
    fun provideDao(database: AppDatabase): AppDao = database.appDao()

    @Provides
    @Singleton
    fun provideRepository(dao: AppDao): ShopRepository = ShopRepository(dao)
}
