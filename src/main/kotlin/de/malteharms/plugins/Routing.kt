package de.malteharms.plugins


import de.malteharms.routes.appSocket
import de.malteharms.routes.getAllItems
import de.malteharms.sessions.SessionController
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val socketController by inject<SessionController>()

    install(Routing) {
        appSocket(socketController)
        getAllItems(socketController)
    }
}
