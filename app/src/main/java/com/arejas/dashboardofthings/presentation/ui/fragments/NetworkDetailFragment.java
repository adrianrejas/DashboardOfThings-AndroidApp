package com.arejas.dashboardofthings.presentation.ui.fragments;

import android.app.Activity;
import android.os.Bundle;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment representing a single Network detail screen.
 * This fragment is either contained in a {@link NetworkListActivity}
 * in two-pane mode (on tablets) or a {@link NetworkDetailActivity}
 * on handsets.
 */
public class NetworkDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String NETWORK_ID = "network_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private NetworkExtended mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NetworkDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(NETWORK_ID)) {
            //TODO
            getArguments().getInt(NETWORK_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_network_detail, container, false);
        return rootView;
    }
}
