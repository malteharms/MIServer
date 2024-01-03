package de.malteharms.data.models

import kotlinx.serialization.Serializable


@Serializable
data class CostResultWrapper(
    val items: List<CostItem>,
    val collectedTimestamp: Long
)


@Serializable
data class CostItem(
    val title: String,              // title of payment
    val groupId: String,            // reference to group
    val payedBy: String,            // uuid of person who payed
    val createdBy: String,          // uuid of person who created this item
    val amount: Float,              // cost amount
    val timestamp: Long             // date
)
