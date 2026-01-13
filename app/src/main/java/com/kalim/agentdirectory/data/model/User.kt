package com.kalim.agentdirectory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.kalim.agentdirectory.data.local.Converters

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val username: String,
    val image: String?,
    val age: Int,
    val gender: String,
    val birthDate: String,
    val height: Double?,
    val weight: Double?,
    val eyeColor: String?,
    val hair: Hair?,
    val address: Address?,
    val company: Company?,
    val university: String?,
    @SerializedName("cachedAt")
    val cachedAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = "$firstName $lastName"
}

data class Hair(
    val color: String?,
    val type: String?
)

data class Address(
    val address: String?,
    val city: String?,
    val state: String?,
    val postalCode: String?,
    val coordinates: Coordinates?
)

data class Coordinates(
    val lat: Double?,
    val lng: Double?
)

data class Company(
    val name: String?,
    val department: String?,
    val title: String?
)

