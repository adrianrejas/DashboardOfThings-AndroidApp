package com.arejas.dashboardofthings.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivityNetworkAddEditBinding
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkAddEditViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.helpers.AddEditNetworkPresenter
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.Utils

import javax.inject.Inject

import dagger.android.AndroidInjection

/**
 * An activity representing a single Network detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [NetworkListActivity].
 */
class NetworkAddEditActivity : AppCompatActivity(), AddEditNetworkPresenter {

    private var networkAddEditViewModel: NetworkAddEditViewModel? = null

    internal var uiBinding: ActivityNetworkAddEditBinding? = null

    private var menu: Menu? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null


    internal var editionMode: Boolean = false
    internal var networkId: Int? = null
    internal var networkEdited: Network? = null
    private var imagePicked: String? = null
    private var httpCertPicked: String? = null
    private var mqttCertPicked: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_network_add_edit)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.cancel)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        /* Get view model*/
        networkAddEditViewModel = ViewModelProviders.of(this, this.viewModelFactory)
            .get(NetworkAddEditViewModel::class.java)

        // Load the data in order to preserve data when orientation changes, setting the network id
        // in the viewmodel only if it's a new activity, in order to preserve the entity currently being modified
        if (savedInstanceState != null && savedInstanceState.containsKey(NETWORK_ID)) {
            networkId = savedInstanceState.getInt(NETWORK_ID)
            if (networkId == NEW_NETWORK_ID) {
                networkId = null
            }
        } else if (intent.extras != null && intent.extras!!.containsKey(NETWORK_ID)) {
            networkId = intent.getIntExtra(NETWORK_ID, -1)
            networkAddEditViewModel!!.networkId = networkId
        } else {
            networkId = null
            networkAddEditViewModel!!.networkId = null
        }

        // set if entity is being edited or created
        if (networkId == null || networkId < 0) {
            supportActionBar!!.setTitle(getString(R.string.toolbar_title_new_network))
            editionMode = false
        } else {
            editionMode = true
            supportActionBar!!.setTitle(getString(R.string.toolbar_title_edit_network))
        }

        // set the UI based on if the element is being created or edited, and if an in-edition version
        // of the entity exists
        uiBinding!!.presenter = this
        uiBinding!!.editionMode = editionMode
        if (editionMode) {
            if (networkAddEditViewModel!!.networkBeingEdited == null) {
                networkAddEditViewModel!!.getNetwork(true)!!.observe(
                    this,
                    { networkExtendedResource ->
                        try {
                            if (networkExtendedResource == null) {
                                ToastHelper.showToast(getString(R.string.toast_edition_failed))
                                finish()
                            } else {
                                if (networkExtendedResource!!.getStatus() == Resource.Status.ERROR) {
                                    ToastHelper.showToast(getString(R.string.toast_edition_failed))
                                    finish()
                                } else if (networkExtendedResource!!.getStatus() == Resource.Status.LOADING) {
                                    showLoading()
                                } else {
                                    val network = networkExtendedResource!!.data
                                    populateUiWithProvidedNetwork(network)
                                    networkEdited = network
                                    networkAddEditViewModel!!.networkBeingEdited = networkEdited
                                    imagePicked = network!!.imageUri
                                    if (network != null) {
                                        uiBinding!!.network = network
                                        uiBinding!!.imagePicked = imagePicked
                                        uiBinding!!.networkTypeSelected =
                                            network!!.networkType!!.ordinal
                                        if (network!!.networkType == Enumerators.NetworkType.HTTP) {
                                            uiBinding!!.networkTypeSelected =
                                                network!!.httpConfiguration!!.httpAauthenticationType!!.ordinal
                                            httpCertPicked =
                                                network!!.httpConfiguration!!.certAuthorityUri
                                            uiBinding!!.httpCertPicked = httpCertPicked
                                            uiBinding!!.useSslInHttp =
                                                network!!.httpConfiguration!!.httpUseSsl
                                        } else if (network!!.networkType == Enumerators.NetworkType.MQTT) {
                                            mqttCertPicked =
                                                network!!.mqttConfiguration!!.mqttCertAuthorityUri
                                            uiBinding!!.mqttCertPicked = mqttCertPicked
                                            uiBinding!!.useSslInMqtt =
                                                network!!.mqttConfiguration!!.mqttUseSsl
                                        }
                                        showEditArea()
                                    } else {
                                        ToastHelper.showToast(getString(R.string.toast_edition_failed))
                                        finish()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    })
            } else {
                networkEdited = networkAddEditViewModel!!.networkBeingEdited
                try {
                    populateUiWithProvidedNetwork(networkAddEditViewModel!!.networkBeingEdited)
                    networkEdited = networkAddEditViewModel!!.networkBeingEdited
                    networkAddEditViewModel!!.networkBeingEdited = networkEdited
                    imagePicked = networkEdited!!.imageUri
                    if (networkEdited != null) {
                        uiBinding!!.network = networkEdited
                        uiBinding!!.imagePicked = imagePicked
                        uiBinding!!.networkTypeSelected = networkEdited!!.networkType!!.ordinal
                        if (networkEdited!!.networkType == Enumerators.NetworkType.HTTP) {
                            uiBinding!!.networkTypeSelected =
                                networkEdited!!.httpConfiguration!!.httpAauthenticationType!!.ordinal
                            httpCertPicked = networkEdited!!.httpConfiguration!!.certAuthorityUri
                            uiBinding!!.httpCertPicked = httpCertPicked
                            uiBinding!!.useSslInHttp =
                                networkEdited!!.httpConfiguration!!.httpUseSsl
                        } else if (networkEdited!!.networkType == Enumerators.NetworkType.MQTT) {
                            mqttCertPicked =
                                networkEdited!!.mqttConfiguration!!.mqttCertAuthorityUri
                            uiBinding!!.mqttCertPicked = mqttCertPicked
                            uiBinding!!.useSslInMqtt =
                                networkEdited!!.mqttConfiguration!!.mqttUseSsl
                        }
                    }
                    showEditArea()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } else {
            showEditArea()
            if (networkAddEditViewModel!!.networkBeingEdited != null) {
                try {
                    populateUiWithProvidedNetwork(networkAddEditViewModel!!.networkBeingEdited)
                    networkEdited = networkAddEditViewModel!!.networkBeingEdited
                    networkAddEditViewModel!!.networkBeingEdited = networkEdited
                    imagePicked = networkEdited!!.imageUri
                    if (networkEdited != null) {
                        uiBinding!!.network = networkEdited
                        uiBinding!!.imagePicked = imagePicked
                        uiBinding!!.networkTypeSelected = networkEdited!!.networkType!!.ordinal
                        if (networkEdited!!.networkType == Enumerators.NetworkType.HTTP) {
                            uiBinding!!.networkTypeSelected =
                                networkEdited!!.httpConfiguration!!.httpAauthenticationType!!.ordinal
                            httpCertPicked = networkEdited!!.httpConfiguration!!.certAuthorityUri
                            uiBinding!!.httpCertPicked = httpCertPicked
                            uiBinding!!.useSslInHttp =
                                networkEdited!!.httpConfiguration!!.httpUseSsl
                        } else if (networkEdited!!.networkType == Enumerators.NetworkType.MQTT) {
                            mqttCertPicked =
                                networkEdited!!.mqttConfiguration!!.mqttCertAuthorityUri
                            uiBinding!!.mqttCertPicked = mqttCertPicked
                            uiBinding!!.useSslInMqtt =
                                networkEdited!!.mqttConfiguration!!.mqttUseSsl
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
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
                    updateNetwork()
                } else {
                    createNetwork()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var networkInEdition = networkAddEditViewModel!!.networkBeingEdited
        if (networkInEdition == null) networkInEdition = Network()
        try {
            configureNetworkObjectWithFoldData(networkInEdition, true)
            networkAddEditViewModel!!.networkBeingEdited = networkInEdition
        } catch (e: Exception) {
            networkAddEditViewModel!!.networkBeingEdited = null
        }

        if (networkId != null) {
            outState.putInt(NETWORK_ID, networkId!!)
        } else {
            outState.putInt(NETWORK_ID, NEW_NETWORK_ID)
        }
        super.onSaveInstanceState(outState)
    }

    private fun createNetwork() {
        try {
            val newNetwork = Network()
            if (checkFoldData()) {
                configureNetworkObjectWithFoldData(newNetwork, true)
                networkAddEditViewModel!!.createNetwork(newNetwork)
                finish()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.toast_create_failed))
            finish()
        }

    }

    private fun updateNetwork() {
        try {
            if (checkFoldData()) {
                configureNetworkObjectWithFoldData(networkEdited!!, false)
                networkAddEditViewModel!!.updateNetwork(networkEdited)
                finish()
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.toast_update_failed))
            finish()
        }

    }

    @Throws(Exception::class)
    fun populateUiWithProvidedNetwork(networkToUse: Network?) {
        try {
            uiBinding!!.edNetworkAddeditName.setText(networkToUse!!.name)
            uiBinding!!.spNetworkAddeditType.setSelection(networkToUse.networkType!!.ordinal)
            imagePicked = networkToUse.imageUri
            uiBinding!!.imagePicked = imagePicked
            when (networkToUse.networkType) {
                Enumerators.NetworkType.HTTP -> {
                    uiBinding!!.edNetworkAddeditHttpBaseurl.setText(networkToUse.httpConfiguration!!.httpBaseUrl)
                    uiBinding!!.spNetworkAddeditHttpAuth.setSelection(networkToUse.httpConfiguration!!.httpAauthenticationType!!.ordinal)
                    if (networkToUse.httpConfiguration!!.httpAauthenticationType == Enumerators.HttpAuthenticationType.BASIC) {
                        uiBinding!!.edNetworkAddeditHttpAuthUsername.setText(networkToUse.httpConfiguration!!.httpUsername)
                        uiBinding!!.edNetworkAddeditHttpAuthPassword.setText(networkToUse.httpConfiguration!!.httpPassword)
                    }
                    uiBinding!!.cbNetworkAddeditHttpUseSsl.isChecked =
                        networkToUse.httpConfiguration!!.httpUseSsl!!
                    if (networkToUse.httpConfiguration!!.httpUseSsl!!) {
                        httpCertPicked = networkToUse.httpConfiguration!!.certAuthorityUri
                        uiBinding!!.httpCertPicked = httpCertPicked
                    }
                }
                Enumerators.NetworkType.MQTT -> {
                    uiBinding!!.edNetworkAddeditMqttBrokerurl.setText(networkToUse.mqttConfiguration!!.mqttBrokerUrl)
                    uiBinding!!.edNetworkAddeditMqttClientid.setText(networkToUse.mqttConfiguration!!.mqttClientId)
                    uiBinding!!.edNetworkAddeditMqttUsername.setText(networkToUse.mqttConfiguration!!.mqttUsername)
                    uiBinding!!.edNetworkAddeditMqttPassword.setText(networkToUse.mqttConfiguration!!.mqttPassword)
                    uiBinding!!.cbNetworkAddeditMqttCleanSession.isChecked =
                        networkToUse.mqttConfiguration!!.mqttCleanSession!!
                    if (networkToUse.mqttConfiguration!!.mqttUseSsl!!) {
                        mqttCertPicked = networkToUse.mqttConfiguration!!.mqttCertAuthorityUri
                        uiBinding!!.mqttCertPicked = mqttCertPicked
                    }
                    uiBinding!!.edNetworkAddeditConnTimeout.setText(
                        if (networkToUse.mqttConfiguration!!.mqttConnTimeout != null)
                            networkToUse.mqttConfiguration!!.mqttConnTimeout!!.toString()
                        else
                            ""
                    )
                    uiBinding!!.edNetworkAddeditKeepaliveInterval.setText(
                        if (networkToUse.mqttConfiguration!!.mqttKeepaliveInterval != null)
                            networkToUse.mqttConfiguration!!.mqttKeepaliveInterval!!.toString()
                        else
                            ""
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(Exception::class)
    fun configureNetworkObjectWithFoldData(
        networkToConfigure: Network,
        creatingNewNetwork: Boolean
    ) {
        val name = uiBinding!!.edNetworkAddeditName.text.toString().trim { it <= ' ' }
        networkToConfigure.name = name
        if (creatingNewNetwork) {
            networkToConfigure.networkType =
                Enumerators.NetworkType.valueOf(uiBinding!!.spNetworkAddeditType.selectedItemPosition)
        }
        networkToConfigure.imageUri = if (imagePicked != null) imagePicked else null
        when (networkToConfigure.networkType) {
            Enumerators.NetworkType.HTTP -> {
                val httpParams = Network.HttpNetworkParameters()
                val baseUrl =
                    uiBinding!!.edNetworkAddeditHttpBaseurl.text.toString().trim { it <= ' ' }
                httpParams.httpBaseUrl = baseUrl
                httpParams.httpAauthenticationType =
                    Enumerators.HttpAuthenticationType.valueOf(uiBinding!!.spNetworkAddeditHttpAuth.selectedItemPosition)
                if (httpParams.httpAauthenticationType == Enumerators.HttpAuthenticationType.BASIC) {
                    val httpUsername = uiBinding!!.edNetworkAddeditHttpAuthUsername.text.toString()
                        .trim { it <= ' ' }
                    httpParams.httpUsername = httpUsername
                    val httpPassword = uiBinding!!.edNetworkAddeditHttpAuthPassword.text.toString()
                        .trim { it <= ' ' }
                    httpParams.httpPassword = httpPassword
                } else {
                    httpParams.httpUsername = null
                    httpParams.httpPassword = null
                }
                httpParams.httpUseSsl = uiBinding!!.cbNetworkAddeditHttpUseSsl.isChecked
                if (httpParams.httpUseSsl!!) {
                    httpParams.certAuthorityUri =
                        if (httpCertPicked != null) httpCertPicked else null
                } else {
                    httpParams.certAuthorityUri = null
                }
                networkToConfigure.httpConfiguration = httpParams
                networkToConfigure.mqttConfiguration = null
            }
            Enumerators.NetworkType.MQTT -> {
                val mqttParams = Network.MqttNetworkParameters()
                val mqttBrokerUrl =
                    uiBinding!!.edNetworkAddeditMqttBrokerurl.text.toString().trim { it <= ' ' }
                mqttParams.mqttBrokerUrl = mqttBrokerUrl
                mqttParams.mqttClientId =
                    uiBinding!!.edNetworkAddeditMqttClientid.text.toString().trim { it <= ' ' }
                mqttParams.mqttUsername =
                    uiBinding!!.edNetworkAddeditMqttUsername.text.toString().trim { it <= ' ' }
                mqttParams.mqttPassword =
                    uiBinding!!.edNetworkAddeditMqttPassword.text.toString().trim { it <= ' ' }
                mqttParams.mqttCleanSession = uiBinding!!.cbNetworkAddeditMqttCleanSession.isChecked
                mqttParams.mqttUseSsl = uiBinding!!.cbNetworkAddeditMqttUseSsl.isChecked
                if (mqttParams.mqttUseSsl!!) {
                    mqttParams.mqttCertAuthorityUri =
                        if (mqttCertPicked != null) mqttCertPicked else null
                } else {
                    mqttParams.mqttCertAuthorityUri = null
                }
                try {
                    mqttParams.mqttConnTimeout =
                        Integer.valueOf(uiBinding!!.edNetworkAddeditConnTimeout.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    mqttParams.mqttConnTimeout = null
                }

                try {
                    mqttParams.mqttKeepaliveInterval =
                        Integer.valueOf(uiBinding!!.edNetworkAddeditKeepaliveInterval.text.toString().trim { it <= ' ' })
                } catch (e: Exception) {
                    mqttParams.mqttKeepaliveInterval = null
                }

                networkToConfigure.mqttConfiguration = mqttParams
                networkToConfigure.httpConfiguration = null
            }
        }
    }

    fun checkFoldData(): Boolean {
        try {
            val name = uiBinding!!.edNetworkAddeditName.text.toString().trim { it <= ' ' }
            if (name.isEmpty()) {
                ToastHelper.showToast(getString(R.string.edit_toast_check_name))
                return false
            }
            when (Enumerators.NetworkType.valueOf(uiBinding!!.spNetworkAddeditType.selectedItemPosition)) {
                Enumerators.NetworkType.HTTP -> {
                    val baseUrl =
                        uiBinding!!.edNetworkAddeditHttpBaseurl.text.toString().trim { it <= ' ' }
                    if (baseUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_http_baseurl))
                        return false
                    }
                    if (uiBinding!!.spNetworkAddeditHttpAuth.selectedItemPosition == Enumerators.HttpAuthenticationType.BASIC.ordinal) {
                        val httpUsername =
                            uiBinding!!.edNetworkAddeditHttpAuthUsername.text.toString()
                                .trim { it <= ' ' }
                        if (httpUsername.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_username))
                            return false
                        }
                        val httpPassword =
                            uiBinding!!.edNetworkAddeditHttpAuthPassword.text.toString()
                                .trim { it <= ' ' }
                        if (httpPassword.isEmpty()) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_http_password))
                            return false
                        }
                    }
                }
                Enumerators.NetworkType.MQTT -> {
                    val mqttBrokerUrl =
                        uiBinding!!.edNetworkAddeditMqttBrokerurl.text.toString().trim { it <= ' ' }
                    if (mqttBrokerUrl.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_brokerurl))
                        return false
                    }
                    val mqttClientId =
                        uiBinding!!.edNetworkAddeditMqttClientid.text.toString().trim { it <= ' ' }
                    if (mqttClientId.isEmpty()) {
                        ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_clientid))
                        return false
                    }
                    if (uiBinding!!.cbNetworkAddeditMqttUseSsl.isChecked) {
                        if (mqttCertPicked == null) {
                            ToastHelper.showToast(getString(R.string.edit_toast_check_mqtt_certificate))
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
            uiBinding!!.networkAddeditEditareaLayout.visibility = View.GONE
            uiBinding!!.networkAddeditLoadingLayout.loadingLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the edition area.
     */
    private fun showEditArea() {
        if (uiBinding != null) {
            uiBinding!!.networkAddeditEditareaLayout.visibility = View.VISIBLE
            uiBinding!!.networkAddeditLoadingLayout.loadingLayout.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_IMAGE) {
                    imagePicked = Utils.getUriFromFilSelected(this, data!!.data!!)
                    uiBinding!!.imagePicked = imagePicked
                } else if (requestCode == REQUEST_PICK_HTTP_CERT) {
                    httpCertPicked = Utils.getUriFromFilSelected(this, data!!.data!!)
                    uiBinding!!.httpCertPicked = httpCertPicked
                } else if (requestCode == REQUEST_PICK_MQTT_CERT) {
                    mqttCertPicked = Utils.getUriFromFilSelected(this, data!!.data!!)
                    /*List<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                    if ((files != null) && (files.size() > 0)) {
                        mqttCertPicked = files.get(0).getPath();
                    }*/
                    uiBinding!!.mqttCertPicked = mqttCertPicked
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

    override fun pickHttpCert() {
        // TODO The use of CA certificates for HTTPS connection hasn't be tested yet
        // because of that, and as okhttp don't require it for establishing SSL connection,
        // the functionality code has been commented for now.
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.setType("*/*");
        //startActivityForResult(Intent.createChooser(intent, getString(R.string.edit_hint_http_cert_pick)), REQUEST_PICK_HTTP_CERT);*/
    }

    override fun cancelHttpCert() {
        // TODO The use of CA certificates for HTTPS connection hasn't be tested yet
        // because of that, and as okhttp don't require it for establishing SSL connection,
        // the functionality code has been commented for now.
        /*httpCertPicked = null;
        uiBinding.setHttpCertPicked(httpCertPicked);*/
    }

    override fun pickMqttCert() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, REQUEST_PICK_MQTT_CERT)
    }

    override fun cancelMqttCert() {
        mqttCertPicked = null
        uiBinding!!.mqttCertPicked = mqttCertPicked
    }

    override fun networkTypeSelected(ordinal: Int) {
        uiBinding!!.networkTypeSelected = ordinal
    }

    override fun httpAuthTypeSelected(ordinal: Int) {
        uiBinding!!.httpAuthTypeSelected = ordinal
    }

    override fun httpUseSslChanged(checked: Boolean) {
        uiBinding!!.useSslInHttp = checked
    }

    override fun mqttUseSslChanged(checked: Boolean) {
        uiBinding!!.useSslInMqtt = checked
    }

    companion object {

        val NETWORK_ID = "network_id"

        val NEW_NETWORK_ID = -1000

        val REQUEST_PICK_IMAGE = 41
        val REQUEST_PICK_HTTP_CERT = 42
        val REQUEST_PICK_MQTT_CERT = 43
    }

}
