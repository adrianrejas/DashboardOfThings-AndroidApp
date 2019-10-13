package com.arejas.dashboardofthings.data.sources.database.converters;

import androidx.room.TypeConverter;

import com.arejas.dashboardofthings.utils.Enumerators;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Map;

public class DotTypeConverters {

    @TypeConverter
    public static Enumerators.ElementType valueOfElementType(int ordinal) {
        return Enumerators.ElementType.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntElementType(Enumerators.ElementType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.NetworkType valueOfNetworkType(int ordinal) {
        return Enumerators.NetworkType.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntNetworkType(Enumerators.NetworkType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.HttpAuthenticationType valueOfHttpAuthenticationType(int ordinal) {
        return Enumerators.HttpAuthenticationType.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntHttpAuthenticationType(Enumerators.HttpAuthenticationType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.MqttAuthenticationType valueOfMqttAuthenticationType(int ordinal) {
        return Enumerators.MqttAuthenticationType.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntMqttAuthenticationType(Enumerators.MqttAuthenticationType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.DataType valueOfDataType(int ordinal) {
        return Enumerators.DataType.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntDataType(Enumerators.DataType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.MessageType valueOfMessageType(int ordinal) {
        return Enumerators.MessageType.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntMessageType(Enumerators.MessageType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.MqttQosLevel valueOfMqttQosLevel(int ordinal) {
        return Enumerators.MqttQosLevel.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntMqttQosLevel(Enumerators.MqttQosLevel value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.HttpMethod valueOfHttpCommand(int ordinal) {
        return Enumerators.HttpMethod.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntLogLevel(Enumerators.LogLevel value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.LogLevel valueOfLogLevel(int ordinal) {
        return Enumerators.LogLevel.valueOf(ordinal);
    }

    @TypeConverter
    public static Integer getValueIntHttpCommand(Enumerators.HttpMethod value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Map<String, String> valueofMapStringString(String value) {
        Type mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(value, mapType);
    }

    @TypeConverter
    public static String getValueStringMapStringString(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }
    @TypeConverter
    public static Date valueofDate (Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long getValueLongDate (Date date) {
        return date == null ? null : date.getTime();
    }

}
