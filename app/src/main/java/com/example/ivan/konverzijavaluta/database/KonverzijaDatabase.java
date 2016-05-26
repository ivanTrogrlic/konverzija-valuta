package com.example.ivan.konverzijavaluta.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Dan;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Drzava;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.TecajnaLista;
import com.example.ivan.konverzijavaluta.util.DbUtils;


public class KonverzijaDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "valute_db";

    private static final int DB_VERSION = 1;

    public interface Tables {
        String DRZAVA                  = "drzava";
        String TECAJNA_LISTA           = "tecajna_lista";
        String TECAJNA_LISTA_PREDCITED = "tecajna_lista_predicted";
        String DAN                     = "dan";

        // TecajnaLista + Dan + Drzava
        String TECAJNA_LISTA_JOIN_DAN_JOIN_DRZAVA = TECAJNA_LISTA
                + " LEFT JOIN " + DAN
                + " ON " + TECAJNA_LISTA + "." + TecajnaLista.DAN_ID + "=" + DAN + "." + Dan._ID
                + " LEFT JOIN " + DRZAVA
                + " ON " + TECAJNA_LISTA + "." + TecajnaLista.DRZAVA_ID + "=" + DRZAVA + "." + Dan._ID;
    }

    public KonverzijaDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDrzavaTable(db);
        createTecajnaListaTable(db);
        createTecajnaListaPredictedTable(db);
        createDanTable(db);
    }

    private void createDrzavaTable(SQLiteDatabase p_db) {
        p_db.execSQL("CREATE TABLE " + Tables.DRZAVA + " ("
                             + Drzava._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                             + Drzava.SIFRA + " TEXT NOT NULL, "
                             + Drzava.VALUTA + " TEXT NOT NULL, "
                             + Drzava.JEDINICA + " INTEGER NOT NULL, "
                             // Unique columns
                             + "UNIQUE (" + Drzava.SIFRA + ")"
                             + "UNIQUE (" + Drzava.VALUTA + ")"
                             + " )");
    }

    private void createTecajnaListaTable(SQLiteDatabase p_db) {
        p_db.execSQL("CREATE TABLE " + Tables.TECAJNA_LISTA + " ("
                             + TecajnaLista._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                             + TecajnaLista.DRZAVA_ID + " INTEGER NOT NULL, "
                             + TecajnaLista.DAN_ID + " INTEGER NOT NULL, "
                             + TecajnaLista.KUPOVNI_TECAJ + " TEXT NOT NULL, "
                             + TecajnaLista.SREDNJI_TECAJ + " TEXT NOT NULL, "
                             + TecajnaLista.PRODAJNI_TECAJ + " TEXT NOT NULL,"
                             // Foreign keys
                             + DbUtils.dbCreateForeignKey(TecajnaLista.DRZAVA_ID, Tables.DRZAVA, Drzava._ID)
                             + DbUtils.dbCreateForeignKey(TecajnaLista.DAN_ID, Tables.DAN, Dan.DAN)
                             + " )");
    }

    private void createTecajnaListaPredictedTable(SQLiteDatabase p_db) {
        p_db.execSQL("CREATE TABLE " + Tables.TECAJNA_LISTA_PREDCITED + " ("
                             + TecajnaLista._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                             + TecajnaLista.DRZAVA_ID + " INTEGER NOT NULL, "
                             + TecajnaLista.DAN_ID + " INTEGER NOT NULL, "
                             + TecajnaLista.KUPOVNI_TECAJ + " TEXT NOT NULL, "
                             + TecajnaLista.SREDNJI_TECAJ + " TEXT NOT NULL, "
                             + TecajnaLista.PRODAJNI_TECAJ + " TEXT NOT NULL,"
                             // Foreign keys
                             + DbUtils.dbCreateForeignKey(TecajnaLista.DRZAVA_ID, Tables.DRZAVA, Drzava._ID)
                             + DbUtils.dbCreateForeignKey(TecajnaLista.DAN_ID, Tables.DAN, Dan.DAN)
                             + " )");
    }

    private void createDanTable(SQLiteDatabase p_db) {
        p_db.execSQL("CREATE TABLE " + Tables.DAN + " ("
                             + Dan._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                             + Dan.DAN + " DATE NOT NULL, "
                             // Unique columns
                             + "UNIQUE (" + Dan.DAN + ")"
                             + " )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing for now
    }

}
