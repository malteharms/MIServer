package de.malteharms.data

import com.mongodb.client.model.Filters.and
import de.malteharms.data.models.CostItem
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.lt

class CostDataSourceImpl(
    private val db: CoroutineDatabase
): CostDataSource {

    private val items = db.getCollection<CostItem>()

    override suspend fun getAllItems(): List<CostItem> {
        return items.find()
            .descendingSort(CostItem::timestamp)
            .toList()
    }

    override suspend fun getGroupItems(groupId: String): List<CostItem> {
        return items.find(CostItem::groupId eq groupId)
            .descendingSort(CostItem::timestamp)
            .toList()
    }

    override suspend fun getGroupItems(groupId: String, start: String, end: String): List<CostItem> {
        return items
            .find(
                and(
                    CostItem::groupId eq groupId,
                    CostItem::timestamp gte start.toLong(),
                    CostItem::timestamp lt end.toLong()
                )
            )
            .descendingSort(CostItem::timestamp)
            .toList()
    }

    override suspend fun insertItem(item: CostItem) {
        items.insertOne(item)
    }
}