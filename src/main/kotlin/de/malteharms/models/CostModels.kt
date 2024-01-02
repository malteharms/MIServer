package de.malteharms.models

import kotlinx.serialization.Serializable

@Serializable
data class CostState(
    val member: List<String> = listOf(),            // list of uuid of each person
    val items: List<CostEntry> = listOf()           // all items provided by each person
)

@Serializable
data class CostEntry(
    val title: String,              // title of payment
    val payedBy: String,            // uuid of person who payed
    val amount: Float,              // cost amount
    val timestamp: String           // date
)
