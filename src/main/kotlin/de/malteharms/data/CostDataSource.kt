package de.malteharms.data

import de.malteharms.data.models.CostItem

interface CostDataSource {

    suspend fun getAllItems(): List<CostItem>
    suspend fun getAllMember(): List<String>
    suspend fun insertItem(item: CostItem)

}