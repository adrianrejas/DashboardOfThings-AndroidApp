package com.arejas.dashboardofthings.presentation.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivityMapBinding;
import com.arejas.dashboardofthings.databinding.ItemActuatorListBinding;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MapViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.converters.DataBindingConverters;
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Utils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class MapActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback {

    private final static int MY_LOCATION_REQUEST_CODE = 98;

    private GoogleMap mMap;

    private MapViewModel mapViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private Menu menu;

    private NavigationView navView;
    private DrawerLayout drawerLayout;

    ActivityMapBinding uiBinding;
    private List<ActuatorExtended> actuators;
    private List<SensorExtended> sensors;
    private Map<Integer, Marker> actuatorMarkers;
    private Map<Integer, Marker> sensorMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_map);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        actuatorMarkers = new HashMap<>();
        sensorMarkers = new HashMap<>();

        /* Get view model*/
        mapViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(MapViewModel.class);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView)findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        boolean fragmentTransaction = false;
                        Fragment fragment = null;

                        Intent intent = null;
                        switch (menuItem.getItemId()) {
                            case R.id.main_navigation_dashboard:
                                intent = new Intent(getApplicationContext(),
                                        MainDashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_sensors:
                                intent = new Intent(getApplicationContext(),
                                        SensorListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_actuators:
                                intent = new Intent(getApplicationContext(),
                                        ActuatorListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_networks:
                                intent = new Intent(getApplicationContext(),
                                        NetworkListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_map:
                                break;
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivites, menu);
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
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_options:
                startActivity(new Intent(getApplicationContext(),
                        SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnInfoWindowClickListener(this);

        mapViewModel.getListOfActuatorsLocated(true).observe(this, listResource -> {
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_map_actuators_failed));
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_map_actuators_failed));
                    } else if (listResource.getStatus() == Resource.Status.SUCCESS) {
                        actuators = listResource.getData();
                        populateMapWithActuatorMarkers(actuators);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastHelper.showToast(getString(R.string.toast_map_actuators_failed));
            }
        });

        mapViewModel.getListOfSensorsLocated(true).observe(this, listResource -> {
            try {
                if (listResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_map_sensors_failed));
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_map_sensors_failed));
                    } else if (listResource.getStatus() == Resource.Status.SUCCESS) {
                        sensors = listResource.getData();
                        populateMapWithSensorMarkers(sensors);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                ToastHelper.showToast(getString(R.string.toast_map_sensors_failed));
            }
        });

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }

    private void populateMapWithSensorMarkers(List<SensorExtended> sensors) {
        // Populate the map with a tag for each sensor, or update existing marker if existed
        List<Integer> idsList = new ArrayList<>();
        for (SensorExtended sensor : sensors) {
            if ((sensor.getLocationLat() != null) && (sensor.getLocationLong() != null)) {
                Marker elementMarker;
                idsList.add(sensor.getId());
                LatLng elementLocation = new LatLng(sensor.getLocationLat(), sensor.getLocationLong());
                if (actuatorMarkers.containsKey(sensor.getId())) {
                    elementMarker = actuatorMarkers.get(sensor.getId());
                    elementMarker.setPosition(elementLocation);
                } else {
                    elementMarker = mMap.addMarker(new MarkerOptions()
                            .position(elementLocation)
                            .title(getString(R.string.sensor))
                            .icon(Utils.vectorToBitmap(R.drawable.marker)));
                }
                elementMarker.setTag(sensor);
                elementMarker.setSnippet(getString(R.string.map_element_spinner, sensor.getName(),
                        sensor.getType()));
            }
        }
        for (Integer markerId : actuatorMarkers.keySet()) {
            if (!idsList.contains(markerId)) {
                actuatorMarkers.get(markerId).remove();
                actuatorMarkers.remove(markerId);
            }
        }
    }

    private void populateMapWithActuatorMarkers(List<ActuatorExtended> actuators) {
        // Populate the map with a tag for each actuator, or update existing marker if existed
        List<Integer> idsList = new ArrayList<>();
        for (ActuatorExtended actuator : actuators) {
            if ((actuator.getLocationLat() != null) && (actuator.getLocationLong() != null)) {
                Marker elementMarker;
                idsList.add(actuator.getId());
                LatLng elementLocation = new LatLng(actuator.getLocationLat(), actuator.getLocationLong());
                if (actuatorMarkers.containsKey(actuator.getId())) {
                    elementMarker = actuatorMarkers.get(actuator.getId());
                    elementMarker.setPosition(elementLocation);
                } else {
                    elementMarker = mMap.addMarker(new MarkerOptions()
                            .position(elementLocation)
                            .title(getString(R.string.actuator))
                            .icon(Utils.vectorToBitmap( R.drawable.marker)));
                }
                elementMarker.setSnippet(getString(R.string.map_element_spinner, actuator.getName(),
                        actuator.getType()));
                elementMarker.setTag(actuator);

            }
        }
        for (Integer markerId : actuatorMarkers.keySet()) {
            if (!idsList.contains(markerId)) {
                actuatorMarkers.get(markerId).remove();
                actuatorMarkers.remove(markerId);
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        boolean bTwoPanelMode = getResources().getBoolean(R.bool.twoPanelMode);
        if (marker.getTag() != null) {
            if (marker.getTag() instanceof ActuatorExtended) {
                ActuatorExtended actuator = (ActuatorExtended) (marker.getTag());
                if (bTwoPanelMode) {
                    Intent intent = new Intent(this, ActuatorListActivity.class);
                    intent.putExtra(ActuatorDetailsFragment.ACTUATOR_ID, actuator.getId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, ActuatorDetailsActivity.class);
                    intent.putExtra(ActuatorDetailsFragment.ACTUATOR_ID, actuator.getId());
                    startActivity(intent);
                }
            } else if (marker.getTag() instanceof SensorExtended) {
                SensorExtended sensor = (SensorExtended)(marker.getTag());
                if (bTwoPanelMode) {
                    Intent intent = new Intent(this, SensorListActivity.class);
                    intent.putExtra(SensorDetailsFragment.SENSOR_ID, sensor.getId());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, SensorDetailsActivity.class);
                    intent.putExtra(SensorDetailsFragment.SENSOR_ID, sensor.getId());
                    startActivity(intent);
                }
            }
        }
    }
}
