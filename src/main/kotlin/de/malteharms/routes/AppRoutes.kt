package de.malteharms.routes

import de.malteharms.data.models.CostItem
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

fun Route.appSocket(sessionController: SessionController) {
    webSocket("/app") {
        val session = call.sessions.get<AppSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "no session!"))
            return@webSocket
        }

        try {
            sessionController.onJoin(
                username = session.username,
                sessionId = session.sessionId,
                socket = this
            )

            incoming.consumeEach { frame ->
                if(frame is Frame.Text) {
                    sessionController.sendItem(
                        item = Json.decodeFromString(frame.readText())

                    )
                }
            }

        } catch (e: MemberAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            sessionController.tryDisconnect(session.username)
        }
    }
}

fun Route.getAllItems(sessionController: SessionController) {
    get("/costItems") {
        call.respond(
            HttpStatusCode.OK,
            sessionController.getAllItems()
        )
    }
}
