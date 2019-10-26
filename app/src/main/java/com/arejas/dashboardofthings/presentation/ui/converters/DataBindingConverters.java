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
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper;
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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
        try {
            if (name != null) {
                if (type != null) {
                    view.setText(name + " - " + type);
                } else {
                    view.setText(name);
                }
            } else {
                view.setText("-");
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
                    view.setText(name + " - " + DotApplication.getContext().getResources().
                            getQuantityString(R.plurals.error_log_number, elementProblems));
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColorText));
                } else {
                    view.setText(name);
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                }
            } else {
                view.setText("-");
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
                view.setText("-");
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
                        view.setText("-");
                        break;
                }
                if ((elementProblems != null) && (elementProblems > 0)) {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.logErrorColorText));
                } else {
                    view.setTextColor(ContextCompat.getColor(DotApplication.getContext(), R.color.primaryTextColor));
                }
            } else {
                view.setText("-");
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
                        view.setText(DotApplication.getContext().getString(R.string.network_type_mqtt));
                        break;
                    case HTTP:
                        view.setText(DotApplication.getContext().getString(R.string.network_type_http));
                        break;
                    default:
                        view.setText("-");
                        break;
                }
            } else {
                view.setText("-");
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                                      Float thresholdAboveWarning,
                                      Float thresholdAboveCritical,
                                      Float thresholdBelowWarning,
                                      Float thresholdBelowCritical,
                                      String thresholdEqualsWarning,
                                      String thresholdEqualsCritical){
        try {
            Enumerators.NotificationState state = DataHelper.getNotificationStatus(dataReceived,
                    dataType, thresholdAboveWarning, thresholdAboveCritical,
                    thresholdBelowWarning, thresholdBelowCritical,
                    thresholdEqualsWarning, thresholdEqualsCritical);
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

    @BindingAdapter("entryList")
    public static void setNetworkTypeSpinnerValue(Spinner spinner, List<Network> entryList) {
        if (entryList != null) {
            List<CharSequence> networkNames = new ArrayList<>();
            for (Network network : entryList) {
                networkNames.add(network.getName());
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter (DotApplication.getContext(),
                    android.R.layout.simple_spinner_dropdown_item, networkNames);
            spinner.setAdapter(adapter);
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
