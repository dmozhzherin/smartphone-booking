package dym.interview.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig

private const val DEFAULT_POOL_SIZE = 5

/**
 * @author dym
 */
class Datasource(config: ApplicationConfig) {

    val pool: HikariDataSource

    init {
        val driverClassName = config.property("storage.driverClassName").getString()
        val jdbcURL = config.property("storage.jdbcURL").getString()
        pool = createHikariDataSource(url = jdbcURL, driver = driverClassName)
    }

    private fun createHikariDataSource(
        url: String,
        driver: String
    ) = HikariDataSource(HikariConfig().apply {
        driverClassName = driver
        jdbcUrl = url
        maximumPoolSize = DEFAULT_POOL_SIZE
        isAutoCommit = true
        transactionIsolation = "TRANSACTION_READ_COMMITTED" // default for PostgreSQL
        validate()
    })
}