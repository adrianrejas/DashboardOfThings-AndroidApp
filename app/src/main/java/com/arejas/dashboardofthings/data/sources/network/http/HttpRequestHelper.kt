package com.arejas.dashboardofthings.data.sources.network.http

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.format.DataTransformationHelper
import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper

import java.io.IOException
import java.net.MalformedURLException
import java.util.Arrays
import java.util.NoSuchElementException

import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

import okhttp3.Authenticator
import okhttp3.ConnectionSpec
import okhttp3.Credentials
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route

object HttpRequestHelper {

    /**
     * Send a HTTP request for getting a data value for a sensor and process the result.
     */
    fun sendSensorDataHttpRequest(context: Context, network: Network, sensor: Sensor) {
        try {
            require(network.networkType == Enumerators.NetworkType.HTTP)
            val socketFactory: SSLSocketFactory? = null
            val trustManager: X509TrustManager? = null
            if (network.httpConfiguration!!.httpUseSsl!!) {
                if (network.httpConfiguration!!.certAuthorityUri != null && !network.httpConfiguration!!.certAuthorityUri!!.isEmpty()) {
                    // TODO The use of CA certificates for HTTPS connection hasn't be tested yet
                    // because of that, and as okhttp don't require it for establishing SSL connection,
                    // the functionality code has been commented for now.
                    /*SslUtility.getInstance().createSocketFactoryAndTrustManager(network.getId(),
                            network.getHttpConfiguration().getCertAuthorityUri());
                    socketFactory = SslUtility.getInstance().getSocketFactory(network.getId());
                    trustManager = SslUtility.getInstance().getTrustedManager(network.getId());
                    if ((socketFactory == null) || (trustManager == null)) {
                        throw new SSLException("");
                    }*/
                }
            }
            val url = Uri.parse(network.httpConfiguration!!.httpBaseUrl)
                .buildUpon()
                .appendEncodedPath(sensor.httpRelativeUrl)
                .toString()
            val response = callToHttpRest(
                url, sensor.httpHeaders,
                Enumerators.HttpMethod.GET, null, null,
                network.httpConfiguration!!.httpAauthenticationType,
                network.httpConfiguration!!.httpUsername,
                network.httpConfiguration!!.httpPassword,
                network.httpConfiguration!!.httpUseSsl!!, socketFactory, trustManager
            )
            if (response.isSuccessful) {
                if (response.body != null) {
                    val messageBody = response.body!!.string()
                    val data = DataMessageHelper.extractDataFromSensorResponse(messageBody, sensor)
                    if (data != null) {
                        if (DataTransformationHelper.checkIfDataTypeIsCorrect(
                                data,
                                sensor.dataType!!
                            )
                        ) {
                            DotRepository.checkThresholdsForDataReceived(context, sensor, data)
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
                        context.getString(R.string.log_critical_http_message_body)
                    )
                }
            } else {
                RxHelper.publishLog(
                    sensor.id, Enumerators.ElementType.SENSOR,
                    sensor.name, Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_http_bad_response, response.code)
                )
            }
        } catch (e: IllegalArgumentException) {
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_wrong_network_conf)
            )
        } catch (e: SSLException) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_wrong_ssl_network_conf)
            )
        } catch (e: MalformedURLException) {
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_malformed_url)
            )
        } catch (e: UnsupportedOperationException) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_unrecognized_http_method)
            )
        } catch (e: NoSuchElementException) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_http_no_response)
            )
        } catch (e: Exception) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_unexpected_http_network)
            )
        }

    }

    /**
     * Send a HTTP request for transmitting data from an actuator and process the result.
     */
    fun sendActuatorCommand(
        context: Context, network: Network,
        actuator: Actuator, dataToSend: String
    ) {
        try {
            require(network.networkType == Enumerators.NetworkType.HTTP)
            val socketFactory: SSLSocketFactory? = null
            val trustManager: X509TrustManager? = null
            if (network.httpConfiguration!!.httpUseSsl!!) {
                if (network.httpConfiguration!!.certAuthorityUri != null && !network.httpConfiguration!!.certAuthorityUri!!.isEmpty()) {
                    // TODO The use of CA certificates for HTTPS connection hasn't be tested yet
                    // because of that, and as okhttp don't require it for establishing SSL connection,
                    // the functionality code has been commented for now.
                    /*SslUtility.getInstance().createSocketFactoryAndTrustManager(network.getId(),
                            network.getHttpConfiguration().getCertAuthorityUri());
                    socketFactory = SslUtility.getInstance().getSocketFactory(network.getId());
                    trustManager = SslUtility.getInstance().getTrustedManager(network.getId());
                    if ((socketFactory == null) || (trustManager == null)) {
                        throw new SSLException("");
                    }*/
                }
            }
            if (DataTransformationHelper.checkIfDataTypeIsCorrect(
                    dataToSend,
                    actuator.dataType!!
                )
            ) {
                val messageToSend = DataMessageHelper.formatActuatorMessage(dataToSend, actuator)
                if (messageToSend != null) {
                    val url = Uri.parse(network.httpConfiguration!!.httpBaseUrl)
                        .buildUpon()
                        .appendEncodedPath(actuator.httpRelativeUrl)
                        .toString()
                    val response = callToHttpRest(
                        url, actuator.httpHeaders,
                        actuator.httpMethod, messageToSend, actuator.httpMimeType,
                        network.httpConfiguration!!.httpAauthenticationType,
                        network.httpConfiguration!!.httpUsername,
                        network.httpConfiguration!!.httpPassword,
                        network.httpConfiguration!!.httpUseSsl!!, socketFactory, trustManager
                    )
                    if (response.isSuccessful) {
                        RxHelper.publishLog(
                            actuator.id, Enumerators.ElementType.ACTUATOR,
                            actuator.name, Enumerators.LogLevel.INFO,
                            context.getString(
                                R.string.log_info_actuator_command_sent_success,
                                dataToSend
                            )
                        )
                    } else {
                        RxHelper.publishLog(
                            actuator.id, Enumerators.ElementType.ACTUATOR,
                            actuator.name, Enumerators.LogLevel.ERROR,
                            context.getString(
                                R.string.log_critical_http_bad_response,
                                response.code
                            )
                        )
                    }
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
        } catch (e: IllegalArgumentException) {
            RxHelper.publishLog(
                actuator.id, Enumerators.ElementType.ACTUATOR,
                actuator.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_wrong_network_conf)
            )
        } catch (e: SSLException) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_wrong_ssl_network_conf)
            )
        } catch (e: MalformedURLException) {
            RxHelper.publishLog(
                actuator.id, Enumerators.ElementType.ACTUATOR,
                actuator.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_malformed_url)
            )
        } catch (e: UnsupportedOperationException) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_unrecognized_http_method)
            )
        } catch (e: NoSuchElementException) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_http_no_response)
            )
        } catch (e: Exception) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_unexpected_http_network)
            )
        }

    }

    @Throws(Exception::class)
    private fun callToHttpRest(
        url: String, headers: Map<String, String>?,
        httpMethod: Enumerators.HttpMethod?,
        messageBody: String?, mimeType: String?,
        authenticationType: Enumerators.HttpAuthenticationType?,
        username: String?, password: String?,
        usesSslConnection: Boolean,
        sslSocketFactory: SSLSocketFactory?,
        trustManager: X509TrustManager?
    ): Response {
        var clientBuilder = OkHttpClient.Builder()
        if (usesSslConnection) {
            clientBuilder = clientBuilder.connectionSpecs(
                Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)
            )
        }

        // TODO The use of CA certificates for HTTPS connection hasn't be tested yet
        // because of that, and as okhttp don't require it for establishing SSL connection,
        // the functionality code has been commented for now.
        /*if (sslSocketFactory != null) {
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
        }*/

        if (authenticationType != Enumerators.HttpAuthenticationType.NONE) {
            clientBuilder.authenticator(object : Authenticator {
                @Throws(IOException::class)
                override fun authenticate(route: Route?, response: Response): Request? {
                    val credential = Credentials.basic(username!!, password!!)
                    return response.request.newBuilder().header("Authorization", credential).build()
                }
            })
        }

        val client = clientBuilder.build()

        val requestBuilder = Request.Builder()

        if (!URLUtil.isValidUrl(url)) {
            throw MalformedURLException("")
        }
        requestBuilder.url(url)

        if (headers != null) {
            for (header in headers.keys) {
                requestBuilder.addHeader(header, headers[header]!!)
            }
        }
        var type: MediaType? = null
        if (mimeType != null) {
            type = mimeType.toMediaType()
            require(!(type == null && httpMethod != Enumerators.HttpMethod.GET)) { "" }
        }

        when (httpMethod) {
            Enumerators.HttpMethod.GET -> requestBuilder.get()
            Enumerators.HttpMethod.PUT -> requestBuilder.put(
                messageBody!!
                    .toRequestBody(type)
            )
            Enumerators.HttpMethod.POST -> requestBuilder.post(
                messageBody!!
                    .toRequestBody(type)
            )
            Enumerators.HttpMethod.PATCH -> requestBuilder.patch(
                messageBody!!
                    .toRequestBody(type)
            )
            else -> throw UnsupportedOperationException("")
        }
        return client.newCall(requestBuilder.build()).execute()
            ?: throw NoSuchElementException("")
    }

}
