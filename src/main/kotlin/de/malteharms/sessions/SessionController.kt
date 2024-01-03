package de.malteharms.sessions

import de.malteharms.data.CostDataSource
import de.malteharms.data.models.CostItem
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

class SessionController(
    private val costDataSource: CostDataSource
) {

    private val members = ConcurrentHashMap<String, Member>()

    fun onJoin(
        username: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        if(members.containsKey(username)) {
            throw MemberAlreadyExistsException()
        }

        members[username] = Member(
            username = username,
            sessionId = sessionId,
            socket = socket
        )
    }

    suspend fun sendItem(item: CostItem) {
        // TODO here should be send the updated cost list which should be displayed

        members.values.forEach { member ->
            val messageEntity = CostItem(
                title = item.title,
                payedBy = item.payedBy,
                amount = item.amount,
                timestamp = item.timestamp
            )
            costDataSource.insertItem(messageEntity)

            val parsedMessage = Json.encodeToString(messageEntity)
            member.socket.send(Frame.Text(parsedMessage))
        }
    }

    suspend fun getAllItems(): List<CostItem> {
        return costDataSource.getAllItems()
    }

    suspend fun tryDisconnect(username: String) {
        members[username]?.socket?.close()
        if(members.containsKey(username)) {
            members.remove(username)
        }
    }

}