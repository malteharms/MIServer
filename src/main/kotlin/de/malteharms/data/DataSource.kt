package de.malteharms.data

import de.malteharms.data.models.Account
import de.malteharms.data.models.CostItem
import de.malteharms.data.models.Group

interface DataSource {

    suspend fun addAccount(username: String, password: String): Boolean
    suspend fun confirmCredentials(uuid: String, password: String): Boolean
    suspend fun getAccount(uuid: String): Account?
    suspend fun getPassword(creatorUuid: String): String?
    suspend fun deleteAccount(uuid: String): Boolean

    suspend fun addGroup(uuid: String, name: String): Boolean
    suspend fun addMemberToGroup(groupId: String, uuidOfNewMember: String): Boolean
    suspend fun getGroup(groupId: String): Group?
    suspend fun deleteGroup(groupId: String, uuid: String): Boolean
    suspend fun getAllItems(): List<CostItem>
    suspend fun getGroupItems(groupId: String): List<CostItem>
    suspend fun getGroupItems(groupId: String, start: String, end: String): List<CostItem>
    suspend fun insertItem(item: CostItem)

}