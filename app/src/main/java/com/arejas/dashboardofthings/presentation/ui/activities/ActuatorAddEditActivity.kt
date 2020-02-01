package com.arejas.dashboardofthings.presentation.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.sources.network.data.DataMessageHelper
import com.arejas.dashboardofthings.databinding.ActivityActuatorAddEditBinding
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorAddEditViewModel
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
 * An activity representing a single Actuator detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ActuatorListActivity].
 */
class ActuatorAddEditActivity : AppCompatActivity(), AddEditSensorActuatorPresenter {

    private var actuatorAddEditViewModel: ActuatorAddEditViewModel? = null

    internal var uiBinding: ActivityActuatorAddEditBinding? = null

    private var menu: Menu? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null


    internal var editionMode: Boolean = false
    internal var actuatorId: Int? = null
    internal var actuatorEdited: ActuatorExtended? = null
    private var imagePicked: String? = null
    private var locationPicked: AddressData? = null
    private var networksAvailable: List<NetworkExtended>? = null
    private var mHttpHeadersAdapter: HttpHeaderListAdapter? = null
    private var mHttpHeaders: MutableMap<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_actuator_add_edit)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.cancel)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        /* Get view model*/
        actuatorAddEditViewModel = ViewModelProviders.of(this, this.viewModelFactory)
            .get(ActuatorAddEditViewModel::class.java)

        // Load the data in order to preserve data when orientation changes, setting the network id
        // in the viewmodel only if it's a new activity, in order to preserve the entity currently being modified
        if (savedInstanceState != null && savedInstanceState.containsKey(ACTUATOR_ID)) {
            actuatorId = savedInstanceState.getInt(ACTUATOR_ID)
            if (actuatorId == NEW_ACTUATOR_ID) {
                actuatorId = null
            }
        } else if (intent.extras != null && intent.extras!!.containsKey(ACTUATOR_ID)) {
            actuatorId = intent.getIntExtra(ACTUATOR_ID, -1)
            actuatorAddEditViewModel!!.actuatorId = actuatorId
        } else {
            actuatorId = null
            actuatorAddEditViewModel!!.actuatorId = null
        }

        // set if entity is being edited or created
        if (actuatorId == null) {
            editionMode = false
            supportActionBar!!.setTitle(getString(R.string.toolbar_title_new_actuator))
        } else {
            editionMode = true
            supportActionBar!!.setTitle(getString(R.string.toolbar_title_edit_actuator))
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
            if (actuatorAddEditViewModel!!.actuatorBeingEdited == null) {
                actuatorAddEditViewModel!!.getActuator(true)!!.observe(
                    this,
                    Observer { actuatorExtendedResource ->
                        try {
                            if (actuatorExtendedResource == null) {
                                ToastHelper.showToast(getString(R.string.toast_edition_failed))
                                finish()
                            } else {
                                if (actuatorExtendedResource!!.status == Resource.Status.ERROR) {
                                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                                    finish()
                                } else if (actuatorExtendedResource!!.status == Resource.Status.LOADING) {
                                    showLoading()
                                } else {
                                    val actuator = actuatorExtendedResource!!.data
                                    populateUiWithProvidedActuator(actuator)
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
                actuatorEdited = actuatorAddEditViewModel!!.actuatorBeingEdited
                try {
                    populateUiWithProvidedActuator(actuatorAddEditViewModel!!.actuatorBeingEdited)
                    showEditArea()
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                    finish()
                }

            }
        } else {
            showEditArea()
            if (actuatorAddEditViewModel!!.actuatorBeingEdited != null) {
                try {
                    populateUiWithProvidedActuator(actuatorAddEditViewModel!!.actuatorBeingEdited)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                    finish()
                }

            }
        }

        actuatorAddEditViewModel!!.getNetworks(true)!!.observe(this, Observer { listResource ->
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                    finish()
                } else {
                    if (listResource!!.status == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_edition_failed))
                        finish()
                    } else if (listResource!!.status == Resource.Status.SUCCESS) {
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
                    updateActuator()
                } else {
                    createActuator()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var actuatorInEdition = actuatorAddEditViewModel!!.actuatorBeingEdited
        if (actuatorInEdition == null) actuatorInEdition = ActuatorExtended()
        try {
            configureActuatorObjectWithFoldData(actuatorInEdition, true)
            actuatorAddEditViewModel!!.actuatorBeingEdited = actuatorInEdition
        } catch (e: Exception) {
            actuatorAddEditViewModel!!.actuatorBeingEdited = null
        }

        if (actuatorId != null) {
            outState.putInt(ACTUATOR_ID, actuatorId!!)
        } else {
            outState.putInt(ACTUATOR_ID, NEW_ACTUATOR_ID)
        }
        super.onSaveInstanceState(outState)
    }

    private fun createActuator() {
        try {
            val newActuator = Actuator()
            if (checkFoldData()) {
                configureActuatorObjectWithFoldData(newActuator, true)
                actuatorAddEditViewModel!!.createActuator(newActuator)
                finish()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.toast_create_failed))
            finish()
        }

    }

    private fun updateActuator() {
        try {
            if (checkFoldData()) {
                configureActuatorObjectWithFoldData(actuatorEdited!!, false)
                actuatorAddEditViewModel!!.updateActuator(actuatorEdited!!)
                finish()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.toast_update_failed))
            finish()
        }

    }

    private fun selectNetworkInSpinner() {
        var positionToSelect = 0
        if (actuatorEdited != null && actuatorEdited!!.networkId != null) {
            for (network in networksAvailable!!) {
                if (actuatorEdited!!.networkId == network.id) {
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
        uiBinding!!.rvActuatorAddeditHeasersSet.layoutManager = linearLayout
        // Configure adapter for recycler view
        mHttpHeadersAdapter = HttpHeaderListAdapter(this)
        uiBinding!!.rvActuatorAddeditHeasersSet.adapter = mHttpHeadersAdapter
    }

    private fun updateHttpHeaderList() {
        if (mHttpHeaders != null) {
            mHttpHeadersAdapter!!.data = mHttpHeaders
            mHttpHeadersAdapter!!.notifyDataSetChanged()
            uiBinding!!.edActuatorAddeditHttpNewheaderName.setText("")
            uiBinding!!.edActuatorAddeditHttpNewheaderValue.setText("")
        }
    }

    @Throws(Exception::class)
    fun populateUiWithProvidedActuator(actuatorToUse: ActuatorExtended?) {
        try {
            actuatorEdited = actuatorToUse
            actuatorAddEditViewModel!!.actuatorBeingEdited = actuatorToUse
            if (actuatorToUse != null) {
                imagePicked = actuatorToUse.imageUri
                if (actuatorToUse.locationLong != null && actuatorToUse.locationLat != null) {
                    locationPicked = AddressData(
                        actuatorToUse.locationLat!!,
                        actuatorToUse.locationLong!!, null
                    )

                } else {
                    locationPicked = null
                }
                uiBinding!!.actuator = actuatorToUse
                uiBinding!!.imagePicked = imagePicked
                uiBinding!!.locationPicked = locationPicked
                uiBinding!!.networkTypeSelected = actuatorToUse.networkType!!.ordinal
                uiBinding!!.dataTypeSelected = actuatorToUse.dataType!!.ordinal
                mHttpHeaders!!.clear()
                if (actuatorEdited!!.httpHeaders != null)
                    mHttpHeaders!!.putAll(actuatorEdited!!.httpHeaders!!)
                updateHttpHeaderList()
            } else {
                ToastHelper.showToast(getString(R.string.toast_edition_failed))
                finish()
            }
            if (uiBinding!!.networks != null) {
                selectNetworkInSpinner()
            }
            uiBinding!!.edActuatorAddeditName.setText(actuatorToUse!!.name)
            uiBinding!!.spActuatorAddeditDataType.setSelection(actuatorToUse.dataType!!.ordinal)
            when (actuatorToUse.networkType) {
                Enumerators.NetworkType.HTTP -> uiBinding!!.spActuatorAddeditHttpMethod.setSelection(
                    actuatorToUse.httpMethod!!.ordinal
                )
                Enumerators.NetworkType.MQTT -> uiBinding!!.spActuatorAddeditMqttQos.setSelection(
                    actuatorToUse.mqttQosLevel!!.ordinal
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(Exception::class)
    fun configureActuatorObjectWithFoldData(
        actuatorToConfigure: Actuator,
        creatingNewActuator: Boolean
    ) {
        val name = uiBinding!!.edActuatorAddeditName.text.toString().trim { it <= ' ' }
        actuatorToConfigure.name = name
        val type = uiBinding!!.edActuatorAddeditType.text.toString().trim { it <= ' ' }
        actuatorToConfigure.type = type
        val networkPos = uiBinding!!.spActuatorAddeditNetwork.selectedItemPosition
        val networkToUse = networksAvailable!![networkPos]
        actuatorToConfigure.networkId = networkToUse.id
        actuatorToConfigure.dataType = Enumerators.DataType.valueOf(
            uiBinding!!.spActuatorAddeditDataType.selectedItemPosition
        )
        actuatorToConfigure.imageUri = if (imagePicked != null) imagePicked else null
        if (locationPicked != null) {
            actuatorToConfigure.locationLat = locationPicked!!.latitude
            actuatorToConfigure.locationLong = locationPicked!!.longitude
        }
        actuatorToConfigure.isShowInMainDashboard =
            uiBinding!!.cbActuatorAddeditShowInMainDashboard.isChecked
        when (networkToUse.networkType) {
            Enumerators.NetworkType.HTTP -> {
                val relativeUrl =
                    uiBinding!!.edActuatorAddeditHttpRelativeurl.text.toString().trim { it <= ' ' }
                actuatorToConfigure.httpRelativeUrl = relativeUrl
                actuatorToConfigure.httpHeaders = mHttpHeadersAdapter!!.data
                actuatorToConfigure.httpMethod = Enumerators.HttpMethod.valueOf(
                    uiBinding!!.spActuatorAddeditHttpMethod.selectedItemPosition
                )
                actuatorToConfigure.mqttTopicToPublish = null
                actuatorToConfigure.mqttQosLevel = Enumerators.MqttQosLevel.QOS_0
                val mimeType =
                    uiBinding!!.edActuatorAddeditHttpMimeType.text.toString().trim { it <= ' ' }
                actuatorToConfigure.httpMimeType = mimeType
            }
            Enumerators.NetworkType.MQTT -> {
                val topic =
                    uiBinding!!.edActuatorAddeditMqttTopic.text.toString().trim { it <= ' ' }
                actuatorToConfigure.mqttTopicToPublish = topic
                actuatorToConfigure.mqttQosLevel = Enumerators.MqttQosLevel.valueOf(
                    uiBinding!!.spActuatorAddeditMqttQos.selectedItemPosition
                )
                actuatorToConfigure.httpRelativeUrl = null
                actuatorToConfigure.httpHeaders = null
                actuatorToConfigure.httpMethod = Enumerators.HttpMethod.GET
                actuatorToConfigure.httpMimeType = null
            }
        }
        val dataFormat =
            uiBinding!!.edActuatorAddeditMessageFormat.text.toString().trim { it <= ' ' }
        actuatorToConfigure.dataFormatMessageToSend = dataFormat
        val dataUnit: String
        var dataNumberMinimum: Float?
        var dataNumberMaximum: Float?
        when (actuatorToConfigure.dataType) {
            Enumerators.DataType.INTEGER -> {
                dataUnit =
                    uiBinding!!.edActuatorAddeditIntegerUnit.text.toString().trim { it <= ' ' }
                actuatorToConfigure.dataUnit = if (!dataUnit.isEmpty()) dataUnit else null
                try {
                    dataNumberMinimum =
                        java.lang.Float.parseFloat(uiBinding!!.edActuatorAddeditIntegerMinimum.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    dataNumberMinimum = null
                }

                actuatorToConfigure.dataNumberMinimum = dataNumberMinimum
                try {
                    dataNumberMaximum =
                        java.lang.Float.parseFloat(uiBinding!!.edActuatorAddeditIntegerMaximum.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    dataNumberMaximum = null
                }

                actuatorToConfigure.dataNumberMaximum = dataNumberMaximum
            }
            Enumerators.DataType.DECIMAL -> {
                dataUnit =
                    uiBinding!!.edActuatorAddeditDecimalUnit.text.toString().trim { it <= ' ' }
                actuatorToConfigure.dataUnit = if (!dataUnit.isEmpty()) dataUnit else null
                try {
                    dataNumberMinimum =
                        java.lang.Float.parseFloat(uiBinding!!.edActuatorAddeditDecimalMinimum.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    dataNumberMinimum = null
                }

                actuatorToConfigure.dataNumberMinimum = dataNumberMinimum
                try {
                    dataNumberMaximum =
                        java.lang.Float.parseFloat(uiBinding!!.edActuatorAddeditDecimalMaximum.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    dataNumberMaximum = null
                }

                actuatorToConfigure.dataNumberMaximum = dataNumberMaximum
            }
            Enumerators.DataType.BOOLEAN, Enumerators.DataType.STRING -> {
                actuatorToConfigure.dataUnit = null
                actuatorToConfigure.dataNumberMinimum = null
                actuatorToConfigure.dataNumberMaximum = null
            }
        }
    }

    fun checkFoldData(): Boolean {
        try {
            val name = uiBinding!!.edActuatorAddeditName.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_name))
                return false
            }
            val type = uiBinding!!.edActuatorAddeditType.text.toString().trim { it <= ' ' }
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_type))
                return false
            }
            val networkPos = uiBinding!!.spActuatorAddeditNetwork.selectedItemPosition
            val networkToUse = networksAvailable!![networkPos]
            if (type.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_network))
                return false
            }
            when (networkToUse.networkType) {
                Enumerators.NetworkType.HTTP -> {
                    val relativeUrl = uiBinding!!.edActuatorAddeditHttpRelativeurl.text.toString()
                        .trim { it <= ' ' }
                    if (relativeUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl))
                        return false
                    }
                    val httpMethod = Enumerators.HttpMethod.valueOf(
                        uiBinding!!.spActuatorAddeditHttpMethod.selectedItemPosition
                    )
                    if (httpMethod != Enumerators.HttpMethod.GET) {
                        val mimeType = uiBinding!!.edActuatorAddeditHttpMimeType.text.toString()
                            .trim { it <= ' ' }
                        if (mimeType.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_mimetype))
                            return false
                        }
                    }
                }
                Enumerators.NetworkType.MQTT -> {
                    val mqttTopic =
                        uiBinding!!.edActuatorAddeditMqttTopic.text.toString().trim { it <= ' ' }
                    if (mqttTopic.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_topic))
                        return false
                    }
                }
            }
            val messageFormat =
                uiBinding!!.edActuatorAddeditMessageFormat.text.toString().trim { it <= ' ' }
            if (messageFormat.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_message_format))
                return false
            } else if (!DataMessageHelper.checkDataPrecenseInActuatorMessageFormat(messageFormat)) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_message_format_data))
                return false
            }
            when (Enumerators.DataType.valueOf(uiBinding!!.spActuatorAddeditDataType.selectedItemPosition)) {
                Enumerators.DataType.INTEGER -> {
                    if (!uiBinding!!.edActuatorAddeditIntegerMinimum.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding!!.edActuatorAddeditIntegerMinimum.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_limits_integer))
                            return false
                        }

                    }
                    if (!uiBinding!!.edActuatorAddeditIntegerMaximum.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            Integer.parseInt(uiBinding!!.edActuatorAddeditIntegerMaximum.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_limits_integer))
                            return false
                        }

                    }
                }
                Enumerators.DataType.DECIMAL -> {
                    if (!uiBinding!!.edActuatorAddeditDecimalMinimum.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            java.lang.Float.parseFloat(uiBinding!!.edActuatorAddeditDecimalMinimum.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_thresholds_decimal))
                            return false
                        }

                    }
                    if (!uiBinding!!.edActuatorAddeditDecimalMaximum.text.toString().trim { it <= ' ' }.isEmpty()) {
                        try {
                            java.lang.Float.parseFloat(uiBinding!!.edActuatorAddeditDecimalMaximum.text.toString().trim { it <= ' ' })
                        } catch (e: Exception) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_limits_decimal))
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
            uiBinding!!.actuatorAddeditEditareaLayout.visibility = View.GONE
            uiBinding!!.actuatorAddeditLoadingLayout.loadingLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the edition area.
     */
    private fun showEditArea() {
        if (uiBinding != null) {
            uiBinding!!.actuatorAddeditEditareaLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorAddeditLoadingLayout.loadingLayout.visibility = View.GONE
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
                val newHeaderName = uiBinding!!.edActuatorAddeditHttpNewheaderName.text.toString()
                val newHeaderValue = uiBinding!!.edActuatorAddeditHttpNewheaderValue.text.toString()
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
        // Not required to be implemented for actuators
    }

    override fun dataTypeSelected(ordinal: Int) {
        uiBinding!!.dataTypeSelected = ordinal
    }

    companion object {

        val ACTUATOR_ID = "actuator_id"

        val NEW_ACTUATOR_ID = -1000

        val MIN_HTTP_INTERVAL = 60

        val REQUEST_PICK_IMAGE = 41
        val REQUEST_PICK_LOCATION = 42
    }

}
