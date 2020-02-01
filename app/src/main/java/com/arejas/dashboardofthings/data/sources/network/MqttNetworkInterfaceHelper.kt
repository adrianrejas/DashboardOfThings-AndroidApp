package com.arejas.dashboardofthings.data.sources.network

import android.content.Context
import android.net.Uri
import android.os.Handler

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.format.DataTransformationHelper
import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper
import com.arejas.dashboardofthings.data.sources.network.mqtt.SocketFactory
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.MqttTopic

import java.io.InputStream

class MqttNetworkInterfaceHelper(network: Network) : NetworkInterfaceHelper(network) {

    private var mqttAndroidClient: MqttAndroidClient? = null

    override fun initNetworkInterface(context: Context, sensors: Array<Sensor>): Boolean {
        try {
            mqttAndroidClient = MqttAndroidClient(
                context,
                network!!.mqttConfiguration!!.mqttBrokerUrl,
                network!!.mqttConfiguration!!.mqttClientId
            )
            mqttAndroidClient!!.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(b: Boolean, s: String) {
                    RxHelper.publishLog(
                        network!!.id, Enumerators.ElementType.NETWORK,
                        network!!.name, Enumerators.LogLevel.INFO,
                        context.getString(R.string.log_info_mqtt_network_connected)
                    )
                    for (sensor in sensors) {
                        configureSensorReceiving(context, sensor)
                    }
                }

                override fun connectionLost(throwable: Throwable) {
                    RxHelper.publishLog(
                        network!!.id, Enumerators.ElementType.NETWORK,
                        network!!.name, Enumerators.LogLevel.ERROR,
                        context.getString(R.string.log_critical_mqtt_connection_lost)
                    )
                }

                @Throws(Exception::class)
                override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                    try {
                        // For each sensor, check if its topic filter matches
                        for (sensor in sensorsRegistered.values) {
                            if (MqttTopic.isMatched(sensor.mqttTopicToSubscribe, topic)) {
                                // If filter matches, extract data and report data received
                                if (mqttMessage.payload != null) {
                                    val messageBody = String(mqttMessage.payload)
                                    val data = DataMessageHelper.extractDataFromSensorResponse(
                                        messageBody,
                                        sensor
                                    )
                                    if (data != null) {
                                        if (DataTransformationHelper.checkIfDataTypeIsCorrect(
                                                data,
                                                sensor.dataType!!
                                            )
                                        ) {
                                            DotRepository.checkThresholdsForDataReceived(
                                                context,
                                                sensor,
                                                data
                                            )
                                            RxHelper.publishSensorData(sensor.id, data)
                                        } else {
                                            RxHelper.publishLog(
                                                sensor.id, Enumerators.ElementType.SENSOR,
                                                sensor.name, Enumerators.LogLevel.ERROR,
                                                context.getString(R.string.log_critical_data_format)
                                            )
                                        }
                                    } else {
                                        RxHelper.publishLog(
                                            sensor.id, Enumerators.ElementType.SENSOR,
                                            sensor.name, Enumerators.LogLevel.ERROR,
                                            context.getString(R.string.log_critical_message_parser)
                                        )
                                    }
                                } else {
                                    RxHelper.publishLog(
                                        sensor.id, Enumerators.ElementType.SENSOR,
                                        sensor.name, Enumerators.LogLevel.ERROR,
                                        context.getString(R.string.log_critical_mqtt_message_body)
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        RxHelper.publishLog(
                            network!!.id, Enumerators.ElementType.NETWORK,
                            network!!.name, Enumerators.LogLevel.ERROR,
                            context.getString(R.string.log_critical_mqtt_connection_fail)
                        )
                    }

                }

                override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                    RxHelper.publishLog(
                        network!!.id, Enumerators.ElementType.NETWORK,
                        network!!.name, Enumerators.LogLevel.INFO,
                        context.getString(R.string.log_info_mqtt_network_connected)
                    )
                }
            })
            val mqttConnectOptions = MqttConnectOptions()
            mqttConnectOptions.isAutomaticReconnect = true
            mqttConnectOptions.isCleanSession = network!!.mqttConfiguration!!.mqttCleanSession!!
            if (network!!.mqttConfiguration!!.mqttUsername != null && !network!!.mqttConfiguration!!.mqttUsername!!.isEmpty())
                mqttConnectOptions.userName = network!!.mqttConfiguration!!.mqttUsername
            if (network!!.mqttConfiguration!!.mqttPassword != null && !network!!.mqttConfiguration!!.mqttPassword!!.isEmpty())
                mqttConnectOptions.password =
                    network!!.mqttConfiguration!!.mqttPassword!!.toCharArray()
            if (network!!.mqttConfiguration!!.mqttConnTimeout != null && network!!.mqttConfiguration!!.mqttConnTimeout != 0)
                mqttConnectOptions.connectionTimeout =
                    network!!.mqttConfiguration!!.mqttConnTimeout!!
            if (network!!.mqttConfiguration!!.mqttKeepaliveInterval != null && network!!.mqttConfiguration!!.mqttKeepaliveInterval != 0)
                mqttConnectOptions.keepAliveInterval =
                    network!!.mqttConfiguration!!.mqttKeepaliveInterval!!
            if (network!!.mqttConfiguration!!.mqttUseSsl!!) {
                val socketFactoryOptions = SocketFactory.SocketFactoryOptions()
                val certUri = Uri.parse(network!!.mqttConfiguration!!.mqttCertAuthorityUri)
                val certInputStream =
                    DotApplication.context.getContentResolver().openInputStream(certUri)
                socketFactoryOptions.withCaInputStream(certInputStream)
                mqttConnectOptions.socketFactory = SocketFactory(socketFactoryOptions)
            }
            try {
                mqttAndroidClient!!.connect(mqttConnectOptions, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        val disconnectedBufferOptions = DisconnectedBufferOptions()
                        disconnectedBufferOptions.isBufferEnabled = true
                        disconnectedBufferOptions.bufferSize = 100
                        disconnectedBufferOptions.isPersistBuffer = false
                        disconnectedBufferOptions.isDeleteOldestMessages = false
                        mqttAndroidClient!!.setBufferOpts(disconnectedBufferOptions)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        RxHelper.publishLog(
                            network!!.id, Enumerators.ElementType.NETWORK,
                            network!!.name, Enumerators.LogLevel.ERROR,
                            context.getString(R.string.log_critical_mqtt_connection_fail)
                        )
                        exception.printStackTrace()
                    }
                })
                return true
            } catch (e: MqttException) {
                RxHelper.publishLog(
                    network!!.id, Enumerators.ElementType.NETWORK,
                    network!!.name, Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_mqtt_connection_fail)
                )
                return false
            }

        } catch (e: Exception) {
            RxHelper.publishLog(
                network!!.id, Enumerators.ElementType.NETWORK,
                network!!.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_mqtt_connection_fail)
            )
            return false
        }

    }

    override fun closeNetworkInterface(context: Context): Boolean {
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient!!.unregisterResources()
                mqttAndroidClient!!.close()
                mqttAndroidClient!!.disconnect()
                mqttAndroidClient!!.setCallback(null)
                mqttAndroidClient = null
            }
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                network!!.id, Enumerators.ElementType.NETWORK,
                network!!.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_mqtt_disconnection_fail)
            )
            return false
        }

    }

    override fun configureSensorReceiving(context: Context, sensor: Sensor): Boolean {
        try {
            mqttAndroidClient!!.subscribe(
                sensor.mqttTopicToSubscribe,
                sensor.mqttQosLevel!!.ordinal
            )
            registerSensor(sensor)
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.INFO,
                context.getString(R.string.log_critical_mqtt_sensor_subscription_success)
            )
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_mqtt_sensor_subscription_fail)
            )
            // Retry the subscription in 60 seconds
            Handler().postDelayed({ configureSensorReceiving(context, sensor) }, 60000)
            return false
        }

    }

    override fun unconfigureSensorReceiving(context: Context, sensor: Sensor): Boolean {
        try {
            unregisterSensor(sensor)
            var otherSensorUsingSameTopicFilter = false
            for (otherSensor in sensorsRegistered.values) {
                if (otherSensor.mqttTopicToSubscribe == sensor.mqttTopicToSubscribe) {
                    otherSensorUsingSameTopicFilter = true
                    break
                }
            }
            if (!otherSensorUsingSameTopicFilter) {
                mqttAndroidClient!!.unsubscribe(sensor.mqttTopicToSubscribe)
            }
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_mqtt_sensor_unsubscription_fail)
            )
            return false
        }

    }

    override fun sendActuatorData(
        context: Context,
        actuator: Actuator,
        dataToSend: String
    ): Boolean {
        try {
            if (DataTransformationHelper.checkIfDataTypeIsCorrect(
                    dataToSend,
                    actuator.dataType!!
                )
            ) {
                val messageToSend = DataMessageHelper.formatActuatorMessage(dataToSend, actuator)
                if (messageToSend != null) {
                    mqttAndroidClient!!.publish(
                        actuator.mqttTopicToPublish,
                        messageToSend.toByteArray(), actuator.mqttQosLevel!!.ordinal,
                        false
                    )
                } else {
                    RxHelper.publishLog(
                        actuator.id, Enumerators.ElementType.ACTUATOR,
                        actuator.name, Enumerators.LogLevel.ERROR,
                        context.getString(R.string.log_critical_message_building)
                    )
                }
            } else {
                RxHelper.publishLog(
                    actuator.id, Enumerators.ElementType.ACTUATOR,
                    actuator.name, Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_data_format)
                )
            }
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                actuator.id, Enumerators.ElementType.ACTUATOR,
                actuator.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_mqtt_actuator_send_failed)
            )
            return false
        }

    }

    override fun requestSensorReload(context: Context, sensor: Sensor): Boolean {
        // Not applicable in MQTT networks
        return false
    }

}
