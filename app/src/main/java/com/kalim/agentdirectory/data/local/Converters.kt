package com.kalim.agentdirectory.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kalim.agentdirectory.data.model.Address
import com.kalim.agentdirectory.data.model.Company
import com.kalim.agentdirectory.data.model.Coordinates
import com.kalim.agentdirectory.data.model.Hair
import com.kalim.agentdirectory.data.model.Reactions

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) null else {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType)
        }
    }

    @TypeConverter
    fun fromHair(hair: Hair?): String? {
        return if (hair == null) null else gson.toJson(hair)
    }

    @TypeConverter
    fun toHair(value: String?): Hair? {
        return if (value == null) null else gson.fromJson(value, Hair::class.java)
    }

    @TypeConverter
    fun fromAddress(address: Address?): String? {
        return if (address == null) null else gson.toJson(address)
    }

    @TypeConverter
    fun toAddress(value: String?): Address? {
        return if (value == null) null else gson.fromJson(value, Address::class.java)
    }

    @TypeConverter
    fun fromCompany(company: Company?): String? {
        return if (company == null) null else gson.toJson(company)
    }

    @TypeConverter
    fun toCompany(value: String?): Company? {
        return if (value == null) null else gson.fromJson(value, Company::class.java)
    }

    @TypeConverter
    fun fromCoordinates(coordinates: Coordinates?): String? {
        return if (coordinates == null) null else gson.toJson(coordinates)
    }

    @TypeConverter
    fun toCoordinates(value: String?): Coordinates? {
        return if (value == null) null else gson.fromJson(value, Coordinates::class.java)
    }

    @TypeConverter
    fun fromReactions(reactions: Reactions?): String? {
        return if (reactions == null) null else gson.toJson(reactions)
    }

    @TypeConverter
    fun toReactions(value: String?): Reactions? {
        return if (value == null) null else gson.fromJson(value, Reactions::class.java)
    }
}

