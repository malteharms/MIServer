package de.malteharms.data

import de.malteharms.data.models.CostItem
import org.litote.kmongo.coroutine.CoroutineDatabase

class CostDataSourceImpl(
    private val db: CoroutineDatabase
): CostDataSource {

    private val items = db.getCollection<CostItem>()

    override suspend fun getAllItems(): List<CostItem> {
        return items.find()
            .descendingSort(CostItem::timestamp)
            .toList()
    }

    override suspend fun getAllMember(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun insertItem(item: CostItem) {
        items.insertOne(item)
    }


}