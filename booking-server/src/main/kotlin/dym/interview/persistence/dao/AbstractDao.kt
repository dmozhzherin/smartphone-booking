package dym.interview.persistence.dao

import dym.interview.persistence.Datasource
import java.sql.Connection

/**
 * Common functions for database access.
 * @author dym
 */
abstract class AbstractDao(val datasource: Datasource) {

    protected fun <R> withConnection(block: (connection: Connection) -> R): R {
        return datasource.pool.connection.use {
            block(it)
        }
    }

    protected fun <R> withTransaction(block: (connection: Connection) -> R): R =
        withConnection { connection ->
            connection.autoCommit = false
            try {
                block(connection).also { connection.commit() }
            } catch (e: Exception) {
                connection.rollback()
                throw e
            } finally {
                connection.autoCommit = true
            }
        }

}