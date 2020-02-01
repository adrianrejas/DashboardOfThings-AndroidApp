package com.arejas.dashboardofthings.presentation.ui.notifications

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle

import androidx.fragment.app.DialogFragment

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorListViewModel
import com.arejas.dashboardofthings.utils.functional.NoArgsFunction

class RemoveActuatorDialogFragment : DialogFragment {

    internal var actuator: Actuator? = null

    internal var actuatorListViewModel: ActuatorListViewModel? = null

    internal var actuatorDetailsViewModel: ActuatorDetailsViewModel? = null

    internal var callIfAccept: NoArgsFunction? = null

    constructor(actuator: Actuator, actuatorListViewModel: ActuatorListViewModel) {
        this.actuator = actuator
        this.actuatorListViewModel = actuatorListViewModel
        this.callIfAccept = null
    }

    constructor(
        actuator: Actuator, actuatorDetailsViewModel: ActuatorDetailsViewModel,
        callIfAccept: NoArgsFunction
    ) {
        this.actuator = actuator
        this.actuatorDetailsViewModel = actuatorDetailsViewModel
        this.callIfAccept = callIfAccept
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.dialog_remove_actuator)
            .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                if (actuator != null) {
                    if (actuatorListViewModel != null)
                        actuatorListViewModel!!.removeActuator(actuator)
                    else if (actuatorDetailsViewModel != null)
                        actuatorDetailsViewModel!!.removeActuator(actuator)
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
