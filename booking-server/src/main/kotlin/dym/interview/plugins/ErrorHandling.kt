package dym.interview.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.util.logging.KtorSimpleLogger
import io.ktor.util.logging.error
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState

/**
 * @author dym
 */

fun Application.configureErrorHandling() {
    install(StatusPages) {
        val logger = KtorSimpleLogger("ErrorHandling")

        exception<PSQLException> { call, cause ->
            if (cause.sqlState == PSQLState.UNIQUE_VIOLATION.state) {
                call.respond(HttpStatusCode.Conflict, "Already booked")
            } else {
                logger.error(cause)
                call.respond(HttpStatusCode.InternalServerError, "Unknown error")
            }
        }

        exception<Throwable> { call, cause ->
            logger.error(cause)
            call.respond(HttpStatusCode.InternalServerError, "Unknown error")
        }
    }
}