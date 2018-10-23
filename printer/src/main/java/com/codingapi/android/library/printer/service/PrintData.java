package com.codingapi.android.library.printer.service;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Vector;

/**
 * Created by icong on 27/03/2018.
 */

public class PrintData implements Parcelable {
    private Vector<Byte> datas;

    public PrintData(Vector<Byte> datas) {
        this.datas = datas;
    }

    public Vector<Byte> getDatas() {
        return datas;
    }

    public boolean isEmpty() {
        return datas == null || datas.isEmpty();
    }

    public void setDatas(Vector<Byte> datas) {
        this.datas = datas;
    }

    private PrintData(Parcel in) {
        datas = (Vector<Byte>) in.readSerializable();
    }

    public static final Creator<PrintData> CREATOR = new Creator<PrintData>() {
        @Override public PrintData createFromParcel(Parcel in) {
            return new PrintData(in);
        }

        @Override public PrintData[] newArray(int size) {
            return new PrintData[size];
        }
    };

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(datas);
    }
}
