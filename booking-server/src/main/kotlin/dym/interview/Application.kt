package dym.interview

import dym.interview.plugins.configureErrorHandling
import dym.interview.plugins.configureOpenApi
import dym.interview.plugins.configureRouting
import dym.interview.plugins.configureSecurity
import dym.interview.plugins.configureSerialization
import dym.interview.plugins.createSingletons
import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    createSingletons()
    configureSecurity()
    configureSerialization()
    configureRouting()
    configureOpenApi()
    configureErrorHandling()
}
