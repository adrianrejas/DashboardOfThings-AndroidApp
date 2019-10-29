package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;

import java.util.HashMap;
import java.util.Map;

public abstract class NetworkInterfaceHelper {

    private Network network;

    private Map<Integer, Sensor> sensorsRegistered;

    private final Object sensorsLock = new Object();

    public NetworkInterfaceHelper(Network network) {
        this.network = network;
        sensorsRegistered = new HashMap<>();
    }

    public abstract boolean initNetworkInterface(Context context, Sensor[] sensors);

    public abstract boolean closeNetworkInterface(Context context);

    public abstract boolean configureSensorReceiving(Context context, Sensor sensor);

    public abstract boolean unconfigureSensorReceiving(Context context, Sensor sensor);

    public abstract boolean sendActuatorData(Context context, Actuator actuator, String dataToSend);

    public abstract boolean requestSensorReload(Context context, Sensor sensor);

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Map<Integer, Sensor> getSensorsRegistered() {
        synchronized (sensorsLock) {
            Map<Integer, Sensor> returnedMap = new HashMap<>();
            returnedMap.putAll(sensorsRegistered);
            return returnedMap;
        }
    }

    public void setSensorsRegistered(Map<Integer, Sensor> sensorsRegistered) {
        synchronized (sensorsLock) {
            this.sensorsRegistered = sensorsRegistered;
        }
    }

    public void registerSensor(Sensor sensor) {
        synchronized (sensorsLock) {
            this.sensorsRegistered.put(sensor.getId(), sensor);
        }
    }

    public void unregisterSensor(Sensor sensor) {
        synchronized (sensorsLock) {
            this.sensorsRegistered.remove(sensor.getId());
        }
    }
}
