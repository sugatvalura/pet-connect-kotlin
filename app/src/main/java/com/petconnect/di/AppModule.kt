package com.petconnect.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.petconnect.data.repository.DeviceLocationRepository
import com.petconnect.data.repository.LocalAuthRepository
import com.petconnect.data.repository.RoomChatRepository
import com.petconnect.data.repository.LocalImageRepository
import com.petconnect.data.repository.LocalListingRepository
import com.petconnect.data.repository.LocalMatchRepository
import com.petconnect.data.repository.LocalNotificationRepository
import com.petconnect.data.repository.LocalPetRepository
import com.petconnect.data.repository.LocalPreferencesRepository
import com.petconnect.data.repository.RemoteFirstAssistantRepository
import com.petconnect.domain.repository.AssistantRepository
import com.petconnect.domain.repository.AuthRepository
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.repository.ImageRepository
import com.petconnect.domain.repository.ListingRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.NotificationRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.repository.SettingsRepository
import com.petconnect.data.repository.DataStoreSettingsRepository
import com.petconnect.domain.util.Dates
import androidx.room.Room
import com.petconnect.data.db.ChatDao
import com.petconnect.data.db.PetConnectDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.petConnectDataStore by preferencesDataStore(name = "petconnect")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.petConnectDataStore

    @Provides @Singleton
    fun json(): Json = Json {
        ignoreUnknownKeys = true // tolerate schema additions
        encodeDefaults = true
        explicitNulls = false
    }

    @Provides @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun dates(): Dates = Dates()
}

/** Room database + DAOs. One database instance for the whole app. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): PetConnectDatabase =
        Room.databaseBuilder(context, PetConnectDatabase::class.java, PetConnectDatabase.NAME).build()

    @Provides
    fun chatDao(db: PetConnectDatabase): ChatDao = db.chatDao()
}

/** Binds domain interfaces to data implementations — the only place they meet. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun auth(impl: LocalAuthRepository): AuthRepository
    @Binds abstract fun pets(impl: LocalPetRepository): PetRepository
    @Binds abstract fun matches(impl: LocalMatchRepository): MatchRepository
    @Binds abstract fun chat(impl: RoomChatRepository): ChatRepository // was LocalChatRepository (DataStore)
    @Binds abstract fun listings(impl: LocalListingRepository): ListingRepository
    @Binds abstract fun notifications(impl: LocalNotificationRepository): NotificationRepository
    @Binds abstract fun location(impl: DeviceLocationRepository): LocationRepository
    @Binds abstract fun preferences(impl: LocalPreferencesRepository): PreferencesRepository
    @Binds abstract fun images(impl: LocalImageRepository): ImageRepository
    @Binds abstract fun assistant(impl: RemoteFirstAssistantRepository): AssistantRepository
    @Binds abstract fun settings(impl: DataStoreSettingsRepository): SettingsRepository
}
