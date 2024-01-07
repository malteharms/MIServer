package de.malteharms.data.models

import kotlinx.serialization.Serializable


@Serializable
data class CostResultWrapper(
    val items: List<CostItem>,
    val collectedTimestamp: Long
): MessageData



