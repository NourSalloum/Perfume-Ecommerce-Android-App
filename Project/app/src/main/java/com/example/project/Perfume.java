package com.example.project;

import android.os.Parcel;
import android.os.Parcelable;

public class Perfume implements Parcelable {

    public String key, perfumeName, imageResourceId, category;
    public double price;


    public Perfume() {
    }

    public Perfume(String key, String perfumeName, String imageResourceId, String category, double price) {
        this.key = key;
        this.perfumeName = perfumeName;
        this.imageResourceId = imageResourceId;
        this.category = category;
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPerfumeName() {
        return perfumeName;
    }

    public void setPerfumeName(String perfumeName) {
        this.perfumeName = perfumeName;
    }

    public String getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(String imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getprice() {
        return price;
    }

    public void setprice(double price) {
        this.price = price;
    }

    protected Perfume(Parcel in) {
        perfumeName = in.readString();
        price = in.readDouble();
        imageResourceId = in.readString();
        key = in.readString();
        category = in.readString();
    }

    public static final Creator<Perfume> CREATOR = new Creator<Perfume>() {
        @Override
        public Perfume createFromParcel(Parcel in) {
            return new Perfume(in);
        }

        @Override
        public Perfume[] newArray(int size) {
            return new Perfume[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(perfumeName);
        dest.writeDouble(price);
        dest.writeString(imageResourceId);
        dest.writeString(key);
        dest.writeString(category);
    }
}
