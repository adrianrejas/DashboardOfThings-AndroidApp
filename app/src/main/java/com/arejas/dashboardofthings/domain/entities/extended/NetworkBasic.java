package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;
import android.os.Parcelable;

public class NetworkBasic implements Parcelable {

    private Integer id;

    private String name;

    public NetworkBasic() {}

    protected NetworkBasic(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
    }

    public static final Creator<NetworkBasic> CREATOR = new Creator<NetworkBasic>() {
        @Override
        public NetworkBasic createFromParcel(Parcel in) {
            return new NetworkBasic(in);
        }

        @Override
        public NetworkBasic[] newArray(int size) {
            return new NetworkBasic[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
        parcel.writeString(name);
    }
}
