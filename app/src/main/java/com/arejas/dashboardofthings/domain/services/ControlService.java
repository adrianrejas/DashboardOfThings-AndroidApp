package com.arejas.dashboardofthings.domain.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.data.sources.network.HttpNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.MqttNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.NetworkInterfaceHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.presentation.ui.widget.SensorWidgetService;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.android.AndroidInjection;

public class ControlService extends Service {

    Map<Integer, NetworkInterfaceHelper> networkHelpers;

    private final Object networkHelpersLock = new Object();

    @Inject
    DotRepository dotRepository;
    @Inject
    @Named("dbExecutorManagement")
    Executor dbExecutorManagement;

    private List<Network> networks;
    private List<Sensor> sensors;

    private boolean initiated;

    @Override
    public void onCreate() {

        startForeground(NotificationsHelper.FOREGROUND_SERVICE_NOTIFICATION_ID,
                NotificationsHelper.showNotificationForegroundService(getApplicationContext()));

        AndroidInjection.inject(this);

        this.networkHelpers = new HashMap<>();

        if (!initiated) {
            dbExecutorManagement.execute(() -> {
                initializeNetworkHelpers();
                initializeSubscriptionsToNetworksAndSensorsManagementChanges();
                initializeSubscriptionsToActuatorDataUpdates();
                initializeSubscriptionsToSensorReloadRequests();
                initializeSubscriptionsToDataReceived();
            });
        }
        initiated = true;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void initializeNetworkHelpers() {
        synchronized (networkHelpersLock) {
            sensors = dotRepository.getListOfSensorsBlocking();
            networks = dotRepository.getListOfNetworksBlocking();
            if (networks != null) {
                for (Network network : networks) {
                    initNetworkHelper(network);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        closeNetworkHelpers();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeSubscriptionsToNetworksAndSensorsManagementChanges() {
        RxHelper.subscribeToAllNetworskManagementChanges(networkManagementPair -> {
            if (networkManagementPair != null)
                switch (networkManagementPair.second) {
                    case CREATE:
                        initNetworkHelper(networkManagementPair.first);
                        break;
                    case UPDATE:
                        restartNetworkHelper(networkManagementPair.first);
                        break;
                    case DELETE:
                        closeNetworkHelper(networkManagementPair.first);
                        break;
            }
        });
        RxHelper.subscribeToAllSensorsManagementChanges(sensorManagementPair -> {
            if (sensorManagementPair != null)
                switch (sensorManagementPair.second) {
                    case CREATE:
                        registerSensorInNetwork(sensorManagementPair.first);
                        break;
                    case UPDATE:
                        restartRegisterSensorInNetwork(sensorManagementPair.first);
                        break;
                    case DELETE:
                        unregisterSensorInNetwork(sensorManagementPair.first);
                        break;
                }
        });
    }

    private void initializeSubscriptionsToActuatorDataUpdates() {
        RxHelper.subscribeToAllActuatorUpdates(message -> {
            if (message != null)
                sendActuatorUpdateToNetworkHelper(message.getActuator(), message.getData());
        });
    }

    private void initializeSubscriptionsToSensorReloadRequests() {
        RxHelper.subscribeToAllSensorReloadRequests(sensor -> {
            if (sensor != null)
                sendSensorReloadRequestToNetworkHelper(sensor);
        });
    }

    private void initializeSubscriptionsToDataReceived() {
        RxHelper.subscribeToAllSensorsData(dataValue -> {
            if (dataValue != null) {
                int widgetId = SensorWidgetService.getWidgetIdForSensorId(getApplicationContext(), dataValue.getSensorId());
                if (widgetId != SensorWidgetService.UNKNOWN_ELEMENT_ID) {
                    SensorWidgetService.startActionUpdateWidgetSetData(getApplicationContext(), widgetId, dataValue.getValue());
                }
            }
        });
    }

    public void closeNetworkHelpers() {
        synchronized (networkHelpersLock) {
            if (sensors != null) {
                for (Sensor sensor : sensors) {
                    unregisterSensorInNetwork(sensor);
                }
            }
            if (networks != null) {
                for (Network network : networks) {
                    closeNetworkHelper(network);
                }
            }
        }
    }

    private boolean initNetworkHelper(@NotNull Network network) {
        synchronized (networkHelpersLock) {
            try {
                closeNetworkHelper(network);
                NetworkInterfaceHelper helper = null;
                switch (network.getNetworkType()) {
                    case HTTP:
                        helper = new HttpNetworkInterfaceHelper(network);
                        break;
                    case MQTT:
                        helper = new MqttNetworkInterfaceHelper(network);
                        break;
                }
                if (helper != null) {
                    List<Sensor> initialSensors = new ArrayList<>();
                    if (sensors != null) {
                        for (Sensor sensor : sensors) {
                            if (sensor.getNetworkId().equals(network.getId())) {
                                initialSensors.add(sensor);
                            }
                        }
                    }
                    helper.initNetworkInterface(getApplicationContext(),
                            initialSensors.toArray(new Sensor[initialSensors.size()]));
                    this.networkHelpers.put(network.getId(), helper);
                    return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean closeNetworkHelper(@NotNull Network network) {
        synchronized (networkHelpersLock) {
            try {
                if (this.networkHelpers.containsKey(network.getId())) {
                    this.networkHelpers.get(network.getId()).closeNetworkInterface(getApplicationContext());
                    this.networkHelpers.remove(network.getId());
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean restartNetworkHelper(@NotNull Network network) {
        synchronized (networkHelpersLock) {
            closeNetworkHelper(network);
            return initNetworkHelper(network);
        }
    }

    private boolean registerSensorInNetwork(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            try {
                if (networkHelpers.containsKey(sensor.getNetworkId())) {
                    return networkHelpers.get(sensor.getNetworkId()).configureSensorReceiving(getApplicationContext(), sensor);
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean unregisterSensorInNetwork(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            try {
                if (networkHelpers.containsKey(sensor.getNetworkId())) {
                    return networkHelpers.get(sensor.getNetworkId()).unconfigureSensorReceiving(getApplicationContext(), sensor);
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean restartRegisterSensorInNetwork(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            if (unregisterSensorInNetwork(sensor)) {
                return registerSensorInNetwork(sensor);
            }
            return false;
        }
    }

    private boolean sendActuatorUpdateToNetworkHelper(@NotNull Actuator actuator, @NotNull String data) {
        synchronized (networkHelpersLock) {
            try {
                if (networkHelpers.containsKey(actuator.getNetworkId())) {
                    return networkHelpers.get(actuator.getNetworkId()).sendActuatorData(getApplicationContext(), actuator, data);
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean sendSensorReloadRequestToNetworkHelper(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            try {
                if (networkHelpers.containsKey(sensor.getNetworkId())) {
                    return networkHelpers.get(sensor.getNetworkId()).requestSensorReload(getApplicationContext(), sensor);
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
