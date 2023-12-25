package dym.interview.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic

fun Application.configureSecurity() {
    install(Authentication) {
        basic() {
            realm = "Ktor Server"
            validate { credentials ->
                // Very reliable, very secure. Do not use in production.
                if (Context.userDao.validateUser(credentials.name, credentials.password)) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}
