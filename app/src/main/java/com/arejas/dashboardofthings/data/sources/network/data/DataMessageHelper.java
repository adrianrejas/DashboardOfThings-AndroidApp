package com.arejas.dashboardofthings.data.sources.network.data;

import android.util.Xml;

import com.arejas.dashboardofthings.data.format.DataTransformationHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataMessageHelper {
    
    public static final String XML_JSON_NODE_SEPARATOR = ".";

    public static final String XML_NODE_ARRAY_REGEX = "(.*)\\[([0-9]+)\\]$";
    public static final String JSON_NODE_ARRAY_REGEX = "(.*)?\\[([0-9]+)\\]$";
    public static final int XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME = 1;
    public static final int XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX = 2;

    public static final String ACTUATOR_MESSAGE_DATA_REGEX = "\\$\\{DATA\\(([^\\);]+)\\)\\}";
    public static final String ACTUATOR_MESSAGE_DATE_REGEX = "\\$\\{DATE\\(([^\\);]+)\\)\\}";
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
            for (String node : jsonNodes) {
                if (arrayNodePattern.matcher(node).matches()) { // array node
                    String newNode = arrayNodePattern.matcher(node).group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME);
                    int index = Integer.parseInt(arrayNodePattern.matcher(node).group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX));
                    if ((newNode != null) &&(!newNode.isEmpty())) {
                        JsonArray jsonArray = jsonObject.getAsJsonArray(newNode);
                        jsonObject = jsonArray.get(index).getAsJsonObject();
                    } else {
                        JsonArray jsonArray = jsonObject.getAsJsonArray();
                        jsonObject = jsonArray.get(index).getAsJsonObject();
                    }
                } else { // normal node
                    if ((node != null) &&(!node.isEmpty())) {
                        jsonObject = jsonObject.getAsJsonObject(node);
                    } else {
                        jsonObject = jsonObject.getAsJsonObject();
                    }
                }
            }
            return jsonObject.getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String extractDataFromSensorResponseXML(String messageBody, Sensor sensor) {
        try {
            int xmlTreeIndex = 0;
            int indexToCountNodes = 0;
            int thresholdToCountNodes = 1;
            String nodeToSearch = null;
            String[] xmlNodes = sensor.getXmlOrJsonNode().split(XML_JSON_NODE_SEPARATOR);
            Pattern arrayNodePattern = Pattern.compile(XML_NODE_ARRAY_REGEX);
            if (arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]).matches()) {
                nodeToSearch = arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]).group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME);
                indexToCountNodes = 0;
                thresholdToCountNodes = Integer.parseInt(arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]).group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX)) + 1;
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
                                if (arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]).matches()) {
                                    nodeToSearch = arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]).group(XML_JSON_NODE_ARRAY_REGEX_GROUP_NAME);
                                    indexToCountNodes = 0;
                                    thresholdToCountNodes = Integer.parseInt(arrayNodePattern.matcher(xmlNodes[xmlTreeIndex]).group(XML_JSON_NODE_ARRAY_REGEX_GROUP_INDEX)) + 1;
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
            Pattern pattertToUse = Pattern.compile(sensor.getRawRegularExpression());
            Matcher patternMatcher = pattertToUse.matcher(messageBody);
            if ((patternMatcher.matches()) && (patternMatcher.groupCount() >= 1)) {
                return patternMatcher.group(0);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String formatActuatorMessage(String dataToSend, Actuator actuator) {
        try {
            Date current = new Date();
            String formattedMessage = new String(actuator.getDataFormatMessageToSend());
            formattedMessage = formattedMessage.replace(ACTUATOR_MESSAGE_TIMESTAMP_INDICATOR, Long.toString(current.getTime()));
            Pattern pattertToDetectDate = Pattern.compile(ACTUATOR_MESSAGE_DATE_REGEX);
            Matcher patternMatcherDate = pattertToDetectDate.matcher(formattedMessage);
            while ((patternMatcherDate.matches()) && (patternMatcherDate.groupCount() >= 1)) {
                String dateFormatStr = patternMatcherDate.group(0);
                SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStr);
                formattedMessage = patternMatcherDate.replaceFirst(dateFormat.format(current));
                patternMatcherDate = pattertToDetectDate.matcher(formattedMessage);
            }
            Pattern pattertToDetectData = Pattern.compile(ACTUATOR_MESSAGE_DATA_REGEX);
            Matcher patternMatcherData = pattertToDetectData.matcher(formattedMessage);
            while ((patternMatcherData.matches()) && (patternMatcherData.groupCount() >= 1)) {
                String dataFormatStr = patternMatcherData.group(0);
                Object dataToFormat = DataTransformationHelper.getDataFromString(dataToSend, actuator.getDataType());
                if (dataToFormat == null) {
                    return null;
                }
                patternMatcherDate.replaceFirst(String.format(dataFormatStr, dataToFormat));
                patternMatcherData = pattertToDetectData.matcher(formattedMessage);
            }
            formattedMessage = formattedMessage.replace(ACTUATOR_MESSAGE_DATA_INDICATOR, dataToSend);
            return formattedMessage;
        } catch (Exception e) {
            return null;
        }
    }
    
}
