package de.malteharms.sessions

import de.malteharms.data.CostDataSource
import de.malteharms.data.models.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import org.litote.kmongo.json
import java.util.concurrent.ConcurrentHashMap

class SessionController(
    private val costDataSource: CostDataSource
) {
    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(
        sessionId: String,
        socket: WebSocketSession
    ) {
        if(members.containsKey(sessionId)) {
            throw MemberAlreadyExistsException()
        }

        members[sessionId] = Member(
            account = null,
            currentlyLoggedIn = false,
            sessionId = sessionId,
            socket = socket
        )
    }

    suspend fun handleRequest(message: JsonObject) {
        val data = message["data"]
        var messageType: IncomingMessageType = IncomingMessageType.COST_ADD_ITEM
        try {
            val tmpType = message["type"]
            val strType = tmpType.toString()
            messageType = IncomingMessageType.valueOf(strType)
        } catch (e: IllegalArgumentException) {
            IncomingMessageType.ERROR
        }

        when (messageType) {
            IncomingMessageType.REGISTER -> {

            }
            IncomingMessageType.LOGIN -> {

            }
            IncomingMessageType.COST_GET_ITEMS -> {
                // TODO: extract session_id to just send the update to that user
                costsBroadcastAllItems()
            }
            IncomingMessageType.COST_ADD_ITEM -> {
                // TODO: check if data is not null
                val decodedData: CostItem = Json.decodeFromJsonElement(data!!)
                costsAddNewItem(decodedData)
                costsBroadcastAllItems()
            }

            IncomingMessageType.ERROR -> {
                // TODO
            }
        }
    }


    private suspend fun costsAddNewItem(item: CostItem) {
        costDataSource.insertItem(item)
    }

    suspend fun costsBroadcastAllItems() {
        val items: List<CostItem> = costDataSource.getAllItems()
        val resultWrapper = Json.encodeToString(
            CostResultWrapper(
                items = items,
                collectedTimestamp = System.currentTimeMillis()
            )
        )

        members.values.forEach { member ->
            member.socket.send(Frame.Text(resultWrapper))
        }
    }

    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()
        if(members.containsKey(username)) {
            members.remove(username)
        }
    }

}