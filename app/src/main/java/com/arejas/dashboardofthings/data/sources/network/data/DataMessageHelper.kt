package com.arejas.dashboardofthings.data.sources.network.data

import android.util.Xml

import com.arejas.dashboardofthings.data.format.DataTransformationHelper
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.utils.Enumerators
import com.google.common.net.UrlEscapers
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive

import org.xmlpull.v1.XmlPullParser

import java.io.StringReader
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Matcher
import java.util.regex.Pattern

object DataMessageHelper {

    val XML_JSON_NODE_SEPARATOR = "\\."

    val XML_NODE_ARRAY_REGEX = "(.*)\\[([0-9]+)\\]$"
    val JSON_NODE_ARRAY_REGEX = "(.*)?\\[([0-9]+)\\]$"
    val XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME = 1
    val XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX = 2

    val ACTUATOR_MESSAGE_DATA_INDICATOR = "\${DATA}"
    val ACTUATOR_MESSAGE_TIMESTAMP_INDICATOR = "\${TIMESTAMP}"

    fun extractDataFromSensorResponse(messageBody: String, sensor: Sensor): String? {
        try {
            when (sensor.messageType) {
                Enumerators.MessageType.JSON -> return extractDataFromSensorResponseJSON(
                    messageBody,
                    sensor
                )
                Enumerators.MessageType.XML -> return extractDataFromSensorResponseXML(
                    messageBody,
                    sensor
                )
                Enumerators.MessageType.RAW -> return extractDataFromSensorResponseRAW(
                    messageBody,
                    sensor
                )
                else -> return null
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun extractDataFromSensorResponseJSON(messageBody: String, sensor: Sensor): String? {
        try {
            val arrayNodePattern = Pattern.compile(JSON_NODE_ARRAY_REGEX)
            val jMessageElement = JsonParser().parse(messageBody)
            var jsonObject = jMessageElement.asJsonObject
            val jsonNodes = sensor.xmlOrJsonNode!!.split(XML_JSON_NODE_SEPARATOR.toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in jsonNodes.indices) {
                val node = jsonNodes[i]
                val matcher = arrayNodePattern.matcher(node)
                if (matcher.find()) {
                    // Get the group matched using group() method
                    val newNode = matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME)
                    val index =
                        Integer.parseInt(matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX))
                    if (newNode != null && !newNode.isEmpty()) {
                        val jsonArray = jsonObject.getAsJsonArray(newNode)
                        if (i == jsonNodes.size - 1) {
                            val primitive = jsonObject.getAsJsonPrimitive(node)
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.dataType!!)
                        } else {
                            jsonObject = jsonArray.get(index).asJsonObject
                        }
                    } else {
                        val jsonArray = jsonObject.asJsonArray
                        if (i == jsonNodes.size - 1) {
                            val primitive = jsonObject.asJsonPrimitive
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.dataType!!)
                        } else {
                            jsonObject = jsonArray.get(index).asJsonObject
                        }
                    }
                } else { // normal node
                    if (node != null && !node.isEmpty()) {
                        if (i == jsonNodes.size - 1) {
                            val primitive = jsonObject.getAsJsonPrimitive(node)
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.dataType!!)
                        } else {
                            jsonObject = jsonObject.getAsJsonObject(node)
                        }
                    } else {
                        if (i == jsonNodes.size - 1) {
                            val primitive = jsonObject.asJsonPrimitive
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.dataType!!)
                        } else {
                            jsonObject = jsonObject.asJsonObject
                        }
                    }
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }

    }

    fun getJsonPrimitiveAsDotDataType(
        primitive: JsonPrimitive,
        type: Enumerators.DataType
    ): String? {
        when (type) {
            Enumerators.DataType.BOOLEAN -> return java.lang.Boolean.valueOf(primitive.asBoolean)
                .toString()
            Enumerators.DataType.INTEGER -> return Integer.valueOf(primitive.asInt).toString()
            Enumerators.DataType.DECIMAL -> return java.lang.Float.valueOf(primitive.asFloat)
                .toString()
            Enumerators.DataType.STRING -> return primitive.asString
        }
        return null
    }

    fun extractDataFromSensorResponseXML(messageBody: String, sensor: Sensor): String? {
        try {
            var xmlTreeIndex = 0
            var indexToCountNodes = 0
            var thresholdToCountNodes = 1
            var nodeToSearch: String? = null
            val xmlNodes = sensor.xmlOrJsonNode!!.split(XML_JSON_NODE_SEPARATOR.toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
            val arrayNodePattern = Pattern.compile(XML_NODE_ARRAY_REGEX)
            val matcher = arrayNodePattern.matcher(xmlNodes[xmlTreeIndex])
            if (matcher.find()) {
                nodeToSearch = matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME)
                indexToCountNodes = 0
                thresholdToCountNodes =
                    Integer.parseInt(matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX)) + 1
            } else if (!xmlNodes[xmlTreeIndex].isEmpty()) {
                nodeToSearch = xmlNodes[xmlTreeIndex]
                indexToCountNodes = 0
                thresholdToCountNodes = 1
            } else {
                return null
            }
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(messageBody))
            var getInfoFromNextText = false
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    val tagToCheck = parser.name
                    if (tagToCheck == nodeToSearch) {
                        indexToCountNodes++
                        if (indexToCountNodes >= thresholdToCountNodes) {
                            xmlTreeIndex++
                            if (xmlTreeIndex >= xmlNodes.size) {
                                getInfoFromNextText = true
                            } else {
                                val matcher2 = arrayNodePattern.matcher(xmlNodes[xmlTreeIndex])
                                if (matcher2.find()) {
                                    nodeToSearch =
                                        matcher2.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME)
                                    indexToCountNodes = 0
                                    thresholdToCountNodes = Integer.parseInt(
                                        matcher2.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX)
                                    ) + 1
                                } else if (!xmlNodes[xmlTreeIndex].isEmpty()) {
                                    nodeToSearch = xmlNodes[xmlTreeIndex]
                                    indexToCountNodes = 0
                                    thresholdToCountNodes = 1
                                } else {
                                    return null
                                }
                            }
                        }
                    }
                } else if (parser.eventType == XmlPullParser.TEXT) {
                    if (getInfoFromNextText) {
                        val result = parser.text
                        return if (result != null && !result.isEmpty()) result else null
                    }
                }
            }
            return null
        } catch (e: Exception) {
            return null
        }

    }

    fun extractDataFromSensorResponseRAW(messageBody: String, sensor: Sensor): String? {
        try {
            val regex = sensor.rawRegularExpression
            val pattertToUse = Pattern.compile(regex)
            val patternMatcher = pattertToUse.matcher(messageBody)
            return if (patternMatcher.find()) {
                URLDecoder.decode(patternMatcher.group(1), "utf-8")
            } else null
        } catch (e: Exception) {
            return null
        }

    }

    fun formatActuatorMessage(dataToSend: String, actuator: Actuator): String? {
        try {
            val current = Date()
            var formattedMessage = actuator.dataFormatMessageToSend
            formattedMessage = formattedMessage!!.replace(
                ACTUATOR_MESSAGE_TIMESTAMP_INDICATOR,
                java.lang.Long.toString(current.time)
            )
            formattedMessage = formattedMessage.replace(ACTUATOR_MESSAGE_DATA_INDICATOR, dataToSend)
            return formattedMessage
        } catch (e: Exception) {
            return null
        }

    }

    fun checkDataPrecenseInActuatorMessageFormat(messageFormat: String): Boolean {
        try {
            return if (messageFormat.contains(ACTUATOR_MESSAGE_DATA_INDICATOR)) {
                true
            } else false
        } catch (e: Exception) {
            return false
        }

    }

}
