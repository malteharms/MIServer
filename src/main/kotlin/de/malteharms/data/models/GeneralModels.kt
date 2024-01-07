package de.malteharms.data.models

import kotlinx.serialization.Serializable

enum class IncomingMessageType {
    REGISTER,
    LOGIN,
    COST_ADD_ITEM,
    COST_GET_ITEMS,
    ERROR
}

@Serializable
sealed interface MessageData

@Serializable
data class IncomingMessage(
    val type: IncomingMessageType,
    val data: MessageData
)

@Serializable
data class CostItem(
    val title: String,              // title of payment
    val groupId: String,            // reference to group
    val payedBy: String,            // uuid of person who paid
    val createdBy: String,          // uuid of person who created this item
    val amount: Long,              // cost amount
    val timestamp: Long             // date
): MessageData