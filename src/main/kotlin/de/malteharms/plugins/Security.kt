package de.malteharms.plugins

import de.malteharms.sessions.AppSession
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<AppSession>("SESSION")
    }

    intercept(Plugins) {
        if(call.sessions.get<AppSession>() == null) {
            call.sessions.set(AppSession(generateSessionId()))
        }
    }
}