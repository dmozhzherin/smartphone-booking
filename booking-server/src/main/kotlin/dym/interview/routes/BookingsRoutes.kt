package dym.interview.routes

import dym.interview.persistence.dto.NewBooking
import dym.interview.plugins.Context
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.NullBody
import io.ktor.server.application.call
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

/**
 * @author dym
 */

fun Route.bookingRoutes() {
    route("/bookings") {
        get {
            call.respond(Context.bookingDao.getActiveBookings())
        }

        get("{id?}") {
            call.parameters["id"]?.let { guid ->
                try {
                    UUID.fromString(guid)
                } catch (e: Exception) {
                    null
                }?.let {uuid ->
                    Context.bookingDao.getActiveBooking(uuid)?.let { booking ->
                        call.respond(booking)
                    }
                }
            } ?: call.respond(HttpStatusCode.NotFound, NullBody)
        }

        authenticate {
            delete("{id?}") {
                call.parameters["id"]?.let { guid ->
                    try {
                        UUID.fromString(guid)
                    } catch (e: Exception) {
                        null
                    }?.let { uuid ->
                        Context.bookingDao.getActiveBooking(uuid)?.let { booking ->
                            context.authentication.principal<UserIdPrincipal>()?.let { principal ->
                                if (principal.name != booking.username) {
                                    call.respond(HttpStatusCode.Forbidden, NullBody)
                                } else {
                                    if (Context.bookingDao.deleteBooking(uuid, principal.name)) {
                                        call.respond(HttpStatusCode.NoContent, NullBody)
                                    } else {
                                        call.respond(HttpStatusCode.NotFound, NullBody)
                                    }
                                }
                            }
                        }
                    }
                } ?: call.respond(HttpStatusCode.NotFound, NullBody)
            }

            post {
                context.authentication.principal<UserIdPrincipal>()?.let { principal ->
                    val bookingReq = call.receive<NewBooking>()
                    Context.bookingDao.createBooking(bookingReq.phoneGuid, principal.name).let {
                        call.respond(HttpStatusCode.Created, it)
                    }
                }
            }
        }

    }
}