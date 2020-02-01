package com.arejas.dashboardofthings.presentation.ui.converters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView

import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.helpers.DataHelper
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.Utils
import com.arejas.dashboardofthings.utils.functional.ConsumerBoolean
import com.arejas.dashboardofthings.utils.functional.ConsumerInt
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.android.gms.maps.model.Dot

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Random

/*
* Class with static functions used by DataBinding library for setting UI according to parameters passed.
 */
object DataBindingConverters {

    @BindingAdapter("imageUrl", "errorResource", "loadingResource")
    fun loadImage(
        view: ImageView,
        url: String?,
        errorResource: Drawable,
        loadingResource: Drawable
    ) {
        // If not null, get poster image URI and load it with Glide library
        if (url != null) {
            view.visibility = View.VISIBLE
            Glide.with(DotApplication.context)
                .load(url)
                .error(errorResource)
                .placeholder(loadingResource)
                .into(view)
        } else {
            view.visibility = View.GONE
        }
    }

    @BindingAdapter("imageUrl", "errorResource", "loadingResource", "alternativeResource")
    fun loadImage(
        view: ImageView, url: String?, errorResource: Drawable,
        loadingResource: Drawable, alternativeResource: Drawable?
    ) {
        // If not null, get poster image URI and load it with Glide library
        if (url != null) {
            view.visibility = View.VISIBLE
            Glide.with(DotApplication.context)
                .load(url)
                .error(errorResource)
                .placeholder(loadingResource)
                .into(view)
        } else {
            if (alternativeResource != null) {
                view.visibility = View.VISIBLE
                Glide.with(DotApplication.context)
                    .load(alternativeResource)
                    .error(errorResource)
                    .placeholder(loadingResource)
                    .into(view)
            } else {
                view.visibility = View.GONE
            }
        }
    }

    @BindingAdapter("logLevel", "iconWarning", "iconCritical")
    fun loadStatusIcon(
        view: ImageView, logLevel: Enumerators.LogLevel?,
        iconWarning: Drawable?, iconCritical: Drawable?
    ) {
        if (logLevel != null) {
            when (logLevel) {
                Enumerators.LogLevel.NOTIF_WARN -> if (iconWarning != null) {
                    view.visibility = View.VISIBLE
                    Glide.with(DotApplication.context)
                        .load(iconWarning)
                        .into(view)
                } else {
                    view.visibility = View.GONE
                }
                Enumerators.LogLevel.NOTIF_CRITICAL -> if (iconCritical != null) {
                    view.visibility = View.VISIBLE
                    Glide.with(DotApplication.context)
                        .load(iconCritical)
                        .into(view)
                } else {
                    view.visibility = View.GONE
                }
                else -> view.visibility = View.GONE
            }
        } else {
            view.visibility = View.GONE
        }
    }

    @BindingAdapter("logLevel")
    fun loadCardBackgroundAcordingToLogLevel(
        view: ConstraintLayout,
        logLevel: Enumerators.LogLevel?
    ) {
        if (logLevel != null) {
            when (logLevel) {
                Enumerators.LogLevel.INFO -> view.setBackgroundColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.logInfoColor
                    )
                )
                Enumerators.LogLevel.WARN -> view.setBackgroundColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.logWarnColor
                    )
                )
                Enumerators.LogLevel.ERROR -> view.setBackgroundColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.logErrorColor
                    )
                )
                Enumerators.LogLevel.NOTIF_NONE -> view.setBackgroundColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.logNotificationNoneColor
                    )
                )
                Enumerators.LogLevel.NOTIF_WARN -> view.setBackgroundColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.logNotificationWarningColor
                    )
                )
                Enumerators.LogLevel.NOTIF_CRITICAL -> view.setBackgroundColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.logNotificationCriticalColor
                    )
                )
                else -> {
                }
            }
        } else {
            view.visibility = View.GONE
        }
    }

    @BindingAdapter("date", "dateFormatToday", "dateFormatAnotherday")
    fun loadDateText(
        view: TextView,
        date: Date?,
        dateFormatToday: String,
        dateFormatAnotherday: String
    ) {
        // If not null, set release date, with the format specified at the strings XML.
        try {
            if (date != null) {
                var dateString = DotApplication.context.getString(R.string.no_data)
                val cal1 = Calendar.getInstance()
                val cal2 = Calendar.getInstance()
                cal1.time = date
                cal2.time = Date()
                if (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(
                        Calendar.YEAR
                    ) == cal2.get(Calendar.YEAR)
                ) {
                    dateString = SimpleDateFormat(dateFormatToday).format(date)
                } else {
                    dateString = SimpleDateFormat(dateFormatAnotherday).format(date)
                }
                view.setText(dateString)
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
            }
        } catch (e: Exception) {
            view.setText(DotApplication.context.getString(R.string.no_data))
        }

    }

    @BindingAdapter("elementName", "elementType")
    fun setTitle(view: TextView, name: String?, type: String?) {
        try {
            if (name != null) {
                if (type != null) {
                    view.setText(
                        DotApplication.context.getString(
                            R.string.two_elements_toghether,
                            name,
                            type
                        )
                    )
                } else {
                    view.text = name
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("elementName", "elementProblems")
    fun setElementName(view: TextView, name: String?, elementProblems: Int?) {
        try {
            if (name != null) {
                if (elementProblems != null && elementProblems > 0) {
                    view.setText(
                        DotApplication.context.getString(
                            R.string.two_elements_toghether, name,
                            DotApplication.context.getResources().getQuantityString(
                                R.plurals.error_log_number_short,
                                elementProblems,
                                elementProblems
                            )
                        )
                    )
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.logErrorColorText
                        )
                    )
                } else {
                    view.text = name
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.primaryTextColor
                        )
                    )
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
                view.setTextColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.primaryTextColor
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("elementType", "elementProblems")
    fun setElementType(view: TextView, elementType: String?, elementProblems: Int?) {
        try {
            if (elementType != null) {
                view.text = elementType
                if (elementProblems != null && elementProblems > 0) {
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.logErrorColorText
                        )
                    )
                } else {
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.primaryTextColor
                        )
                    )
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
                view.setTextColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.primaryTextColor
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("networkType", "elementProblems")
    fun setNetworkType(
        view: TextView,
        networkType: Enumerators.NetworkType?,
        elementProblems: Int?
    ) {
        try {
            if (networkType != null) {
                when (networkType) {
                    Enumerators.NetworkType.MQTT -> view.setText(DotApplication.context.getString(R.string.network_type_mqtt))
                    Enumerators.NetworkType.HTTP -> view.setText(DotApplication.context.getString(R.string.network_type_http))
                    else -> view.setText(DotApplication.context.getString(R.string.no_data))
                }
                if (elementProblems != null && elementProblems > 0) {
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.logErrorColorText
                        )
                    )
                } else {
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.primaryTextColor
                        )
                    )
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
                view.setTextColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.primaryTextColor
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("sensorType", "elementProblems")
    fun setSensorType(view: TextView, sensorType: String?, elementProblems: Int?) {
        try {
            if (sensorType != null) {
                view.text = sensorType
                if (elementProblems != null && elementProblems > 0) {
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.logErrorColorText
                        )
                    )
                } else {
                    view.setTextColor(
                        ContextCompat.getColor(
                            DotApplication.context,
                            R.color.primaryTextColor
                        )
                    )
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
                view.setTextColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.primaryTextColor
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("networkType")
    fun setNetworkType(view: TextView, networkType: Enumerators.NetworkType?) {
        try {
            if (networkType != null) {
                when (networkType) {
                    Enumerators.NetworkType.MQTT -> {
                        val dataString = DotApplication.context.getString(
                            R.string.data_fancy_type,
                            DotApplication.context.getString(R.string.network_type_mqtt)
                        )
                        view.text = Utils.fromHtml(dataString)
                    }
                    Enumerators.NetworkType.HTTP -> {
                        val dataString2 = DotApplication.context.getString(
                            R.string.data_fancy_type,
                            DotApplication.context.getString(R.string.network_type_http)
                        )
                        view.setText(dataString2)
                    }
                    else -> view.setText(DotApplication.context.getString(R.string.no_data))
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("networkObject")
    fun setNetworkBaseUrl(view: TextView, networkObject: Network?) {
        try {
            if (networkObject != null) {
                when (networkObject.networkType) {
                    Enumerators.NetworkType.MQTT -> {
                        val dataString = DotApplication.context.getString(
                            R.string.data_fancy_data_base_url,
                            networkObject.mqttConfiguration!!.mqttBrokerUrl
                        )
                        view.text = Utils.fromHtml(dataString)
                    }
                    Enumerators.NetworkType.HTTP -> {
                        val dataString2 = DotApplication.context.getString(
                            R.string.data_fancy_data_base_url,
                            networkObject.httpConfiguration!!.httpBaseUrl
                        )
                        view.setText(dataString2)
                    }
                    else -> view.setText(DotApplication.context.getString(R.string.no_data))
                }
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("sensorValue", "sensorDataType", "sensorUnit")
    fun setSensorValue(
        view: TextView, value: String?, type: Enumerators.DataType,
        unit: String
    ) {
        // If not null, set release date, with the format specified at the strings XML.
        try {
            if (value != null) {
                val dataToPrint = Utils.getStringDataToPrint(value, type, unit)
                view.text = dataToPrint
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data_value))
            }
        } catch (e: Exception) {
            ToastHelper.showToast(DotApplication.context.getString(R.string.toast_actuator_printdata_error))
        }

    }

    @BindingAdapter("dataReceived", "sensorObject", "envoltureText")
    fun setSensorState(
        view: TextView, dataReceived: String,
        sensor: Sensor, envoltureText: String
    ) {
        try {
            val state = DataHelper.getNotificationStatus(
                dataReceived,
                sensor.dataType, sensor.thresholdAboveWarning,
                sensor.thresholdAboveCritical, sensor.thresholdBelowWarning,
                sensor.thresholdBelowCritical, sensor.thresholdEqualsWarning,
                sensor.thresholdEqualsCritical
            )
            if (state != null) {
                var dataText = DotApplication.context.getString(R.string.no_data)
                when (state) {
                    Enumerators.NotificationState.CRITICAL -> {
                        dataText = DotApplication.context.getString(R.string.sensor_state_critical)
                        view.setTextColor(
                            ContextCompat.getColor(
                                DotApplication.context,
                                R.color.logNotificationCriticalColorSolid
                            )
                        )
                    }
                    Enumerators.NotificationState.WARNING -> {
                        dataText = DotApplication.context.getString(R.string.sensor_state_warning)
                        view.setTextColor(
                            ContextCompat.getColor(
                                DotApplication.context,
                                R.color.logNotificationWarningColorSolid
                            )
                        )
                    }
                    Enumerators.NotificationState.NONE -> {
                        dataText = DotApplication.context.getString(R.string.sensor_state_normal)
                        view.setTextColor(
                            ContextCompat.getColor(
                                DotApplication.context,
                                R.color.primaryTextColor
                            )
                        )
                    }
                }
                view.text = Utils.fromHtml(String.format(envoltureText, dataText))
            }
        } catch (e: Exception) {
            view.setText(DotApplication.context.getString(R.string.no_data))
            e.printStackTrace()
        }

    }

    @BindingAdapter("sensorInfo", "data", "spinnerHistorySelected")
    fun setSensorValue(
        chart: LineChart, sensorInfo: Sensor?, data: List<DataValue>?,
        spinnerHistorySelected: Int?
    ) {
        var spinnerHistorySelected = spinnerHistorySelected
        var dateMin: Date? = null
        var dateMax: Date? = null
        try {
            if (sensorInfo != null && data != null && spinnerHistorySelected != null) {
                if (spinnerHistorySelected == null) spinnerHistorySelected = 0
                chart.setTouchEnabled(false)
                chart.setPinchZoom(false)
                // First, work out the X time Axis
                val xAxis = chart.xAxis
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.textSize = 10f
                xAxis.setDrawAxisLine(true)
                xAxis.setDrawGridLines(true)
                xAxis.labelRotationAngle = 45f
                if (data.size > 0) {
                    for (value in data) {
                        if (dateMin == null)
                            dateMin = value.dateReceived
                        else if (dateMin.time > value.dateReceived!!.time) {
                            dateMin = value.dateReceived
                        }
                        if (dateMax == null)
                            dateMax = value.dateReceived
                        else if (dateMax.time < value.dateReceived!!.time) {
                            dateMax = value.dateReceived
                        }
                    }
                    xAxis.valueFormatter = HistoryChartHelper.getValueFormatterForTimeAxis(
                        spinnerHistorySelected,
                        dateMin, dateMax
                    )
                } else {
                    dateMax = Date()
                    dateMin = dateMax
                    xAxis.valueFormatter = HistoryChartHelper.getValueFormatterForTimeAxis(
                        spinnerHistorySelected,
                        dateMin, dateMax
                    )
                }
                // Second, work out the Y data Axis
                val yAxis = chart.axisLeft
                yAxis.setDrawLabels(true)
                yAxis.setDrawAxisLine(true)
                yAxis.setDrawGridLines(true)
                yAxis.setDrawZeroLine(true)
                yAxis.valueFormatter =
                    HistoryChartHelper.getValueFormatterForDataAxis(sensorInfo.dataType!!)
                chart.axisRight.isEnabled = false
                // Third, set the data entry set
                val entries = ArrayList<Entry>()
                for (value in data) {
                    if (sensorInfo.dataType == Enumerators.DataType.BOOLEAN) {
                        entries.add(
                            Entry(
                                (value.dateReceived!!.time - dateMin!!.time).toFloat(),
                                if (value.value!!.equals("true", ignoreCase = true)) 1.0f else 0.0f
                            )
                        )
                    } else {
                        entries.add(
                            Entry(
                                (value.dateReceived!!.time - dateMin!!.time).toFloat(),
                                java.lang.Float.parseFloat(value.value!!)
                            )
                        )
                    }
                }
                Collections.sort(entries, EntryXComparator())
                // Fourth, set the data entry set and style it
                val dataSet = LineDataSet(
                    entries,
                    DotApplication.context.getResources().getStringArray(R.array.history_options_aray)[spinnerHistorySelected]
                ) // add entries to dataset

                dataSet.setDrawIcons(false)
                dataSet.color = ContextCompat.getColor(DotApplication.context, R.color.primaryColor)
                dataSet.setCircleColor(
                    ContextCompat.getColor(
                        DotApplication.context,
                        R.color.primaryDarkColor
                    )
                )
                dataSet.lineWidth = 1f
                dataSet.circleRadius = 3f
                dataSet.setDrawCircleHole(false)
                dataSet.valueTextSize = 9f
                dataSet.setDrawFilled(false)
                dataSet.setDrawValues(false)
                dataSet.valueFormatter =
                    HistoryChartHelper.getValueFormatterForDataAxis(sensorInfo.dataType!!)
                // Firth, generate the data and refresh the chart
                val lineData = LineData(dataSet)
                chart.data = lineData
                chart.description = null
                chart.legend.isEnabled = false
                chart.invalidate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @BindingAdapter("sensorList")
    fun loadListOfSensors(view: TextView, dataList: List<Sensor>?) {
        // If not null, compose the list and set it.
        if (dataList != null && dataList.size != 0) {
            val elementNames = ArrayList<String>()
            for (dataUnit in dataList) {
                elementNames.add(dataUnit.name)
            }
            val dataString =
                DotApplication.context.getString(R.string.network_list_sensors) + TextUtils.join(
                    ", ",
                    elementNames
                )
            view.text = Utils.fromHtml(dataString)
        } else {
            view.text =
                Utils.fromHtml(DotApplication.context.getString(R.string.network_no_sensors))
        }
    }

    @BindingAdapter("dataType", "envoltureText")
    fun loadFancyDataType(view: TextView, dataType: Enumerators.DataType?, envoltureText: String?) {
        try {
            val typesArray = DotApplication.context.getResources()
                .getStringArray(R.array.edit_array_element_data_type)
            if (dataType != null && envoltureText != null) {
                view.text =
                    Utils.fromHtml(String.format(envoltureText, typesArray[dataType.ordinal]))
            } else {
                view.setText(DotApplication.context.getString(R.string.no_data))
            }
        } catch (e: Exception) {
            view.text = Utils.fromHtml(DotApplication.context.getString(R.string.no_data))
        }

    }

    @BindingAdapter("dataText", "envoltureText")
    fun loadFancyText(view: TextView, dataText: String?, envoltureText: String?) {
        if (dataText != null && envoltureText != null) {
            view.text = Utils.fromHtml(String.format(envoltureText, dataText))
        } else {
            view.setText(DotApplication.context.getString(R.string.no_data))
        }
    }

    @BindingAdapter("fancyText")
    fun loadAsHtml(view: TextView, fancyText: String?) {
        if (fancyText != null) {
            view.text = Utils.fromHtml(fancyText)
        } else {
            view.setText(DotApplication.context.getString(R.string.no_data))
        }
    }

    @BindingAdapter("actuatorList")
    fun loadListOfActuators(view: TextView, dataList: List<Actuator>?) {
        // If not null, compose the list and set it.
        if (dataList != null && dataList.size != 0) {
            val elementNames = ArrayList<String>()
            for (dataUnit in dataList) {
                elementNames.add(dataUnit.name)
            }
            val dataString =
                DotApplication.context.getString(R.string.network_list_actuators) + TextUtils.join(
                    ", ",
                    elementNames
                )
            view.text = Utils.fromHtml(dataString)
        } else {
            view.text =
                Utils.fromHtml(DotApplication.context.getString(R.string.network_no_actuators))
        }
    }

    @BindingAdapter("entryList", "selectedPosition")
    fun setNetworkTypeSpinnerValue(
        spinner: Spinner, entryList: List<NetworkExtended>?,
        selectedPosition: Int?
    ) {
        if (entryList != null) {
            val networkNames = ArrayList<CharSequence>()
            for (network in entryList) {
                networkNames.add(network.name)
            }
            val adapter = ArrayAdapter(
                DotApplication.context,
                android.R.layout.simple_spinner_dropdown_item, networkNames
            )
            spinner.adapter = adapter
            if (selectedPosition != null) {
                spinner.setSelection(selectedPosition)
            }
        }
    }

    @BindingAdapter("selectionListener")
    fun setSpinnerSelectionListener(spinner: Spinner, selectionListener: ConsumerInt?) {
        if (selectionListener != null) {
            spinner.isFocusable = true
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    selectionListener.accept(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectionListener.accept(null)
                }
            }
        }
    }

    @BindingAdapter("checkingListener")
    fun setCheckboxCheckingListener(checkBox: CheckBox, checkingListener: ConsumerBoolean?) {
        if (checkingListener != null) {
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                checkingListener.accept(
                    isChecked
                )
            }
        }
    }

    @BindingAdapter("imageUrl", "pickResource", "errorResource", "loadingResource")
    fun loadImageToBePicked(
        view: ImageButton, url: String?, pickResource: Drawable?,
        errorResource: Drawable, loadingResource: Drawable
    ) {
        if (url != null) {
            view.visibility = View.VISIBLE
            Glide.with(DotApplication.context)
                .load(url)
                .error(errorResource)
                .placeholder(loadingResource)
                .into(view)
        } else if (pickResource != null) {
            view.visibility = View.VISIBLE
            Glide.with(DotApplication.context)
                .load(pickResource)
                .error(errorResource)
                .placeholder(loadingResource)
                .into(view)
        } else {
            view.visibility = View.GONE
        }
    }

}
