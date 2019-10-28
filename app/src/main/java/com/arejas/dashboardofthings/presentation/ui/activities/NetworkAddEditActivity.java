package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivityNetworkAddEditBinding;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkAddEditViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.helpers.AddEditNetworkPresenter;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.Utils;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Network detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link NetworkListActivity}.
 */
public class NetworkAddEditActivity extends AppCompatActivity implements AddEditNetworkPresenter {

    public static final String NETWORK_ID = "network_id";

    public static final int REQUEST_PICK_IMAGE = 41;
    public static final int REQUEST_PICK_HTTP_CERT = 42;
    public static final int REQUEST_PICK_MQTT_CERT = 43;

    private NetworkAddEditViewModel networkAddEditViewModel;

    ActivityNetworkAddEditBinding uiBinding;

    private Menu menu;

    @Inject
    ViewModelFactory viewModelFactory;

    
    boolean editionMode;
    Integer networkId;
    Network networkEdited;
    private String imagePicked;
    private String httpCertPicked;
    private String mqttCertPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_network_add_edit);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.cancel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(NETWORK_ID))) {
            networkId = savedInstanceState.getInt(NETWORK_ID);
        } if ((getIntent().getExtras() != null) && (getIntent().getExtras().containsKey(NETWORK_ID))) {
            networkId = getIntent().getIntExtra(NETWORK_ID, -1);
        } else {
            networkId = null;
        }
        if ((networkId == null) || (networkId < 0)) {
            editionMode = false;
        } else {
            editionMode = true;
        }

        /* Get view model*/
        networkAddEditViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(NetworkAddEditViewModel.class);
        networkAddEditViewModel.setNetworkId(networkId);

        (new Handler()).post(() -> {
            uiBinding.setPresenter(this);
            uiBinding.setEditionMode(editionMode);
            if (editionMode) {
                uiBinding.toolbar.setTitle(getString(R.string.toolbar_title_edit_network));
                if (networkAddEditViewModel.getNetworkBeingEdited() == null) {
                    networkAddEditViewModel.getNetwork(true).observe(this, networkExtendedResource -> {
                        try {
                            if (networkExtendedResource == null) {
                                ToastHelper.showToast(getString(R.string.toast_edition_failed));
                                finish();
                            } else {
                                if (networkExtendedResource.getStatus() == Resource.Status.ERROR) {
                                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                                    finish();
                                } else if (networkExtendedResource.getStatus() == Resource.Status.LOADING) {
                                    showLoading();
                                } else {
                                    NetworkExtended network = networkExtendedResource.getData();
                                    populateUiWithProvidedNetwork(network);
                                    networkEdited = network;
                                    networkAddEditViewModel.setNetworkBeingEdited(networkEdited);
                                    imagePicked = network.getImageUri();
                                    if (network != null) {
                                        uiBinding.setNetwork(network);
                                        uiBinding.setImagePicked(imagePicked);
                                        uiBinding.setNetworkTypeSelected(network.getNetworkType().ordinal());
                                        if (network.getNetworkType().equals(Enumerators.NetworkType.HTTP)) {
                                            uiBinding.setNetworkTypeSelected(network.getHttpConfiguration().getHttpAauthenticationType().ordinal());
                                            httpCertPicked = network.getHttpConfiguration().getCertAuthorityUri();
                                            uiBinding.setHttpCertPicked(httpCertPicked);
                                            uiBinding.setUseSslInHttp(network.getHttpConfiguration().getHttpUseSsl());
                                        } else if (network.getNetworkType().equals(Enumerators.NetworkType.MQTT)) {
                                            mqttCertPicked = network.getMqttConfiguration().getMqttCertAuthorityUri();
                                            uiBinding.setMqttCertPicked(mqttCertPicked);
                                            uiBinding.setUseSslInMqtt(network.getMqttConfiguration().getMqttUseSsl());
                                        }
                                        showEditArea();
                                    } else {
                                        ToastHelper.showToast(getString(R.string.toast_edition_failed));
                                        finish();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    networkEdited = networkAddEditViewModel.getNetworkBeingEdited();
                    try {
                        populateUiWithProvidedNetwork(networkAddEditViewModel.getNetworkBeingEdited());
                        networkEdited = networkAddEditViewModel.getNetworkBeingEdited();
                        networkAddEditViewModel.setNetworkBeingEdited(networkEdited);
                        imagePicked = networkEdited.getImageUri();
                        if (networkEdited != null) {
                            uiBinding.setNetwork(networkEdited);
                            uiBinding.setImagePicked(imagePicked);
                            uiBinding.setNetworkTypeSelected(networkEdited.getNetworkType().ordinal());
                            if (networkEdited.getNetworkType().equals(Enumerators.NetworkType.HTTP)) {
                                uiBinding.setNetworkTypeSelected(networkEdited.getHttpConfiguration().getHttpAauthenticationType().ordinal());
                                httpCertPicked = networkEdited.getHttpConfiguration().getCertAuthorityUri();
                                uiBinding.setHttpCertPicked(httpCertPicked);
                                uiBinding.setUseSslInHttp(networkEdited.getHttpConfiguration().getHttpUseSsl());
                            } else if (networkEdited.getNetworkType().equals(Enumerators.NetworkType.MQTT)) {
                                mqttCertPicked = networkEdited.getMqttConfiguration().getMqttCertAuthorityUri();
                                uiBinding.setMqttCertPicked(mqttCertPicked);
                                uiBinding.setUseSslInMqtt(networkEdited.getMqttConfiguration().getMqttUseSsl());
                            }
                        }
                        showEditArea();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                uiBinding.toolbar.setTitle(getString(R.string.toolbar_title_new_network));
                showEditArea();
                if (networkAddEditViewModel.getNetworkBeingEdited() != null) {
                    try {
                        populateUiWithProvidedNetwork(networkAddEditViewModel.getNetworkBeingEdited());
                        networkEdited = networkAddEditViewModel.getNetworkBeingEdited();
                        networkAddEditViewModel.setNetworkBeingEdited(networkEdited);
                        imagePicked = networkEdited.getImageUri();
                        if (networkEdited != null) {
                            uiBinding.setNetwork(networkEdited);
                            uiBinding.setImagePicked(imagePicked);
                            uiBinding.setNetworkTypeSelected(networkEdited.getNetworkType().ordinal());
                            if (networkEdited.getNetworkType().equals(Enumerators.NetworkType.HTTP)) {
                                uiBinding.setNetworkTypeSelected(networkEdited.getHttpConfiguration().getHttpAauthenticationType().ordinal());
                                httpCertPicked = networkEdited.getHttpConfiguration().getCertAuthorityUri();
                                uiBinding.setHttpCertPicked(httpCertPicked);
                                uiBinding.setUseSslInHttp(networkEdited.getHttpConfiguration().getHttpUseSsl());
                            } else if (networkEdited.getNetworkType().equals(Enumerators.NetworkType.MQTT)) {
                                mqttCertPicked = networkEdited.getMqttConfiguration().getMqttCertAuthorityUri();
                                uiBinding.setMqttCertPicked(mqttCertPicked);
                                uiBinding.setUseSslInMqtt(networkEdited.getMqttConfiguration().getMqttUseSsl());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addeditelement, menu);
        this.menu = menu;
        return true;
    }

    /**
     * Function called when a menu item is selected.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_ok:
                if (editionMode) {
                    updateNetwork();
                } else {
                    createNetwork();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Network networkInEdition = networkAddEditViewModel.getNetworkBeingEdited();
        if (networkInEdition == null) networkInEdition = new Network();
        try {
            configureNetworkObjectWithFoldData(networkInEdition, true);
            networkAddEditViewModel.setNetworkBeingEdited(networkInEdition);
        } catch (Exception e) {
            networkAddEditViewModel.setNetworkBeingEdited(null);
        }
        if (networkId != null) {
            outState.putInt(NETWORK_ID, networkId);
        }
        super.onSaveInstanceState(outState);
    }

    private void createNetwork() {
        try {
            Network newNetwork = new Network();
            if (checkFoldData()) {
                configureNetworkObjectWithFoldData(newNetwork, true);
                networkAddEditViewModel.createNetwork(newNetwork);
                finish();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.toast_create_failed));
            finish();
        }
    }

    private void updateNetwork() {
        try {
            if (checkFoldData()) {
                configureNetworkObjectWithFoldData(networkEdited, false);
                networkAddEditViewModel.updateNetwork(networkEdited);
                finish();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.toast_update_failed));
            finish();
        }
    }

    public void populateUiWithProvidedNetwork(Network networkToUse) throws Exception {
        try {
            uiBinding.edNetworkAddeditName.setText(networkToUse.getName());
            uiBinding.spNetworkAddeditType.setSelection(networkToUse.getNetworkType().ordinal());
            imagePicked = networkToUse.getImageUri();
            uiBinding.setImagePicked(imagePicked);
            switch (networkToUse.getNetworkType()) {
                case HTTP:
                    uiBinding.edNetworkAddeditHttpBaseurl.setText(networkToUse.getHttpConfiguration().getHttpBaseUrl());
                    uiBinding.spNetworkAddeditHttpAuth.setSelection(networkToUse.getHttpConfiguration().getHttpAauthenticationType().ordinal());
                    if (networkToUse.getHttpConfiguration().getHttpAauthenticationType().equals(Enumerators.HttpAuthenticationType.BASIC)) {
                        uiBinding.edNetworkAddeditHttpAuthUsername.setText(networkToUse.getHttpConfiguration().getHttpUsername());
                        uiBinding.edNetworkAddeditHttpAuthPassword.setText(networkToUse.getHttpConfiguration().getHttpPassword());
                    }
                    uiBinding.cbNetworkAddeditHttpUseSsl.setChecked(networkToUse.getHttpConfiguration().getHttpUseSsl());
                    if (networkToUse.getHttpConfiguration().getHttpUseSsl()) {
                        httpCertPicked = networkToUse.getHttpConfiguration().getCertAuthorityUri();
                        uiBinding.setHttpCertPicked(httpCertPicked);
                    }
                    break;
                case MQTT:
                    uiBinding.edNetworkAddeditMqttBrokerurl.setText(networkToUse.getMqttConfiguration().getMqttBrokerUrl());
                    uiBinding.edNetworkAddeditMqttClientid.setText(networkToUse.getMqttConfiguration().getMqttClientId());
                    uiBinding.edNetworkAddeditMqttUsername.setText(networkToUse.getMqttConfiguration().getMqttUsername());
                    uiBinding.edNetworkAddeditMqttPassword.setText(networkToUse.getMqttConfiguration().getMqttPassword());
                    uiBinding.cbNetworkAddeditMqttCleanSession.setChecked(networkToUse.getMqttConfiguration().getMqttCleanSession());
                    if (networkToUse.getMqttConfiguration().getMqttCleanSession()) {
                        mqttCertPicked = networkToUse.getHttpConfiguration().getCertAuthorityUri();
                        uiBinding.setMqttCertPicked(mqttCertPicked);
                    }
                    uiBinding.edNetworkAddeditConnTimeout.setText((networkToUse.getMqttConfiguration().getMqttConnTimeout() != null) ?
                            networkToUse.getMqttConfiguration().getMqttConnTimeout().toString() : "");
                    uiBinding.edNetworkAddeditKeepaliveInterval.setText((networkToUse.getMqttConfiguration().getMqttKeepaliveInterval() != null) ?
                            networkToUse.getMqttConfiguration().getMqttKeepaliveInterval().toString() : "");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configureNetworkObjectWithFoldData(Network networkToConfigure, boolean creatingNewNetwork) throws Exception {
        String name = uiBinding.edNetworkAddeditName.getText().toString().trim();
        networkToConfigure.setName(name);
        if (creatingNewNetwork) {
            networkToConfigure.setNetworkType(Enumerators.NetworkType.valueOf(uiBinding.spNetworkAddeditType.getSelectedItemPosition()));
        }
        networkToConfigure.setImageUri((imagePicked != null) ? imagePicked : null);
        switch (networkToConfigure.getNetworkType()) {
            case HTTP:
                Network.HttpNetworkParameters httpParams = new Network.HttpNetworkParameters();
                String baseUrl = uiBinding.edNetworkAddeditHttpBaseurl.getText().toString().trim();
                httpParams.setHttpBaseUrl(baseUrl);
                httpParams.setHttpAauthenticationType(Enumerators.HttpAuthenticationType.valueOf(uiBinding.spNetworkAddeditHttpAuth.getSelectedItemPosition()));
                if (httpParams.getHttpAauthenticationType().equals(Enumerators.HttpAuthenticationType.BASIC)) {
                    String httpUsername = uiBinding.edNetworkAddeditHttpAuthUsername.getText().toString().trim();
                    httpParams.setHttpUsername(httpUsername);
                    String httpPassword = uiBinding.edNetworkAddeditHttpAuthPassword.getText().toString().trim();
                    httpParams.setHttpPassword(httpPassword);
                } else {
                    httpParams.setHttpUsername(null);
                    httpParams.setHttpPassword(null);
                }
                httpParams.setHttpUseSsl(uiBinding.cbNetworkAddeditHttpUseSsl.isChecked());
                if (httpParams.getHttpUseSsl()) {
                    httpParams.setCertAuthorityUri((httpCertPicked != null) ? httpCertPicked : null);
                } else {
                    httpParams.setCertAuthorityUri(null);
                }
                networkToConfigure.setHttpConfiguration(httpParams);
                networkToConfigure.setMqttConfiguration(null);
                break;
            case MQTT:
                Network.MqttNetworkParameters mqttParams = new Network.MqttNetworkParameters();
                String mqttBrokerUrl = uiBinding.edNetworkAddeditMqttBrokerurl.getText().toString().trim();
                mqttParams.setMqttBrokerUrl(mqttBrokerUrl);
                mqttParams.setMqttClientId(uiBinding.edNetworkAddeditMqttClientid.getText().toString().trim());
                mqttParams.setMqttUsername(uiBinding.edNetworkAddeditMqttUsername.getText().toString().trim());
                mqttParams.setMqttPassword(uiBinding.edNetworkAddeditMqttPassword.getText().toString().trim());
                mqttParams.setMqttCleanSession(uiBinding.cbNetworkAddeditMqttCleanSession.isChecked());
                mqttParams.setMqttUseSsl(uiBinding.cbNetworkAddeditMqttUseSsl.isChecked());
                if (mqttParams.getMqttUseSsl()) {
                    mqttParams.setMqttCertAuthorityUri((mqttCertPicked != null) ? mqttCertPicked : null);
                } else {
                    mqttParams.setMqttCertAuthorityUri(null);
                }
                try {
                    mqttParams.setMqttConnTimeout(Integer.valueOf(uiBinding.edNetworkAddeditConnTimeout.getText().toString().trim()));
                } catch (Exception e) {
                    mqttParams.setMqttConnTimeout(0);
                }
                try {
                    mqttParams.setMqttKeepaliveInterval(Integer.valueOf(uiBinding.edNetworkAddeditKeepaliveInterval.getText().toString().trim()));
                } catch (Exception e) {
                    mqttParams.setMqttKeepaliveInterval(0);
                }
                networkToConfigure.setMqttConfiguration(mqttParams);
                networkToConfigure.setHttpConfiguration(null);
                break;
        }
    }

    public boolean checkFoldData() {
        try {
            String name = uiBinding.edNetworkAddeditName.getText().toString().trim();
            if (name.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_name));
                return false;
            }
            switch (Enumerators.NetworkType.valueOf(uiBinding.spNetworkAddeditType.getSelectedItemPosition())) {
                case HTTP:
                    String baseUrl = uiBinding.edNetworkAddeditHttpBaseurl.getText().toString().trim();
                    if (baseUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl));
                        return false;
                    }
                    if (uiBinding.spNetworkAddeditHttpAuth.getSelectedItemPosition() ==
                            Enumerators.HttpAuthenticationType.BASIC.ordinal()) {
                        String httpUsername = uiBinding.edNetworkAddeditHttpAuthUsername.getText().toString().trim();
                        if (httpUsername.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_username));
                            return false;
                        }
                        String httpPassword = uiBinding.edNetworkAddeditHttpAuthPassword.getText().toString().trim();
                        if (httpPassword.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_password));
                            return false;
                        }
                    }
                    break;
                case MQTT:
                    String mqttBrokerUrl = uiBinding.edNetworkAddeditMqttBrokerurl.getText().toString().trim();
                    if (mqttBrokerUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_brokerurl));
                        return false;
                    }
                    break;
            }
            return true;
        }  catch (Exception e) {
            return false;
        }
    }

    /**
     * Show the activity info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.networkAddeditEditareaLayout.setVisibility(View.GONE);
            uiBinding.networkAddeditLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the edition area.
     */
    private void showEditArea() {
        if (uiBinding != null) {
            uiBinding.networkAddeditEditareaLayout.setVisibility(View.VISIBLE);
            uiBinding.networkAddeditLoadingLayout.loadingLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_PICK_IMAGE) {
                    imagePicked = Utils.getUriFromFilSelected(this, data.getData());
                    uiBinding.setImagePicked(imagePicked);
                } else if (requestCode == REQUEST_PICK_HTTP_CERT) {
                    httpCertPicked = Utils.getUriFromFilSelected(this, data.getData());
                    uiBinding.setHttpCertPicked(httpCertPicked);
                } else if (requestCode == REQUEST_PICK_MQTT_CERT) {
                    mqttCertPicked = Utils.getUriFromFilSelected(this, data.getData());
                    uiBinding.setMqttCertPicked(mqttCertPicked);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.edit_hint_image_pick)), REQUEST_PICK_IMAGE);
    }

    @Override
    public void cancelImagePicked() {
        imagePicked = null;
        uiBinding.setImagePicked(imagePicked);
    }

    @Override
    public void pickHttpCert() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.edit_hint_http_cert_pick)), REQUEST_PICK_HTTP_CERT);
    }

    @Override
    public void cancelHttpCert() {
        httpCertPicked = null;
        uiBinding.setHttpCertPicked(httpCertPicked);
    }

    @Override
    public void pickMqttCert() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.edit_hint_mqtt_cert_pick)), REQUEST_PICK_MQTT_CERT);
    }

    @Override
    public void cancelMqttCert() {
        mqttCertPicked = null;
        uiBinding.setHttpCertPicked(mqttCertPicked);
    }

    @Override
    public void networkTypeSelected(int ordinal) {
        uiBinding.setNetworkTypeSelected(ordinal);
    }

    @Override
    public void httpAuthTypeSelected(int ordinal) {
        uiBinding.setHttpAuthTypeSelected(ordinal);
    }

    @Override
    public void httpUseSslChanged(boolean checked) {
        uiBinding.setUseSslInHttp(checked);
    }

    @Override
    public void mqttUseSslChanged(boolean checked) {
        uiBinding.setUseSslInMqtt(checked);
    }

}
