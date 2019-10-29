package com.arejas.dashboardofthings.presentation.ui.converters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.helpers.DataHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.Utils;
import com.arejas.dashboardofthings.utils.functional.ConsumerBoolean;
import com.arejas.dashboardofthings.utils.functional.ConsumerInt;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.android.gms.maps.model.Dot;

import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

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

    @BindingAdapter({"imageUrl", "errorResource", "loadingResource", "alternativeResource"})
    public static void loadImage(ImageView view, String url, Drawable errorResource,
                                 Drawable loadingResource, Drawable alternativeResource) {
        // If not null, get poster image URI and load it with Glide library
        if (url != null) {
            view.setVisibility(View.VISIBLE);
            Glide.with(DotApplication.getContext())
                    .load(url)
                    .error(errorResource)
                    .placeholder(loadingResource)
                    .into(view);
        } else {
            if (alternativeResource != null) {
                view.setVisibility(View.VISIBLE);
                Glide.with(DotApplication.getContext())
                        .load(alternativeResource)
                        .error(errorResource)
                        .placeholder(loadingResource)
                        .into(view);
            } else {
                view.setVisibility(View.GONE);
            }
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
    public static void loadCardBackgroundAcordingToLogLevel(ConstraintLayout view, Enumerators.LogLevel logLevel) {
        if (logLevel != null) {
            switch (logLevel) {
                case INFO:
                    view.setBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logInfoColor));
                    break;
                case WARN:
                    view.setBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logWarnColor));
                    break;
                case ERROR:
                    view.setBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColor));
                    break;
                case NOTIF_NONE:
                    view.setBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationNoneColor));
                    break;
                case NOTIF_WARN:
                    view.setBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationWarningColor));
                    break;
                case NOTIF_CRITICAL:
                    view.setBackgroundColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationCriticalColor));
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
                String dateString = DotApplication.getContext().getString(R.string.no_data);
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
                view.setText(DotApplication.getContext().getString(R.string.no_data));
            }
        } catch (Exception e) {
            view.setText(DotApplication.getContext().getString(R.string.no_data));
        }
    }

    @BindingAdapter({"elementName", "elementType"})
    public static void setTitle(TextView view, String name, String type) {
        try {
            if (name != null) {
                if (type != null) {
                    view.setText(DotApplication.getContext().getString(R.string.two_elements_toghether, name, type));
                } else {
                    view.setText(name);
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"elementName", "elementProblems"})
    public static void setElementName(TextView view, String name, Integer elementProblems) {
        try {
            if (name != null) {
                if ((elementProblems != null) && (elementProblems > 0)) {
                    view.setText(DotApplication.getContext().getString(R.string.two_elements_toghether, name,
                            DotApplication.getContext().getResources().
                            getQuantityString(R.plurals.error_log_number_short, elementProblems, elementProblems)));
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColorText));
                } else {
                    view.setText(name);
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
                view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"elementType", "elementProblems"})
    public static void setElementType(TextView view, String elementType, Integer elementProblems) {
        try {
            if (elementType != null) {
                view.setText(elementType);
                if ((elementProblems != null) && (elementProblems > 0)) {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColorText));
                } else {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
                view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"networkType", "elementProblems"})
    public static void setNetworkType(TextView view, Enumerators.NetworkType networkType, Integer elementProblems) {
        try {
            if (networkType != null) {
                switch (networkType) {
                    case MQTT:
                        view.setText(DotApplication.getContext().getString(R.string.network_type_mqtt));
                        break;
                    case HTTP:
                        view.setText(DotApplication.getContext().getString(R.string.network_type_http));
                        break;
                    default:
                        view.setText(DotApplication.getContext().getString(R.string.no_data));
                        break;
                }
                if ((elementProblems != null) && (elementProblems > 0)) {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColorText));
                } else {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
                view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"sensorType", "elementProblems"})
    public static void setSensorType(TextView view, String sensorType, Integer elementProblems) {
        try {
            if (sensorType != null) {
                view.setText(sensorType);
                if ((elementProblems != null) && (elementProblems > 0)) {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColorText));
                } else {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
                view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"networkType"})
    public static void setNetworkType(TextView view, Enumerators.NetworkType networkType) {
        try {
            if (networkType != null) {
                switch (networkType) {
                    case MQTT:
                        String dataString = DotApplication.getContext().getString(R.string.data_fancy_type,
                                DotApplication.getContext().getString(R.string.network_type_mqtt));
                        view.setText(Utils.fromHtml(dataString));
                        break;
                    case HTTP:
                        String dataString2 = DotApplication.getContext().getString(R.string.data_fancy_type,
                                DotApplication.getContext().getString(R.string.network_type_http));
                        view.setText(dataString2);
                        break;
                    default:
                        view.setText(DotApplication.getContext().getString(R.string.no_data));
                        break;
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"networkObject"})
    public static void setNetworkBaseUrl(TextView view, Network networkObject) {
        try {
            if (networkObject != null) {
                switch (networkObject.getNetworkType()) {
                    case MQTT:
                        String dataString = DotApplication.getContext().getString(R.string.data_fancy_data_base_url,
                                networkObject.getMqttConfiguration().getMqttBrokerUrl());
                        view.setText(Utils.fromHtml(dataString));
                        break;
                    case HTTP:
                        String dataString2 = DotApplication.getContext().getString(R.string.data_fancy_data_base_url,
                                networkObject.getHttpConfiguration().getHttpBaseUrl());
                        view.setText(dataString2);
                        break;
                    default:
                        view.setText(DotApplication.getContext().getString(R.string.no_data));
                        break;
                }
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"sensorValue", "sensorDataType", "sensorUnit"})
    public static void setSensorValue(TextView view, String value, Enumerators.DataType type,
                                      String unit) {
        // If not null, set release date, with the format specified at the strings XML.
        try {
            if (value != null) {
                String dataToPrint = Utils.getStringDataToPrint(value, type, unit);
                view.setText(dataToPrint);
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data_value));
            }
        } catch (Exception e) {
            ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_actuator_printdata_error));
        }
    }

    @BindingAdapter({"dataReceived", "sensorObject", "envoltureText"})
    public static void setSensorState(TextView view, String dataReceived,
                                      Sensor sensor, String envoltureText){
        try {
            Enumerators.NotificationState state = DataHelper.getNotificationStatus(dataReceived,
                    sensor.getDataType(), sensor.getThresholdAboveWarning(),
                    sensor.getThresholdAboveCritical(), sensor.getThresholdBelowWarning(),
                    sensor.getThresholdBelowCritical(), sensor.getThresholdEqualsWarning(),
                    sensor.getThresholdEqualsCritical());
            if (state != null) {
                String dataText = DotApplication.getContext().getString(R.string.no_data);
                switch (state) {
                    case CRITICAL:
                        dataText = DotApplication.getContext().getString(R.string.sensor_state_critical);
                        view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationCriticalColorSolid));
                        break;
                    case WARNING:
                        dataText = DotApplication.getContext().getString(R.string.sensor_state_warning);
                        view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logNotificationWarningColorSolid));
                        break;
                    case NONE:
                        dataText = DotApplication.getContext().getString(R.string.sensor_state_normal);
                        view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                        break;
                }
                view.setText(Utils.fromHtml(String.format(envoltureText, dataText)));
            }
        } catch (Exception e) {
            view.setText(DotApplication.getContext().getString(R.string.no_data));
            e.printStackTrace();
        }
    }

    @BindingAdapter({"sensorInfo", "data", "spinnerHistorySelected"})
    public static void setSensorValue(LineChart chart, Sensor sensorInfo, List<DataValue> data,
                                      Integer spinnerHistorySelected) {
        Date dateMin = null, dateMax = null;
        try {
            if ((sensorInfo != null) && (data != null) && (spinnerHistorySelected != null)) {
                if (spinnerHistorySelected == null) spinnerHistorySelected = 0;
                chart.setTouchEnabled(false);
                chart.setPinchZoom(false);
                // First, work out the X time Axis
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setTextSize(10f);
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(true);
                xAxis.setLabelRotationAngle(45);
                if (data.size() > 0) {
                    for (DataValue value : data) {
                        if (dateMin == null) dateMin = value.getDateReceived();
                        else if (dateMin.getTime() > value.getDateReceived().getTime()) {
                            dateMin = value.getDateReceived();
                        }
                        if (dateMax == null) dateMax = value.getDateReceived();
                        else if (dateMax.getTime() < value.getDateReceived().getTime()) {
                            dateMax = value.getDateReceived();
                        }
                    }
                    xAxis.setValueFormatter(HistoryChartHelper.getValueFormatterForTimeAxis(spinnerHistorySelected,
                            dateMin, dateMax));
                } else {
                    dateMin = dateMax = new Date();
                    xAxis.setValueFormatter(HistoryChartHelper.getValueFormatterForTimeAxis(spinnerHistorySelected,
                            dateMin, dateMax));
                }
                // Second, work out the Y data Axis
                YAxis yAxis = chart.getAxisLeft();
                yAxis.setDrawLabels(true);
                yAxis.setDrawAxisLine(true);
                yAxis.setDrawGridLines(true);
                yAxis.setDrawZeroLine(true);
                yAxis.setValueFormatter(HistoryChartHelper.getValueFormatterForDataAxis(sensorInfo.getDataType()));
                chart.getAxisRight().setEnabled(false);
                // Third, set the data entry set
                List<Entry> entries = new ArrayList<>();
                for (DataValue value : data) {
                    if (sensorInfo.getDataType().equals(Enumerators.DataType.BOOLEAN)) {
                        entries.add(new Entry(value.getDateReceived().getTime() - dateMin.getTime(),
                                value.getValue().equalsIgnoreCase("true") ? 1.0f : 0.0f));
                    } else {
                        entries.add(new Entry(value.getDateReceived().getTime() - dateMin.getTime(),
                                Float.parseFloat(value.getValue())));
                    }
                }
                Collections.sort(entries, new EntryXComparator());
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
                dataSet.setDrawValues(false);
                dataSet.setValueFormatter(HistoryChartHelper.getValueFormatterForDataAxis(sensorInfo.getDataType()));
                // Firth, generate the data and refresh the chart
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                chart.setDescription(null);
                chart.getLegend().setEnabled(false);
                chart.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter({"sensorList"})
    public static void loadListOfSensors(TextView view, List<Sensor> dataList) {
        // If not null, compose the list and set it.
        if ((dataList != null) && (dataList.size() != 0)) {
            List<String> elementNames = new ArrayList<>();
            for (Sensor dataUnit : dataList) {
                elementNames.add(dataUnit.getName());
            }
            String dataString = DotApplication.getContext().getString(R.string.network_list_sensors).
                    concat(TextUtils.join(", ", elementNames));
            view.setText(Utils.fromHtml(dataString));
        } else {
            view.setText(Utils.fromHtml(DotApplication.getContext().getString(R.string.network_no_sensors)));
        }
    }

    @BindingAdapter({"dataType", "envoltureText"})
    public static void loadFancyDataType(TextView view, Enumerators.DataType dataType, String envoltureText) {
        try {
            String[] typesArray = DotApplication.getContext().getResources().getStringArray(R.array.edit_array_element_data_type);
            if ((dataType != null) && (envoltureText != null)) {
                view.setText(Utils.fromHtml(String.format(envoltureText, typesArray[dataType.ordinal()])));
            } else {
                view.setText(DotApplication.getContext().getString(R.string.no_data));
            }
        } catch (Exception e) {
            view.setText(Utils.fromHtml(DotApplication.getContext().getString(R.string.no_data)));
        }
    }

    @BindingAdapter({"dataText", "envoltureText"})
    public static void loadFancyText(TextView view, String dataText, String envoltureText) {
        if ((dataText != null) && (envoltureText != null)) {
            view.setText(Utils.fromHtml(String.format(envoltureText, dataText)));
        } else {
            view.setText(DotApplication.getContext().getString(R.string.no_data));
        }
    }

    @BindingAdapter({"fancyText"})
    public static void loadAsHtml(TextView view, String fancyText) {
        if (fancyText != null) {
            view.setText(Utils.fromHtml(fancyText));
        } else {
            view.setText(DotApplication.getContext().getString(R.string.no_data));
        }
    }

    @BindingAdapter({"actuatorList"})
    public static void loadListOfActuators(TextView view, List<Actuator> dataList) {
        // If not null, compose the list and set it.
        if ((dataList != null) && (dataList.size() != 0)) {
            List<String> elementNames = new ArrayList<>();
            for (Actuator dataUnit : dataList) {
                elementNames.add(dataUnit.getName());
            }
            String dataString = DotApplication.getContext().getString(R.string.network_list_actuators).
                    concat(TextUtils.join(", ", elementNames));
            view.setText(Utils.fromHtml(dataString));
        } else {
            view.setText(Utils.fromHtml(DotApplication.getContext().getString(R.string.network_no_actuators)));
        }
    }

    @BindingAdapter({"entryList", "selectedPosition"})
    public static void setNetworkTypeSpinnerValue(Spinner spinner, List<NetworkExtended> entryList,
                                                  Integer selectedPosition) {
        if (entryList != null) {
            List<CharSequence> networkNames = new ArrayList<>();
            for (Network network : entryList) {
                networkNames.add(network.getName());
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter (DotApplication.getContext(),
                    android.R.layout.simple_spinner_dropdown_item, networkNames);
            spinner.setAdapter(adapter);
            if (selectedPosition != null) {
                spinner.setSelection(selectedPosition);
            }
        }
    }

    @BindingAdapter("selectionListener")
    public static void setSpinnerSelectionListener(Spinner spinner, ConsumerInt selectionListener) {
        if (selectionListener != null) {
            spinner.setFocusable(true);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectionListener.accept(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectionListener.accept(null);
                }
            });
        }
    }

    @BindingAdapter("checkingListener")
    public static void setCheckboxCheckingListener(CheckBox checkBox, ConsumerBoolean checkingListener) {
        if (checkingListener != null) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkingListener.accept(isChecked);
            });
        }
    }

    @BindingAdapter({"imageUrl", "pickResource", "errorResource", "loadingResource"})
    public static void loadImageToBePicked (ImageButton view, String url, Drawable pickResource,
                                            Drawable errorResource, Drawable loadingResource) {
        if (url != null) {
            view.setVisibility(View.VISIBLE);
            Glide.with(DotApplication.getContext())
                    .load(url)
                    .error(errorResource)
                    .placeholder(loadingResource)
                    .into(view);
        } else if (pickResource != null) {
            view.setVisibility(View.VISIBLE);
            Glide.with(DotApplication.getContext())
                    .load(pickResource)
                    .error(errorResource)
                    .placeholder(loadingResource)
                    .into(view);
        } else {
            view.setVisibility(View.GONE);
        }
    }

}
