package de.malteharms

import de.malteharms.models.CostEntry
import de.malteharms.states.CostsWrapper
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json

fun Route.socket(state: CostsWrapper) {
    route("/costs") {
        webSocket {
            state.connectUser(this)

            try {
                incoming.consumeEach { frame ->
                    if(frame is Frame.Text) {
                        val item = extractMessage(frame.readText())
                        state.addItem(item)
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

    return if(type == "add_item") {
        Json.decodeFromString(body)
    } else CostEntry("", "", 0F, "")
}