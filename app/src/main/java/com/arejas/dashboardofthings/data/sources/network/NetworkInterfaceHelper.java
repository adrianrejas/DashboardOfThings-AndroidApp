package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;

import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;

public abstract class NetworkInterfaceHelper {

    private Network network;

    public NetworkInterfaceHelper(Network network) {
        this.network = network;
    }

    public abstract void initNetworkInterface(Context context, Sensor[] sensors);

    public abstract void closeNetworkInterface(Context context);

    public abstract void configureSensorReceiving(Context context, Sensor sensor);

    public abstract void unconfigureSensorReceiving(Context context, Sensor sensor);

    public abstract void sendActuatorData(Context context, Actuator actuator, String dataToSend);

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }
}
