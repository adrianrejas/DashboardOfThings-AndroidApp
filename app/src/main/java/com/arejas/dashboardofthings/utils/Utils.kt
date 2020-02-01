package com.arejas.dashboardofthings.utils

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.Html
import android.text.Spanned

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.services.ControlService
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.sucho.placepicker.MapType
import com.sucho.placepicker.PlacePicker

import java.util.Date

object Utils {

    val intentBuilderForLocationPicker: PlacePicker.IntentBuilder
        get() = PlacePicker.IntentBuilder()
            .showLatLong(false)
            .setMarkerImageImageColor(R.color.primaryColor)
            .setPrimaryTextColor(R.color.primaryTextColor)
            .setSecondaryTextColor(R.color.secondaryTextColor)
            .setMapType(MapType.NORMAL)
            .setAddressRequired(true)
            .hideMarkerShadow(true)
            .setMarkerDrawable(R.drawable.marker)
            .setFabColor(R.color.secondaryColor)
            .onlyCoordinates(true)

    /**
     * Function for calling Html.fromHtml function correctly depending on the Android version
     *
     * @param htmlText text to convert
     * @return Html.fromHtml returned object
     */
    fun fromHtml(htmlText: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(htmlText)
        }
    }

    fun getUriFromFilSelected(context: Context, contentUri: Uri): String {
        return contentUri.toString()
    }

    fun vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
        val vectorDrawable =
            ResourcesCompat.getDrawable(DotApplication.context.getResources(), id, null)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun getStringDataToPrint(value: String, type: Enumerators.DataType?, unit: String?): String {
        var dataToPrint = String(value.toCharArray())
        if (type != null) {
            when (type) {
                Enumerators.DataType.INTEGER -> dataToPrint =
                    String.format("%d", Integer.valueOf(value))
                Enumerators.DataType.DECIMAL -> dataToPrint =
                    String.format("%.2f", java.lang.Float.valueOf(value))
                Enumerators.DataType.BOOLEAN -> if (value == java.lang.Boolean.TRUE.toString()) {
                    dataToPrint = DotApplication.context.getString(R.string.boolean_active)
                } else {
                    dataToPrint = DotApplication.context.getString(R.string.boolean_not_active)
                }
            }
        }
        if (unit != null) {
            dataToPrint = dataToPrint + " " + unit
        }
        return dataToPrint
    }

    fun startControlService(context: Context) {
        val serviceIntent = Intent(context, ControlService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopControlService(context: Context) {
        val serviceIntent = Intent(context, ControlService::class.java)
        context.stopService(serviceIntent)
    }

}
