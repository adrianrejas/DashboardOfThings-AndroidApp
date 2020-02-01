package com.arejas.dashboardofthings.data.helpers

import com.arejas.dashboardofthings.utils.Enumerators

object DataHelper {

    fun getNotificationStatus(
        dataReceived: String,
        dataType: Enumerators.DataType?,
        thresholdAboveWarningRaw: Float?,
        thresholdAboveCriticalRaw: Float?,
        thresholdBelowWarningRaw: Float?,
        thresholdBelowCriticalRaw: Float?,
        thresholdEqualsWarningString: String?,
        thresholdEqualsCriticalString: String?
    ): Enumerators.NotificationState? {
        try {
            if (dataType != null) {
                if (dataType == Enumerators.DataType.INTEGER) {
                    val thresholdAboveWarning =
                        thresholdAboveWarningRaw?.toInt() ?: Integer.MAX_VALUE
                    val thresholdAboveCritical =
                        thresholdAboveCriticalRaw?.toInt() ?: Integer.MAX_VALUE
                    val thresholdBelowWarning =
                        thresholdBelowWarningRaw?.toInt() ?: Integer.MIN_VALUE
                    val thresholdBelowCritical =
                        thresholdBelowCriticalRaw?.toInt() ?: Integer.MIN_VALUE
                    val intValue = Integer.valueOf(dataReceived)
                    return if (intValue >= thresholdAboveCritical || intValue <= thresholdBelowCritical) {
                        Enumerators.NotificationState.CRITICAL
                    } else if (intValue >= thresholdAboveWarning || intValue <= thresholdBelowWarning) {
                        Enumerators.NotificationState.WARNING
                    } else {
                        Enumerators.NotificationState.NONE
                    }
                } else if (dataType == Enumerators.DataType.DECIMAL) {
                    val thresholdAboveWarning =
                        thresholdAboveWarningRaw ?: java.lang.Float.MAX_VALUE
                    val thresholdAboveCritical =
                        thresholdAboveCriticalRaw ?: java.lang.Float.MAX_VALUE
                    val thresholdBelowWarning =
                        thresholdBelowWarningRaw ?: java.lang.Float.MIN_VALUE
                    val thresholdBelowCritical =
                        thresholdBelowCriticalRaw ?: java.lang.Float.MIN_VALUE
                    val floatValue = java.lang.Float.valueOf(dataReceived)
                    return if (floatValue >= thresholdAboveCritical || floatValue <= thresholdBelowCritical) {
                        Enumerators.NotificationState.CRITICAL
                    } else if (floatValue >= thresholdAboveWarning || floatValue <= thresholdBelowWarning) {
                        Enumerators.NotificationState.WARNING
                    } else {
                        Enumerators.NotificationState.NONE
                    }
                } else if (dataType == Enumerators.DataType.BOOLEAN) {
                    val thresholdEqualsWarning =
                        if (thresholdEqualsWarningString != null && !thresholdEqualsWarningString.isEmpty())
                            java.lang.Boolean.valueOf(thresholdEqualsWarningString)
                        else
                            null
                    val thresholdEqualsCritical =
                        if (thresholdEqualsCriticalString != null && !thresholdEqualsCriticalString.isEmpty())
                            java.lang.Boolean.valueOf(thresholdEqualsCriticalString)
                        else
                            null
                    val booleanValue = java.lang.Boolean.valueOf(dataReceived)
                    return if (thresholdEqualsCritical != null && booleanValue == thresholdEqualsCritical) {
                        Enumerators.NotificationState.CRITICAL
                    } else if (thresholdEqualsWarning != null && booleanValue == thresholdEqualsWarning) {
                        Enumerators.NotificationState.WARNING
                    } else {
                        Enumerators.NotificationState.NONE
                    }
                } else {
                    return if (thresholdEqualsCriticalString != null && dataReceived == thresholdEqualsCriticalString) {
                        Enumerators.NotificationState.CRITICAL
                    } else if (thresholdEqualsWarningString != null && dataReceived == thresholdEqualsWarningString) {
                        Enumerators.NotificationState.WARNING
                    } else {
                        Enumerators.NotificationState.NONE
                    }
                }
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

}
