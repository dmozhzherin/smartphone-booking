package dym.interview.routes

import dym.interview.plugins.Context
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.NullBody
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

/**
 * @author dym
 */

fun Route.phonesRoutes() {
    route("/phones") {
        get {
            call.respond(Context.phoneDao.getSmartphones())
        }

        get("/{guid}") {
            call.parameters["guid"]?.let { guid ->
                try {
                    UUID.fromString(guid)
                } catch (e: Exception) {
                    null
                }?.let { uuid ->
                    Context.smartPhoneSpecsService.getUpdatedSmartphone(uuid)?.let { smartphone ->
                        call.respond(smartphone)
                    }
                }
            }?: call.respond(HttpStatusCode.NotFound, NullBody)
        }
    }
}