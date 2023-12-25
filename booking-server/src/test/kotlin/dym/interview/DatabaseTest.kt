package dym.interview

import dym.interview.persistence.Datasource
import dym.interview.plugins.Context
import io.ktor.server.config.MapApplicationConfig
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.postgresql.Driver
import org.testcontainers.containers.PostgreSQLContainer

/**
 * @author dym
 */
open class DatabaseTest {

    companion object {
        private val postgresContainer = PostgreSQLContainer("postgres:16.1-alpine")

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            postgresContainer.portBindings = listOf("2345:5432")
            postgresContainer.addEnv("POSTGRES_USER", "test")
            postgresContainer.addEnv("POSTGRES_PASSWORD", "test")
            postgresContainer.addEnv("POSTGRES_DB", "test")
            postgresContainer.withInitScript("initdb.sql").start()
        }

        @JvmStatic
        @AfterAll
        fun afterAll() {
            postgresContainer.close()
        }

        /**
         * Initiate the datasource singleton with the test container's connection details bypassing the config
         */
        @JvmStatic
        protected fun initDatasource() {
            Context.datasource = Datasource(
                MapApplicationConfig(
                    "storage.driverClassName" to Driver::class.java.name,
                    "storage.jdbcURL" to "jdbc:postgresql://localhost:2345/test?user=test&password=test"
                )
            )
        }
    }
}