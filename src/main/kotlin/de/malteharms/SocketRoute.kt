package de.malteharms

import de.malteharms.models.CostEntry
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket() {
    route("/costs") {
        webSocket {
            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val message = extractMessage(frame.readText())
                        println(message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

private fun extractMessage(message: String): CostEntry {
    // v#{..}
    val type = message.substringBefore("#")
    val body = message.substringAfter("#")

    return if(type == "new_item") {
        Json.decodeFromString(body)
    } else CostEntry("", "", 0F, "")
}