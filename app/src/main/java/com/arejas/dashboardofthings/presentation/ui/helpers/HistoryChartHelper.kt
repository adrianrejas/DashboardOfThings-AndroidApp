package com.arejas.dashboardofthings.presentation.ui.helpers

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.utils.Enumerators
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

import java.text.SimpleDateFormat
import java.util.Date

object HistoryChartHelper {

    val SPINNER_HISTORY_LASTVAL = 0
    val SPINNER_HISTORY_LASTDAY = 1
    val SPINNER_HISTORY_LASTWEEK = 2
    val SPINNER_HISTORY_LASTMONTH = 3
    val SPINNER_HISTORY_LASTYEAR = 4

    val ONE_HOUR_MILLIS = (3600 * 1000).toLong()

    fun getValueFormatterForTimeAxis(
        spinnerHistorySelection: Int,
        firstDate: Date, lastDate: Date
    ): ValueFormatter {
        return ValueFormatterXAxisDate(spinnerHistorySelection, firstDate, lastDate)
    }

    fun getValueFormatterForDataAxis(dataType: Enumerators.DataType): ValueFormatter {
        when (dataType) {
            Enumerators.DataType.BOOLEAN -> return object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return if (value == 1.0f) {
                        DotApplication.context.getString(R.string.boolean_active)
                    } else {
                        DotApplication.context.getString(R.string.boolean_not_active)
                    }
                }
            }
            Enumerators.DataType.INTEGER -> return DefaultValueFormatter(0)
            else -> return DefaultValueFormatter(1)
        }
    }

    internal class ValueFormatterXAxisDate(
        val spinnerHistoryValue: Int,
        val dateMin: Date,
        val dateMax: Date
    ) : ValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            try {
                val date = Date(java.lang.Float.valueOf(value).toLong() + dateMin.time)
                when (spinnerHistoryValue) {
                    SPINNER_HISTORY_LASTDAY -> return SimpleDateFormat("HH:00").format(date)
                    SPINNER_HISTORY_LASTWEEK -> return SimpleDateFormat("E").format(date)
                    SPINNER_HISTORY_LASTMONTH -> return SimpleDateFormat("dd/MM").format(date)
                    SPINNER_HISTORY_LASTYEAR -> return SimpleDateFormat("MMM yy").format(date)
                    else -> {
                        val difference = Math.abs(dateMax.time - dateMin.time)
                        if (difference < ONE_HOUR_MILLIS) {
                            return SimpleDateFormat("HH:mm:ss").format(date)
                        }
                    }
                }
                return SimpleDateFormat("dd/MM HH:mm:ss").format(date)
            } catch (e: Exception) {
                return "NNONONONO"
            }

        }
    }

}
