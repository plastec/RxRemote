package com.example.ypavshl.example;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ypavshl on 5.1.16.
 */
public class ColorItem implements Parcelable {

    private String mText;
    private int mColor;

    public ColorItem(String text, int color) {
        mText = text;
        mColor = color;
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int mColor) {
        this.mColor = mColor;
    }

    @Override
    public String toString() {
        return "ColorItem{" +
                "mText='" + mText + '\'' +
                ", mColor=" + mColor +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mText);
        dest.writeInt(this.mColor);
    }

    protected ColorItem(Parcel in) {
        this.mText = in.readString();
        this.mColor = in.readInt();
    }

    public static final Creator<ColorItem> CREATOR = new Creator<ColorItem>() {
        public ColorItem createFromParcel(Parcel source) {
            return new ColorItem(source);
        }

        public ColorItem[] newArray(int size) {
            return new ColorItem[size];
        }
    };
}
