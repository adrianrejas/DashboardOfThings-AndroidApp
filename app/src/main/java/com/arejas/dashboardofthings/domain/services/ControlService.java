package com.arejas.dashboardofthings.domain.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.data.sources.network.HttpNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.MqttNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.NetworkInterfaceHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ControlService extends Service {

    Map<Integer, NetworkInterfaceHelper> networkHelpers;

    private final Object networkHelpersLock = new Object();

    @Inject
    DotRepository dotRepository;
    private List<Network> networks;
    private List<Sensor> sensors;

    @Override
    public void onCreate() {
        this.networkHelpers = new HashMap<>();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NotificationsHelper.FOREGROUND_SERVICE_NOTIFICATION_ID,
                NotificationsHelper.showNotificationForegroundService(getApplicationContext()));
        initializeNetworkHelpers();
        initializeSubscriptionsToNetworksAndSensorsManagementChanges();
        initializeSubscriptionsToActuatorDataUpdates();
        initializeSubscriptionsToSensorReloadRequests();
        return START_NOT_STICKY;
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

    public static void startAsForegroundService(Context context) {
        Intent serviceIntent = new Intent(context, ControlService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    private void initializeSubscriptionsToNetworksAndSensorsManagementChanges() {
        RxHelper.subscribeToAllNetworskManagementChanges(networkManagementPair -> {
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
        RxHelper.subscribeToAllActuatorUpdates(actuatorDataPair -> {
            sendActuatorUpdateToNetworkHelper(actuatorDataPair.first, actuatorDataPair.second);
        });
    }

    private void initializeSubscriptionsToSensorReloadRequests() {
        RxHelper.subscribeToAllSensorReloadRequests(sensor -> {
            sendSensorReloadRequestToNetworkHelper(sensor);
        });
    }

    public void initializeNetworkHelpers() {
        synchronized (networkHelpersLock) {
            networks = dotRepository.getListOfNetworksBlocking();
            if (networks != null) {
                for (Network network : networks) {
                    initNetworkHelper(network);
                }
            }
            sensors = dotRepository.getListOfSensorsBlocking();
            if (sensors != null) {
                for (Sensor sensor : sensors) {
                    registerSensorInNetwork(sensor);
                }
            }
        }
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
                    for (Sensor sensor : sensors) {
                        if (sensor.getNetworkId().equals(network.getId())) {
                            initialSensors.add(sensor);
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
            if(closeNetworkHelper(network))
                return initNetworkHelper(network);
            return false;
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
