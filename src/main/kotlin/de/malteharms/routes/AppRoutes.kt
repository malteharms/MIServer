package de.malteharms.routes

import de.malteharms.sessions.AppSession
import de.malteharms.sessions.MemberAlreadyExistsException
import de.malteharms.sessions.SessionController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

fun Route.appSocket(sessionController: SessionController) {
    webSocket("/app") {
        val session: AppSession? = call.sessions.get<AppSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "no session!"))
            return@webSocket
        }

        try {
            sessionController.onJoin(
                sessionId = session.sessionId,
                socket = this
            )

            incoming.consumeEach { frame ->
                if(frame is Frame.Text) {
                    try {
                        val jsonRequest: JsonObject = Json.decodeFromString(frame.readText())
                        sessionController.handleRequest(
                            message = jsonRequest,
                            sessionId = session.sessionId
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        } catch (e: MemberAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            sessionController.tryDisconnect(session.sessionId)
        }
    }


}

fun Route.getAllItems(sessionController: SessionController) {
    get("/costItems") {
        call.respond(
            HttpStatusCode.OK,
            sessionController.costsBroadcastAllItems()
        )
    }
}
