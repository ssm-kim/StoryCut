package com.ssafy.storycut.di

import android.content.Context
import com.ssafy.storycut.BuildConfig
import com.ssafy.storycut.data.api.service.AuthApiService
import com.ssafy.storycut.data.api.service.RoomApiService
import com.ssafy.storycut.data.api.service.VideoApiService
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.AuthRepository
import com.ssafy.storycut.data.repository.GoogleAuthService
import com.ssafy.storycut.util.network.AuthInterceptor // AuthInterceptor import는 유지
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideGoogleAuthService(): GoogleAuthService {
        return GoogleAuthService()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        tokenManager: TokenManager,
        @ApplicationContext context: Context
    ): AuthRepository {
        return AuthRepository(tokenManager, context)
    }

    // AuthInterceptor를 생성하는 Provides 메서드 추가
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        authRepository: AuthRepository,
        @ApplicationContext context: Context // Context 파라미터 추가
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, authRepository, context)
    }

    @Provides
    @Singleton
    // Hilt가 AuthInterceptor를 자동으로 주입해 줍니다.
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // Hilt가 제공한 AuthInterceptor 인스턴스 사용
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideVideoApiService(retrofit: Retrofit): VideoApiService {
        return retrofit.create(VideoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRoomApiService(retrofit: Retrofit): RoomApiService {
        return retrofit.create(RoomApiService::class.java)
    }
}