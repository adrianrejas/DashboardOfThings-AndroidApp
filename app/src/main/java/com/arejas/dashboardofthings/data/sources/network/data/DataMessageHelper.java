package com.arejas.dashboardofthings.data.sources.network.data;

import android.util.Xml;

import com.arejas.dashboardofthings.data.format.DataTransformationHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.google.common.net.UrlEscapers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMessageHelper {
    
    public static final String XML_JSON_NODE_SEPARATOR = "\\.";

    public static final String XML_NODE_ARRAY_REGEX = "(.*)\\[([0-9]+)\\]$";
    public static final String JSON_NODE_ARRAY_REGEX = "(.*)?\\[([0-9]+)\\]$";
    public static final int XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME = 1;
    public static final int XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX = 2;

    public static final String ACTUATOR_MESSAGE_DATA_INDICATOR = "${DATA}";
    public static final String ACTUATOR_MESSAGE_TIMESTAMP_INDICATOR = "${TIMESTAMP}";
    
    public static String extractDataFromSensorResponse(String messageBody, Sensor sensor) {
        try {
            switch (sensor.getMessageType()) {
                case JSON:
                    return extractDataFromSensorResponseJSON(messageBody, sensor);
                case XML:
                    return extractDataFromSensorResponseXML(messageBody, sensor);
                case RAW:
                    return extractDataFromSensorResponseRAW(messageBody, sensor);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractDataFromSensorResponseJSON(String messageBody, Sensor sensor) {
        try {
            Pattern arrayNodePattern = Pattern.compile(JSON_NODE_ARRAY_REGEX);
            JsonElement jMessageElement = new JsonParser().parse(messageBody);
            JsonObject jsonObject = jMessageElement.getAsJsonObject();
            String[] jsonNodes = sensor.getXmlOrJsonNode().split(XML_JSON_NODE_SEPARATOR);
            for (int i = 0; i < jsonNodes.length; i++) {
                String node = jsonNodes[i];
                Matcher matcher = arrayNodePattern.matcher(node);
                if (matcher.find()) {
                    // Get the group matched using group() method
                    String newNode = matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME);
                    int index = Integer.parseInt(matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX));
                    if ((newNode != null) &&(!newNode.isEmpty())) {
                        JsonArray jsonArray = jsonObject.getAsJsonArray(newNode);
                        if (i == (jsonNodes.length -1)) {
                            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(node);
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.getDataType());
                        } else {
                            jsonObject = jsonArray.get(index).getAsJsonObject();
                        }
                    } else {
                        JsonArray jsonArray = jsonObject.getAsJsonArray();
                        if (i == (jsonNodes.length -1)) {
                            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive();
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.getDataType());
                        } else {
                            jsonObject = jsonArray.get(index).getAsJsonObject();
                        }
                    }
                } else { // normal node
                    if ((node != null) &&(!node.isEmpty())) {
                        if (i == (jsonNodes.length -1)) {
                            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive(node);
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.getDataType());
                        } else {
                            jsonObject = jsonObject.getAsJsonObject(node);
                        }
                    } else {
                        if (i == (jsonNodes.length -1)) {
                            JsonPrimitive primitive = jsonObject.getAsJsonPrimitive();
                            return getJsonPrimitiveAsDotDataType(primitive, sensor.getDataType());
                        } else {
                            jsonObject = jsonObject.getAsJsonObject();
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getJsonPrimitiveAsDotDataType(JsonPrimitive primitive,
                                                       Enumerators.DataType type) {
        switch (type) {
            case BOOLEAN:
                return Boolean.valueOf(primitive.getAsBoolean()).toString();
            case INTEGER:
                return Integer.valueOf(primitive.getAsInt()).toString();
            case DECIMAL:
                return Float.valueOf(primitive.getAsFloat()).toString();
            case STRING:
                return primitive.getAsString();
        }
        return null;
    }

    public static String extractDataFromSensorResponseXML(String messageBody, Sensor sensor) {
        try {
            int xmlTreeIndex = 0;
            int indexToCountNodes = 0;
            int thresholdToCountNodes = 1;
            String nodeToSearch = null;
            String[] xmlNodes = sensor.getXmlOrJsonNode().split(XML_JSON_NODE_SEPARATOR);
            Pattern arrayNodePattern = Pattern.compile(XML_NODE_ARRAY_REGEX);
            Matcher matcher = arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]);
            if (matcher.find()) {
                nodeToSearch = matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME);
                indexToCountNodes = 0;
                thresholdToCountNodes = Integer.parseInt(matcher.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX)) + 1;
            } else if (!xmlNodes[xmlTreeIndex].isEmpty()){
                nodeToSearch = xmlNodes[xmlTreeIndex];
                indexToCountNodes = 0;
                thresholdToCountNodes = 1;
            } else {
                return null;
            }
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(messageBody));
            parser.nextTag();
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String tagToCheck = parser.getName();
                    if (tagToCheck.equals(nodeToSearch)) {
                        indexToCountNodes++;
                        if (indexToCountNodes >= thresholdToCountNodes) {
                            xmlTreeIndex++;
                            if (xmlTreeIndex >= xmlNodes.length) {
                                String result = parser.getText();
                                return ((result != null) && (!result.isEmpty())) ? result : null;
                            } else {
                                Matcher matcher2 = arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]);
                                if (matcher2.find()) {
                                    nodeToSearch = matcher2.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME);
                                    indexToCountNodes = 0;
                                    thresholdToCountNodes = Integer.parseInt(matcher2.group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX)) + 1;
                                } else if (!xmlNodes[xmlTreeIndex].isEmpty()){
                                    nodeToSearch = xmlNodes[xmlTreeIndex];
                                    indexToCountNodes = 0;
                                    thresholdToCountNodes = 1;
                                } else {
                                    return null;
                                }
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractDataFromSensorResponseRAW(String messageBody, Sensor sensor) {
        try {
            String regex = sensor.getRawRegularExpression();
            Pattern pattertToUse = Pattern.compile(regex);
            Matcher patternMatcher = pattertToUse.matcher(messageBody);
            if (patternMatcher.find()) {
                return URLDecoder.decode(patternMatcher.group(1), "utf-8");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatActuatorMessage(String dataToSend, Actuator actuator) {
        try {
            Date current = new Date();
            String formattedMessage = actuator.getDataFormatMessageToSend();
            formattedMessage = formattedMessage.replace(ACTUATOR_MESSAGE_TIMESTAMP_INDICATOR, Long.toString(current.getTime()));
            formattedMessage = formattedMessage.replace(ACTUATOR_MESSAGE_DATA_INDICATOR, dataToSend);
            return formattedMessage;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean checkDataPrecenseInActuatorMessageFormat(String messageFormat) {
        try {
            if (messageFormat.contains(ACTUATOR_MESSAGE_DATA_INDICATOR)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
}
