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
        switch (ordinal) {
            case 0:
                return Enumerators.ElementType.NETWORK;
            case 1:
                return Enumerators.ElementType.SENSOR;
            default:
                return Enumerators.ElementType.ACTUATOR;
        }
    }

    @TypeConverter
    public static Integer getValueIntElementType(Enumerators.ElementType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.NetworkType valueOfNetworkType(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.NetworkType.HTTP;
            default:
                return Enumerators.NetworkType.MQTT;
        }
    }

    @TypeConverter
    public static Integer getValueIntNetworkType(Enumerators.NetworkType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.NetworkStatus valueOfNetworkStatus(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.NetworkStatus.STOPPED;
            case 1:
                return Enumerators.NetworkStatus.CONNECTED;
            case 2:
                return Enumerators.NetworkStatus.DISCONNECTED;
            default:
                return Enumerators.NetworkStatus.FAILURE;
        }
    }

    @TypeConverter
    public static Integer getValueIntNetworkStatus(Enumerators.NetworkStatus value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.HttpAuthenticationType valueOfHttpAuthenticationType(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.HttpAuthenticationType.NONE;
            default:
                return Enumerators.HttpAuthenticationType.BASIC;
        }
    }

    @TypeConverter
    public static Integer getValueIntHttpAuthenticationType(Enumerators.HttpAuthenticationType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.MqttAuthenticationType valueOfMqttAuthenticationType(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.MqttAuthenticationType.NONE;
            default:
                return Enumerators.MqttAuthenticationType.BASIC;
        }
    }

    @TypeConverter
    public static Integer getValueIntMqttAuthenticationType(Enumerators.MqttAuthenticationType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.DataType valueOfDataType(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.DataType.BOOLEAN;
            case 1:
                return Enumerators.DataType.INTEGER;
            case 2:
                return Enumerators.DataType.DECIMAL;
            default:
                return Enumerators.DataType.STRING;
        }
    }

    @TypeConverter
    public static Integer getValueIntDataType(Enumerators.DataType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.MessageType valueOfMessageType(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.MessageType.XML;
            case 1:
                return Enumerators.MessageType.JSON;
            default:
                return Enumerators.MessageType.RAW;
        }
    }

    @TypeConverter
    public static Integer getValueIntMessageType(Enumerators.MessageType value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.MqttQosLevel valueOfMqttQosLevel(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.MqttQosLevel.QOS_0;
            case 1:
                return Enumerators.MqttQosLevel.QOS_1;
            default:
                return Enumerators.MqttQosLevel.QOS_2;
        }
    }

    @TypeConverter
    public static Integer getValueIntMqttQosLevel(Enumerators.MqttQosLevel value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.HttpMethod valueOfHttpCommand(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.HttpMethod.GET;
            case 1:
                return Enumerators.HttpMethod.PUT;
            case 2:
                return Enumerators.HttpMethod.POST;
            default:
                return Enumerators.HttpMethod.PATCH;
        }
    }

    @TypeConverter
    public static Integer getValueIntLogLevel(Enumerators.LogLevel value) {
        return value.ordinal();
    }

    @TypeConverter
    public static Enumerators.LogLevel valueOfLogLevel(int ordinal) {
        switch (ordinal) {
            case 0:
                return Enumerators.LogLevel.INFO;
            case 1:
                return Enumerators.LogLevel.WARN;
            case 2:
                return Enumerators.LogLevel.ERROR;
            default:
                return Enumerators.LogLevel.CRITICAL;
        }
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
