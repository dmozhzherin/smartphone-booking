package dym.interview.persistence.dao

import dym.interview.persistence.Datasource
import dym.interview.persistence.dto.Booking
import dym.interview.plugins.Context
import java.sql.ResultSet
import java.util.UUID

/**
 * @author dym
 */
class BookingDao(datasource: Datasource = Context.datasource) : AbstractDao(datasource) {

    /**
     * Returns all active bookings.
     * In ArrayList, because kotlinx.serialization doesn't want to serialize List.
     */
    fun getActiveBookings(): ArrayList<Booking> = withConnection { connection ->
        connection.prepareStatement("select * from booking.active_bookings").use { statement ->
            statement.executeQuery().use { resultSet ->
                val result = ArrayList<Booking>()
                while (resultSet.next()) {
                    result += resultSet.toBooking()
                }
                result
            }
        }
    }

    fun getActiveBooking(guid: UUID): Booking? = withConnection { connection ->
        connection.prepareStatement("select * from booking.active_bookings where guid = ?").use { statement ->
            statement.setObject(1, guid)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) resultSet.toBooking() else null
            }
        }
    }

    fun getActiveBooking(id: Long): Booking? = withConnection { connection ->
        connection.prepareStatement("select * from booking.active_bookings where id = ?").use { statement ->
            statement.setLong(1, id)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) resultSet.toBooking() else null
            }
        }
    }

    /**
     * Creates a new booking.
     * This is a lazy implementation - the database will handle data integrity,
     * but in case of a failure it might be difficult to determine the exact cause.
     * SQL State to the rescue!
     */
    fun createBooking(phoneGuid: String, username: String): Booking {
        val id = withTransaction { connection ->
            connection.prepareStatement(
                "insert into booking.bookings (asset_id, user_id) values (" +
                        "(select id from booking.assets where guid = ?)," +
                        "(select id from customer.users where username = ?))", arrayOf("id")
            ).use { statement ->
                statement.setObject(1, UUID.fromString(phoneGuid))
                statement.setString(2, username)
                if (statement.executeUpdate() > 0) {
                    statement.generatedKeys.use { keys ->
                        if (keys.next()) {
                            keys.getLong("id")
                        } else {
                            throw IllegalStateException("No keys were generated")
                        }
                    }
                } else {
                    throw IllegalStateException("No rows were inserted")
                }
            }
        }
        return getActiveBooking(id) ?: throw IllegalStateException("No booking was created")
    }

    /**
     * Marks a booking as inactive.
     */
    fun deleteBooking(guid: UUID, username: String): Boolean {
        return withTransaction { connection ->
            connection.prepareStatement(
                "update booking.bookings set active = false " +
                        "where guid = ? " +
                        "and user_id = (select id from customer.users where username = ?) " +   //access control
                        "and active"  //optimistic locking
            ).use { statement ->
                statement.setObject(1, guid)
                statement.setString(2, username)
                statement.executeUpdate() > 0
            }
        }
    }

    private fun ResultSet.toBooking() = Booking(
        guid = getString("guid"),
        phoneGuid = getString("phone_guid"),
        manufacturer = getString("manufacturer"),
        model = getString("model"),
        username = getString("username"),
        name = getString("name"),
        //this is a serialization shortcut, normally a proper date type should be used
        bookedAt = getTimestamp("created_at").let { String.format("%tFT%<tTZ", it) }
    )
}