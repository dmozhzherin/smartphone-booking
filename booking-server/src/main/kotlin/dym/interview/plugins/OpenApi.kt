package dym.interview.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

/**
 * @author dym
 */

fun Application.configureOpenApi() {
    routing {
        route("/") {
            get {
                call.respondRedirect("/openapi")
            }
        }
        openAPI(path = "/openapi", swaggerFile = "openapi/documentation.yaml")
    }
}