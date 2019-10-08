package com.arejas.dashboardofthings.utils.rx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arejas.dashboardofthings.domain.entities.DataValue;
import com.arejas.dashboardofthings.domain.entities.Log;
import com.arejas.dashboardofthings.utils.Enumerators;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

public class RxHelper {

    // Subject to communicate data values received from sensors
    private static final BehaviorSubject<DataValue> sensorDataSubject = BehaviorSubject.create();

    // Subject to communicate logs generated
    private static final BehaviorSubject<Log> logSubject = BehaviorSubject.create();

    // Receive data values from all sensors
    public static Disposable subscribeToAllSensorsData(@NonNull Consumer<DataValue> action) {
        return sensorDataSubject.subscribe(action);
    }

    // Receive data values from one sensor
    public static Disposable subscribeToOneSensorData(
            @NonNull Integer sensorId, @NonNull Consumer<DataValue> action) {
        return sensorDataSubject
                .filter(dataValue -> sensorId.equals(dataValue.getSensorId()))
                .subscribe(action);
    }

    // Send data values to those interested
    private static void publishSensorData(@NonNull DataValue message) {
        sensorDataSubject.onNext(message);
    }

    public static void publishSensorData(Integer sensorId, String sensorData) {
        try {
            DataValue value = new DataValue();
            value.setSensorId(sensorId);
            value.setValue(sensorData);
            value.setDateReceived(new Date());
            publishSensorData(value);
        }  catch (Exception e) {
            e.printStackTrace();    // We don't report through RX this exception for avoiding loops
        }
    }

    // Receive logs from all elements
    public static Disposable subscribeToAllLogs(@NonNull Consumer<Log> action) {
        return logSubject.subscribe(action);
    }

    // Receive logs from all elements with a minimum log level
    public static Disposable subscribeToAllLogsWithMinLogLevel(
            @NonNull Enumerators.LogLevel logLevel, @NonNull Consumer<Log> action) {
        return logSubject
                .filter(log -> logLevel.ordinal() <= log.getLogLevel().ordinal())
                .subscribe(action);
    }

    // Receive logs from one element with a minimum log level if wanted
    public static Disposable subscribeToOneElementLogs(
            @NonNull Integer elementId, @NotNull Enumerators.ElementType elementType,
            @Nullable Enumerators.LogLevel logLevel,
            @NonNull Consumer<Log> action) {
        return logSubject
                .filter(log ->
                        elementId.equals(log.getElementId()) &&
                                elementType.equals(log.getElementType()) &&
                                ((logLevel == null) || (logLevel.ordinal() <= log.getLogLevel().ordinal())) )
                .subscribe(action);
    }

    // Send logs to those interested
    private static void publishLog(@NonNull Log message) {
        logSubject.onNext(message);
    }

    public static void publishLog(Integer elementId, Enumerators.ElementType elementType,
                                  String elementName, Enumerators.LogLevel logLevel, String message) {
        try {
            Log log = new Log();
            log.setElementId(elementId);
            log.setElementName(elementName);
            log.setElementType(elementType);
            log.setLogLevel(logLevel);
            log.setLogMessage(message);
            log.setDateRegistered(new Date());
            publishLog(log);
        } catch (Exception e) {
            e.printStackTrace();    // We don't report through RX this exception for avoiding loops
        }
    }
}
