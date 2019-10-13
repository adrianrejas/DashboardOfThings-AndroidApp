package com.arejas.dashboardofthings.data.sources.network.http;

import android.content.Context;
import android.net.Uri;
import android.webkit.URLUtil;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.format.DataTransformationHelper;
import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class HttpRequestHelper {

    /**
     * Send a HTTP request for getting a data value for a sensor and process the result.
     */
    public static void sendSensorDataHttpRequest(Context context, Network network, Sensor sensor) {
        try {
            if (!network.getNetworkType().equals(Enumerators.NetworkType.HTTP)) {
                throw new IllegalArgumentException();
            }
            SSLSocketFactory socketFactory = null;
            X509TrustManager trustManager = null;
            if (network.getHttpConfiguration().getHttpUseSsl()) {
                if ((network.getHttpConfiguration().getCertAuthorityUrl() != null) &&
                        (!network.getHttpConfiguration().getCertAuthorityUrl().isEmpty())) {
                    SslUtility.getInstance().createSocketFactoryAndTrustManager(network.getId(),
                            network.getHttpConfiguration().getCertAuthorityUrl());
                    socketFactory = SslUtility.getInstance().getSocketFactory(network.getId());
                    trustManager = SslUtility.getInstance().getTrustedManager(network.getId());
                    if ((socketFactory == null) || (trustManager == null)) {
                        throw new SSLException("");
                    }
                }
            }
            String url = Uri.parse(network.getHttpConfiguration().getHttpBaseUrl())
                    .buildUpon()
                    .appendEncodedPath(sensor.getHttpRelativeUrl())
                    .toString();
            Response response = callToHttpRest(url, sensor.getHttpHeaders(),
                    Enumerators.HttpMethod.GET,null, null,
                    network.getHttpConfiguration().getHttpAauthenticationType(),
                    network.getHttpConfiguration().getHttpUsername(),
                    network.getHttpConfiguration().getHttpPassword(),
                    network.getHttpConfiguration().getHttpUseSsl(), socketFactory, trustManager);
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    String messageBody = response.body().string();
                    String data = DataMessageHelper.extractDataFromSensorResponse(messageBody, sensor);
                    if (data != null) {
                        if (DataTransformationHelper.checkIfDataTypeIsCorrect(data, sensor.getDataType())) {
                            DotRepository.checkThresholdsForDataReceived(context, sensor, data);
                            RxHelper.publishSensorData(sensor.getId(), data);
                        } else {
                            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                                    sensor.getName(),Enumerators.LogLevel.ERROR_CONF,
                                    context.getString(R.string.log_critical_data_format));
                        }
                    } else {
                        RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                                sensor.getName(),Enumerators.LogLevel.ERROR_CONF,
                                context.getString(R.string.log_critical_message_parser));
                    }
                } else {
                    RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                            sensor.getName(),Enumerators.LogLevel.ERROR_CONF,
                            context.getString(R.string.log_critical_http_message_body));
                }
            } else {
                RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                        sensor.getName(),Enumerators.LogLevel.ERROR_CONF,
                        context.getString(R.string.log_critical_http_bad_response, response.code()));
            }
        } catch (IllegalArgumentException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    sensor.getName(),Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_wrong_network_conf));
        } catch (SSLException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(),Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_wrong_ssl_network_conf));
        } catch (MalformedURLException e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    sensor.getName(),Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_malformed_url));
        } catch (UnsupportedOperationException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_unrecognized_http_method));
        } catch (NoSuchElementException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_http_no_response));
        } catch (Exception e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_unexpected_http_network));
        }
    }

    /**
     * Send a HTTP request for transmitting data from an actuator and process the result.
     */
    public static void sendActuatorCommand(Context context, Network network,
                                           Actuator actuator, String dataToSend) {
        try {
            if (!network.getNetworkType().equals(Enumerators.NetworkType.HTTP)) {
                throw new IllegalArgumentException();
            }
            SSLSocketFactory socketFactory = null;
            X509TrustManager trustManager = null;
            if (network.getHttpConfiguration().getHttpUseSsl()) {
                if ((network.getHttpConfiguration().getCertAuthorityUrl() != null) &&
                        (!network.getHttpConfiguration().getCertAuthorityUrl().isEmpty())) {
                    SslUtility.getInstance().createSocketFactoryAndTrustManager(network.getId(),
                            network.getHttpConfiguration().getCertAuthorityUrl());
                    socketFactory = SslUtility.getInstance().getSocketFactory(network.getId());
                    trustManager = SslUtility.getInstance().getTrustedManager(network.getId());
                    if ((socketFactory == null) || (trustManager == null)) {
                        throw new SSLException("");
                    }
                }
            }
            if (DataTransformationHelper.checkIfDataTypeIsCorrect(dataToSend, actuator.getDataType())) {
                String messageToSend = DataMessageHelper.formatActuatorMessage(dataToSend, actuator);
                if (messageToSend != null) {
                    String url = Uri.parse(network.getHttpConfiguration().getHttpBaseUrl())
                            .buildUpon()
                            .appendEncodedPath(actuator.getHttpRelativeUrl())
                            .toString();
                    Response response = callToHttpRest(url, actuator.getHttpHeaders(),
                            actuator.getHttpMethod(), messageToSend, actuator.getMimeType(),
                            network.getHttpConfiguration().getHttpAauthenticationType(),
                            network.getHttpConfiguration().getHttpUsername(),
                            network.getHttpConfiguration().getHttpPassword(),
                            network.getHttpConfiguration().getHttpUseSsl(), socketFactory, trustManager);
                    if (response.isSuccessful()) {
                        RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                                actuator.getName(), Enumerators.LogLevel.INFO,
                                context.getString(R.string.log_info_actuator_command_sent_success, dataToSend));
                    } else {
                        RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                                actuator.getName(), Enumerators.LogLevel.ERROR_CONF,
                                context.getString(R.string.log_critical_http_bad_response, response.code()));
                    }
                } else {
                    RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                            actuator.getName(), Enumerators.LogLevel.ERROR_CONF,
                            context.getString(R.string.log_critical_message_building));
                }
            } else {
                RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                        actuator.getName(), Enumerators.LogLevel.ERROR_CONF,
                        context.getString(R.string.log_critical_data_format));
            }
        } catch (IllegalArgumentException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_wrong_network_conf));
        } catch (SSLException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_wrong_ssl_network_conf));
        } catch (MalformedURLException e) {
            RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                    actuator.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_malformed_url));
        } catch (UnsupportedOperationException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_unrecognized_http_method));
        } catch (NoSuchElementException e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_http_no_response));
        } catch (Exception e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR_CONF,
                    context.getString(R.string.log_critical_unexpected_http_network));
        }
    }

    private static Response callToHttpRest(String url, Map<String, String> headers,
                                           Enumerators.HttpMethod httpMethod,
                                           String messageBody, String mimeType,
                                           Enumerators.HttpAuthenticationType authenticationType,
                                           String username, String password,
                                           boolean usesSslConnection,
                                           SSLSocketFactory socketFactory,
                                           X509TrustManager trustManager) throws Exception {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (usesSslConnection) {
            if ((socketFactory == null) || (trustManager == null)) {
                throw new SSLException("");
            } else {
                clientBuilder = clientBuilder.connectionSpecs(
                        Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
            }
        }
        if (!authenticationType.equals(Enumerators.HttpAuthenticationType.NONE)) {
            clientBuilder.authenticator(new Authenticator() {
                @Nullable
                @Override
                public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
                    String credential = Credentials.basic(username, password);
                    return response.request().newBuilder().header("Authorization", credential).build();
                }
            });
        }

        OkHttpClient client = clientBuilder.build();
        Request.Builder requestBuilder = new Request.Builder();

        if (!URLUtil.isValidUrl(url)){
            throw new MalformedURLException("");
        }
        if (headers != null) {
            for (String header : headers.keySet()) {
                requestBuilder.addHeader(header, headers.get(header));
            }
        }

        MediaType type = MediaType.get(mimeType);
        if ((type == null) && (!httpMethod.equals(Enumerators.HttpMethod.GET))){
            throw new IllegalArgumentException("");
        }

        switch (httpMethod) {
            case GET:
                requestBuilder.get();
                break;
            case PUT:
                requestBuilder.put(RequestBody.create(messageBody, type));
                break;
            case POST:
                requestBuilder.post(RequestBody.create(messageBody, type));
                break;
            case PATCH:
                requestBuilder.patch(RequestBody.create(messageBody, type));
                break;
            default:
                throw new UnsupportedOperationException("");
        }
        Response response = client.newCall(requestBuilder.build()).execute();
        if (response == null) {
            throw new NoSuchElementException("");
        }
        return response;
    }

}
