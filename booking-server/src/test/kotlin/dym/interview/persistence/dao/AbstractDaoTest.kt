package dym.interview.persistence.dao

import dym.interview.persistence.Datasource
import io.ktor.server.config.MapApplicationConfig
import org.h2.Driver
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @author dym
 */
class AbstractDaoTest {

    private val subject = object : AbstractDao(datasource) {
        fun <R> wConnection(block: (connection: java.sql.Connection) -> R) {
            withConnection (block)
        }

        fun <R> wTransaction(block: (connection: java.sql.Connection) -> R) {
            withTransaction (block)
        }
    }

    @Test
    fun testWithConnection() {
        subject.wConnection {connection ->
            connection.createStatement().executeUpdate("insert into foo (bar, baz) values ('bar', 'baz')")
        }

        subject.wConnection {connection ->
            connection.createStatement().executeQuery("select * from foo").let {
                assertTrue(it.next())
                assertEquals("bar", it.getString("bar"))
                assertEquals("baz", it.getString("baz"))
            }
        }

        subject.wConnection {connection ->
            connection.createStatement().executeUpdate("delete from foo")
        }

        subject.wConnection {connection ->
            connection.createStatement().executeQuery("select * from foo").let {
                assertFalse(it.next())
            }
        }
    }

    @BeforeEach
    fun setUp() {
        datasource.pool.connection.use {
            it.createStatement().executeUpdate("delete from foo")
        }
    }

    @Test
    fun withTransaction() {
        subject.wTransaction {connection ->
            connection.createStatement().executeUpdate("insert into foo (bar, baz) values ('bar', 'baz')")
        }

        subject.wTransaction {connection ->
            connection.createStatement().executeQuery("select * from foo").let {
                assertTrue(it.next())
                assertEquals("bar", it.getString("bar"))
                assertEquals("baz", it.getString("baz"))
            }
        }

        assertThrows(Exception::class.java) {
            subject.wTransaction { connection ->
                connection.createStatement().executeUpdate("insert into foo (bar, baz) values ('bar1', 'baz1')")
                connection.createStatement().executeUpdate("insert into foo (bar, baz) values ('bar2', 'baz2')")

                throw Exception("rollback")
            }
        }

        subject.wTransaction {connection ->
            connection.createStatement().executeQuery("select * from foo").let {
                assertTrue(it.next())
                assertEquals("bar", it.getString("bar"))
                assertEquals("baz", it.getString("baz"))

                assertFalse(it.next())
            }
        }
    }

    companion object {

        lateinit var datasource: Datasource

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            datasource = Datasource(
                MapApplicationConfig(
                    "storage.driverClassName" to Driver::class.java.name,
                    "storage.jdbcURL" to "jdbc:h2:mem:test"
                )
            ).apply {

                pool.connection.use {
                    it.createStatement().executeUpdate(
                        """
                        CREATE TABLE foo (
                            id SERIAL PRIMARY KEY,
                            bar VARCHAR(255) NOT NULL UNIQUE,
                            baz VARCHAR(255) NOT NULL
                        );
                    """.trimIndent()
                    )
                }
            }
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            datasource.pool.close()
        }
    }
}