package de.malteharms.sessions

import de.malteharms.data.models.Account
import io.ktor.websocket.*

data class Member(
    val account: Account?,
    val currentlyLoggedIn: Boolean,
    val sessionId: String,
    val socket: WebSocketSession
)
