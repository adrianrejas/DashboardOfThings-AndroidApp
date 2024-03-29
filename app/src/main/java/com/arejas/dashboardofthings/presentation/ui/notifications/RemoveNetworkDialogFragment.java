package com.arejas.dashboardofthings.presentation.ui.notifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel;
import com.arejas.dashboardofthings.utils.functional.Consumer;
import com.arejas.dashboardofthings.utils.functional.NoArgsFunction;

public class RemoveNetworkDialogFragment extends DialogFragment {

    Network network;

    NetworkListViewModel networkListViewModel;

    NetworkDetailsViewModel networkDetailsViewModel;

    NoArgsFunction callIfAccept;

    public RemoveNetworkDialogFragment(Network network, NetworkListViewModel networkListViewModel) {
        this.network = network;
        this.networkListViewModel = networkListViewModel;
        this.callIfAccept = null;
    }

    public RemoveNetworkDialogFragment(Network network, NetworkDetailsViewModel networkDetailsViewModel,
                                       NoArgsFunction callIfAccept) {
        this.network = network;
        this.networkDetailsViewModel = networkDetailsViewModel;
        this.callIfAccept = callIfAccept;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_remove_network)
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                    if (network != null) {
                        if (networkListViewModel != null)
                            networkListViewModel.removeNetwork(network);
                        else if (networkDetailsViewModel != null)
                            networkDetailsViewModel.removeNetwork(network);
                        dismiss();
                    }
                    if (callIfAccept != null) {
                        callIfAccept.call();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {
                    dismiss();
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
