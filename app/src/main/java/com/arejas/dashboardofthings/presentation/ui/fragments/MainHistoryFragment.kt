package com.arejas.dashboardofthings.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.CardMaindashboardHistoryBinding
import com.arejas.dashboardofthings.databinding.FragmentMainhistoryBinding
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper
import com.arejas.dashboardofthings.utils.Enumerators

import java.util.ArrayList
import java.util.HashMap
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

class MainHistoryFragment : Fragment() {

    internal var uiBinding: FragmentMainhistoryBinding? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var mainHistoryViewModel: MainDashboardViewModel? = null

    private var currentListShown: LiveData<Resource<List<SensorExtended>>>? = null
    private var glm_grid: GridLayoutManager? = null
    private var mAdapter: MainHistoryFragment.HistoryListAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        // Get the main dashboard activity view model and observe the changes in the details
        mainHistoryViewModel = ViewModelProviders.of(
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
        uiBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_mainhistory, container, false)
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
        currentListShown = mainHistoryViewModel!!.getListOfSensorsMainDashboard(refreshData)
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
                        var dataToSet: MutableList<SensorExtended>? = mAdapter!!.data
                        if (dataToSet != null) {
                            dataToSet.clear()
                        } else {
                            dataToSet = ArrayList()
                        }
                        for (sensor in listResource!!.data!!) {
                            if (sensor.dataType != Enumerators.DataType.STRING) {
                                dataToSet.add(sensor)
                            }
                        }
                        updateList(dataToSet)
                        uiBinding!!.srlRefreshLayout.isRefreshing = false
                        showList()
                    }
                }
            })
        }
    }

    private fun updateList(newList: List<SensorExtended>?) {
        if (newList != null && !newList.isEmpty()) {
            showList()
            mAdapter!!.data = newList
            mAdapter!!.notifyDataSetChanged()
        } else {
            showNoElements()
        }
    }

    private fun configureListAdapter() {

        /* Get number of items in a row*/
        val iElementsPerRow = resources.getInteger(R.integer.list_maindash_history_column_count)

        // Configure recycler view with a grid layout
        glm_grid = GridLayoutManager(context, iElementsPerRow)
        uiBinding!!.historyMainListListLayout.mainList.layoutManager = glm_grid

        // Configure adapter for recycler view
        mAdapter = MainHistoryFragment.HistoryListAdapter()
        uiBinding!!.historyMainListListLayout.mainList.adapter = mAdapter
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.historyMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.historyMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.historyMainListErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.historyMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
            uiBinding!!.historyMainListErrorLayout.tvError.text = getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.historyMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.historyMainListLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.historyMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.historyMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.historyMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.historyMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.historyMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.historyMainListNoElementsLayout.noElementsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the fragment info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.historyMainListListLayout.listLayout.visibility = View.VISIBLE
            uiBinding!!.historyMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.historyMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.historyMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    internal inner class HistoryListAdapter :
        RecyclerView.Adapter<MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder>() {

        private var mData: List<SensorExtended>? = null
        private var mSpinnerOptionSelectedPerSensor: MutableMap<Int, Int>? = null

        var data: MutableList<SensorExtended>?
            get() = mData
            set(mData) {
                this.mData = mData
                if (mData != null) {
                    if (mSpinnerOptionSelectedPerSensor != null) {
                        mSpinnerOptionSelectedPerSensor!!.clear()
                    } else {
                        mSpinnerOptionSelectedPerSensor = HashMap()
                    }
                    for (sensor in mData) {
                        mSpinnerOptionSelectedPerSensor!![sensor.id!!] = 0
                    }
                } else {
                    mSpinnerOptionSelectedPerSensor = null
                }
            }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<CardMaindashboardHistoryBinding>(
                inflater, R.layout.card_maindashboard_history,
                parent, false
            )
            return MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder,
            position: Int
        ) {
            if (mData != null && mData!!.size > position) {
                holder.itemView.tag = position
                val sensor = mData!![position]
                if (sensor != null && mSpinnerOptionSelectedPerSensor != null && mSpinnerOptionSelectedPerSensor!!.containsKey(
                        sensor.id
                    )
                ) {
                    if (sensor.dataType == Enumerators.DataType.STRING) {
                        holder.binding!!.lcHistorySpinnerCard.visibility = View.GONE
                        holder.binding.lcHistoryChartCard.visibility = View.GONE
                    } else if (sensor.dataType == Enumerators.DataType.BOOLEAN) {
                        mSpinnerOptionSelectedPerSensor!![sensor.id!!] =
                            HistoryChartHelper.SPINNER_HISTORY_LASTVAL
                        holder.binding!!.spinnerSelected =
                            HistoryChartHelper.SPINNER_HISTORY_LASTVAL
                        holder.binding.lcHistorySpinnerCard.setSelection(HistoryChartHelper.SPINNER_HISTORY_LASTVAL)
                        holder.binding.lcHistorySpinnerCard.visibility = View.GONE
                    } else {
                        holder.binding!!.spinnerSelected =
                            mSpinnerOptionSelectedPerSensor!![sensor.id]
                        holder.binding.lcHistorySpinnerCard.setSelection(
                            mSpinnerOptionSelectedPerSensor!![sensor.id]!!
                        )
                        holder.binding.lcHistorySpinnerCard.visibility = View.VISIBLE
                    }
                    holder.sensor = sensor
                    holder.binding.sensor = sensor
                    holder.requestHistoryData()
                }
            }
        }

        override fun getItemCount(): Int {
            return if (mData != null) {
                mData!!.size
            } else {
                0
            }
        }

        internal inner class HistoryListViewHolder(val binding: CardMaindashboardHistoryBinding?) :
            RecyclerView.ViewHolder(binding.getRoot()), HistorySpinnerChangeListener {

            var sensor: Sensor? = null

            private var dataToObserve: LiveData<Resource<List<DataValue>>>? = null

            init {
                binding.setPresenter(this)
            }

            override fun onSpinnerItemSelected(position: Int) {
                if (sensor != null) {
                    mSpinnerOptionSelectedPerSensor!![sensor!!.id!!] = position
                    requestHistoryData()
                }
            }

            fun requestHistoryData() {
                if (sensor != null) {
                    val position = mSpinnerOptionSelectedPerSensor!![sensor!!.id]!!
                    if (dataToObserve != null)
                        dataToObserve!!.removeObservers(this@MainHistoryFragment)
                    dataToObserve =
                        mainHistoryViewModel!!.getHistoricalValue(sensor!!.id!!, position)
                    dataToObserve!!.observe(this@MainHistoryFragment, { listResource ->
                        if (listResource == null) {
                            showErrorCard()
                        } else {
                            if (listResource!!.getStatus() == Resource.Status.ERROR) {
                                showErrorCard()
                            } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                                showLoadingCard()
                            } else {
                                showDataCard()
                                binding!!.spinnerSelected = position
                                binding.data = listResource!!.data
                            }
                        }
                    })
                }
            }

            private fun showDataCard() {
                if (binding != null) {
                    binding.historyDataLayout.visibility = View.VISIBLE
                    binding.historyErrorLayout.errorLayout.visibility = View.GONE
                    binding.historyLoadingLayout.loadingLayout.visibility = View.GONE
                }
            }

            private fun showErrorCard() {
                if (binding != null) {
                    binding.historyDataLayout.visibility = View.GONE
                    binding.historyErrorLayout.errorLayout.visibility = View.VISIBLE
                    binding.historyLoadingLayout.loadingLayout.visibility = View.GONE
                }
            }

            private fun showLoadingCard() {
                if (uiBinding != null) {
                    binding!!.historyDataLayout.visibility = View.GONE
                    binding.historyErrorLayout.errorLayout.visibility = View.GONE
                    binding.historyLoadingLayout.loadingLayout.visibility = View.VISIBLE
                }
            }

        }

    }

    interface HistorySpinnerChangeListener {

        fun onSpinnerItemSelected(position: Int)

    }

}
