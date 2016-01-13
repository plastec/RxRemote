package com.example.ypavshl.example;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ypavshl on 11.1.16.
 */
public class ButtonItem implements Parcelable {

    private int mState;

    public ButtonItem(int state) {
        mState = state;
    }

    public int getState() {
        return mState;
    }

    public void setState(int action) {
        mState = action;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mState);
    }

    protected ButtonItem(Parcel in) {
        this.mState = in.readInt();
    }

    public static final Creator<ButtonItem> CREATOR = new Creator<ButtonItem>() {
        public ButtonItem createFromParcel(Parcel source) {
            return new ButtonItem(source);
        }

        public ButtonItem[] newArray(int size) {
            return new ButtonItem[size];
        }
    };
}
