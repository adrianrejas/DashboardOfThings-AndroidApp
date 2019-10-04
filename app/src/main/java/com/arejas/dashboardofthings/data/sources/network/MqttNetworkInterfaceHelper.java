package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.format.DataTransformationHelper;
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper;
import com.arejas.dashboardofthings.data.sources.network.http.SslUtility;
import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;

import okhttp3.Response;

public class MqttNetworkInterfaceHelper extends NetworkInterfaceHelper {

    private MqttAndroidClient mqttAndroidClient;

    private Map<Integer, Sensor> sensorsRegistered;

    public MqttNetworkInterfaceHelper(Network network) {
        super(network);
    }

    @Override
    public void initNetworkInterface(Context context, Sensor[] sensors) {
        try {
            mqttAndroidClient = new MqttAndroidClient(context,
                    getNetwork().getMqttConfiguration().getMttBrokerUrl(),
                    getNetwork().getMqttConfiguration().getMqttClientId());
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                            Enumerators.LogLevel.INFO,
                            context.getString(R.string.log_info_mqtt_network_connected));
                    for (Sensor sensor : sensors) {
                        configureSensorReceiving(context, sensor);
                    }
                }

                @Override
                public void connectionLost(Throwable throwable) {
                    RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                            Enumerators.LogLevel.CRITICAL,
                            context.getString(R.string.log_critical_mqtt_connection_lost));
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    Log.w("Mqtt", mqttMessage.toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                            Enumerators.LogLevel.INFO,
                            context.getString(R.string.log_info_mqtt_network_connected));
                }
            });
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(getNetwork().getMqttConfiguration().getMqttCleanSession());
            mqttConnectOptions.setUserName(getNetwork().getMqttConfiguration().getMqttUsername());
            mqttConnectOptions.setPassword(getNetwork().getMqttConfiguration().getMqttPassword().toCharArray());
            mqttConnectOptions.setConnectionTimeout(getNetwork().getMqttConfiguration().getMqttConnTimeout());
            if (getNetwork().getMqttConfiguration().getMqttUseSsl()) {
                SslUtility.getInstance().createSocketFactoryAndTrustManager(getNetwork().getId(),
                        getNetwork().getMqttConfiguration().getMqttCertAuthorityUrl());
                mqttConnectOptions.setSocketFactory(SslUtility.getInstance().getSocketFactory(getNetwork().getId()));
            }
            try {

                mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                                Enumerators.LogLevel.CRITICAL,
                                context.getString(R.string.log_critical_mqtt_connection_fail));
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_mqtt_connection_fail));
        }
    }

    @Override
    public void closeNetworkInterface(Context context) {
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect();
                mqttAndroidClient.setCallback(null);
                mqttAndroidClient = null;
            }
        } catch (Exception e) {
            RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_mqtt_disconnection_fail));
        }
    }

    @Override
    public void configureSensorReceiving(Context context, Sensor sensor) {
        try {
            sensorsRegistered.put(sensor.getId(), sensor);
            mqttAndroidClient.subscribe(sensor.getMqttTopicToSubscribe(), sensor.getMqttQosLevel().ordinal());
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_mqtt_sensor_subscription_fail));
        }
    }

    @Override
    public void unconfigureSensorReceiving(Context context, Sensor sensor) {
        try {
            mqttAndroidClient.unsubscribe(sensor.getMqttTopicToSubscribe());
            sensorsRegistered.remove(sensor.getId());
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_mqtt_sensor_unsubscription_fail));
        }
    }

    @Override
    public void sendActuatorData(Context context, Actuator actuator, String dataToSend) {
        try {
            if (DataTransformationHelper.checkIfDataTypeIsCorrect(dataToSend, actuator.getDataType())) {
                String messageToSend = DataMessageHelper.formatActuatorMessage(dataToSend, actuator);
                if (messageToSend != null) {
                    mqttAndroidClient.publish(actuator.getMqttTopicToPublish(),
                            messageToSend.getBytes(), actuator.getMqttQosLevel().ordinal(),
                            false);
                } else {
                    RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                            Enumerators.LogLevel.CRITICAL,
                            context.getString(R.string.log_critical_message_building));
                }
            } else {
                RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                        Enumerators.LogLevel.CRITICAL,
                        context.getString(R.string.log_critical_data_format));
            }
        } catch (Exception e) {
            RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_mqtt_actuator_send_failed));
        }
    }
}
