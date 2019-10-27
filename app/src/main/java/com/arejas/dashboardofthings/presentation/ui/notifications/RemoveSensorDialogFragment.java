package com.arejas.dashboardofthings.presentation.ui.notifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel;
import com.arejas.dashboardofthings.utils.functional.NoArgsFunction;

public class RemoveSensorDialogFragment extends DialogFragment {

    Sensor sensor;

    SensorListViewModel sensorListViewModel;

    SensorDetailsViewModel sensorDetailsViewModel;

    NoArgsFunction callIfAccept;

    public RemoveSensorDialogFragment(Sensor sensor, SensorListViewModel sensorListViewModel) {
        this.sensor = sensor;
        this.sensorListViewModel = sensorListViewModel;
        this.callIfAccept = null;
    }

    public RemoveSensorDialogFragment(Sensor sensor, SensorDetailsViewModel sensorDetailsViewModel,
                                      NoArgsFunction callIfAccept) {
        this.sensor = sensor;
        this.sensorDetailsViewModel = sensorDetailsViewModel;
        this.callIfAccept = callIfAccept;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_remove_sensor)
                .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                    if (sensor != null) {
                        if (sensorListViewModel != null)
                            sensorListViewModel.removeSensor(sensor);
                        else if (sensorDetailsViewModel != null)
                            sensorDetailsViewModel.removeSensor(sensor);
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
