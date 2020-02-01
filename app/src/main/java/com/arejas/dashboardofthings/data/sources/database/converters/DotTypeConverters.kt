package com.arejas.dashboardofthings.data.sources.database.converters

import androidx.room.TypeConverter

import com.arejas.dashboardofthings.utils.Enumerators
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type
import java.util.Date

object DotTypeConverters {

    @TypeConverter
    fun valueOfElementType(ordinal: Int): Enumerators.ElementType {
        return Enumerators.ElementType.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntElementType(value: Enumerators.ElementType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfNetworkType(ordinal: Int): Enumerators.NetworkType {
        return Enumerators.NetworkType.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntNetworkType(value: Enumerators.NetworkType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfHttpAuthenticationType(ordinal: Int): Enumerators.HttpAuthenticationType {
        return Enumerators.HttpAuthenticationType.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntHttpAuthenticationType(value: Enumerators.HttpAuthenticationType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfMqttAuthenticationType(ordinal: Int): Enumerators.MqttAuthenticationType {
        return Enumerators.MqttAuthenticationType.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntMqttAuthenticationType(value: Enumerators.MqttAuthenticationType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfDataType(ordinal: Int): Enumerators.DataType {
        return Enumerators.DataType.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntDataType(value: Enumerators.DataType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfMessageType(ordinal: Int): Enumerators.MessageType {
        return Enumerators.MessageType.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntMessageType(value: Enumerators.MessageType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfMqttQosLevel(ordinal: Int): Enumerators.MqttQosLevel {
        return Enumerators.MqttQosLevel.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntMqttQosLevel(value: Enumerators.MqttQosLevel): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfHttpCommand(ordinal: Int): Enumerators.HttpMethod {
        return Enumerators.HttpMethod.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntLogLevel(value: Enumerators.LogLevel): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueOfLogLevel(ordinal: Int): Enumerators.LogLevel {
        return Enumerators.LogLevel.valueOf(ordinal)
    }

    @TypeConverter
    fun getValueIntHttpCommand(value: Enumerators.HttpMethod): Int {
        return value.ordinal
    }

    @TypeConverter
    fun valueofMapStringString(value: String): Map<String, String>? {
        val mapType = object : TypeToken<Map<String, String>>() {

        }.type
        return Gson().fromJson<Map<String, String>>(value, mapType)
    }

    @TypeConverter
    fun getValueStringMapStringString(map: Map<String, String>): String {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun valueofDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun getValueLongDate(date: Date?): Long? {
        return date?.time
    }

}
