package dym.interview.persistence.dao

import dym.interview.DatabaseTest
import dym.interview.plugins.Context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.Test

/**
 * @author dym
 */
class BookingDaoTest: DatabaseTest() {

    private val bookingDao: BookingDao by lazy { BookingDao() }
    private val phoneDao: PhoneDao by lazy { PhoneDao() }

    @BeforeEach
    fun setUp() {
        Context.datasource.pool.connection.use {
            it.createStatement().executeUpdate("delete from booking.bookings")
        }
    }

    @Test
    fun endToEndTest() {
        assertTrue(bookingDao.getActiveBookings().isEmpty())

        val phone = phoneDao.getSmartphones().let {
            assertTrue(it.isNotEmpty())     //just in case
            it.first() //it is auto-generated, so take it from the DB
        }

        val booking = bookingDao.createBooking(phone.guid, "test").let { booking ->
            assertEquals(phone.guid, booking.phoneGuid)
            assertEquals("test", booking.username)
            assertEquals("Test User", booking.name)
            assertNotNull(booking.guid)
            assertNotNull(booking.manufacturer)
            assertNotNull(booking.model)
            assertNotNull(booking.bookedAt)
            booking
        }

        //check that the booking is active and the phone is not available
        bookingDao.getActiveBookings().let {
            assertEquals(1, it.size)
            assertEquals(phone.guid, it.first().phoneGuid)
        }

        assertFalse(phoneDao.getSmartphones().first { it.guid == phone.guid }.available)

        //delete the booking
        assertTrue(bookingDao.deleteBooking(UUID.fromString(booking.guid), "test"))

        //check that the booking is not active and the phone is available
        assertTrue(bookingDao.getActiveBookings().isEmpty())

        assertTrue(phoneDao.getSmartphones().first { it.guid == phone.guid }.available)
    }

    @Test
    fun failBookingWhenAlreadyBooked() {
        val phone = phoneDao.getSmartphones().first()

        assertNotNull(bookingDao.createBooking(phone.guid, "test").guid)

        //try to book the same phone again
        assertThrows<Exception> { bookingDao.createBooking(phone.guid, "test")}

    }

    @Test
    fun failBookingWithNonExistingUser() {
        val phone = phoneDao.getSmartphones().first()
        assertThrows<Exception> { bookingDao.createBooking(phone.guid, "non-existing") }
    }

    @Test
    fun failToBookWithNonExistingPhone() {
        assertThrows<Exception> { bookingDao.createBooking("non-existing", "test") }
    }

    @Test
    fun failToDeleteWhenNotBooked() {
        val phone = phoneDao.getSmartphones().first()

        //try to delete non-existing booking
        assertFalse(bookingDao.deleteBooking(UUID.randomUUID(), "test"))

        //try to delete non-active booking
        val booking = bookingDao.createBooking(phone.guid, "test").let { booking ->
            assertNotNull(booking.guid)
            booking
        }
        assertTrue(bookingDao.deleteBooking(UUID.fromString(booking.guid), "test"))
        assertFalse(bookingDao.deleteBooking(UUID.fromString(booking.guid), "test"))
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            initDatasource()
        }
    }
}

