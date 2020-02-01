package com.arejas.dashboardofthings.presentation.ui.notifications

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle

import androidx.fragment.app.DialogFragment

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel
import com.arejas.dashboardofthings.utils.functional.NoArgsFunction

class RemoveSensorDialogFragment : DialogFragment {

    internal var sensor: Sensor? = null

    internal var sensorListViewModel: SensorListViewModel? = null

    internal var sensorDetailsViewModel: SensorDetailsViewModel? = null

    internal var callIfAccept: NoArgsFunction? = null

    constructor(sensor: Sensor, sensorListViewModel: SensorListViewModel) {
        this.sensor = sensor
        this.sensorListViewModel = sensorListViewModel
        this.callIfAccept = null
    }

    constructor(
        sensor: Sensor, sensorDetailsViewModel: SensorDetailsViewModel,
        callIfAccept: NoArgsFunction
    ) {
        this.sensor = sensor
        this.sensorDetailsViewModel = sensorDetailsViewModel
        this.callIfAccept = callIfAccept
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.dialog_remove_sensor)
            .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                if (sensor != null) {
                    if (sensorListViewModel != null)
                        sensorListViewModel!!.removeSensor(sensor)
                    else if (sensorDetailsViewModel != null)
                        sensorDetailsViewModel!!.removeSensor(sensor)
                    dismiss()
                }
                if (callIfAccept != null) {
                    callIfAccept!!.call()
                }
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, which -> dismiss() }
        // Create the AlertDialog object and return it
        return builder.create()
    }
}
