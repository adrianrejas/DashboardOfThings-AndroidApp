package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.format.DataTransformationHelper;
import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper;
import com.arejas.dashboardofthings.data.sources.network.mqtt.SocketFactory;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
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
import org.eclipse.paho.client.mqttv3.MqttTopic;

import java.io.InputStream;

public class MqttNetworkInterfaceHelper extends NetworkInterfaceHelper {

    private MqttAndroidClient mqttAndroidClient;

    public MqttNetworkInterfaceHelper(Network network) {
        super(network);
    }

    @Override
    public boolean initNetworkInterface(Context context, Sensor[] sensors) {
        try {
            mqttAndroidClient = new MqttAndroidClient(context,
                    getNetwork().getMqttConfiguration().getMqttBrokerUrl(),
                    getNetwork().getMqttConfiguration().getMqttClientId());
            mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                            getNetwork().getName(), Enumerators.LogLevel.INFO,
                            context.getString(R.string.log_info_mqtt_network_connected));
                    for (Sensor sensor : sensors) {
                        configureSensorReceiving(context, sensor);
                    }
                }

                @Override
                public void connectionLost(Throwable throwable) {
                    RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                            getNetwork().getName(), Enumerators.LogLevel.ERROR,
                            context.getString(R.string.log_critical_mqtt_connection_lost));
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    try {
                        // For each sensor, check if its topic filter matches
                        for (Sensor sensor : getSensorsRegistered().values()) {
                            if (MqttTopic.isMatched(sensor.getMqttTopicToSubscribe(), topic)) {
                                // If filter matches, extract data and report data received
                                if (mqttMessage.getPayload() != null) {
                                    String messageBody = new String(mqttMessage.getPayload());
                                    String data = DataMessageHelper.extractDataFromSensorResponse(messageBody, sensor);
                                    if (data != null) {
                                        if (DataTransformationHelper.checkIfDataTypeIsCorrect(data, sensor.getDataType())) {
                                            DotRepository.checkThresholdsForDataReceived(context, sensor, data);
                                            RxHelper.publishSensorData(sensor.getId(), data);
                                        } else {
                                            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                                                    sensor.getName(), Enumerators.LogLevel.ERROR,
                                                    context.getString(R.string.log_critical_data_format));
                                        }
                                    } else {
                                        RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                                                sensor.getName(), Enumerators.LogLevel.ERROR,
                                                context.getString(R.string.log_critical_message_parser));
                                    }
                                } else {
                                    RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                                            sensor.getName(), Enumerators.LogLevel.ERROR,
                                            context.getString(R.string.log_critical_mqtt_message_body));
                                }
                            }
                        }
                    } catch (Exception e) {
                        RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                                getNetwork().getName(), Enumerators.LogLevel.ERROR,
                                context.getString(R.string.log_critical_mqtt_connection_fail));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                            getNetwork().getName(), Enumerators.LogLevel.INFO,
                            context.getString(R.string.log_info_mqtt_network_connected));
                }
            });
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(true);
            mqttConnectOptions.setCleanSession(getNetwork().getMqttConfiguration().getMqttCleanSession());
            if ((getNetwork().getMqttConfiguration().getMqttUsername() != null) &&
                    (!getNetwork().getMqttConfiguration().getMqttUsername().isEmpty()))
                mqttConnectOptions.setUserName(getNetwork().getMqttConfiguration().getMqttUsername());
            if ((getNetwork().getMqttConfiguration().getMqttPassword() != null) &&
                    (!getNetwork().getMqttConfiguration().getMqttPassword().isEmpty()))
                mqttConnectOptions.setPassword(getNetwork().getMqttConfiguration().getMqttPassword().toCharArray());
            if ((getNetwork().getMqttConfiguration().getMqttConnTimeout() != null) &&
            (!getNetwork().getMqttConfiguration().getMqttConnTimeout().equals(0)))
                mqttConnectOptions.setConnectionTimeout(getNetwork().getMqttConfiguration().getMqttConnTimeout());
            if ((getNetwork().getMqttConfiguration().getMqttKeepaliveInterval() != null) &&
                    (!getNetwork().getMqttConfiguration().getMqttKeepaliveInterval().equals(0)))
                mqttConnectOptions.setKeepAliveInterval(getNetwork().getMqttConfiguration().getMqttKeepaliveInterval());
            if (getNetwork().getMqttConfiguration().getMqttUseSsl()) {
                SocketFactory.SocketFactoryOptions socketFactoryOptions = new SocketFactory.SocketFactoryOptions();
                Uri certUri = Uri.parse(getNetwork().getMqttConfiguration().getMqttCertAuthorityUri());
                InputStream certInputStream = DotApplication.getContext().getContentResolver().openInputStream(certUri);
                socketFactoryOptions.withCaInputStream(certInputStream);
                mqttConnectOptions.setSocketFactory(new SocketFactory(socketFactoryOptions));
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
                                getNetwork().getName(), Enumerators.LogLevel.ERROR,
                                context.getString(R.string.log_critical_mqtt_connection_fail));
                        exception.printStackTrace();
                    }
                });
                return true;
            } catch (MqttException e) {
                RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                        getNetwork().getName(), Enumerators.LogLevel.ERROR,
                        context.getString(R.string.log_critical_mqtt_connection_fail));
                return false;
            }
        } catch (Exception e) {
            RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                    getNetwork().getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_mqtt_connection_fail));
            return false;
        }
    }

    @Override
    public boolean closeNetworkInterface(Context context) {
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect();
                mqttAndroidClient.setCallback(null);
                mqttAndroidClient = null;
            }
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(getNetwork().getId(), Enumerators.ElementType.NETWORK,
                    getNetwork().getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_mqtt_disconnection_fail));
            return false;
        }
    }

    @Override
    public boolean configureSensorReceiving(Context context, Sensor sensor) {
        try {
            mqttAndroidClient.subscribe(sensor.getMqttTopicToSubscribe(), sensor.getMqttQosLevel().ordinal());
            registerSensor(sensor);
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    sensor.getName(), Enumerators.LogLevel.INFO,
                    context.getString(R.string.log_critical_mqtt_sensor_subscription_success));
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    sensor.getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_mqtt_sensor_subscription_fail));
            // Retry the subscription in 60 seconds
            (new Handler()).postDelayed(() -> {
                configureSensorReceiving(context, sensor);
            }, 60000);
            return false;
        }
    }

    @Override
    public boolean unconfigureSensorReceiving(Context context, Sensor sensor) {
        try {
            unregisterSensor(sensor);
            boolean otherSensorUsingSameTopicFilter = false;
            for (Sensor otherSensor : getSensorsRegistered().values()) {
                if (otherSensor.getMqttTopicToSubscribe().equals(sensor.getMqttTopicToSubscribe())) {
                    otherSensorUsingSameTopicFilter = true;
                    break;
                }
            }
            if (!otherSensorUsingSameTopicFilter) {
                mqttAndroidClient.unsubscribe(sensor.getMqttTopicToSubscribe());
            }
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    sensor.getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_mqtt_sensor_unsubscription_fail));
            return false;
        }
    }

    @Override
    public boolean sendActuatorData(Context context, Actuator actuator, String dataToSend) {
        try {
            if (DataTransformationHelper.checkIfDataTypeIsCorrect(dataToSend, actuator.getDataType())) {
                String messageToSend = DataMessageHelper.formatActuatorMessage(dataToSend, actuator);
                if (messageToSend != null) {
                    mqttAndroidClient.publish(actuator.getMqttTopicToPublish(),
                            messageToSend.getBytes(), actuator.getMqttQosLevel().ordinal(),
                            false);
                } else {
                    RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                            actuator.getName(), Enumerators.LogLevel.ERROR,
                            context.getString(R.string.log_critical_message_building));
                }
            } else {
                RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                        actuator.getName(), Enumerators.LogLevel.ERROR,
                        context.getString(R.string.log_critical_data_format));
            }
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                    actuator.getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_mqtt_actuator_send_failed));
            return false;
        }
    }

    @Override
    public boolean requestSensorReload(Context context, Sensor sensor) {
        // Not applicable in MQTT networks
        return false;
    }

}
