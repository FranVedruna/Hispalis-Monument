package com.example.hispalismonumentapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TypeDTO implements Parcelable {
    private String typeName;

    public TypeDTO() {}

    public TypeDTO(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    // Parcelable implementation

    protected TypeDTO(Parcel in) {
        typeName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(typeName);
    }

    public static final Creator<TypeDTO> CREATOR = new Creator<TypeDTO>() {
        @Override
        public TypeDTO createFromParcel(Parcel in) {
            return new TypeDTO(in);
        }

        @Override
        public TypeDTO[] newArray(int size) {
            return new TypeDTO[size];
        }
    };
}
