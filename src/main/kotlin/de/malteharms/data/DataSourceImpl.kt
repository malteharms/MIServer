package de.malteharms.data

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import de.malteharms.data.models.Account
import de.malteharms.data.models.CostItem
import de.malteharms.data.models.Credentials
import de.malteharms.data.models.Group
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.lt
import java.util.UUID

class DataSourceImpl(
    db: CoroutineDatabase
): DataSource {

    private val accounts: CoroutineCollection<Account> = db.getCollection<Account>()
    private val groups: CoroutineCollection<Group> = db.getCollection<Group>()
    private val credentials: CoroutineCollection<Credentials> = db.getCollection<Credentials>()

    private val costItems: CoroutineCollection<CostItem> = db.getCollection<CostItem>()


    // ======= ACCOUNT =======

    override suspend fun addAccount(username: String, password: String): Boolean {
        // generate a uuid
        var newUuid: String = UUID.randomUUID().toString()
        while (getAccount(newUuid) != null) {
            // generate a new uuid if any account has already this uuid
            newUuid = UUID.randomUUID().toString()
        }

        try {
            // insert account into database
            accounts.insertOne(
                Account(
                    uuid = newUuid,
                    username = username,
                    groups = listOf()
                )
            )
            return true
        } catch (e: Exception) {
            println("Could not insert account, because database is not available")
            return false
        }
    }

    override suspend fun confirmCredentials(uuid: String, password: String): Boolean {
        val account: Account? = getAccount(uuid = uuid)

        if (account == null) {
            println("Could not conform user because the uuid $uuid does not exist")
            return false
        }

        val passwordStored: String? = getPassword(creatorUuid = uuid)
        if (passwordStored == null) {
            println("Could not confirm account because credentials of uuid $uuid are not stored")
            return false
        }

        return passwordStored == password
    }


    override suspend fun getAccount(uuid: String): Account? {
        val accountList: List<Account> = accounts
            .find(Account::uuid eq uuid)
            .toList()

        if (accountList.size > 1) {
            println("Got more then more account for uuid $uuid from database! Abort!")
            return null
        }

        if (accountList.isEmpty()) {
            println("Found no account for uuid $uuid in database")
            return null
        }

        return accountList[0]
    }

    override suspend fun deleteAccount(uuid: String): Boolean {
        try {
            accounts.deleteOne(Account::uuid eq uuid)
            return true
        } catch (e: Exception) {
            println("Could not delete Account because database is not available")
            return false
        }
    }

    override suspend fun getPassword(creatorUuid: String): String? {
        val credentials: List<Credentials> =
            credentials
                .find(Credentials::uuid eq creatorUuid)
                .toList()

        if (credentials.size > 1) {
            println("Got more then more account for uuid $creatorUuid from database! Abort!")
            return null
        }

        if (credentials.isEmpty()) {
            println("Found no account for uuid $creatorUuid in database")
            return null
        }

        return credentials[0].passwordHash
    }

    override suspend fun addGroup(uuid: String, name: String): Boolean {
        // generate a uuid
        var newUuid: String = UUID.randomUUID().toString()
        while (getAccount(newUuid) != null) {
            // generate a new uuid if any account has already this uuid
            newUuid = UUID.randomUUID().toString()
        }

        try {
            // insert account into database
            groups.insertOne(
                Group(
                    id = newUuid,
                    name = name,
                    creator = uuid,
                    member = listOf(uuid)
                )
            )
            return true
        } catch (e: Exception) {
            println("Could not insert account, because database is not available")
            return false
        }
    }

    override suspend fun getGroup(groupId: String): Group? {
        val groupList: List<Group> = groups
            .find(Group::id eq groupId)
            .toList()

        if (groupList.size > 1) {
            println("Got more then more account for uuid $groupId from database! Abort!")
            return null
        }

        if (groupList.isEmpty()) {
            println("Found no account for uuid $groupId in database")
            return null
        }

        return groupList[0]
    }

    override suspend fun addMemberToGroup(groupId: String, uuidOfNewMember: String): Boolean {
        val group: Group? = getGroup(groupId = groupId)

        if (group == null) {
            println("Could not add member to group because the groupId $groupId does not exist")
            return false
        }

        if (group.member.contains(uuidOfNewMember)) {
            println("Could not add member to group because member $uuidOfNewMember is already in group $groupId")
            return false
        }

        val newMemberList = group.member + uuidOfNewMember

        val dbFilter = Filters.eq("id", groupId)
        val dbUpdate = Updates.push(Group::member.name, newMemberList)
        val dbOptions = FindOneAndUpdateOptions()
            .returnDocument(ReturnDocument.AFTER)
        val dbResult = groups.findOneAndUpdate(filter = dbFilter, update = dbUpdate, options = dbOptions)

        return dbResult != null
    }

    override suspend fun deleteGroup(groupId: String, uuid: String): Boolean {
        val groupToDelete: Group? = getGroup(groupId)

        if (groupToDelete == null) {
            println("Could not delete Group because groupId $groupId is not available")
            return false
        }

        if (groupToDelete.creator != uuid) {
            println("User with UUID $uuid cannot delete this group because he is not the creator")
            return false
        }


        groups.deleteOne(Group::id eq groupId)
        return true
    }



    // ======= COSTS =======

    override suspend fun getAllItems(): List<CostItem> {
        val list: List<CostItem> = costItems.find()
            .descendingSort(CostItem::timestamp)
            .toList()

        println(list)
        return list
    }

    override suspend fun getGroupItems(groupId: String): List<CostItem> {
        return costItems.find(CostItem::groupId eq groupId)
            .descendingSort(CostItem::timestamp)
            .toList()
    }

    override suspend fun getGroupItems(groupId: String, start: String, end: String): List<CostItem> {
        return costItems
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
        costItems.insertOne(item)
    }
}