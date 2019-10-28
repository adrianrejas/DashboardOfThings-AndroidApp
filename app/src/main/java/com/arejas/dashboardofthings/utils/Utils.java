package com.arejas.dashboardofthings.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.sucho.placepicker.MapType;
import com.sucho.placepicker.PlacePicker;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

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

    public static String getUriFromFilSelected (Context context, Uri contentUri) {
        return contentUri.toString();
    }

    @NotNull
    public static PlacePicker.IntentBuilder getIntentBuilderForLocationPicker() {
        return new PlacePicker.IntentBuilder()
                .showLatLong(false)
                .setMarkerImageImageColor(R.color.primaryColor)
                .setPrimaryTextColor(R.color.primaryTextColor)
                .setSecondaryTextColor(R.color.secondaryTextColor)
                .setMapType(MapType.NORMAL)
                .setAddressRequired(true)
                .hideMarkerShadow(true)
                .setMarkerDrawable(R.drawable.marker)
                .setFabColor(R.color.secondaryColor)
                .onlyCoordinates(true);
    }

    public static BitmapDescriptor vectorToBitmap(@DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(DotApplication.getContext().getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static String getStringDataToPrint(String value, Enumerators.DataType type, String unit) {
        String dataToPrint = new String(value);
        if (type != null) {
            switch (type) {
                case INTEGER:
                    dataToPrint = String.format("%d", Integer.valueOf(value));
                    break;
                case DECIMAL:
                    dataToPrint = String.format("%.2f", Float.valueOf(value));
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
        return dataToPrint;
    }

}
