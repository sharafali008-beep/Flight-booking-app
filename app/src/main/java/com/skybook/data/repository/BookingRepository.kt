package com.skybook.data.repository

import com.skybook.data.local.BookingDao
import com.skybook.data.local.toEntity
import com.skybook.data.local.toModel
import com.skybook.data.model.Booking
import com.skybook.data.model.BookingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface BookingRepository {
    fun observeBookings(): Flow<List<Booking>>
    fun observeBooking(id: String): Flow<Booking?>
    suspend fun save(booking: Booking)
    suspend fun cancel(id: String)
}

@Singleton
class RoomBookingRepository @Inject constructor(
    private val dao: BookingDao,
) : BookingRepository {
    override fun observeBookings(): Flow<List<Booking>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeBooking(id: String): Flow<Booking?> =
        dao.observeById(id).map { it?.toModel() }

    override suspend fun save(booking: Booking) = dao.insert(booking.toEntity())

    override suspend fun cancel(id: String) = dao.updateStatus(id, BookingStatus.CANCELLED)
}
