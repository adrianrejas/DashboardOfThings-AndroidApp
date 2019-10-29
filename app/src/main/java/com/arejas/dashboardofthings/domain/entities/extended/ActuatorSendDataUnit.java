package com.arejas.dashboardofthings.domain.entities.extended;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;

public class ActuatorSendDataUnit {

    private Actuator actuator;

    private String data;

    public ActuatorSendDataUnit(Actuator actuator, String data) {
        this.actuator = actuator;
        this.data = data;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public void setActuator(Actuator actuator) {
        this.actuator = actuator;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
