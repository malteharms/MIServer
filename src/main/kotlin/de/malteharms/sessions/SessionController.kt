package de.malteharms.sessions

import de.malteharms.data.CostDataSource
import de.malteharms.data.models.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
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
        /*
            An incoming message has the following definition on client side:

                data class *OutgoingMessage(
                    val type: MessageType,
                    val data: Any
                )

            Because we cannot access the class name and therefor don't know how data is looking like,
            we have to analyse the json representation of MessageType, to then parse the data object
            into our needed object type.
         */

        if(message["data"] == null) { return }
        val data: JsonElement = message["data"]!!

        val messageType: MessageType =  try {
            MessageType.valueOf(message["type"].toString().trim('\"'))
        } catch (e: IllegalArgumentException) {
            MessageType.ERROR
        }

        when (messageType) {
            MessageType.REGISTER -> {

            }
            MessageType.LOGIN -> {

            }
            MessageType.COST_GET_ITEMS -> {
                // TODO: extract session_id to just send the update to that user
                costsBroadcastAllItems()
            }
            MessageType.COST_ADD_ITEM -> {
                val decodedData: CostItem = Json.decodeFromJsonElement(data)
                costsAddNewItem(decodedData)
                costsBroadcastAllItems()
            }

            MessageType.ERROR -> {
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