package com.arejas.dashboardofthings.presentation.ui.helpers;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryChartHelper {

    public static final int SPINNER_HISTORY_LASTVAL = 0;
    public static final int SPINNER_HISTORY_LASTDAY = 1;
    public static final int SPINNER_HISTORY_LASTWEEK = 2;
    public static final int SPINNER_HISTORY_LASTMONTH = 3;
    public static final int SPINNER_HISTORY_LASTYEAR = 4;

    public static final long ONE_HOUR_MILLIS = 3600*1000;

    public static ValueFormatter getValueFormatterForTimeAxis(int spinnerHistorySelection,
                                                              Date firstDate, Date lastDate) {
        return new ValueFormatterXAxisDate(spinnerHistorySelection, firstDate, lastDate);
    }

    public static ValueFormatter getValueFormatterForDataAxis(Enumerators.DataType dataType) {
        switch (dataType) {
            case BOOLEAN:
                return new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        if (value == 1.0f) {
                            return  DotApplication.getContext().getString(R.string.boolean_active);
                        } else {
                            return DotApplication.getContext().getString(R.string.boolean_not_active);
                        }
                    }
                };
            case INTEGER:
                return new DefaultValueFormatter(0);
            default:
                return new DefaultValueFormatter(1);
        }
    }

    static class ValueFormatterXAxisDate extends ValueFormatter {

        final int spinnerHistoryValue;
        final Date dateMin;
        final Date dateMax;

        public ValueFormatterXAxisDate(int spinnerHistoryValue, Date dateMin, Date dateMax) {
            this.spinnerHistoryValue = spinnerHistoryValue;
            this.dateMin = dateMin;
            this.dateMax = dateMax;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            try {
                Date date = new Date(Float.valueOf(value).longValue() + dateMin.getTime());
                switch (spinnerHistoryValue) {
                    case SPINNER_HISTORY_LASTDAY:
                        return (new SimpleDateFormat("HH:00")).format(date);
                    case SPINNER_HISTORY_LASTWEEK:
                        return (new SimpleDateFormat("E")).format(date);
                    case SPINNER_HISTORY_LASTMONTH:
                        return (new SimpleDateFormat("dd/MM")).format(date);
                    case SPINNER_HISTORY_LASTYEAR:
                        return (new SimpleDateFormat("MMM yy")).format(date);
                    default:
                        long difference = Math.abs(dateMax.getTime() - dateMin.getTime());
                        if (difference < ONE_HOUR_MILLIS) {
                            return (new SimpleDateFormat("HH:mm:ss")).format(date);
                        }
                }
                return (new SimpleDateFormat("dd/MM HH:mm:ss")).format(date);
            } catch (Exception e) {
                return "NNONONONO";
            }
        }
    }

}
