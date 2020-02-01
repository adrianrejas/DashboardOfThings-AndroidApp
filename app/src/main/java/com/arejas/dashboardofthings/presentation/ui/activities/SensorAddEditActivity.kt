package com.arejas.dashboardofthings.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivitySensorAddEditBinding
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorAddEditViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.helpers.AddEditSensorActuatorPresenter
import com.arejas.dashboardofthings.presentation.ui.helpers.HttpHeaderListAdapter
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.Utils
import com.sucho.placepicker.AddressData
import com.sucho.placepicker.Constants
import com.sucho.placepicker.PlacePicker

import java.util.HashMap

import javax.inject.Inject

import dagger.android.AndroidInjection

/**
 * An activity representing a single Sensor detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [SensorListActivity].
 */
class SensorAddEditActivity : AppCompatActivity(), AddEditSensorActuatorPresenter {

    private var sensorAddEditViewModel: SensorAddEditViewModel? = null

    internal var uiBinding: ActivitySensorAddEditBinding? = null

    private var menu: Menu? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null


    internal var editionMode: Boolean = false
    internal var sensorId: Int? = null
    internal var sensorEdited: SensorExtended? = null
    private var imagePicked: String? = null
    private var locationPicked: AddressData? = null
    private var networksAvailable: List<NetworkExtended>? = null
    private var mHttpHeadersAdapter: HttpHeaderListAdapter? = null
    private var mHttpHeaders: MutableMap<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_sensor_add_edit)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.cancel)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        /* Get view model*/
        sensorAddEditViewModel = ViewModelProviders.of(this, this.viewModelFactory)
            .get(SensorAddEditViewModel::class.java)

        // Load the data in order to preserve data when orientation changes, setting the network id
        // in the viewmodel only if it's a new activity, in order to preserve the entity currently being modified
        if (savedInstanceState != null && savedInstanceState.containsKey(SENSOR_ID)) {
            sensorId = savedInstanceState.getInt(SENSOR_ID)
            if (sensorId == NEW_SENSOR_ID) {
                sensorId = null
            }
        } else if (intent.extras != null && intent.extras!!.containsKey(SENSOR_ID)) {
            sensorId = intent.getIntExtra(SENSOR_ID, -1)
            sensorAddEditViewModel!!.sensorId = sensorId
        } else {
            sensorId = null
            sensorAddEditViewModel!!.sensorId = null
        }

        // set if entity is being edited or created
        if (sensorId == null || sensorId < 0) {
            editionMode = false
            supportActionBar!!.setTitle(getString(R.string.toolbar_title_new_sensor))
        } else {
            editionMode = true
            supportActionBar!!.setTitle(getString(R.string.toolbar_title_edit_sensor))
        }

        // set listener and edition mode
        uiBinding!!.presenter = this
        uiBinding!!.editionMode = editionMode

        // set the UI based on if the element is being created or edited, and if an in-edition version
        // of the entity exists
        mHttpHeaders = HashMap()
        configureListAdapter()
        updateHttpHeaderList()
        if (editionMode) {
            if (sensorAddEditViewModel!!.sensorBeingEdited == null) {
                sensorAddEditViewModel!!.getSensor(true)!!.observe(this, { sensorExtendedResource ->
                    try {
                        if (sensorExtendedResource == null) {
                            ToastHelper.showToast(getString(R.string.toast_edition_failed))
                            finish()
                        } else {
                            if (sensorExtendedResource!!.getStatus() == Resource.Status.ERROR) {
                                ToastHelper.showToast(getString(R.string.toast_edition_failed))
                                finish()
                            } else if (sensorExtendedResource!!.getStatus() == Resource.Status.LOADING) {
                                showLoading()
                            } else {
                                val sensor = sensorExtendedResource!!.data
                                populateUiWithProvidedSensor(sensor)
                                showEditArea()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ToastHelper.showToast(getString(R.string.toast_edition_failed))
                        finish()
                    }
                })
            } else {
                sensorEdited = sensorAddEditViewModel!!.sensorBeingEdited
                try {
                    populateUiWithProvidedSensor(sensorAddEditViewModel!!.sensorBeingEdited)
                    showEditArea()
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                    finish()
                }

            }
        } else {
            showEditArea()
            if (sensorAddEditViewModel!!.sensorBeingEdited != null) {
                try {
                    populateUiWithProvidedSensor(sensorAddEditViewModel!!.sensorBeingEdited)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                    finish()
                }

            }
        }

        sensorAddEditViewModel!!.getNetworks(true)!!.observe(this, { listResource ->
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                    finish()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_edition_failed))
                        finish()
                    } else if (listResource!!.getStatus() == Resource.Status.SUCCESS) {
                        networksAvailable = listResource!!.data
                        if (networksAvailable!!.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.toast_create_first_network))
                            finish()
                        }
                        uiBinding!!.networks = networksAvailable
                        selectNetworkInSpinner()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastHelper.showToast(getString(R.string.toast_edition_failed))
                finish()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_addeditelement, menu)
        this.menu = menu
        return true
    }

    /**
     * Function called when a menu item is selected.
     *
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_ok -> {
                if (editionMode) {
                    updateSensor()
                } else {
                    createSensor()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var sensorInEdition = sensorAddEditViewModel!!.sensorBeingEdited
        if (sensorInEdition == null) sensorInEdition = SensorExtended()
        try {
            configureSensorObjectWithFoldData(sensorInEdition, true)
            sensorAddEditViewModel!!.sensorBeingEdited = sensorInEdition
        } catch (e: Exception) {
            sensorAddEditViewModel!!.sensorBeingEdited = null
        }

        if (sensorId != null) {
            outState.putInt(SENSOR_ID, sensorId!!)
        } else {
            outState.putInt(SENSOR_ID, NEW_SENSOR_ID)
        }
        super.onSaveInstanceState(outState)
    }

    private fun createSensor() {
        try {
            val newSensor = Sensor()
            if (checkFoldData()) {
                configureSensorObjectWithFoldData(newSensor, true)
                sensorAddEditViewModel!!.createSensor(newSensor)
                finish()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.toast_create_failed))
            finish()
        }

    }

    private fun updateSensor() {
        try {
            if (checkFoldData()) {
                configureSensorObjectWithFoldData(sensorEdited!!, false)
                sensorAddEditViewModel!!.updateSensor(sensorEdited)
                finish()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.toast_update_failed))
            finish()
        }

    }

    private fun selectNetworkInSpinner() {
        var positionToSelect = 0
        if (sensorEdited != null && sensorEdited!!.networkId != null) {
            for (network in networksAvailable!!) {
                if (sensorEdited!!.networkId == network.id) {
                    break
                }
                positionToSelect++
            }
        }
        uiBinding!!.networkSelected = positionToSelect
    }

    private fun configureListAdapter() {
        // Configure recycler view with a linear layout
        val linearLayout = LinearLayoutManager(applicationContext)
        uiBinding!!.rvSensorAddeditHeasersSet.layoutManager = linearLayout
        // Configure adapter for recycler view
        mHttpHeadersAdapter = HttpHeaderListAdapter(this)
        uiBinding!!.rvSensorAddeditHeasersSet.adapter = mHttpHeadersAdapter
    }

    private fun updateHttpHeaderList() {
        if (mHttpHeaders != null) {
            mHttpHeadersAdapter!!.data = mHttpHeaders
            mHttpHeadersAdapter!!.notifyDataSetChanged()
            uiBinding!!.edSensorAddeditHttpNewheaderName.setText("")
            uiBinding!!.edSensorAddeditHttpNewheaderValue.setText("")
        }
    }

    @Throws(Exception::class)
    fun populateUiWithProvidedSensor(sensorToUse: SensorExtended?) {
        try {
            sensorEdited = sensorToUse
            sensorAddEditViewModel!!.sensorBeingEdited = sensorToUse
            if (sensorToUse != null) {
                imagePicked = sensorToUse.imageUri
                if (sensorToUse.locationLong != null && sensorToUse.locationLat != null) {
                    locationPicked = AddressData(
                        sensorToUse.locationLat!!,
                        sensorToUse.locationLong!!, null
                    )

                } else {
                    locationPicked = null
                }
                uiBinding!!.sensor = sensorToUse
                uiBinding!!.imagePicked = imagePicked
                uiBinding!!.locationPicked = locationPicked
                uiBinding!!.networkTypeSelected = sensorToUse.networkType!!.ordinal
                uiBinding!!.messageTypeSelected = sensorToUse.messageType!!.ordinal
                uiBinding!!.dataTypeSelected = sensorToUse.dataType!!.ordinal
                mHttpHeaders!!.clear()
                if (sensorEdited!!.httpHeaders != null)
                    mHttpHeaders!!.putAll(sensorEdited!!.httpHeaders!!)
                updateHttpHeaderList()
            } else {
                ToastHelper.showToast(getString(R.string.toast_edition_failed))
                finish()
            }
            if (uiBinding!!.networks != null) {
                selectNetworkInSpinner()
            }
            uiBinding!!.edSensorAddeditName.setText(sensorToUse!!.name)
            uiBinding!!.spSensorAddeditMessageType.setSelection(sensorToUse.messageType!!.ordinal)
            uiBinding!!.spSensorAddeditDataType.setSelection(sensorToUse.dataType!!.ordinal)
            when (sensorToUse.networkType) {
                Enumerators.NetworkType.HTTP -> {
                }
                Enumerators.NetworkType.MQTT -> uiBinding!!.spSensorAddeditMqttQos.setSelection(
                    sensorToUse.mqttQosLevel!!.ordinal
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(Exception::class)
    fun configureSensorObjectWithFoldData(sensorToConfigure: Sensor, creatingNewSensor: Boolean) {
        val name = uiBinding!!.edSensorAddeditName.text.toString().trim { it <= ' ' }
        sensorToConfigure.name = name
        val type = uiBinding!!.edSensorAddeditType.text.toString().trim { it <= ' ' }
        sensorToConfigure.type = type
        val networkPos = uiBinding!!.spSensorAddeditNetwork.selectedItemPosition
        val networkToUse = networksAvailable!![networkPos]
        sensorToConfigure.networkId = networkToUse.id
        sensorToConfigure.messageType = Enumerators.MessageType.valueOf(
            uiBinding!!.spSensorAddeditMessageType.selectedItemPosition
        )
        sensorToConfigure.dataType = Enumerators.DataType.valueOf(
            uiBinding!!.spSensorAddeditDataType.selectedItemPosition
        )
        sensorToConfigure.imageUri = if (imagePicked != null) imagePicked else null
        if (locationPicked != null) {
            sensorToConfigure.locationLat = locationPicked!!.latitude
            sensorToConfigure.locationLong = locationPicked!!.longitude
        }
        sensorToConfigure.isShowInMainDashboard =
            uiBinding!!.cbSensorAddeditShowInMainDashboard.isChecked
        when (networkToUse.networkType) {
            Enumerators.NetworkType.HTTP -> {
                val relativeUrl =
                    uiBinding!!.edSensorAddeditHttpRelativeurl.text.toString().trim { it <= ' ' }
                sensorToConfigure.httpRelativeUrl = relativeUrl
                sensorToConfigure.httpHeaders = mHttpHeadersAdapter!!.data
                try {
                    sensorToConfigure.httpSecondsBetweenRequests = Integer.valueOf(
                        uiBinding!!.edSensorAddeditHttpInterval.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    sensorToConfigure.httpSecondsBetweenRequests = 0
                }

                sensorToConfigure.mqttTopicToSubscribe = null
                sensorToConfigure.mqttQosLevel = Enumerators.MqttQosLevel.QOS_0
            }
            Enumerators.NetworkType.MQTT -> {
                val topic = uiBinding!!.edSensorAddeditMqttTopic.text.toString().trim { it <= ' ' }
                sensorToConfigure.mqttTopicToSubscribe = topic
                sensorToConfigure.mqttQosLevel = Enumerators.MqttQosLevel.valueOf(
                    uiBinding!!.spSensorAddeditMqttQos.selectedItemPosition
                )
                sensorToConfigure.httpRelativeUrl = null
                sensorToConfigure.httpHeaders = null
                sensorToConfigure.httpSecondsBetweenRequests = null
            }
        }
        when (sensorToConfigure.messageType) {
            Enumerators.MessageType.XML -> {
                val xmlNode = uiBinding!!.edSensorAddeditXmlNode.text.toString().trim { it <= ' ' }
                sensorToConfigure.xmlOrJsonNode = xmlNode
                sensorToConfigure.rawRegularExpression = null
            }
            Enumerators.MessageType.JSON -> {
                val jsonNode =
                    uiBinding!!.edSensorAddeditJsonNode.text.toString().trim { it <= ' ' }
                sensorToConfigure.xmlOrJsonNode = jsonNode
                sensorToConfigure.rawRegularExpression = null
            }
            Enumerators.MessageType.RAW -> {
                val rawRegex =
                    uiBinding!!.edSensorAddeditRawRegex.text.toString().trim { it <= ' ' }
                sensorToConfigure.rawRegularExpression = rawRegex
                sensorToConfigure.xmlOrJsonNode = null
            }
        }
        val dataUnit: String
        var thresholdAboveWarning: Float?
        var thresholdAboveCritical: Float?
        var thresholdBelowWarning: Float?
        var thresholdBelowCritical: Float?
        val thresholdEqualsWarning: String
        val thresholdEqualsCritical: String
        when (sensorToConfigure.dataType) {
            Enumerators.DataType.INTEGER -> {
                dataUnit = uiBinding!!.edSensorAddeditIntegerUnit.text.toString().trim { it <= ' ' }
                sensorToConfigure.dataUnit = if (!dataUnit.isEmpty()) dataUnit else null
                try {
                    thresholdAboveWarning =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditIntegerWarningAbove.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdAboveWarning = null
                }

                sensorToConfigure.thresholdAboveWarning = thresholdAboveWarning
                try {
                    thresholdAboveCritical =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditIntegerCriticalAbove.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdAboveCritical = null
                }

                sensorToConfigure.thresholdAboveCritical = thresholdAboveCritical
                try {
                    thresholdBelowWarning =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditIntegerWarningBelow.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdBelowWarning = null
                }

                sensorToConfigure.thresholdBelowWarning = thresholdBelowWarning
                try {
                    thresholdBelowCritical =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditIntegerCriticalBelow.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdBelowCritical = null
                }

                sensorToConfigure.thresholdBelowCritical = thresholdBelowCritical
                sensorToConfigure.thresholdEqualsWarning = null
                sensorToConfigure.thresholdEqualsCritical = null
            }
            Enumerators.DataType.DECIMAL -> {
                dataUnit = uiBinding!!.edSensorAddeditDecimalUnit.text.toString().trim { it <= ' ' }
                sensorToConfigure.dataUnit = if (!dataUnit.isEmpty()) dataUnit else null
                try {
                    thresholdAboveWarning =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalWarningAbove.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdAboveWarning = null
                }

                sensorToConfigure.thresholdAboveWarning = thresholdAboveWarning
                try {
                    thresholdAboveCritical =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalCriticalAbove.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdAboveCritical = null
                }

                sensorToConfigure.thresholdAboveCritical = thresholdAboveCritical
                try {
                    thresholdBelowWarning =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalWarningBelow.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdBelowWarning = null
                }

                sensorToConfigure.thresholdBelowWarning = thresholdBelowWarning
                try {
                    thresholdBelowCritical =
                        java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalCriticalBelow.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    thresholdBelowCritical = null
                }

                sensorToConfigure.thresholdBelowCritical = thresholdBelowCritical
                sensorToConfigure.thresholdEqualsWarning = null
                sensorToConfigure.thresholdEqualsCritical = null
            }
            Enumerators.DataType.BOOLEAN -> {
                sensorToConfigure.dataUnit = null
                sensorToConfigure.thresholdAboveWarning = null
                sensorToConfigure.thresholdAboveCritical = null
                sensorToConfigure.thresholdBelowWarning = null
                sensorToConfigure.thresholdBelowCritical = null
                thresholdEqualsWarning =
                    uiBinding!!.edSensorAddeditBooleanWarningEquals.text.toString()
                        .trim { it <= ' ' }
                try {
                    sensorToConfigure.thresholdEqualsWarning =
                        if (!thresholdEqualsWarning.isEmpty()) java.lang.Boolean.valueOf(
                            thresholdEqualsWarning
                        ).toString() else null
                } catch (e: Exception) {
                    sensorToConfigure.thresholdEqualsWarning = null
                }

                thresholdEqualsCritical =
                    uiBinding!!.edSensorAddeditBooleanCriticalEquals.text.toString()
                        .trim { it <= ' ' }
                try {
                    sensorToConfigure.thresholdEqualsCritical =
                        if (!thresholdEqualsCritical.isEmpty()) java.lang.Boolean.valueOf(
                            thresholdEqualsCritical
                        ).toString() else null
                } catch (e: Exception) {
                    sensorToConfigure.thresholdEqualsCritical = null
                }

            }
            Enumerators.DataType.STRING -> {
                sensorToConfigure.dataUnit = null
                sensorToConfigure.thresholdAboveWarning = null
                sensorToConfigure.thresholdAboveCritical = null
                sensorToConfigure.thresholdBelowWarning = null
                sensorToConfigure.thresholdBelowCritical = null
                thresholdEqualsWarning =
                    uiBinding!!.edSensorAddeditStringWarningEquals.text.toString()
                        .trim { it <= ' ' }
                try {
                    sensorToConfigure.thresholdEqualsWarning =
                        if (!thresholdEqualsWarning.isEmpty()) thresholdEqualsWarning else null
                } catch (e: Exception) {
                    sensorToConfigure.thresholdEqualsWarning = null
                }

                thresholdEqualsCritical =
                    uiBinding!!.edSensorAddeditStringCriticalEquals.text.toString()
                        .trim { it <= ' ' }
                try {
                    sensorToConfigure.thresholdEqualsCritical =
                        if (!thresholdEqualsCritical.isEmpty()) thresholdEqualsCritical else null
                } catch (e: Exception) {
                    sensorToConfigure.thresholdEqualsCritical = null
                }

            }
        }
    }

    fun checkFoldData(): Boolean {
        try {
            val name = uiBinding!!.edSensorAddeditName.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_name))
                return false
            }
            val type = uiBinding!!.edSensorAddeditType.text.toString().trim { it <= ' ' }
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_type))
                return false
            }
            val networkPos = uiBinding!!.spSensorAddeditNetwork.selectedItemPosition
            val networkToUse = networksAvailable!![networkPos]
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_network))
                return false
            }
            when (networkToUse.networkType) {
                Enumerators.NetworkType.HTTP -> {
                    val relativeUrl = uiBinding!!.edSensorAddeditHttpRelativeurl.text.toString()
                        .trim { it <= ' ' }
                    if (relativeUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl))
                        return false
                    }
                    try {
                        val interval = Integer.valueOf(
                            uiBinding!!.edSensorAddeditHttpInterval.text.toString().trim { it <= ' ' })
                        if (interval < MIN_HTTP_INTERVAL) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_interval_number))
                            return false
                        }
                    } catch (e: Exception) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_interval))
                        return false
                    }

                }
                Enumerators.NetworkType.MQTT -> {
                    val mqttTopic =
                        uiBinding!!.edSensorAddeditMqttTopic.text.toString().trim { it <= ' ' }
                    if (mqttTopic.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_topic))
                        return false
                    }
                }
            }
            when (Enumerators.MessageType.valueOf(uiBinding!!.spSensorAddeditMessageType.selectedItemPosition)) {
                Enumerators.MessageType.XML -> {
                    val xmlNode =
                        uiBinding!!.edSensorAddeditXmlNode.text.toString().trim { it <= ' ' }
                    if (xmlNode.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_xml_node))
                        return false
                    }
                }
                Enumerators.MessageType.JSON -> {
                    val jsonNode =
                        uiBinding!!.edSensorAddeditJsonNode.text.toString().trim { it <= ' ' }
                    if (jsonNode.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_json_node))
                        return false
                    }
                }
                Enumerators.MessageType.RAW -> {
                    val rawRegex =
                        uiBinding!!.edSensorAddeditRawRegex.text.toString().trim { it <= ' ' }
                    if (rawRegex.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_raw_regex))
                        return false
                    }
                }
            }
            when (Enumerators.DataType.valueOf(uiBinding!!.spSensorAddeditDataType.selectedItemPosition)) {
                Enumerators.DataType.INTEGER -> {
                    if (!uiBinding!!.edSensorAddeditIntegerWarningAbove.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding!!.edSensorAddeditIntegerWarningAbove.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer))
                            return false
                        }

                    }
                    if (!uiBinding!!.edSensorAddeditIntegerCriticalAbove.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding!!.edSensorAddeditIntegerCriticalAbove.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer))
                            return false
                        }

                    }
                    if (!uiBinding!!.edSensorAddeditIntegerWarningBelow.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding!!.edSensorAddeditIntegerWarningBelow.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer))
                            return false
                        }

                    }
                    if (!uiBinding!!.edSensorAddeditIntegerCriticalBelow.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding!!.edSensorAddeditIntegerCriticalBelow.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_integer))
                            return false
                        }

                    }
                }
                Enumerators.DataType.DECIMAL -> {
                    if (!uiBinding!!.edSensorAddeditDecimalWarningAbove.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalWarningAbove.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal))
                            return false
                        }

                    }
                    if (!uiBinding!!.edSensorAddeditDecimalCriticalAbove.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalCriticalAbove.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal))
                            return false
                        }

                    }
                    if (!uiBinding!!.edSensorAddeditDecimalWarningBelow.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalWarningBelow.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal))
                            return false
                        }

                    }
                    if (!uiBinding!!.edSensorAddeditDecimalCriticalBelow.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            java.lang.Float.parseFloat(uiBinding!!.edSensorAddeditDecimalCriticalBelow.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal))
                            return false
                        }

                    }
                }
                Enumerators.DataType.BOOLEAN -> {
                    if (!uiBinding!!.edSensorAddeditBooleanWarningEquals.text.toString().trim { it <= ' ' }.isEmpty()) {
                        val booleanStr =
                            uiBinding!!.edSensorAddeditBooleanWarningEquals.text.toString()
                                .trim { it <= ' ' }
                        if (!(booleanStr.equals(
                                "true",
                                ignoreCase = true
                            ) || booleanStr.equals("false", ignoreCase = true))
                        ) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_boolean))
                            return false
                        }
                    }
                    if (!uiBinding!!.edSensorAddeditBooleanCriticalEquals.text.toString().trim { it <= ' ' }.isEmpty()) {
                        val booleanStr =
                            uiBinding!!.edSensorAddeditBooleanCriticalEquals.text.toString()
                                .trim { it <= ' ' }
                        if (!(booleanStr.equals(
                                "true",
                                ignoreCase = true
                            ) || booleanStr.equals("false", ignoreCase = true))
                        ) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_boolean))
                            return false
                        }
                    }
                }
            }
            return true
        } catch (e: Exception) {
            return false
        }

    }

    /**
     * Show the activity info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.sensorAddeditEditareaLayout.visibility = View.GONE
            uiBinding!!.sensorAddeditLoadingLayout.loadingLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the edition area.
     */
    private fun showEditArea() {
        if (uiBinding != null) {
            uiBinding!!.sensorAddeditEditareaLayout.visibility = View.VISIBLE
            uiBinding!!.sensorAddeditLoadingLayout.loadingLayout.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_IMAGE) {
                    imagePicked = Utils.getUriFromFilSelected(this, data!!.data!!)
                    uiBinding!!.imagePicked = imagePicked
                } else if (requestCode == REQUEST_PICK_LOCATION) {
                    locationPicked = data!!.getParcelableExtra(Constants.ADDRESS_INTENT)
                    uiBinding!!.locationPicked = locationPicked
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun pickImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.edit_hint_image_pick)
            ), REQUEST_PICK_IMAGE
        )
    }

    override fun cancelImagePicked() {
        imagePicked = null
        uiBinding!!.imagePicked = imagePicked
    }

    override fun pickLocation() {
        try {
            val builder = Utils.intentBuilderForLocationPicker
            if (locationPicked != null) {
                builder.setLatLong(locationPicked!!.latitude, locationPicked!!.longitude)
            }
            startActivityForResult(builder.build(this), REQUEST_PICK_LOCATION)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun cancelLocationPicked() {
        locationPicked = null
        uiBinding!!.locationPicked = locationPicked
    }

    override fun addHttpHeader() {
        try {
            if (mHttpHeaders != null) {
                val newHeaderName = uiBinding!!.edSensorAddeditHttpNewheaderName.text.toString()
                val newHeaderValue = uiBinding!!.edSensorAddeditHttpNewheaderValue.text.toString()
                mHttpHeaders!![newHeaderName] = newHeaderValue
                updateHttpHeaderList()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.edit_toast_check_http_headers))
        }

    }

    override fun cancelHttpHeader(headerName: String) {
        if (mHttpHeaders != null && mHttpHeaders!!.containsKey(headerName)) {
            mHttpHeaders!!.remove(headerName)
            updateHttpHeaderList()
        }
    }

    override fun networkSelected(ordinal: Int) {
        try {
            if (networksAvailable != null) {
                val network = networksAvailable!![ordinal]
                uiBinding!!.networkTypeSelected = network.networkType!!.ordinal
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun messageTypeSelected(ordinal: Int) {
        uiBinding!!.messageTypeSelected = ordinal
    }

    override fun dataTypeSelected(ordinal: Int) {
        uiBinding!!.dataTypeSelected = ordinal
    }

    companion object {

        val SENSOR_ID = "sensor_id"

        val NEW_SENSOR_ID = -1000

        val MIN_HTTP_INTERVAL = 60

        val REQUEST_PICK_IMAGE = 41
        val REQUEST_PICK_LOCATION = 42
    }

}
