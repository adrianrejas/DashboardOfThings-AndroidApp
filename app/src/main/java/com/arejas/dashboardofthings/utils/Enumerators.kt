package com.arejas.dashboardofthings.utils

class Enumerators {

    enum class ElementType {
        NETWORK,
        SENSOR,
        ACTUATOR;


        companion object {

            fun valueOf(ordinal: Int): ElementType {
                when (ordinal) {
                    0 -> return NETWORK
                    1 -> return SENSOR
                    else -> return ACTUATOR
                }
            }
        }
    }

    enum class NetworkType {
        HTTP,
        MQTT;


        companion object {

            fun valueOf(ordinal: Int): NetworkType {
                when (ordinal) {
                    0 -> return HTTP
                    else -> return MQTT
                }
            }
        }
    }

    enum class HttpAuthenticationType {
        NONE,
        BASIC;


        companion object {

            fun valueOf(ordinal: Int): HttpAuthenticationType {
                when (ordinal) {
                    0 -> return NONE
                    else -> return BASIC
                }
            }
        }
    }

    enum class MqttAuthenticationType {
        NONE,
        BASIC;


        companion object {

            fun valueOf(ordinal: Int): MqttAuthenticationType {
                when (ordinal) {
                    0 -> return NONE
                    else -> return BASIC
                }
            }
        }
    }

    enum class DataType {
        BOOLEAN,
        INTEGER,
        DECIMAL,
        STRING;


        companion object {

            fun valueOf(ordinal: Int): DataType {
                when (ordinal) {
                    0 -> return BOOLEAN
                    1 -> return INTEGER
                    2 -> return DECIMAL
                    else -> return STRING
                }
            }
        }
    }

    enum class MessageType {
        XML,
        JSON,
        RAW;


        companion object {

            fun valueOf(ordinal: Int): MessageType {
                when (ordinal) {
                    0 -> return XML
                    1 -> return JSON
                    else -> return RAW
                }
            }
        }
    }

    enum class MqttQosLevel {
        QOS_0,
        QOS_1,
        QOS_2;


        companion object {

            fun valueOf(ordinal: Int): MqttQosLevel {
                when (ordinal) {
                    0 -> return QOS_0
                    1 -> return QOS_1
                    else -> return QOS_2
                }
            }
        }
    }

    enum class HttpMethod {
        GET,
        PUT,
        POST,
        PATCH;

        override fun toString(): String {
            when (ordinal) {
                0 -> return "GET"
                1 -> return "PUT"
                2 -> return "POST"
                else -> return "PATCH"
            }
        }

        companion object {

            fun valueOf(ordinal: Int): HttpMethod {
                when (ordinal) {
                    0 -> return GET
                    1 -> return PUT
                    2 -> return POST
                    else -> return PATCH
                }
            }
        }
    }

    enum class LogLevel {
        INFO,
        WARN,
        ERROR,
        NOTIF_NONE,
        NOTIF_WARN,
        NOTIF_CRITICAL;

        override fun toString(): String {
            when (ordinal) {
                0 -> return "INFO"
                1 -> return "WARNING"
                2 -> return "ERROR"
                3 -> return "NOTIF_NONE"
                4 -> return "NOTIF_WARN"
                else -> return "NOTIF_CRITICAL"
            }
        }

        companion object {

            fun valueOf(ordinal: Int): LogLevel {
                when (ordinal) {
                    0 -> return INFO
                    1 -> return WARN
                    2 -> return ERROR
                    3 -> return NOTIF_NONE
                    4 -> return NOTIF_WARN
                    else -> return NOTIF_CRITICAL
                }
            }
        }
    }

    enum class NotificationType {
        NONE,
        WARNING,
        CRITICAL;

        override fun toString(): String {
            when (ordinal) {
                0 -> return "INFO"
                1 -> return "NOTIF_WARN"
                else -> return "NOTIF_CRITICAL"
            }
        }

        companion object {

            fun valueOf(ordinal: Int): NotificationType {
                when (ordinal) {
                    0 -> return NONE
                    1 -> return WARNING
                    else -> return CRITICAL
                }
            }
        }
    }

    enum class ElementManagementFunction {
        CREATE,
        UPDATE,
        DELETE;

        override fun toString(): String {
            when (ordinal) {
                0 -> return "CREATE"
                1 -> return "UPDATE"
                else -> return "DELETE"
            }
        }

        companion object {

            fun valueOf(ordinal: Int): ElementManagementFunction {
                when (ordinal) {
                    0 -> return CREATE
                    1 -> return UPDATE
                    else -> return DELETE
                }
            }
        }
    }

    enum class NotificationState {
        NONE,
        WARNING,
        CRITICAL;

        override fun toString(): String {
            when (ordinal) {
                0 -> return "NONE"
                1 -> return "WARNING"
                else -> return "CRITICAL"
            }
        }

        companion object {

            fun valueOf(ordinal: Int): NotificationState {
                when (ordinal) {
                    0 -> return NONE
                    1 -> return WARNING
                    else -> return CRITICAL
                }
            }
        }
    }

}
