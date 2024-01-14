package de.malteharms.sessions

import de.malteharms.data.DataSource
import de.malteharms.data.models.*
import io.ktor.websocket.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap

class SessionController(
    private val db: DataSource
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

    suspend fun handleRequest(
        message: JsonObject,
        sessionId: String
    ) {
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
            MessageType.REGISTER -> {           // Incoming data is from type Registration
                try {
                    val decodedData: Registration =             // decode to CostItem data class
                        Json.decodeFromJsonElement(data)

                    if (!db.addAccount(username = decodedData.username, password = decodedData.passwordHash)) {
                        // TODO: Improve the error handling
                        return
                    }

                } catch (e: SerializationException) {
                    println("Could not decode from Json because 'data' is not a json string")
                } catch (e: IllegalArgumentException) {
                    println("Could not decode from Json because 'data' is not a representation of CostItem")
                }
            }

            MessageType.LOGIN -> {
                try {
                    val decodedData: Login =             // decode to CostItem data class
                        Json.decodeFromJsonElement(data)

                    if (!db.confirmCredentials(uuid = decodedData.uuid, password = decodedData.passwordHash)) {
                        // TODO: send response, that the user could not be confirmed
                        return
                    }
                    // TODO: send response, that the credentials are correct and the user can access his data

                } catch (e: SerializationException) {
                    println("Could not decode from Json because 'data' is not a json string")
                } catch (e: IllegalArgumentException) {
                    println("Could not decode from Json because 'data' is not a representation of CostItem")
                }
            }

            // the user wants to get an update of all cost items
            MessageType.COST_GET_ITEMS -> {
                if (members.containsKey(sessionId)) {
                    // send update to the requested person
                    costsSendUpdate(member = members[sessionId]!!)
                } else println("Could not send update to $sessionId, because user is not online")
            }

            // the user want to add an cost item
            MessageType.COST_ADD_ITEM -> {                  // incoming data is from type CostItem
                try {
                    val decodedData: CostItem =             // decode to CostItem data class
                        Json.decodeFromJsonElement(data)

                    db.insertItem(item = decodedData)  // save the item inside database
                    costsBroadcastAllItems()                // broadcast update to all online users

                } catch (e: SerializationException) {
                    println("Could not decode from Json because 'data' is not a json string")
                } catch (e: IllegalArgumentException) {
                    println("Could not decode from Json because 'data' is not a representation of CostItem")
                }

            }

            MessageType.ERROR -> {
                println("Could not map the received MessageType: $messageType")
                return
            }
        }
    }

    suspend fun costsBroadcastAllItems() {
        val resultWrapper: String = Json.encodeToString(
            CostResultWrapper(
                items = db.getAllItems(),
                collectedTimestamp = System.currentTimeMillis()
            )
        )

        members.values.forEach { member: Member ->
            member.socket.send(Frame.Text(resultWrapper))
        }
    }

    private suspend fun costsSendUpdate(member: Member) {
        val resultWrapper: String = Json.encodeToString(
            CostResultWrapper(
                items = db.getAllItems(),
                collectedTimestamp = System.currentTimeMillis()
            )
        )

        member.socket.send(Frame.Text(resultWrapper))
    }

    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()
        if(members.containsKey(username)) {
            members.remove(username)
        }
    }

}