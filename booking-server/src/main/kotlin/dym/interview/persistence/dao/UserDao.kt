package dym.interview.persistence.dao

import dym.interview.persistence.Datasource
import dym.interview.plugins.Context

/**
 * @author dym
 */
class UserDao(datasource: Datasource = Context.datasource) : AbstractDao(datasource) {

    fun validateUser(username: String, password: String): Boolean = withConnection { connection ->
        connection.prepareStatement("SELECT 1 FROM customer.users WHERE username = ? AND password = ?").use {
            it.setString(1, username)
            it.setString(2, password)
            it.executeQuery().use { rs -> rs.next() }
        }
    }
}