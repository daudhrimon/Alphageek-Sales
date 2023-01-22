package com.gdm.alphageeksales.di

import android.content.Context
import androidx.room.Room
import com.gdm.alphageeksales.api.ApiService
import com.gdm.alphageeksales.repository.AppRepository
import com.gdm.alphageeksales.database.AppDao
import com.gdm.alphageeksales.database.AppDatabase
import com.gdm.alphageeksales.utils.SharedPref
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context):AppDatabase =
        Room.databaseBuilder(context,AppDatabase::class.java,"AppDatabase")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun providesPostDao(appDatabase: AppDatabase):AppDao =
        appDatabase.getAppDao()

    @Provides
    fun providesMainRepository(appDao: AppDao,apiService: ApiService): AppRepository =
        AppRepository(appDao,apiService)

    @Provides
    fun providesBaseUrl():String = "http://64.225.72.133/gdm/api/"

    @Provides
    @Singleton
    fun providesRetrofitBuilder(baseUrl: String) : Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.MINUTES)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()

                requestBuilder.header(
                    "authorization",
                    "Bearer ${SharedPref.getJWTToken()}"
                )
                chain.proceed(requestBuilder.build())
            }.build())
            .build()

    @Provides
    fun providesApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)
}