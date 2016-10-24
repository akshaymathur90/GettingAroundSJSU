package edu.sjsu.gettingaroundsjsu;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dmodh on 10/23/16.
 */

public class Building implements Parcelable {

    String buildingName;
    String address;
    String distance;
    String imgString;

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getImgString() {
        return imgString;
    }

    public void setImgString(String imgString) {
        this.imgString = imgString;
    }

    public static final Parcelable.Creator<Building> CREATOR = new Creator<Building>() {
        public Building createFromParcel(Parcel source) {
            Building building = new Building();
            building.buildingName = source.readString();
            building.address = source.readString();
            building.distance = source.readString();
            building.imgString = source.readString();
            return building;
        }

        public Building[] newArray(int size) {
            return new Building[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(buildingName);
        dest.writeString(address);
        dest.writeString(distance);
        dest.writeString(imgString);
    }
}
