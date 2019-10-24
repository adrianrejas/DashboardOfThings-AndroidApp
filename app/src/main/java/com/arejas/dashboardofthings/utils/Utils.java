package com.arejas.dashboardofthings.utils;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;

import com.arejas.dashboardofthings.DotApplication;

public class Utils {

    /**
     * Function for calling Html.fromHtml function correctly depending on the Android version
     *
     * @param htmlText text to convert
     * @return Html.fromHtml returned object
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(htmlText);
        }
    }

}
