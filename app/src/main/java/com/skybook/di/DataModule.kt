package com.skybook.di

import android.content.Context
import androidx.room.Room
import com.skybook.data.local.BookingDao
import com.skybook.data.local.SkyBookDatabase
import com.skybook.data.repository.BookingRepository
import com.skybook.data.repository.FlightRepository
import com.skybook.data.repository.MockFlightRepository
import com.skybook.data.repository.RoomBookingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Tells Hilt how to create the database. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SkyBookDatabase =
        Room.databaseBuilder(context, SkyBookDatabase::class.java, "skybook.db").build()

    @Provides
    fun provideBookingDao(db: SkyBookDatabase): BookingDao = db.bookingDao()
}

/** Tells Hilt which implementation to use for each repository interface. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindFlightRepository(impl: MockFlightRepository): FlightRepository

    @Binds
    abstract fun bindBookingRepository(impl: RoomBookingRepository): BookingRepository
}
