package com.arejas.dashboardofthings.data.helpers;

import com.arejas.dashboardofthings.utils.Enumerators;

public class DataHelper {

    public static Enumerators.NotificationState getNotificationStatus(String dataReceived,
                                                                      Enumerators.DataType dataType,
                                                                      String thresholdAboveWarningString,
                                                                      String thresholdAboveCriticalString,
                                                                      String thresholdBelowWarningString,
                                                                      String thresholdBelowCriticalString,
                                                                      String thresholdEqualsWarningString,
                                                                      String thresholdEqualsCriticalString) {
        try {
            if (dataType != null) {
                if (dataType.equals(Enumerators.DataType.INTEGER)) {
                    int thresholdAboveWarning = ((thresholdAboveWarningString != null) &&
                            (!thresholdAboveWarningString.isEmpty())) ?
                            Integer.valueOf(thresholdAboveWarningString) : Integer.MAX_VALUE;
                    int thresholdAboveCritical = ((thresholdAboveCriticalString != null) &&
                            (!thresholdAboveCriticalString.isEmpty())) ?
                            Integer.valueOf(thresholdAboveCriticalString) : Integer.MAX_VALUE;
                    int thresholdBelowWarning = ((thresholdBelowWarningString != null) &&
                            (!thresholdBelowWarningString.isEmpty())) ?
                            Integer.valueOf(thresholdBelowWarningString) : Integer.MIN_VALUE;
                    int thresholdBelowCritical = ((thresholdBelowCriticalString != null) &&
                            (!thresholdBelowCriticalString.isEmpty())) ?
                            Integer.valueOf(thresholdBelowCriticalString) : Integer.MIN_VALUE;
                    int intValue = Integer.valueOf(dataReceived);
                    if ((intValue >= thresholdAboveCritical) || (intValue <= thresholdBelowCritical)) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((intValue >= thresholdAboveWarning) || (intValue <= thresholdBelowWarning)) {
                        return Enumerators.NotificationState.WARNING;
                    } else {
                        return Enumerators.NotificationState.NONE;
                    }
                } else if (dataType.equals(Enumerators.DataType.DECIMAL)) {
                    float thresholdAboveWarning = ((thresholdAboveWarningString != null) &&
                            (!thresholdAboveWarningString.isEmpty())) ?
                            Float.valueOf(thresholdAboveWarningString) : Float.MAX_VALUE;
                    float thresholdAboveCritical = ((thresholdAboveCriticalString != null) &&
                            (!thresholdAboveCriticalString.isEmpty())) ?
                            Float.valueOf(thresholdAboveCriticalString) : Float.MAX_VALUE;
                    float thresholdBelowWarning = ((thresholdBelowWarningString != null) &&
                            (!thresholdBelowWarningString.isEmpty())) ?
                            Float.valueOf(thresholdBelowWarningString) : Float.MIN_VALUE;
                    float thresholdBelowCritical = ((thresholdBelowCriticalString != null) &&
                            (!thresholdBelowCriticalString.isEmpty())) ?
                            Float.valueOf(thresholdBelowCriticalString) : Float.MIN_VALUE;
                    float floatValue = Float.valueOf(dataReceived);
                    if ((floatValue >= thresholdAboveCritical) || (floatValue <= thresholdBelowCritical)) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((floatValue >= thresholdAboveWarning) || (floatValue <= thresholdBelowWarning)) {
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
                    if ((thresholdEqualsCritical != null) || (booleanValue == thresholdEqualsCritical.booleanValue())) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((thresholdEqualsWarning != null) || (booleanValue == thresholdEqualsWarning.booleanValue())) {
                        return Enumerators.NotificationState.WARNING;
                    } else {
                        return Enumerators.NotificationState.NONE;
                    }
                } else {
                    if ((thresholdEqualsCriticalString != null) || (dataReceived.equals(thresholdEqualsCriticalString))) {
                        return Enumerators.NotificationState.CRITICAL;
                    } else if ((thresholdEqualsWarningString != null) || (dataReceived.equals(thresholdEqualsWarningString))) {
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
