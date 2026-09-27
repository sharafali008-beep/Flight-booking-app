package com.skybook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [BookingEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class SkyBookDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
}
