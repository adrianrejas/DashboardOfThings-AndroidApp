package com.arejas.dashboardofthings.domain.entities.widget

import android.os.Parcel
import android.os.Parcelable

import com.arejas.dashboardofthings.utils.Enumerators

class SensorWidgetItem : Parcelable {

    var sensorId: Int? = null

    var sensorName: String? = null

    var sensorType: String? = null

    var sensorDataType: Enumerators.DataType? = null

    var sensorUnit: String? = null

    var lastValueReceived: String? = null

    constructor() {}

    protected constructor(`in`: Parcel) {
        if (`in`.readByte().toInt() == 0) {
            sensorId = null
        } else {
            sensorId = `in`.readInt()
        }
        sensorName = `in`.readString()
        sensorType = `in`.readString()
        sensorUnit = `in`.readString()
        sensorDataType = Enumerators.DataType.valueOf(`in`.readInt())
        lastValueReceived = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (sensorId == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(sensorId!!)
        }
        dest.writeString(sensorName)
        dest.writeString(sensorType)
        dest.writeString(sensorUnit)
        dest.writeInt(sensorDataType!!.ordinal)
        dest.writeString(lastValueReceived)
    }

    companion object CREATOR : Parcelable.Creator<SensorWidgetItem> {
        override fun createFromParcel(parcel: Parcel): SensorWidgetItem {
            return SensorWidgetItem(parcel)
        }

        override fun newArray(size: Int): Array<SensorWidgetItem?> {
            return arrayOfNulls(size)
        }
    }
}
