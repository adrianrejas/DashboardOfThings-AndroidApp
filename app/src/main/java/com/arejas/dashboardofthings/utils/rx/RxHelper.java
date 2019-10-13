package com.arejas.dashboardofthings.utils.rx;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

public class RxHelper {

    // Subject to communicate element management changes to Control Service
    private static final BehaviorSubject<Pair<Network, Enumerators.ElementManagementFunction>> networkManagementSubject = BehaviorSubject.create();
    private static final BehaviorSubject<Pair<Sensor, Enumerators.ElementManagementFunction>> sensorManagementSubject = BehaviorSubject.create();
    private static final BehaviorSubject<Pair<Actuator, Enumerators.ElementManagementFunction>> actuatorManagementSubject = BehaviorSubject.create();

    // Subject to communicate actuator data updates to Control Service
    private static final BehaviorSubject<Pair<Actuator, String>> actuatorDataUpdateSubject = BehaviorSubject.create();

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

    // Receive data values from all sensors
    public static Disposable subscribeToAllActuatorUpdates(@NonNull Consumer<Pair<Actuator, String>> action) {
        return actuatorDataUpdateSubject.subscribe(action);
    }

    // Receive data values from one sensor
    public static Disposable subscribeToOneActuatorUpdates(
            @NonNull Integer actuatorId, @NonNull Consumer<Pair<Actuator, String>> action) {
        return actuatorDataUpdateSubject
                .filter(data -> actuatorId.equals(data.first.getId()))
                .subscribe(action);
    }

    // Send actuator updates to those interested
    public static void publishActuatorUpdate(@NonNull Pair<Actuator, String> message) {
        actuatorDataUpdateSubject.onNext(message);
    }

    // Receive management changes from all networks
    public static Disposable subscribeToAllNetworskManagementChanges(@NonNull Consumer<Pair<Network, Enumerators.ElementManagementFunction>> action) {
        return networkManagementSubject.subscribe(action);
    }

    // Send network management changes to those interested
    public static void publishNetworkManagementChange(@NonNull Pair<Network, Enumerators.ElementManagementFunction> message) {
        networkManagementSubject.onNext(message);
    }

    // Receive management changes from all sensors
    public static Disposable subscribeToAllSensorsManagementChanges(@NonNull Consumer<Pair<Sensor, Enumerators.ElementManagementFunction>> action) {
        return sensorManagementSubject.subscribe(action);
    }

    // Send sensor management changes to those interested
    public static void publishSensorManagementChange(@NonNull Pair<Sensor, Enumerators.ElementManagementFunction> message) {
        sensorManagementSubject.onNext(message);
    }

    // Receive management changes from all actuators
    public static Disposable subscribeToAllActuatorsManagementChanges(@NonNull Consumer<Pair<Actuator, Enumerators.ElementManagementFunction>> action) {
        return actuatorManagementSubject.subscribe(action);
    }

    // Send actuator management changes to those interested
    public static void publishActuatorManagementChange(@NonNull Pair<Actuator, Enumerators.ElementManagementFunction> message) {
        actuatorManagementSubject.onNext(message);
    }
}
