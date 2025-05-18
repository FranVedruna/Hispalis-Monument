package com.example.hispalismonumentapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class MonumentoDTO implements Parcelable {
    private Integer id;
    private String nombre;
    private String descripcionEs;
    private String descripcionEn;
    private String fotoUrl;
    private Double latitud;
    private Double longitud;
    private List<TypeDTO> types;
    private String wikiPath;

    public MonumentoDTO() {}

    public MonumentoDTO(Integer id, String nombre, String descripcionEs, String descripcionEn, String fotoUrl, Double latitud, Double longitud, List<TypeDTO> types, String wikiPath) {
        this.id = id;
        this.nombre = nombre;
        this.descripcionEs = descripcionEs;
        this.descripcionEn = descripcionEn;
        this.fotoUrl = fotoUrl;
        this.latitud = latitud;
        this.longitud = longitud;
        this.types = types;
        this.wikiPath = wikiPath;
    }

    protected MonumentoDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        nombre = in.readString();
        descripcionEs = in.readString();
        descripcionEn = in.readString();
        fotoUrl = in.readString();
        if (in.readByte() == 0) {
            latitud = null;
        } else {
            latitud = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitud = null;
        } else {
            longitud = in.readDouble();
        }
        types = new ArrayList<>();
        in.readList(types, TypeDTO.class.getClassLoader());
        wikiPath = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(nombre);
        dest.writeString(descripcionEs);
        dest.writeString(descripcionEn);
        dest.writeString(fotoUrl);
        if (latitud == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitud);
        }
        if (longitud == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitud);
        }
        dest.writeList(types);
        dest.writeString(wikiPath);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MonumentoDTO> CREATOR = new Creator<MonumentoDTO>() {
        @Override
        public MonumentoDTO createFromParcel(Parcel in) {
            return new MonumentoDTO(in);
        }

        @Override
        public MonumentoDTO[] newArray(int size) {
            return new MonumentoDTO[size];
        }
    };
    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcionEs() {
        return descripcionEs;
    }

    public void setDescripcionEs(String descripcionEs) {
        this.descripcionEs = descripcionEs;
    }

    public String getDescripcionEn() {
        return descripcionEn;
    }

    public void setDescripcionEn(String descripcionEn) {
        this.descripcionEn = descripcionEn;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public List<TypeDTO> getTypes() {
        return types;
    }

    public void setTypes(List<TypeDTO> types) {
        this.types = types;
    }

    public String getWikiPath() {
        return wikiPath;
    }

    public void setWikiPath(String wikiPath) {
        this.wikiPath = wikiPath;
    }
}