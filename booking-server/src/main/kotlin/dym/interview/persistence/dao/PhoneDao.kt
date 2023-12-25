package dym.interview.persistence.dao

import dym.interview.persistence.Datasource
import dym.interview.persistence.dto.Smartphone
import dym.interview.plugins.Context
import java.sql.ResultSet
import java.util.UUID

/**
 * @author dym
 */
class PhoneDao(datasource: Datasource = Context.datasource) : AbstractDao(datasource) {

    fun getSmartphones(): ArrayList<Smartphone> = withConnection { connection ->
        connection.prepareStatement(
            "select bk.active is null as avail, bk.username, bk.name, bk.created_at, ast.* from booking.assets ast " +
                    "left join booking.active_bookings bk on ast.id = bk.asset_id"
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                val result = ArrayList<Smartphone>()
                while (resultSet.next()) {
                    result += resultSet.toSmartphone()
                }
                result
            }
        }
    }

    fun getSmartphone(guid: UUID): Smartphone? = withConnection { connection ->
        connection.prepareStatement(
            "select bk.active is null as avail, bk.username, bk.name, bk.created_at, ast.* from booking.assets ast " +
                    "left join booking.active_bookings bk on ast.id = bk.asset_id where ast.guid = ?"
        ).use { statement ->
            statement.setObject(1, guid)
            statement.executeQuery().use { resultSet ->
                if (resultSet.next()) resultSet.toSmartphone() else null
            }
        }
    }

    /**
     * Updates a smartphone.
     * Returns Smartphone with updated version if successful,
     * null otherwise (if optimistic locking failed, i.e. someone else updated the same smartphone).
     */
    fun updateSmartphone(smartphone: Smartphone): Smartphone? = withTransaction { connection ->
        connection.prepareStatement(
            "update booking.assets set technology = ?, bands2g = ?,  bands3g = ?, bands4g = ?, version = ? " +
                    "where id = ? and version = ?"
        ).use { statement ->
            statement.setString(1, smartphone.technology)
            statement.setString(2, smartphone.bands2g)
            statement.setString(3, smartphone.bands3g)
            statement.setString(4, smartphone.bands4g)
            statement.setInt(5, smartphone.version + 1)
            statement.setLong(6, smartphone.id)
            statement.setInt(7, smartphone.version)
            if (statement.executeUpdate() > 0)
                smartphone.copy(version = smartphone.version + 1) else null
        }
    }

    private fun ResultSet.toSmartphone() = Smartphone(
        id = getLong("id"),
        guid = getString("guid"),
        manufacturer = getString("manufacturer"),
        model = getString("model"),
        technology = getString("technology"),
        bands2g = getString("bands2g"),
        bands3g = getString("bands3g"),
        bands4g = getString("bands4g"),
        version = getInt("version"),
        available = getBoolean("avail"),
        bookedBy = getString("username"),
        bookedByName = getString("name"),
        bookedAt = getTimestamp("created_at")?.let { String.format("%tFT%<tTZ", it) }
    )
}