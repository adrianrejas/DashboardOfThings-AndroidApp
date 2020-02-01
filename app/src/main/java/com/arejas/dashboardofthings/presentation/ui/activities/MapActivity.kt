package com.arejas.dashboardofthings.presentation.ui.activities

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivityMapBinding
import com.arejas.dashboardofthings.databinding.ItemActuatorListBinding
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MapViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.converters.DataBindingConverters
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.utils.Utils
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView

import java.util.ArrayList
import java.util.HashMap

import javax.inject.Inject

import dagger.android.AndroidInjection

class MapActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    private var mapViewModel: MapViewModel? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var menu: Menu? = null

    private var navView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null

    internal var uiBinding: ActivityMapBinding
    private var actuators: List<ActuatorExtended>? = null
    private var sensors: List<SensorExtended>? = null
    private var actuatorMarkers: MutableMap<Int, Marker>? = null
    private var sensorMarkers: Map<Int, Marker>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_map)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.navigation)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        actuatorMarkers = HashMap()
        sensorMarkers = HashMap()

        /* Get view model*/
        mapViewModel =
            ViewModelProviders.of(this, this.viewModelFactory).get(MapViewModel::class.java)

        drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        navView = findViewById<View>(R.id.navigation_view) as NavigationView
        navView!!.setNavigationItemSelectedListener { menuItem ->
            val fragmentTransaction = false
            val fragment: Fragment? = null

            var intent: Intent? = null
            when (menuItem.itemId) {
                R.id.main_navigation_dashboard -> {
                    intent = Intent(
                        applicationContext,
                        MainDashboardActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_sensors -> {
                    intent = Intent(
                        applicationContext,
                        SensorListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_actuators -> {
                    intent = Intent(
                        applicationContext,
                        ActuatorListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_networks -> {
                    intent = Intent(
                        applicationContext,
                        NetworkListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_map -> {
                }
            }
            drawerLayout!!.closeDrawers()
            true
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_mainactivites, menu)
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
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.menu_options -> {
                startActivity(
                    Intent(
                        applicationContext,
                        SettingsActivity::class.java
                    )
                )
                return true
            }
            R.id.menu_shutdown_app -> {
                Utils.stopControlService(this)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.size == 1 &&
                permissions[0] === Manifest.permission.ACCESS_FINE_LOCATION &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap!!.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_LOCATION_REQUEST_CODE
            )
        }
        mMap!!.setOnMyLocationButtonClickListener(this)
        mMap!!.setOnMyLocationClickListener(this)
        mMap!!.setOnInfoWindowClickListener(this)

        mapViewModel!!.getListOfActuatorsLocated(true)!!.observe(this, { listResource ->
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_map_actuators_failed))
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_map_actuators_failed))
                    } else if (listResource!!.getStatus() == Resource.Status.SUCCESS) {
                        actuators = listResource!!.data
                        populateMapWithActuatorMarkers(actuators!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastHelper.showToast(getString(R.string.toast_map_actuators_failed))
            }
        })

        mapViewModel!!.getListOfSensorsLocated(true)!!.observe(this, { listResource ->
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_map_sensors_failed))
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_map_sensors_failed))
                    } else if (listResource!!.getStatus() == Resource.Status.SUCCESS) {
                        sensors = listResource!!.data
                        populateMapWithSensorMarkers(sensors!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastHelper.showToast(getString(R.string.toast_map_sensors_failed))
            }
        })

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
    }

    private fun populateMapWithSensorMarkers(sensors: List<SensorExtended>) {
        // Populate the map with a tag for each sensor, or update existing marker if existed
        val idsList = ArrayList<Int>()
        for (sensor in sensors) {
            if (sensor.locationLat != null && sensor.locationLong != null) {
                val elementMarker: Marker?
                idsList.add(sensor.id)
                val elementLocation = LatLng(sensor.locationLat!!, sensor.locationLong!!)
                if (actuatorMarkers!!.containsKey(sensor.id)) {
                    elementMarker = actuatorMarkers!![sensor.id]
                    elementMarker!!.position = elementLocation
                } else {
                    elementMarker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(elementLocation)
                            .title(getString(R.string.sensor))
                            .icon(Utils.vectorToBitmap(R.drawable.marker))
                    )
                }
                elementMarker!!.tag = sensor
                elementMarker.snippet = getString(
                    R.string.map_element_spinner, sensor.name,
                    sensor.type
                )
            }
        }
        for (markerId in actuatorMarkers!!.keys) {
            if (!idsList.contains(markerId)) {
                actuatorMarkers!![markerId]!!.remove()
                actuatorMarkers!!.remove(markerId)
            }
        }
    }

    private fun populateMapWithActuatorMarkers(actuators: List<ActuatorExtended>) {
        // Populate the map with a tag for each actuator, or update existing marker if existed
        val idsList = ArrayList<Int>()
        for (actuator in actuators) {
            if (actuator.locationLat != null && actuator.locationLong != null) {
                val elementMarker: Marker?
                idsList.add(actuator.id)
                val elementLocation = LatLng(actuator.locationLat!!, actuator.locationLong!!)
                if (actuatorMarkers!!.containsKey(actuator.id)) {
                    elementMarker = actuatorMarkers!![actuator.id]
                    elementMarker!!.position = elementLocation
                } else {
                    elementMarker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(elementLocation)
                            .title(getString(R.string.actuator))
                            .icon(Utils.vectorToBitmap(R.drawable.marker))
                    )
                }
                elementMarker!!.snippet = getString(
                    R.string.map_element_spinner, actuator.name,
                    actuator.type
                )
                elementMarker.tag = actuator

            }
        }
        for (markerId in actuatorMarkers!!.keys) {
            if (!idsList.contains(markerId)) {
                actuatorMarkers!![markerId]!!.remove()
                actuatorMarkers!!.remove(markerId)
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(location: Location) {

    }

    override fun onInfoWindowClick(marker: Marker) {
        val bTwoPanelMode = resources.getBoolean(R.bool.twoPanelMode)
        if (marker.tag != null) {
            if (marker.tag is ActuatorExtended) {
                val actuator = marker.tag as ActuatorExtended
                if (bTwoPanelMode) {
                    val intent = Intent(this, ActuatorListActivity::class.java)
                    intent.putExtra(ActuatorDetailsFragment.ACTUATOR_ID, actuator.id)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, ActuatorDetailsActivity::class.java)
                    intent.putExtra(ActuatorDetailsFragment.ACTUATOR_ID, actuator.id)
                    startActivity(intent)
                }
            } else if (marker.tag is SensorExtended) {
                val sensor = marker.tag as SensorExtended
                if (bTwoPanelMode) {
                    val intent = Intent(this, SensorListActivity::class.java)
                    intent.putExtra(SensorDetailsFragment.SENSOR_ID, sensor.id)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, SensorDetailsActivity::class.java)
                    intent.putExtra(SensorDetailsFragment.SENSOR_ID, sensor.id)
                    startActivity(intent)
                }
            }
        }
    }

    companion object {

        private val MY_LOCATION_REQUEST_CODE = 98
    }
}
