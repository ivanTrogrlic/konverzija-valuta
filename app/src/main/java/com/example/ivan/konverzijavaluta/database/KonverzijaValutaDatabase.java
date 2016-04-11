package com.example.ivan.konverzijavaluta.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import com.example.ivan.konverzijavaluta.entitet.Valute;


public class KonverzijaValutaDatabase extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "valute_data";
    public static final String TABLE_VALUTE = "valute";
    public static final String TABLE_SAMO_VALUTE = "samo_valute";
    public static final String ID = "_id";
    public static final String COL_VALUTA = "valuta";
    public static final String COL_DATE = "datum";
    public static final String COL_ZEMLJA_VALUTA = "zemlja_valuta";
    public static final String COL_KUPOVNI = "kupovni";
    public static final String COL_SREDNJI = "srednji";
    public static final String COL_PRODAJNI = "prodajni";

    private static final String CREATE_TABLE_VALUTE = "create table " + TABLE_VALUTE
            + " (" + ID + " integer primary key autoincrement, " + COL_DATE + " text not null, "
            + COL_ZEMLJA_VALUTA + " text not null, " + COL_KUPOVNI
            + " text not null, " + COL_SREDNJI + " text not null," + COL_PRODAJNI + " text not null);";

    private static final String CREATE_TABLE_SAMO_VALUTE = "create table " + TABLE_SAMO_VALUTE
            + " (" + ID + " integer primary key autoincrement, " + COL_VALUTA + " text not null);";

    private static final String DB_SCHEMA = CREATE_TABLE_VALUTE;
    private static final String DB_SCHEMA_VALUTE = CREATE_TABLE_SAMO_VALUTE;

    public KonverzijaValutaDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_SCHEMA);
        db.execSQL(DB_SCHEMA_VALUTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VALUTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMO_VALUTE);
        onCreate(db);
    }

    public void addValuta(Valute valute) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ZEMLJA_VALUTA, valute.getZemlja());
        values.put(COL_KUPOVNI, valute.getKupovni());
        values.put(COL_SREDNJI, valute.getSrednji());
        values.put(COL_PRODAJNI, valute.getProdajni());

        db.insert(TABLE_VALUTE,
                null,
                values);

        db.close();
    }

    public List<Valute> getAll() {
        List<Valute> valueList = new ArrayList<>();

        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Valute valute;
        if (cursor.moveToFirst()) {
            do {
                valute = new Valute();
                valute.setId(Integer.parseInt(cursor.getString(0)));
                valute.setZemlja(cursor.getString(1));
                valute.setKupovni(cursor.getString(2));
                valute.setSrednji(cursor.getString(3));
                valute.setProdajni(cursor.getString(4));

                valueList.add(valute);
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return valueList;
    }

    public void deleteEverything() {

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("delete from " + TABLE_VALUTE);

        db.close();

    }

    public List<String> getValuteCountry() {
        List<String> labels = new ArrayList<>();

        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String country = cursor.getString(1).substring(3, 6);
                labels.add(country);
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return labels;
    }

    public List<String> getTecajKupovni(String ulaznaValuta) {

        List<String> tecaj = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).substring(3, 6).equals(ulaznaValuta)) {
                    tecaj.add(cursor.getString(2));
                    tecaj.add(cursor.getString(1).substring(6, 9));
                    break;
                }
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return tecaj;
    }

    public List<String> getTecajSrednji(String ulaznaValuta) {

        List<String> tecaj = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).substring(3, 6).equals(ulaznaValuta)) {
                    tecaj.add(cursor.getString(3));
                    tecaj.add(cursor.getString(1).substring(6, 9));
                    break;
                }
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return tecaj;
    }

    public List<String> getTecajProdajni(String ulaznaValuta) {

        List<String> tecaj = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).substring(3, 6).equals(ulaznaValuta)) {
                    tecaj.add(cursor.getString(4));
                    tecaj.add(cursor.getString(1).substring(6, 9));
                    break;
                }
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return tecaj;
    }

    public boolean chkDB() {

        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Boolean rowExists;

        if (cursor.moveToFirst()) {
            rowExists = true;

        } else {
            rowExists = false;
        }

        db.close();
        cursor.close();

        return rowExists;
    }

    public String getLastEntry() {

        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        cursor.moveToLast();
        String lastEntry = cursor.getString(1);

        db.close();
        cursor.close();

        return lastEntry;


    }

    public void addSamoValuta(String valuta) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_VALUTA, valuta);

        db.insert(TABLE_SAMO_VALUTE,
                null,
                values);

        db.close();
    }

    public boolean valutaVecPostoji(String valuta) {

        String query = "SELECT  * FROM " + TABLE_SAMO_VALUTE;
        boolean exists = false;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).equals(valuta)) {
                    exists = true;
                    break;
                }
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return exists;
    }

    public List<String> getValute() {
        List<String> labels = new ArrayList<>();

        String query = "SELECT  * FROM " + TABLE_SAMO_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String country = cursor.getString(1);
                labels.add(country);
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return labels;
    }

    public List<String> getGodine() {
        List<String> labels = new ArrayList<>();

        String query = "SELECT  * FROM " + TABLE_VALUTE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String godina = cursor.getString(1).substring(4, 8);
                if (!labels.contains(godina)) {
                    labels.add(godina);
                }
            } while (cursor.moveToNext());
        }

        db.close();
        cursor.close();

        return labels;
    }

}

