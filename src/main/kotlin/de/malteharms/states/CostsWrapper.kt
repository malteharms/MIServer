package de.malteharms.states

import de.malteharms.data.models.CostItem
import de.malteharms.data.models.CostState
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class CostsWrapper {

    private val state = MutableStateFlow(CostState())

    private val userSockets = ConcurrentHashMap<String, WebSocketSession>()

    private val costsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        state.onEach(::broadcast).launchIn(costsScope)
    }

    fun connectUser(session: WebSocketSession) {
        val user = "Malte"

        if (userSockets.containsKey(user)) {
            return
        }

        state.update {
            if(!userSockets.containsKey(user)) {
                userSockets[user] = session
            }

            it.copy(
                member = it.member + user
            )
        }
    }

    fun disconnectUser(user: String) {
        userSockets.remove(user)
        state.update {
            it.copy(
                member = it.member - user
            )
        }
    }

    private suspend fun broadcast(state: CostState) {
        userSockets.values.forEach{ socket ->
            socket.send(
                Json.encodeToString<CostState>(state)
            )
        }
    }

    fun addItem(item: CostItem) {
        state.update {
            it.copy(
                items = it.items + item
            )
        }
    }








}