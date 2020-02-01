package com.arejas.dashboardofthings.data.format

import com.arejas.dashboardofthings.utils.Enumerators

object DataTransformationHelper {

    fun checkIfDataTypeIsCorrect(dataReceived: String?, dataType: Enumerators.DataType): Boolean {
        when (dataType) {
            Enumerators.DataType.STRING -> return dataReceived != null
            Enumerators.DataType.DECIMAL -> {
                try {
                    java.lang.Float.parseFloat(dataReceived!!)
                    return true
                } catch (e: NumberFormatException) {
                    return false
                }

                try {
                    Integer.parseInt(dataReceived)
                    return true
                } catch (e: NumberFormatException) {
                    return false
                }

                return dataReceived != null
            }
            Enumerators.DataType.INTEGER -> {
                try {
                    Integer.parseInt(dataReceived!!)
                    return true
                } catch (e: NumberFormatException) {
                    return false
                }

                return dataReceived != null
            }
            Enumerators.DataType.BOOLEAN -> return dataReceived != null
            else -> return false
        }
    }

    fun getDataFromString(dataString: String, dataType: Enumerators.DataType): Any? {
        try {
            when (dataType) {
                Enumerators.DataType.STRING -> return dataString
                Enumerators.DataType.DECIMAL -> return java.lang.Float.parseFloat(dataString)
                Enumerators.DataType.INTEGER -> return Integer.parseInt(dataString)
                Enumerators.DataType.BOOLEAN -> return java.lang.Boolean.parseBoolean(dataString)
                else -> return null
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun convertDataToString(data: Any, dataType: Enumerators.DataType): String? {
        try {
            when (dataType) {
                Enumerators.DataType.STRING -> return String((data as String).toCharArray())
                Enumerators.DataType.DECIMAL -> return java.lang.Float.toString(data as Float)
                Enumerators.DataType.INTEGER -> return Integer.toString(data as Int)
                Enumerators.DataType.BOOLEAN -> return java.lang.Boolean.toString(data as Boolean)
                else -> return null
            }
        } catch (e: Exception) {
            return null
        }

    }

}
