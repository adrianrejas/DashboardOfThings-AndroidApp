package com.arejas.dashboardofthings.data.format;

import com.arejas.dashboardofthings.utils.Enumerators;

public class DataTransformationHelper {

    public static boolean checkIfDataTypeIsCorrect(String dataReceived, Enumerators.DataType dataType) {
        switch (dataType) {
            case STRING:
                return (dataReceived != null);
            case DECIMAL:
                try{
                    Float.parseFloat(dataReceived);
                    return true;
                }catch(NumberFormatException e){
                    return false;
                }
            case INTEGER:
                try{
                    Integer.parseInt(dataReceived);
                    return true;
                }catch(NumberFormatException e){
                    return false;
                }
            case BOOLEAN:
                return (dataReceived != null);
            default:
                return false;
        }
    }

    public static Object getDataFromString(String dataString, Enumerators.DataType dataType) {
        try {
            switch (dataType) {
                case STRING:
                    return dataString;
                case DECIMAL:
                    return Float.parseFloat(dataString);
                case INTEGER:
                    return Integer.parseInt(dataString);
                case BOOLEAN:
                    return Boolean.parseBoolean(dataString);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String convertDataToString(Object data, Enumerators.DataType dataType) {
        try {
            switch (dataType) {
                case STRING:
                    return new String((String) data);
                case DECIMAL:
                    return Float.toString((Float) data);
                case INTEGER:
                    return Integer.toString((Integer) data);
                case BOOLEAN:
                    return Boolean.toString((Boolean) data);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
