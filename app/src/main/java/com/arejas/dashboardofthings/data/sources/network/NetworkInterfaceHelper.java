package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class NetworkInterfaceHelper {

    private Network network;

    private Map<Integer, Sensor> sensorsRegistered;

    public NetworkInterfaceHelper(Network network) {
        this.network = network;
        sensorsRegistered = new HashMap<>();
    }

    public abstract boolean initNetworkInterface(Context context, Sensor[] sensors);

    public abstract boolean closeNetworkInterface(Context context);

    public abstract boolean configureSensorReceiving(Context context, Sensor sensor);

    public abstract boolean unconfigureSensorReceiving(Context context, Sensor sensor);

    public abstract boolean sendActuatorData(Context context, Actuator actuator, String dataToSend);

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Map<Integer, Sensor> getSensorsRegistered() {
        return sensorsRegistered;
    }

    public void setSensorsRegistered(Map<Integer, Sensor> sensorsRegistered) {
        this.sensorsRegistered = sensorsRegistered;
    }
}
