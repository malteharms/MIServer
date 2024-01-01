package de.malteharms.plugins

import de.malteharms.socket
import de.malteharms.states.CostsWrapper
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    costState: CostsWrapper
) {
    routing {
        socket(costState)
    }
}
