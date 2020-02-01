package com.arejas.dashboardofthings.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.CardLogBinding
import com.arejas.dashboardofthings.databinding.FragmentMainlogsBinding
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

class MainLogsFragment : Fragment() {

    internal var uiBinding: FragmentMainlogsBinding? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var mainnetwork_addeditViewModel: MainDashboardViewModel? = null

    private var currentListShown: LiveData<Resource<List<Log>>>? = null
    private var llm_linear: LinearLayoutManager? = null
    private var mAdapter: MainLogsFragment.LogsListAdapter? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        // Get the main dashboard activity view model and observe the changes in the details
        mainnetwork_addeditViewModel = ViewModelProviders.of(
            Objects.requireNonNull<FragmentActivity>(activity),
            viewModelFactory
        ).get(MainDashboardViewModel::class.java)
        setList(true, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mainlogs, container, false)
        // Configure adapter for recycler view
        configureListAdapter()
        // Set action of refreshing list when refreshing gesture detected
        uiBinding!!.srlRefreshLayout.setOnRefreshListener { setList(false, true) }
        return uiBinding!!.root
    }

    /**
     * Function called for setting a new list, requesting it to the Database
     *
     * @param showLoading true if wanted to show the loading layout until info got (we don't want in
     * case of swipe refresh, because it has it's own way to info about the loading process).
     * @param refreshData true if wanted to reload the data, false if it's enough with cached data.
     */
    private fun setList(showLoading: Boolean, refreshData: Boolean) {
        if (showLoading) {
            showLoading()
        }
        if (currentListShown != null) {
            currentListShown!!.removeObservers(this)
            currentListShown = null
        }
        currentListShown = mainnetwork_addeditViewModel!!.getLastConfigurationLogs(refreshData)
        if (currentListShown != null) {
            currentListShown!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        showError()
                        uiBinding!!.srlRefreshLayout.isRefreshing = false
                    } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading()
                    } else {
                        updateList(listResource!!.data)
                        uiBinding!!.srlRefreshLayout.isRefreshing = false
                    }
                }
            })
        }
    }

    private fun updateList(newList: List<Log>?) {
        if (newList != null && !newList.isEmpty()) {
            showList()
            mAdapter!!.data = newList
            mAdapter!!.notifyDataSetChanged()
        } else {
            showNoElements()
        }
    }

    private fun configureListAdapter() {
        // Configure recycler view with a grid layout
        llm_linear = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        uiBinding!!.logsMainListListLayout.mainList.layoutManager = llm_linear

        // Configure adapter for recycler view
        mAdapter = MainLogsFragment.LogsListAdapter()
        uiBinding!!.logsMainListListLayout.mainList.adapter = mAdapter
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.logsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.logsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.logsMainListErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.logsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
            uiBinding!!.logsMainListErrorLayout.tvError.text = getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.logsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.logsMainListLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.logsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.logsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.logsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.logsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.logsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.logsMainListNoElementsLayout.noElementsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the fragment info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.logsMainListListLayout.listLayout.visibility = View.VISIBLE
            uiBinding!!.logsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.logsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.logsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    internal inner class LogsListAdapter :
        RecyclerView.Adapter<MainLogsFragment.LogsListAdapter.LogListViewHolder>() {

        var data: List<Log>? = null

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MainLogsFragment.LogsListAdapter.LogListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<CardLogBinding>(
                inflater, R.layout.card_log,
                parent, false
            )
            return MainLogsFragment.LogsListAdapter.LogListViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: MainLogsFragment.LogsListAdapter.LogListViewHolder,
            position: Int
        ) {
            if (data != null && data!!.size > position) {
                holder.itemView.tag = position
                holder.binding.log = data!![position]
            }
        }

        override fun getItemCount(): Int {
            return if (data != null) {
                data!!.size
            } else {
                0
            }
        }

        internal inner class LogListViewHolder(val binding: CardLogBinding) :
            RecyclerView.ViewHolder(binding.root)

    }


}
