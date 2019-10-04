package com.arejas.dashboardofthings.utils;

public class Enumerators {

    public enum ElementType {
        NETWORK,
        SENSOR,
        ACTUATOR;

        public static ElementType valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return NETWORK;
                case 1:
                    return SENSOR;
                default:
                    return ACTUATOR;
            }
        }
    }

    public enum NetworkType {
        HTTP,
        MQTT;

        public static NetworkType valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return HTTP;
                default:
                    return MQTT;
            }
        }
    }

    public enum NetworkStatus {
        STOPPED,
        CONNECTED,
        DISCONNECTED,
        FAILURE;

        public static NetworkStatus valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return STOPPED;
                case 1:
                    return CONNECTED;
                case 2:
                    return DISCONNECTED;
                default:
                    return FAILURE;
            }
        }
    }

    public enum HttpAuthenticationType {
        NONE,
        BASIC;

        public static HttpAuthenticationType valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return NONE;
                default:
                    return BASIC;
            }
        }
    }

    public enum MqttAuthenticationType {
        NONE,
        BASIC;

        public static MqttAuthenticationType valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return NONE;
                default:
                    return BASIC;
            }
        }
    }

    public enum DataType {
        BOOLEAN,
        INTEGER,
        DECIMAL,
        STRING;

        public static DataType valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return BOOLEAN;
                case 1:
                    return INTEGER;
                case 2:
                    return DECIMAL;
                default:
                    return STRING;
            }
        }
    }

    public enum MessageType {
        XML,
        JSON,
        RAW;

        public static MessageType valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return XML;
                case 1:
                    return JSON;
                default:
                    return RAW;
            }
        }
    }

    public enum MqttQosLevel {
        QOS_0,
        QOS_1,
        QOS_2;

        public static MqttQosLevel valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return QOS_0;
                case 1:
                    return QOS_1;
                default:
                    return QOS_2;
            }
        }
    }

    public enum HttpMethod {
        GET,
        PUT,
        POST,
        PATCH;

        public static HttpMethod valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return GET;
                case 1:
                    return PUT;
                case 2:
                    return POST;
                default:
                    return PATCH;
            }
        }

        @Override
        public String toString() {
            switch (ordinal()) {
                case 0:
                    return "GET";
                case 1:
                    return "PUT";
                case 2:
                    return "POST";
                default:
                    return "PATCH";
            }
        }
    }

    public enum LogLevel {
        INFO,
        WARN,
        ERROR,
        CRITICAL;

        public static LogLevel valueOf(int ordinal) {
            switch (ordinal) {
                case 0:
                    return INFO;
                case 1:
                    return WARN;
                case 2:
                    return ERROR;
                default:
                    return CRITICAL;
            }
        }

        @Override
        public String toString() {
            switch (ordinal()) {
                case 0:
                    return "INFO";
                case 1:
                    return "WARN";
                case 2:
                    return "ERROR";
                default:
                    return "CRITICAL";
            }
        }
    }

}
