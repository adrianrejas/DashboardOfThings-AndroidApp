package com.arejas.dashboardofthings.presentation.ui.notifications

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel
import com.arejas.dashboardofthings.utils.functional.Consumer
import com.arejas.dashboardofthings.utils.functional.NoArgsFunction

class RemoveNetworkDialogFragment : DialogFragment {

    internal var network: Network? = null

    internal var networkListViewModel: NetworkListViewModel? = null

    internal var networkDetailsViewModel: NetworkDetailsViewModel? = null

    internal var callIfAccept: NoArgsFunction? = null

    constructor(network: Network, networkListViewModel: NetworkListViewModel) {
        this.network = network
        this.networkListViewModel = networkListViewModel
        this.callIfAccept = null
    }

    constructor(
        network: Network, networkDetailsViewModel: NetworkDetailsViewModel,
        callIfAccept: NoArgsFunction
    ) {
        this.network = network
        this.networkDetailsViewModel = networkDetailsViewModel
        this.callIfAccept = callIfAccept
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.dialog_remove_network)
            .setPositiveButton(R.string.dialog_ok) { dialog, which ->
                if (network != null) {
                    if (networkListViewModel != null)
                        networkListViewModel!!.removeNetwork(network)
                    else if (networkDetailsViewModel != null)
                        networkDetailsViewModel!!.removeNetwork(network)
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
