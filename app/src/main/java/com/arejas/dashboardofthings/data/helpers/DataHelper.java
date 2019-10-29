package com.arejas.dashboardofthings.data.helpers;

import com.arejas.dashboardofthings.utils.Enumerators;

public class DataHelper {

    public static Enumerators.NotificationState getNotificationStatus(String dataReceived,
                                                                      Enumerators.DataType dataType,
                                                                      Float thresholdAboveWarningRaw,
                                                                      Float thresholdAboveCriticalRaw,
                                                                      Float thresholdBelowWarningRaw,
                                                                      Float thresholdBelowCriticalRaw,
                                                                      String thresholdEqualsWarningString,
                                                                      String thresholdEqualsCriticalString) {
        try {
            if (dataType != null) {
                if (dataType.equals(Enumerators.DataType.INTEGER)) {
                    int thresholdAboveWarning = (thresholdAboveWarningRaw != null) ?
                            thresholdAboveWarningRaw.intValue() : Integer.MAX_VALUE;
                    int thresholdAboveCritical = (thresholdAboveCriticalRaw != null) ?
                            thresholdAboveCriticalRaw.intValue() : Integer.MAX_VALUE;
                    int thresholdBelowWarning = (thresholdBelowWarningRaw != null) ?
                            thresholdBelowWarningRaw.intValue() : Integer.MIN_VALUE;
                    int thresholdBelowCritical = (thresholdBelowCriticalRaw != null) ?
                            thresholdBelowCriticalRaw.intValue() : Integer.MIN_VALUE;
                    int intValue = Integer.valueOf(dataReceived);
                    if ((intValue >= thresholdAboveCritical) && (intValue <= thresholdBelowCritical)) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((intValue >= thresholdAboveWarning) && (intValue <= thresholdBelowWarning)) {
                        return Enumerators.NotificationState.WARNING;
                    } else {
                        return Enumerators.NotificationState.NONE;
                    }
                } else if (dataType.equals(Enumerators.DataType.DECIMAL)) {
                    float thresholdAboveWarning = (thresholdAboveWarningRaw != null) ?
                            thresholdAboveWarningRaw : Float.MAX_VALUE;
                    float thresholdAboveCritical = (thresholdAboveCriticalRaw != null) ?
                            thresholdAboveCriticalRaw : Float.MAX_VALUE;
                    float thresholdBelowWarning = (thresholdBelowWarningRaw != null) ?
                            thresholdBelowWarningRaw : Float.MIN_VALUE;
                    float thresholdBelowCritical = (thresholdBelowCriticalRaw != null) ?
                            thresholdBelowCriticalRaw : Float.MIN_VALUE;
                    float floatValue = Float.valueOf(dataReceived);
                    if ((floatValue >= thresholdAboveCritical) && (floatValue <= thresholdBelowCritical)) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((floatValue >= thresholdAboveWarning) && (floatValue <= thresholdBelowWarning)) {
                        return Enumerators.NotificationState.WARNING;
                    } else {
                        return Enumerators.NotificationState.NONE;
                    }
                } else if (dataType.equals(Enumerators.DataType.BOOLEAN)) {
                    Boolean thresholdEqualsWarning = ((thresholdEqualsWarningString != null) &&
                            (!thresholdEqualsWarningString.isEmpty())) ?
                            Boolean.valueOf(thresholdEqualsWarningString) : null;
                    Boolean thresholdEqualsCritical = ((thresholdEqualsCriticalString != null) &&
                            (!thresholdEqualsCriticalString.isEmpty())) ?
                            Boolean.valueOf(thresholdEqualsCriticalString) : null;
                    boolean booleanValue = Boolean.valueOf(dataReceived);
                    if ((thresholdEqualsCritical != null) && (booleanValue == thresholdEqualsCritical.booleanValue())) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((thresholdEqualsWarning != null) && (booleanValue == thresholdEqualsWarning.booleanValue())) {
                        return Enumerators.NotificationState.WARNING;
                    } else {
                        return Enumerators.NotificationState.NONE;
                    }
                } else {
                    if ((thresholdEqualsCriticalString != null) && (dataReceived.equals(thresholdEqualsCriticalString))) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((thresholdEqualsWarningString != null) && (dataReceived.equals(thresholdEqualsWarningString))) {
                        return Enumerators.NotificationState.WARNING;
                    } else {
                        return Enumerators.NotificationState.NONE;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
