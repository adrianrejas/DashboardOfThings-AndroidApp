package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivityMaindashboardBinding;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainActuatorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainHistoryFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainLogsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainSensorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainStatusFragment;
import com.arejas.dashboardofthings.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.navigation.NavigationView;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class MainDashboardActivity extends AppCompatActivity {

    private MainDashboardViewModel mainDashboardViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private Menu menu;

    private NavigationView navView;
    private DrawerLayout drawerLayout;

    ActivityMaindashboardBinding uiBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_maindashboard);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        // Configure toolbar
        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_maindash);

        // Starts control service if not initiated
        Utils.startControlService();

        /* Get view model*/
        mainDashboardViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(MainDashboardViewModel.class);

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
                                intent = new Intent(getApplicationContext(),
                                        MapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });

        // Init the view pager with a fragment adapter for showing the fragments with different info
        // of the movie in a tab system
        MainDashboardFragmentAdapter fragmentAdapter = new MainDashboardFragmentAdapter(getSupportFragmentManager(), this);
        uiBinding.vpTabViewerMaindashboard.setAdapter(fragmentAdapter);
        uiBinding.tlTabsMaindashboard.setupWithViewPager(uiBinding.vpTabViewerMaindashboard);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("ADS", "Anuncios cargados");
            }
        });
        AdView mAdView = (AdView) findViewById(R.id.ad_maindashboard);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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
            case R.id.menu_shutdown_app:
                Utils.stopControlService();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This adapter is used for defining the tab system of the movie activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    static class MainDashboardFragmentAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_ITEMS = 5;

        private final Context mContext;

        MainDashboardFragmentAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mContext = context;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Details
                    return (new MainSensorsFragment());
                case 1: // Cast
                    return (new MainActuatorsFragment());
                case 2: // Crew
                    return (new MainHistoryFragment());
                case 3: // Reviews
                    return (new MainStatusFragment());
                case 4: // Videos
                    return (new MainLogsFragment());
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if (mContext != null) {
                switch (position) {
                    case 0: // Details
                        return mContext.getString(R.string.main_dashboard_tab_sensors);
                    case 1: // Cast
                        return mContext.getString(R.string.main_dashboard_tab_actuators);
                    case 2: // Crew
                        return mContext.getString(R.string.main_dashboard_tab_history);
                    case 3: // Reviews
                        return mContext.getString(R.string.main_dashboard_tab_status);
                    case 4: // Videos
                        return mContext.getString(R.string.main_dashboard_tab_logs);
                    default:
                        return null;
                }
            } else {
                return "";
            }
        }

    }

}
