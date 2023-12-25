package dym.interview.persistence

import io.ktor.server.config.MapApplicationConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * @author dym
 */
class DatasourceTest {

    @Test
    fun testInit() {
        Datasource(MapApplicationConfig(
            "storage.driverClassName" to "org.h2.Driver",
            "storage.jdbcURL" to "jdbc:h2:mem:test"
        )).pool.use { pool ->
            pool.connection.use {
                it.createStatement().executeUpdate("create table foo (bar varchar(255), baz varchar(255))")
            }

            pool.connection.use {
                it.createStatement().executeUpdate("insert into foo (bar, baz) values ('bar', 'baz')")
            }

            pool.connection.use { connection ->
                connection.createStatement().executeQuery("select * from foo").let {
                    assertTrue(it.next())
                    assertEquals("bar", it.getString("bar"))
                    assertEquals("baz", it.getString("baz"))
                }
            }
        }
    }
}