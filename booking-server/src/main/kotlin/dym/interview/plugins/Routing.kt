package dym.interview.plugins

import dym.interview.routes.bookingRoutes
import dym.interview.routes.phonesRoutes
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        bookingRoutes()
        phonesRoutes()
    }
}
