package com.arejas.dashboardofthings.presentation.ui.converters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.helpers.DataHelper;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper;
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.maps.model.Dot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/*
* Class with static functions used by DataBinding library for setting UI according to parameters passed.
 */
public class DataBindingConverters {

    @BindingAdapter({"imageUrl", "errorResource", "loadingResource"})
    public static void loadImage(ImageView view, String url, Drawable errorResource, Drawable loadingResource) {
        // If not null, get poster image URI and load it with Glide library
        if (url != null) {
            view.setVisibility(View.VISIBLE);
            Glide.with(DotApplication.getContext())
                    .load(url)
                    .error(errorResource)
                    .placeholder(loadingResource)
                    .into(view);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"logLevel", "iconWarning", "iconCritical"})
    public static void loadStatusIcon(ImageView view, Enumerators.LogLevel logLevel,
                                       Drawable iconWarning, Drawable iconCritical) {
        if (logLevel != null) {
            switch (logLevel) {
                case NOTIF_WARN:
                    if (iconWarning != null) {
                        view.setVisibility(View.VISIBLE);
                        Glide.with(DotApplication.getContext())
                                .load(iconWarning)
                                .into(view);
                    } else {
                        view.setVisibility(View.GONE);
                    }
                    break;
                case NOTIF_CRITICAL:
                    if (iconCritical != null) {
                        view.setVisibility(View.VISIBLE);
                        Glide.with(DotApplication.getContext())
                                .load(iconCritical)
                                .into(view);
                    } else {
                        view.setVisibility(View.GONE);
                    }
                    break;
                default:
                    view.setVisibility(View.GONE);
                    break;
            }
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"logLevel"})
    public static void loadCardBackgroundAcordingToLogLevel(CardView view, Enumerators.LogLevel logLevel) {
        if (logLevel != null) {
            switch (logLevel) {
                case INFO:
                    view.setCardBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logInfoColor));
                    break;
                case WARN:
                    view.setCardBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logWarnColor));
                    break;
                case ERROR:
                    view.setCardBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColor));
                    break;
                case NOTIF_NONE:
                    view.setCardBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationNoneColor));
                    break;
                case NOTIF_WARN:
                    view.setCardBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationWarningColor));
                    break;
                case NOTIF_CRITICAL:
                    view.setCardBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationCriticalColor));
                    break;
                default:
                    break;
            }
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"date", "dateFormatToday" ,"dateFormatAnotherday"})
    public static void loadDateText(TextView view, Date date, String dateFormatToday, String dateFormatAnotherday) {
        // If not null, set release date, with the format specified at the strings XML.
        try {
            if (date != null) {
                String dateString = "-";
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(date);
                cal2.setTime(new Date());
                if (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
                    dateString = (new SimpleDateFormat(dateFormatToday)).format(date);
                } else {
                    dateString = (new SimpleDateFormat(dateFormatAnotherday)).format(date);
                }
                view.setText(dateString);
            } else {
                view.setText("-");
            }
        } catch (Exception e) {
            view.setText("-");
        }
    }

    @BindingAdapter({"elementName", "elementType"})
    public static void setTitle(TextView view, String name, String type) {
        // If not null, set release date, with the format specified at the strings XML.
        if (name != null) {
            if (type != null) {
                view.setText(name + " - " + type);
            } else {
                view.setText(name);
            }
        } else {
            view.setText("-");
        }
    }

    @BindingAdapter({"sensorValue", "sensorDataType", "sensorUnit"})
    public static void setSensorValue(TextView view, String value, Enumerators.DataType type,
                                      String unit) {
        // If not null, set release date, with the format specified at the strings XML.
        if (value != null) {
            String dataToPrint = new String(value);
            if (type != null) {
                switch (type) {
                    case INTEGER:
                        dataToPrint = String.format("%i", Integer.valueOf(value));
                        break;
                    case DECIMAL:
                        dataToPrint = String.format("%2f", Integer.valueOf(value));
                        break;
                    case BOOLEAN:
                        if (value.equals(Boolean.TRUE.toString())) {
                            dataToPrint = DotApplication.getContext().getString(R.string.boolean_active);
                        } else {
                            dataToPrint = DotApplication.getContext().getString(R.string.boolean_not_active);
                        }
                        break;
                }
            }
            if (unit != null) {
                dataToPrint = dataToPrint.concat(" ").concat(unit);
            }
            view.setText(dataToPrint);
        } else {
            view.setText("-");
        }
    }

    @BindingAdapter({"minValue", "maxValue"})
    public static void setNumberEditTextMinMaxFilter(EditText view, Float minValue, Float maxValue) {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                try {
                    float input = Float.parseFloat(dest.toString() + source.toString());
                    if (isInRange(minValue, maxValue, input))
                        return null;
                } catch (NumberFormatException nfe) { }
                return "";
            }
            private boolean isInRange(float a, float b, float c) {
                return b > a ? c >= a && c <= b : c >= b && c <= a;
            }
        };
        view.setFilters(filters);
    }

    @BindingAdapter({"dataReceived", "dataType", "thresholdAboveWarning", "thresholdAboveCritical",
            "thresholdBelowWarning", "thresholdBelowCritical", "thresholdEqualsWarning", "thresholdEqualsCritical"})
    public static void setSensorState(TextView view, String dataReceived,
                                      Enumerators.DataType dataType,
                                      String thresholdAboveWarningString,
                                      String thresholdAboveCriticalString,
                                      String thresholdBelowWarningString,
                                      String thresholdBelowCriticalString,
                                      String thresholdEqualsWarningString,
                                      String thresholdEqualsCriticalString){
        try {
            Enumerators.NotificationState state = DataHelper.getNotificationStatus(dataReceived,
                    dataType, thresholdAboveWarningString, thresholdAboveCriticalString,
                    thresholdBelowWarningString, thresholdBelowCriticalString,
                    thresholdEqualsWarningString, thresholdEqualsCriticalString);
            if (state != null) {
                switch (state) {
                    case NONE:
                        view.setText(DotApplication.getContext().getString(R.string.sensor_state_critical));
                        break;
                    case WARNING:
                        view.setText(DotApplication.getContext().getString(R.string.sensor_state_warning));
                        break;
                    case CRITICAL:
                        view.setText(DotApplication.getContext().getString(R.string.sensor_state_normal));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"sensorInfo", "data", "spinnerHistorySelected"})
    public static void setSensorValue(LineChart chart, Sensor sensorInfo, List<DataValue> data,
                                      Integer spinnerHistorySelected) {
        try {
            chart.setTouchEnabled(true);
            chart.setPinchZoom(true);
            // First, work out the X time Axis
            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(Color.RED);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(false);
            xAxis.setValueFormatter(HistoryChartHelper.getValueFormatterForTimeAxis(spinnerHistorySelected,
                    data.get(0).getDateReceived(), data.get(data.size() - 1).getDateReceived()));
            // Second, work out the Y data Axis
            YAxis yAxis = chart.getAxisLeft();
            yAxis.setDrawLabels(true);
            yAxis.setDrawAxisLine(false);
            yAxis.setDrawGridLines(false);
            yAxis.setDrawZeroLine(true);
            yAxis.setValueFormatter(HistoryChartHelper.getValueFormatterForDataAxis(sensorInfo.getDataType()));
            chart.getAxisRight().setEnabled(false);
            // Third, set the data entry set
            List<Entry> entries = new ArrayList<>();
            for (DataValue value : data) {
                entries.add(new Entry(Float.parseFloat(value.getValue()),
                        value.getDateReceived().getTime()));
            }
            // Fourth, set the data entry set and style it
            LineDataSet dataSet = new LineDataSet(entries,
                    DotApplication.getContext().getResources().getStringArray(R.array.history_options_aray)[spinnerHistorySelected]); // add entries to dataset

            dataSet.setDrawIcons(false);
            dataSet.setColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryColor));
            dataSet.setCircleColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryDarkColor));
            dataSet.setLineWidth(1f);
            dataSet.setCircleRadius(3f);
            dataSet.setDrawCircleHole(false);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawFilled(false);
            dataSet.setValueFormatter(HistoryChartHelper.getValueFormatterForDataAxis(sensorInfo.getDataType()));
            // Firth, generate the data and refresh the chart
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}