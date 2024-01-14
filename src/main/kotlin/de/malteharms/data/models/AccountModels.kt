package de.malteharms.data.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class Account(
    @BsonId val uuid: String,
    val username: String,
    val groups: List<String>    /* List<groupId> */

    // val profilePicture: Any
)

@Serializable
data class Group(
    @BsonId val id: String,             // identifier of a group
    val name: String,           // displayed name
    val creator: String,         // uuid of Account how created this group
    val member: List<String>    // list of uuid's
)

@Serializable
data class Credentials(
    @BsonId val uuid: String,           // uuid references Account.uuid
    val passwordHash: String
)

@Serializable
data class Registration(
    val username: String,
    val passwordHash: String,
)

@Serializable
data class Login(
    val uuid: String,
    val passwordHash: String
)
