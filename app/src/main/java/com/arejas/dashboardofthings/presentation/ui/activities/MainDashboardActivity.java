package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainActuatorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainHistoryFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainLogsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainSensorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainStatusFragment;

public class MainDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maindashboard);
    }

    /**
     * This adapter is used for defining the tab system of the movie activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    static class MainDashboardFragmentAdapter extends FragmentPagerAdapter {

        private static final int NUM_ITEMS = 5;

        private final Context mContext;

        MainDashboardFragmentAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
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
                        return mContext.getString(R.string.main_dashboard_tab_history);
                    default:
                        return null;
                }
            } else {
                return "";
            }
        }

    }
}
