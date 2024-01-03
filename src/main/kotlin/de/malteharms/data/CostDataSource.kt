package de.malteharms.data

import de.malteharms.data.models.CostItem

interface CostDataSource {

    suspend fun getAllItems(): List<CostItem>
    suspend fun getGroupItems(groupId: String): List<CostItem>
    suspend fun getGroupItems(groupId: String, start: String, end: String): List<CostItem>
    suspend fun insertItem(item: CostItem)

}