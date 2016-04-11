package com.example.ivan.konverzijavaluta.entitet;

import android.os.Parcel;
import android.os.Parcelable;


public class Valute implements Parcelable {

    private int id;
    private String kupovni, prodajni, srednji, zemlja, datum;

    public Valute() {
    }

    public Valute(String datum, String zemlja, String kupovni, String srednji, String prodajni) {
        this.zemlja = zemlja;
        this.prodajni = prodajni;
        this.srednji = srednji;
        this.kupovni = kupovni;
        this.datum = datum;
    }

    public Valute(int id, String datum, String zemlja, String kupovni, String srednji, String prodajni) {
        this.zemlja = zemlja;
        this.prodajni = prodajni;
        this.srednji = srednji;
        this.kupovni = kupovni;
        this.datum = datum;
        this.id = id;
    }

    public String getProdajni() {
        return prodajni;
    }

    public void setProdajni(String prodajni) {
        this.prodajni = prodajni;
    }

    public String getZemlja() {
        return zemlja;
    }

    public void setZemlja(String zemlja) {
        this.zemlja = zemlja;
    }

    public String getSrednji() {
        return srednji;
    }

    public void setSrednji(String srednji) {
        this.srednji = srednji;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKupovni() {
        return kupovni;
    }

    public void setKupovni(String kupovni) {
        this.kupovni = kupovni;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }


    @Override
    public String toString() {
        return "GameStatistics{" +
                "prodajni=" + prodajni +
                ", id=" + id +
                "datum=" + datum +
                ", datum=" + datum +
                ", kupovni=" + kupovni +
                ", srednji=" + srednji +
                ", zemlja='" + zemlja + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(zemlja);
        dest.writeString(datum);
        dest.writeString(kupovni);
        dest.writeString(prodajni);
        dest.writeString(srednji);
    }

    private Valute(Parcel in) {
        this.id = in.readInt();
        this.zemlja = in.readString();
        this.datum = in.readString();
        this.kupovni = in.readString();
        this.prodajni = in.readString();
        this.srednji = in.readString();
    }

    public static final Parcelable.Creator<Valute> CREATOR = new Parcelable.Creator<Valute>() {

        @Override
        public Valute createFromParcel(Parcel source) {
            return new Valute(source);
        }

        @Override
        public Valute[] newArray(int size) {
            return new Valute[size];
        }
    };

}
