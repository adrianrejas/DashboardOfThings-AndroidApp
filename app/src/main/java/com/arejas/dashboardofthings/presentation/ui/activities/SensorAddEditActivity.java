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
import com.arejas.dashboardofthings.databinding.ActivitySensorAddEditBinding;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorAddEditViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.helpers.AddEditSensorActuatorPresenter;
import com.arejas.dashboardofthings.presentation.ui.helpers.HttpHeaderListAdapter;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.Utils;
import com.sucho.placepicker.AddressData;
import com.sucho.placepicker.Constants;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Sensor detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SensorListActivity}.
 */
public class SensorAddEditActivity extends AppCompatActivity implements AddEditSensorActuatorPresenter {

    public static final String SENSOR_ID = "sensor_id";

    public static final int MIN_HTTP_INTERVAL = 60;

    public static final int REQUEST_PICK_IMAGE = 41;
    public static final int REQUEST_PICK_LOCATION = 42;

    private SensorAddEditViewModel sensorAddEditViewModel;

    ActivitySensorAddEditBinding uiBinding;

    private Menu menu;

    @Inject
    ViewModelFactory viewModelFactory;

    
    boolean editionMode;
    Integer sensorId;
    SensorExtended sensorEdited;
    private String imagePicked;
    private AddressData locationPicked;
    private List<NetworkExtended> networksAvailable;
    private HttpHeaderListAdapter mHttpHeadersAdapter;
    private Map<String, String> mHttpHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_sensor_add_edit);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.cancel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(SENSOR_ID))) {
            sensorId = savedInstanceState.getInt(SENSOR_ID);
        } if ((getIntent().getExtras() != null) && (getIntent().getExtras().containsKey(SENSOR_ID))) {
            sensorId = getIntent().getIntExtra(SENSOR_ID, -1);
        } else {
            sensorId = null;
        }
        if ((sensorId == null) || (sensorId < 0)) {
            editionMode = false;
        } else {
            editionMode = true;
        }

        /* Get view model*/
        sensorAddEditViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(SensorAddEditViewModel.class);
        sensorAddEditViewModel.setSensorId(sensorId);

        uiBinding.setPresenter(this);
        uiBinding.setEditionMode(editionMode);

        mHttpHeaders = new HashMap<>();
        configureListAdapter();
        updateHttpHeaderList();

        if (editionMode) {
            uiBinding.toolbar.setTitle(getString(R.string.toolbar_title_edit_sensor));
            if (sensorAddEditViewModel.getSensorBeingEdited() == null) {
                sensorAddEditViewModel.getSensor(true).observe(this, sensorExtendedResource -> {
                    try {
                        if (sensorExtendedResource == null) {
                            ToastHelper.showToast(getString(R.string.toast_edition_failed));
                            finish();
                        } else {
                            if (sensorExtendedResource.getStatus() == Resource.Status.ERROR) {
                                ToastHelper.showToast(getString(R.string.toast_edition_failed));
                                finish();
                            } else if (sensorExtendedResource.getStatus() == Resource.Status.LOADING) {
                                showLoading();
                            } else {
                                SensorExtended sensor = sensorExtendedResource.getData();
                                populateUiWithProvidedSensor(sensor);
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
                sensorEdited = sensorAddEditViewModel.getSensorBeingEdited();
                try {
                    populateUiWithProvidedSensor(sensorAddEditViewModel.getSensorBeingEdited());
                    showEditArea();
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                    finish();
                }
            }
        } else {
            uiBinding.toolbar.setTitle(getString(R.string.toolbar_title_new_sensor));
            showEditArea();
            if (sensorAddEditViewModel.getSensorBeingEdited() != null) {
                try {
                    populateUiWithProvidedSensor(sensorAddEditViewModel.getSensorBeingEdited());
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                    finish();
                }
            }
        }

        sensorAddEditViewModel.getNetworks(true).observe(this, listResource -> {
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
                    updateSensor();
                } else {
                    createSensor();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        SensorExtended sensorInEdition = sensorAddEditViewModel.getSensorBeingEdited();
        if (sensorInEdition == null) sensorInEdition = new SensorExtended();
        try {
            configureSensorObjectWithFoldData(sensorInEdition, true);
            sensorAddEditViewModel.setSensorBeingEdited(sensorInEdition);
        } catch (Exception e) {
            sensorAddEditViewModel.setSensorBeingEdited(null);
        }
        if (sensorId != null) {
            outState.putInt(SENSOR_ID, sensorId);
        }
        super.onSaveInstanceState(outState);
    }

    private void createSensor() {
        try {
            Sensor newSensor = new Sensor();
            if (checkFoldData()) {
                configureSensorObjectWithFoldData(newSensor, true);
                sensorAddEditViewModel.createSensor(newSensor);
                finish();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.toast_create_failed));
            finish();
        }
    }

    private void updateSensor() {
        try {
            if (checkFoldData()) {
                configureSensorObjectWithFoldData(sensorEdited, false);
                sensorAddEditViewModel.updateSensor(sensorEdited);
                finish();
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.toast_update_failed));
            finish();
        }
    }

    private void selectNetworkInSpinner() {
        int positionToSelect = 0;
        if ((sensorEdited != null) && (sensorEdited.getNetworkId() != null)) {
            for (NetworkExtended network: networksAvailable) {
                if (sensorEdited.getNetworkId().equals(network.getId())) {
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
        uiBinding.rvSensorAddeditHeasersSet.setLayoutManager(linearLayout);
        // Configure adapter for recycler view
        mHttpHeadersAdapter = new HttpHeaderListAdapter(this);
        uiBinding.rvSensorAddeditHeasersSet.setAdapter(mHttpHeadersAdapter);
    }

    private void updateHttpHeaderList() {
        if (mHttpHeaders != null) {
            mHttpHeadersAdapter.setData(mHttpHeaders);
            mHttpHeadersAdapter.notifyDataSetChanged();
            uiBinding.edSensorAddeditHttpNewheaderName.setText("");
            uiBinding.edSensorAddeditHttpNewheaderValue.setText("");
        }
    }

    public void populateUiWithProvidedSensor(SensorExtended sensorToUse) throws Exception {
        try {
            sensorEdited = sensorToUse;
            sensorAddEditViewModel.setSensorBeingEdited(sensorToUse);
            if (sensorToUse != null) {
                imagePicked = sensorToUse.getImageUri();
                if ((sensorToUse.getLocaltionLong() != null) && (sensorToUse.getLocationLat() != null)) {
                    locationPicked = new AddressData(sensorToUse.getLocationLat(),
                            sensorToUse.getLocaltionLong(), null);

                } else {
                    locationPicked = null;
                }
                uiBinding.setSensor(sensorToUse);
                uiBinding.setImagePicked(imagePicked);
                uiBinding.setLocationPicked(locationPicked);
                uiBinding.setNetworkTypeSelected(sensorToUse.getNetworkType().ordinal());
                uiBinding.setMessageTypeSelected(sensorToUse.getMessageType().ordinal());
                uiBinding.setDataTypeSelected(sensorToUse.getDataType().ordinal());
                mHttpHeaders.clear();
                if (sensorEdited.getHttpHeaders() != null)
                    mHttpHeaders.putAll(sensorEdited.getHttpHeaders());
                updateHttpHeaderList();
            } else {
                ToastHelper.showToast(getString(R.string.toast_edition_failed));
                finish();
            }
            if (uiBinding.getNetworks() != null) {
                selectNetworkInSpinner();
            }
            uiBinding.edSensorAddeditName.setText(sensorToUse.getName());
            uiBinding.spSensorAddeditMessageType.setSelection(sensorToUse.getMessageType().ordinal());
            uiBinding.spSensorAddeditDataType.setSelection(sensorToUse.getDataType().ordinal());
            switch (sensorToUse.getNetworkType()) {
                case HTTP:
                    break;
                case MQTT:
                    uiBinding.spSensorAddeditMqttQos.setSelection(sensorToUse.getMqttQosLevel().ordinal());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configureSensorObjectWithFoldData(Sensor sensorToConfigure, boolean creatingNewSensor) throws Exception {
        String name = uiBinding.edSensorAddeditName.getText().toString().trim();
        sensorToConfigure.setName(name);
        String type = uiBinding.edSensorAddeditType.getText().toString().trim();
        sensorToConfigure.setType(type);
        int networkPos = uiBinding.spSensorAddeditNetwork.getSelectedItemPosition();
        NetworkExtended networkToUse = networksAvailable.get(networkPos);
        sensorToConfigure.setNetworkId(networkToUse.getId());
        sensorToConfigure.setMessageType(Enumerators.MessageType.valueOf(
                uiBinding.spSensorAddeditMessageType.getSelectedItemPosition()
        ));
        sensorToConfigure.setDataType(Enumerators.DataType.valueOf(
                uiBinding.spSensorAddeditDataType.getSelectedItemPosition()
        ));
        sensorToConfigure.setImageUri((imagePicked != null) ? imagePicked : null);
        if (locationPicked != null) {
            sensorToConfigure.setLocationLat(locationPicked.getLatitude());
            sensorToConfigure.setLocaltionLong(locationPicked.getLongitude());
        }
        sensorToConfigure.setShowInMainDashboard(uiBinding.cbSensorAddeditShowInMainDashboard.isChecked());
        switch (networkToUse.getNetworkType()) {
            case HTTP:
                String relativeUrl = uiBinding.edSensorAddeditHttpRelativeurl.getText().toString().trim();
                sensorToConfigure.setHttpRelativeUrl(relativeUrl);
                sensorToConfigure.setHttpHeaders(mHttpHeadersAdapter.getData());
                try {
                    sensorToConfigure.setHttpSecondsBetweenRequests(Integer.valueOf(
                            uiBinding.edSensorAddeditHttpInterval.getText().toString().trim()));
                } catch (Exception e) {
                    sensorToConfigure.setHttpSecondsBetweenRequests(0);
                }
                sensorToConfigure.setMqttTopicToSubscribe(null);
                sensorToConfigure.setMqttQosLevel(Enumerators.MqttQosLevel.QOS_0);
                break;
            case MQTT:
                String topic = uiBinding.edSensorAddeditMqttTopic.getText().toString().trim();
                sensorToConfigure.setMqttTopicToSubscribe(topic);
                sensorToConfigure.setMqttQosLevel(Enumerators.MqttQosLevel.valueOf(
                        uiBinding.spSensorAddeditMqttQos.getSelectedItemPosition()
                ));
                sensorToConfigure.setHttpRelativeUrl(null);
                sensorToConfigure.setHttpHeaders(null);
                sensorToConfigure.setHttpSecondsBetweenRequests(null);
                break;
        }
        switch (sensorToConfigure.getMessageType()) {
            case XML:
                String xmlNode = uiBinding.edSensorAddeditXmlNode.getText().toString().trim();
                sensorToConfigure.setXmlOrJsonNode(xmlNode);
                sensorToConfigure.setRawRegularExpression(null);
                break;
            case JSON:
                String jsonNode = uiBinding.edSensorAddeditJsonNode.getText().toString().trim();
                sensorToConfigure.setXmlOrJsonNode(jsonNode);
                sensorToConfigure.setRawRegularExpression(null);
                break;
            case RAW:
                String rawRegex = uiBinding.edSensorAddeditRawRegex.getText().toString().trim();
                sensorToConfigure.setRawRegularExpression(rawRegex);
                sensorToConfigure.setXmlOrJsonNode(null);
                break;
        }
        String dataUnit;
        Float thresholdAboveWarning;
        Float thresholdAboveCritical;
        Float thresholdBelowWarning;
        Float thresholdBelowCritical;
        String thresholdEqualsWarning;
        String thresholdEqualsCritical;
        switch (sensorToConfigure.getDataType()) {
            case INTEGER:
                dataUnit = uiBinding.edSensorAddeditIntegerUnit.getText().toString().trim();
                sensorToConfigure.setDataUnit(!dataUnit.isEmpty() ? dataUnit : null);
                try {
                    thresholdAboveWarning = Float.parseFloat(uiBinding.edSensorAddeditIntegerWarningAbove.getText().toString().trim());
                } catch (Exception e) {
                    thresholdAboveWarning = null;
                }
                sensorToConfigure.setThresholdAboveWarning(thresholdAboveWarning);
                try {
                    thresholdAboveCritical = Float.parseFloat(uiBinding.edSensorAddeditIntegerCriticalAbove.getText().toString().trim());
                } catch (Exception e) {
                    thresholdAboveCritical = null;
                }
                sensorToConfigure.setThresholdAboveCritical(thresholdAboveCritical);
                try {
                    thresholdBelowWarning = Float.parseFloat(uiBinding.edSensorAddeditIntegerWarningBelow.getText().toString().trim());
                } catch (Exception e) {
                    thresholdBelowWarning = null;
                }
                sensorToConfigure.setThresholdBelowWarning(thresholdBelowWarning);
                try {
                    thresholdBelowCritical = Float.parseFloat(uiBinding.edSensorAddeditIntegerCriticalBelow.getText().toString().trim());
                } catch (Exception e) {
                    thresholdBelowCritical = null;
                }
                sensorToConfigure.setThresholdBelowCritical(thresholdBelowCritical);
                sensorToConfigure.setThresholdEqualsWarning(null);
                sensorToConfigure.setThresholdEqualsCritical(null);
                break;
            case DECIMAL:
                dataUnit = uiBinding.edSensorAddeditDecimalUnit.getText().toString().trim();
                sensorToConfigure.setDataUnit(!dataUnit.isEmpty() ? dataUnit : null);
                try {
                    thresholdAboveWarning = Float.parseFloat(uiBinding.edSensorAddeditDecimalWarningAbove.getText().toString().trim());
                } catch (Exception e) {
                    thresholdAboveWarning = null;
                }
                sensorToConfigure.setThresholdAboveWarning(thresholdAboveWarning);
                try {
                    thresholdAboveCritical = Float.parseFloat(uiBinding.edSensorAddeditDecimalCriticalAbove.getText().toString().trim());
                } catch (Exception e) {
                    thresholdAboveCritical = null;
                }
                sensorToConfigure.setThresholdAboveCritical(thresholdAboveCritical);
                try {
                    thresholdBelowWarning = Float.parseFloat(uiBinding.edSensorAddeditDecimalWarningBelow.getText().toString().trim());
                } catch (Exception e) {
                    thresholdBelowWarning = null;
                }
                sensorToConfigure.setThresholdBelowWarning(thresholdBelowWarning);
                try {
                    thresholdBelowCritical = Float.parseFloat(uiBinding.edSensorAddeditDecimalCriticalBelow.getText().toString().trim());
                } catch (Exception e) {
                    thresholdBelowCritical = null;
                }
                sensorToConfigure.setThresholdBelowCritical(thresholdBelowCritical);
                sensorToConfigure.setThresholdEqualsWarning(null);
                sensorToConfigure.setThresholdEqualsCritical(null);
                break;
            case BOOLEAN:
                sensorToConfigure.setDataUnit(null);
                sensorToConfigure.setThresholdAboveWarning(null);
                sensorToConfigure.setThresholdAboveCritical(null);
                sensorToConfigure.setThresholdBelowWarning(null);
                sensorToConfigure.setThresholdBelowCritical(null);
                thresholdEqualsWarning = uiBinding.edSensorAddeditBooleanWarningEquals.getText().toString().trim();
                try {
                    sensorToConfigure.setThresholdEqualsWarning((!thresholdEqualsWarning.isEmpty()) ? Boolean.valueOf(thresholdEqualsWarning).toString() : null);
                } catch (Exception e) {
                    sensorToConfigure.setThresholdEqualsWarning(null);
                }
                thresholdEqualsCritical = uiBinding.edSensorAddeditBooleanCriticalEquals.getText().toString().trim();
                try {
                    sensorToConfigure.setThresholdEqualsCritical((!thresholdEqualsCritical.isEmpty()) ? Boolean.valueOf(thresholdEqualsCritical).toString() : null);
                } catch (Exception e) {
                    sensorToConfigure.setThresholdEqualsCritical(null);
                }
                break;
            case STRING:
                sensorToConfigure.setDataUnit(null);
                sensorToConfigure.setThresholdAboveWarning(null);
                sensorToConfigure.setThresholdAboveCritical(null);
                sensorToConfigure.setThresholdBelowWarning(null);
                sensorToConfigure.setThresholdBelowCritical(null);
                thresholdEqualsWarning = uiBinding.edSensorAddeditStringWarningEquals.getText().toString().trim();
                try {
                    sensorToConfigure.setThresholdEqualsWarning((!thresholdEqualsWarning.isEmpty()) ? Boolean.valueOf(thresholdEqualsWarning).toString() : null);
                } catch (Exception e) {
                    sensorToConfigure.setThresholdEqualsWarning(null);
                }
                thresholdEqualsCritical = uiBinding.edSensorAddeditStringCriticalEquals.getText().toString().trim();
                try {
                    sensorToConfigure.setThresholdEqualsCritical((!thresholdEqualsCritical.isEmpty()) ? Boolean.valueOf(thresholdEqualsCritical).toString() : null);
                } catch (Exception e) {
                    sensorToConfigure.setThresholdEqualsCritical(null);
                }
                break;
        }
    }

    public boolean checkFoldData() {
        try {
            String name = uiBinding.edSensorAddeditName.getText().toString().trim();
            if (name.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_name));
                return false;
            }
            String type = uiBinding.edSensorAddeditType.getText().toString().trim();
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_type));
                return false;
            }
            int networkPos = uiBinding.spSensorAddeditNetwork.getSelectedItemPosition();
            NetworkExtended networkToUse = networksAvailable.get(networkPos);
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_network));
                return false;
            }
            switch (networkToUse.getNetworkType()) {
                case HTTP:
                    String relativeUrl = uiBinding.edSensorAddeditHttpRelativeurl.getText().toString().trim();
                    if (relativeUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl));
                        return false;
                    }
                    try {
                        Integer interval = Integer.valueOf(
                                uiBinding.edSensorAddeditHttpInterval.getText().toString().trim());
                        if (interval < MIN_HTTP_INTERVAL) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_interval_number));
                            return false;
                        }
                    } catch (Exception e) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_interval));
                        return false;
                    }
                    break;
                case MQTT:
                    String mqttTopic = uiBinding.edSensorAddeditMqttTopic.getText().toString().trim();
                    if (mqttTopic.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl));
                        return false;
                    }
                    break;
            }
            switch (Enumerators.MessageType.valueOf(uiBinding.spSensorAddeditMessageType.getSelectedItemPosition())) {
                case XML:
                    String xmlNode = uiBinding.edSensorAddeditXmlNode.getText().toString().trim();
                    if (xmlNode.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_xml_node));
                        return false;
                    }
                    break;
                case JSON:
                    String jsonNode = uiBinding.edSensorAddeditJsonNode.getText().toString().trim();
                    if (jsonNode.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_json_node));
                        return false;
                    }
                    break;
                case RAW:
                    String rawRegex = uiBinding.edSensorAddeditRawRegex.getText().toString().trim();
                    if (rawRegex.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_raw_regex));
                        return false;
                    }
                    break;
            }
            switch (Enumerators.DataType.valueOf(uiBinding.spSensorAddeditDataType.getSelectedItemPosition())) {
                case INTEGER:
                    if (!uiBinding.edSensorAddeditIntegerWarningAbove.getText().toString().trim().isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding.edSensorAddeditIntegerWarningAbove.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditIntegerCriticalAbove.getText().toString().trim().isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding.edSensorAddeditIntegerCriticalAbove.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditIntegerWarningBelow.getText().toString().trim().isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding.edSensorAddeditIntegerWarningBelow.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditIntegerCriticalBelow.getText().toString().trim().isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding.edSensorAddeditIntegerCriticalBelow.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer));
                            return false;
                        }
                    }
                    break;
                case DECIMAL:
                    if (!uiBinding.edSensorAddeditIntegerWarningAbove.getText().toString().trim().isEmpty()) {
                        try {
                            Float.parseFloat(uiBinding.edSensorAddeditIntegerWarningAbove.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditIntegerCriticalAbove.getText().toString().trim().isEmpty()) {
                        try {
                            Float.parseFloat(uiBinding.edSensorAddeditIntegerCriticalAbove.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditIntegerWarningBelow.getText().toString().trim().isEmpty()) {
                        try {
                            Float.parseFloat(uiBinding.edSensorAddeditIntegerWarningBelow.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditIntegerCriticalBelow.getText().toString().trim().isEmpty()) {
                        try {
                            Float.parseFloat(uiBinding.edSensorAddeditIntegerCriticalBelow.getText().toString().trim());
                        } catch (Exception e) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal));
                            return false;
                        }
                    }
                    break;
                case BOOLEAN:
                    if (!uiBinding.edSensorAddeditBooleanWarningEquals.getText().toString().trim().isEmpty()) {
                        String booleanStr = uiBinding.edSensorAddeditBooleanWarningEquals.getText().toString().trim();
                        if (!((booleanStr.equalsIgnoreCase("true")) ||
                                (booleanStr.equalsIgnoreCase("false")))) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_boolean));
                            return false;
                        }
                    }
                    if (!uiBinding.edSensorAddeditBooleanCriticalEquals.getText().toString().trim().isEmpty()) {
                        String booleanStr = uiBinding.edSensorAddeditBooleanCriticalEquals.getText().toString().trim();
                        if (!((booleanStr.equalsIgnoreCase("true")) ||
                                (booleanStr.equalsIgnoreCase("false")))) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_boolean));
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
            uiBinding.sensorAddeditEditareaLayout.setVisibility(View.GONE);
            uiBinding.sensorAddeditLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the edition area.
     */
    private void showEditArea() {
        if (uiBinding != null) {
            uiBinding.sensorAddeditEditareaLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorAddeditLoadingLayout.loadingLayout.setVisibility(View.GONE);
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
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder()
                    .showLatLong(true)  // Show Coordinates in the Activity
                    .setMarkerImageImageColor(R.color.colorPrimary)
                    .setPrimaryTextColor(R.color.primaryTextColor) // Change text color of Shortened Address
                    .setSecondaryTextColor(R.color.secondaryTextColor) // Change text color of full Address
                    .setMapType(MapType.NORMAL)
                    .onlyCoordinates(true);  //Get only Coordinates from Place Picker
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
                String newHeaderName = uiBinding.edSensorAddeditHttpNewheaderName.getText().toString();
                String newHeaderValue = uiBinding.edSensorAddeditHttpNewheaderValue.getText().toString();
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
        uiBinding.setMessageTypeSelected(ordinal);
    }

    @Override
    public void dataTypeSelected(int ordinal) {
        uiBinding.setDataTypeSelected(ordinal);
    }

}
