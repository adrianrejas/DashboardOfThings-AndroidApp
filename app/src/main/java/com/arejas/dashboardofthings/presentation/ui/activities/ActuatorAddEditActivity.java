package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper;
import com.arejas.dashboardofthings.databinding.ActivityActuatorAddEditBinding;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorAddEditViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.helpers.AddEditSensorActuatorPresenter;
import com.arejas.dashboardofthings.presentation.ui.helpers.HttpHeaderListAdapter;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.Utils;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.PlacePicker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Actuator detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ActuatorListActivity}.
 */
public class ActuatorAddEditActivity extends AppCompatActivity implements AddEditSensorActuatorPresenter {

    public static final String ACTUATOR_ID = "actuator_id";

    public static final int NEW_ACTUATOR_ID = -1000;

    public static final int MIN_HTTP_INTERVAL = 60;

    public static final int REQUEST_PICK_IMAGE = 41;
    public static final int REQUEST_PICK_LOCATION = 42;

    private ActuatorAddEditViewModel actuatorAddEditViewModel;

    ActivityActuatorAddEditBinding uiBinding;

    private Menu menu;

    @Inject
    ViewModelFactory viewModelFactory;

    
    boolean editionMode;
    Integer actuatorId;
    ActuatorExtended actuatorEdited;
    private String imagePicked;
    private AddressData locationPicked;
    private List<NetworkExtended> networksAvailable;
    private HttpHeaderListAdapter mHttpHeadersAdapter;
    private Map<String, String> mHttpHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_actuator_add_edit);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.cancel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Get view model*/
        actuatorAddEditViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(ActuatorAddEditViewModel.class);

        // Load the data in order to preserve data when orientation changes, setting the network id
        // in the viewmodel only if it's a new activity, in order to preserve the entity currently being modified
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(ACTUATOR_ID))) {
            actuatorId = savedInstanceState.getInt(ACTUATOR_ID);
            if (actuatorId == NEW_ACTUATOR_ID) {
                actuatorId = null;
            }
        } else if ((getIntent().getExtras() != null) && (getIntent().getExtras().containsKey(ACTUATOR_ID))) {
            actuatorId = getIntent().getIntExtra(ACTUATOR_ID, -1);
            actuatorAddEditViewModel.setActuatorId(actuatorId);
        } else {
            actuatorId = null;
            actuatorAddEditViewModel.setActuatorId(null);
        }

        // set if entity is being edited or created
        if ((actuatorId == null) || (actuatorId < 0)) {
            editionMode = false;
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_new_actuator));
        } else {
            editionMode = true;
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_edit_sensor));
        }

        // set listener and edition mode
        uiBinding.setPresenter(this);
        uiBinding.setEditionMode(editionMode);

        // set the UI based on if the element is being created or edited, and if an in-edition version
        // of the entity exists
        mHttpHeaders = new HashMap<>();
        configureListAdapter();
        updateHttpHeaderList();
        if (editionMode) {
            if (actuatorAddEditViewModel.getActuatorBeingEdited() == null) {
                actuatorAddEditViewModel.getActuator(true).observe(this, actuatorExtendedResource -> {
                    try {
                        if (actuatorExtendedResource == null) {
                            ToastHelper.showToast(getString(R.string.toast_edition_failed));
                            finish();
                        } else {
                            if (actuatorExtendedResource.getStatus() == Resource.Status.ERROR) {
                                ToastHelper.showToast(getString(R.string.toast_edition_failed));
                                finish();
                            } else if (actuatorExtendedResource.getStatus() == Resource.Status.LOADING) {
                                showLoading();
                            } else {
                                ActuatorExtended actuator = actuatorExtendedResource.getData();
                                populateUiWithProvidedActuator(actuator);
                                showEditArea();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastHelper.showToast(getString(R.string.toast_edition_failed));
                        finish();
                    }
                });
            } else {
                actuatorEdited = actuatorAddEditViewModel.getActuatorBeingEdited();
                try {
                    populateUiWithProvidedActuator(actuatorAddEditViewModel.getActuatorBeingEdited());
                    showEditArea();
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                    finish();
                }
            }
        } else {
            showEditArea();
            if (actuatorAddEditViewModel.getActuatorBeingEdited() != null) {
                try {
                    populateUiWithProvidedActuator(actuatorAddEditViewModel.getActuatorBeingEdited());
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                    finish();
                }
            }
        }

        actuatorAddEditViewModel.getNetworks(true).observe(this, listResource -> {
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                    finish();
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_edition_failed));
                        finish();
                    } else if (listResource.getStatus() == Resource.Status.SUCCESS) {
                        networksAvailable = listResource.getData();
                        if (networksAvailable.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.toast_create_first_network));
                            finish();
                        }
                        uiBinding.setNetworks(networksAvailable);
                        selectNetworkInSpinner();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastHelper.showToast(getString(R.string.toast_edition_failed));
                finish();
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
                    updateActuator();
                } else {
                    createActuator();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        ActuatorExtended actuatorInEdition = actuatorAddEditViewModel.getActuatorBeingEdited();
        if (actuatorInEdition == null) actuatorInEdition = new ActuatorExtended();
        try {
            configureActuatorObjectWithFoldData(actuatorInEdition, true);
            actuatorAddEditViewModel.setActuatorBeingEdited(actuatorInEdition);
        } catch (Exception e) {
            actuatorAddEditViewModel.setActuatorBeingEdited(null);
        }
        if (actuatorId != null) {
            outState.putInt(ACTUATOR_ID, actuatorId);
        } else {
            outState.putInt(ACTUATOR_ID, NEW_ACTUATOR_ID);
        }
        super.onSaveInstanceState(outState);
    }

    private void createActuator() {
        try {
            Actuator newActuator = new Actuator();
            if (checkFoldData()) {
                configureActuatorObjectWithFoldData(newActuator, true);
                actuatorAddEditViewModel.createActuator(newActuator);
                finish();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.toast_create_failed));
            finish();
        }
    }

    private void updateActuator() {
        try {
            if (checkFoldData()) {
                configureActuatorObjectWithFoldData(actuatorEdited, false);
                actuatorAddEditViewModel.updateActuator(actuatorEdited);
                finish();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.toast_update_failed));
            finish();
        }
    }

    private void selectNetworkInSpinner() {
        int positionToSelect = 0;
        if ((actuatorEdited != null) && (actuatorEdited.getNetworkId() != null)) {
            for (NetworkExtended network: networksAvailable) {
                if (actuatorEdited.getNetworkId().equals(network.getId())) {
                    break;
                }
                positionToSelect++;
            }
        }
        uiBinding.setNetworkSelected(positionToSelect);
    }

    private void configureListAdapter() {
        // Configure recycler view with a linear layout
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        uiBinding.rvActuatorAddeditHeasersSet.setLayoutManager(linearLayout);
        // Configure adapter for recycler view
        mHttpHeadersAdapter = new HttpHeaderListAdapter(this);
        uiBinding.rvActuatorAddeditHeasersSet.setAdapter(mHttpHeadersAdapter);
    }

    private void updateHttpHeaderList() {
        if (mHttpHeaders != null) {
            mHttpHeadersAdapter.setData(mHttpHeaders);
            mHttpHeadersAdapter.notifyDataSetChanged();
            uiBinding.edActuatorAddeditHttpNewheaderName.setText("");
            uiBinding.edActuatorAddeditHttpNewheaderValue.setText("");
        }
    }

    public void populateUiWithProvidedActuator(ActuatorExtended actuatorToUse) throws Exception {
        try {
            actuatorEdited = actuatorToUse;
            actuatorAddEditViewModel.setActuatorBeingEdited(actuatorToUse);
            if (actuatorToUse != null) {
                imagePicked = actuatorToUse.getImageUri();
                if ((actuatorToUse.getLocationLong() != null) && (actuatorToUse.getLocationLat() != null)) {
                    locationPicked = new AddressData(actuatorToUse.getLocationLat(),
                            actuatorToUse.getLocationLong(), null);

                } else {
                    locationPicked = null;
                }
                uiBinding.setActuator(actuatorToUse);
                uiBinding.setImagePicked(imagePicked);
                uiBinding.setLocationPicked(locationPicked);
                uiBinding.setNetworkTypeSelected(actuatorToUse.getNetworkType().ordinal());
                uiBinding.setDataTypeSelected(actuatorToUse.getDataType().ordinal());
                mHttpHeaders.clear();
                if (actuatorEdited.getHttpHeaders() != null)
                    mHttpHeaders.putAll(actuatorEdited.getHttpHeaders());
                updateHttpHeaderList();
            } else {
                ToastHelper.showToast(getString(R.string.toast_edition_failed));
                finish();
            }
            if (uiBinding.getNetworks() != null) {
                selectNetworkInSpinner();
            }
            uiBinding.edActuatorAddeditName.setText(actuatorToUse.getName());
            uiBinding.spActuatorAddeditDataType.setSelection(actuatorToUse.getDataType().ordinal());
            switch (actuatorToUse.getNetworkType()) {
                case HTTP:
                    uiBinding.spActuatorAddeditHttpMethod.setSelection(actuatorToUse.getHttpMethod().ordinal());
                    break;
                case MQTT:
                    uiBinding.spActuatorAddeditMqttQos.setSelection(actuatorToUse.getMqttQosLevel().ordinal());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configureActuatorObjectWithFoldData(Actuator actuatorToConfigure, boolean creatingNewActuator) throws Exception {
        String name = uiBinding.edActuatorAddeditName.getText().toString().trim();
        actuatorToConfigure.setName(name);
        String type = uiBinding.edActuatorAddeditType.getText().toString().trim();
        actuatorToConfigure.setType(type);
        int networkPos = uiBinding.spActuatorAddeditNetwork.getSelectedItemPosition();
        NetworkExtended networkToUse = networksAvailable.get(networkPos);
        actuatorToConfigure.setNetworkId(networkToUse.getId());
        actuatorToConfigure.setDataType(Enumerators.DataType.valueOf(
                uiBinding.spActuatorAddeditDataType.getSelectedItemPosition()
        ));
        actuatorToConfigure.setImageUri((imagePicked != null) ? imagePicked : null);
        if (locationPicked != null) {
            actuatorToConfigure.setLocationLat(locationPicked.getLatitude());
            actuatorToConfigure.setLocationLong(locationPicked.getLongitude());
        }
        actuatorToConfigure.setShowInMainDashboard(uiBinding.cbActuatorAddeditShowInMainDashboard.isChecked());
        switch (networkToUse.getNetworkType()) {
            case HTTP:
                String relativeUrl = uiBinding.edActuatorAddeditHttpRelativeurl.getText().toString().trim();
                actuatorToConfigure.setHttpRelativeUrl(relativeUrl);
                actuatorToConfigure.setHttpHeaders(mHttpHeadersAdapter.getData());
                actuatorToConfigure.setHttpMethod(Enumerators.HttpMethod.valueOf(
                        uiBinding.spActuatorAddeditHttpMethod.getSelectedItemPosition()));
                actuatorToConfigure.setMqttTopicToPublish(null);
                actuatorToConfigure.setMqttQosLevel(Enumerators.MqttQosLevel.QOS_0);
                String mimeType = uiBinding.edActuatorAddeditHttpMimeType.getText().toString().trim();
                actuatorToConfigure.setHttpMimeType(mimeType);
                break;
            case MQTT:
                String topic = uiBinding.edActuatorAddeditMqttTopic.getText().toString().trim();
                actuatorToConfigure.setMqttTopicToPublish(topic);
                actuatorToConfigure.setMqttQosLevel(Enumerators.MqttQosLevel.valueOf(
                        uiBinding.spActuatorAddeditMqttQos.getSelectedItemPosition()
                ));
                actuatorToConfigure.setHttpRelativeUrl(null);
                actuatorToConfigure.setHttpHeaders(null);
                actuatorToConfigure.setHttpMethod(Enumerators.HttpMethod.GET);
                actuatorToConfigure.setHttpMimeType(null);
                break;
        }
        String dataFormat = uiBinding.edActuatorAddeditMessageFormat.getText().toString().trim();
        actuatorToConfigure.setDataFormatMessageToSend(dataFormat);
        String dataUnit;
        Float dataNumberMinimum;
        Float dataNumberMaximum;
        switch (actuatorToConfigure.getDataType()) {
            case INTEGER:
                dataUnit = uiBinding.edActuatorAddeditIntegerUnit.getText().toString().trim();
                actuatorToConfigure.setDataUnit(!dataUnit.isEmpty() ? dataUnit : null);
                try {
                    dataNumberMinimum = Float.parseFloat(uiBinding.edActuatorAddeditIntegerMinimum.getText().toString().trim());
                } catch (Exception e) {
                    dataNumberMinimum = null;
                }
                actuatorToConfigure.setDataNumberMinimum(dataNumberMinimum);
                try {
                    dataNumberMaximum = Float.parseFloat(uiBinding.edActuatorAddeditIntegerMaximum.getText().toString().trim());
                } catch (Exception e) {
                    dataNumberMaximum = null;
                }
                actuatorToConfigure.setDataNumberMaximum(dataNumberMaximum);
                break;
            case DECIMAL:
                dataUnit = uiBinding.edActuatorAddeditDecimalUnit.getText().toString().trim();
                actuatorToConfigure.setDataUnit(!dataUnit.isEmpty() ? dataUnit : null);
                try {
                    dataNumberMinimum = Float.parseFloat(uiBinding.edActuatorAddeditDecimalMinimum.getText().toString().trim());
                } catch (Exception e) {
                    dataNumberMinimum = null;
                }
                actuatorToConfigure.setDataNumberMinimum(dataNumberMinimum);
                try {
                    dataNumberMaximum = Float.parseFloat(uiBinding.edActuatorAddeditDecimalMaximum.getText().toString().trim());
                } catch (Exception e) {
                    dataNumberMaximum = null;
                }
                actuatorToConfigure.setDataNumberMaximum(dataNumberMaximum);
                break;
            case BOOLEAN:
            case STRING:
                actuatorToConfigure.setDataUnit(null);
                actuatorToConfigure.setDataNumberMinimum(null);
                actuatorToConfigure.setDataNumberMaximum(null);
                break;
        }
    }

    public boolean checkFoldData() {
        try {
            String name = uiBinding.edActuatorAddeditName.getText().toString().trim();
            if (name.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_name));
                return false;
            }
            String type = uiBinding.edActuatorAddeditType.getText().toString().trim();
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_type));
                return false;
            }
            int networkPos = uiBinding.spActuatorAddeditNetwork.getSelectedItemPosition();
            NetworkExtended networkToUse = networksAvailable.get(networkPos);
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_network));
                return false;
            }
            switch (networkToUse.getNetworkType()) {
                case HTTP:
                    String relativeUrl = uiBinding.edActuatorAddeditHttpRelativeurl.getText().toString().trim();
                    if (relativeUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl));
                        return false;
                    }
                    Enumerators.HttpMethod httpMethod = Enumerators.HttpMethod.valueOf(
                            uiBinding.spActuatorAddeditHttpMethod.getSelectedItemPosition());
                    if (!httpMethod.equals(Enumerators.HttpMethod.GET)) {
                        String mimeType = uiBinding.edActuatorAddeditHttpMimeType.getText().toString().trim();
                        if (mimeType.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_mimetype));
                            return false;
                        }
                    }
                    break;
                case MQTT:
                    String mqttTopic = uiBinding.edActuatorAddeditMqttTopic.getText().toString().trim();
                    if (mqttTopic.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_topic));
                        return false;
                    }
                    break;
            }
            String messageFormat = uiBinding.edActuatorAddeditMessageFormat.getText().toString().trim();
            if (messageFormat.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_message_format));
                return false;
            } else if (!DataMessageHelper.checkDataPrecenseInActuatorMessageFormat(messageFormat)) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_message_format_data));
                return false;
            }
            switch (Enumerators.DataType.valueOf(uiBinding.spActuatorAddeditDataType.getSelectedItemPosition())) {
                case INTEGER:
                    if (!uiBinding.edActuatorAddeditIntegerMinimum.getText().toString().trim().isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding.edActuatorAddeditIntegerMinimum.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_limits_integer));
                            return false;
                        }
                    }
                    if (!uiBinding.edActuatorAddeditIntegerMaximum.getText().toString().trim().isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding.edActuatorAddeditIntegerMaximum.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_limits_integer));
                            return false;
                        }
                    }
                    break;
                case DECIMAL:
                    if (!uiBinding.edActuatorAddeditDecimalMinimum.getText().toString().trim().isEmpty()) {
                        try {
                            Float.parseFloat(uiBinding.edActuatorAddeditDecimalMinimum.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal));
                            return false;
                        }
                    }
                    if (!uiBinding.edActuatorAddeditDecimalMaximum.getText().toString().trim().isEmpty()) {
                        try {
                            Float.parseFloat(uiBinding.edActuatorAddeditDecimalMaximum.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_limits_decimal));
                            return false;
                        }
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
            uiBinding.actuatorAddeditEditareaLayout.setVisibility(View.GONE);
            uiBinding.actuatorAddeditLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the edition area.
     */
    private void showEditArea() {
        if (uiBinding != null) {
            uiBinding.actuatorAddeditEditareaLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorAddeditLoadingLayout.loadingLayout.setVisibility(View.GONE);
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
                } else if (requestCode == REQUEST_PICK_LOCATION) {
                    locationPicked = data.getParcelableExtra(Constants.ADDRESS_INTENT);
                    uiBinding.setLocationPicked(locationPicked);
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
    public void pickLocation() {
        try {
            PlacePicker.IntentBuilder builder = Utils.getIntentBuilderForLocationPicker();
            if (locationPicked != null) {
                builder.setLatLong(locationPicked.getLatitude(), locationPicked.getLongitude());
            }
            startActivityForResult(builder.build(this), REQUEST_PICK_LOCATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancelLocationPicked() {
        locationPicked = null;
        uiBinding.setLocationPicked(locationPicked);
    }

    @Override
    public void addHttpHeader() {
        try {
            if (mHttpHeaders != null) {
                String newHeaderName = uiBinding.edActuatorAddeditHttpNewheaderName.getText().toString();
                String newHeaderValue = uiBinding.edActuatorAddeditHttpNewheaderValue.getText().toString();
                mHttpHeaders.put(newHeaderName, newHeaderValue);
                updateHttpHeaderList();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.edit_toast_check_http_headers));
        }
    }

    @Override
    public void cancelHttpHeader(String headerName) {
        if ((mHttpHeaders != null) && (mHttpHeaders.containsKey(headerName))) {
            mHttpHeaders.remove(headerName);
            updateHttpHeaderList();
        }
    }

    @Override
    public void networkSelected(int ordinal) {
        try {
            if (networksAvailable != null) {
                Network network = networksAvailable.get(ordinal);
                uiBinding.setNetworkTypeSelected(network.getNetworkType().ordinal());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageTypeSelected(int ordinal) {
        // Not required to be implemented for actuators
    }

    @Override
    public void dataTypeSelected(int ordinal) {
        uiBinding.setDataTypeSelected(ordinal);
    }

}
