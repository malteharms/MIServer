package de.malteharms.plugins

import de.malteharms.socket
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        socket()
    }
}
