package com.example.ivan.konverzijavaluta.database;

import android.net.Uri;

import com.example.ivan.konverzijavaluta.BuildConfig;

/**
 * Created by Ivan on 11.4.2016..
 */
public class KonverzijaContract {
    public static final String AUTHORITY        = BuildConfig.PROVIDER_AUTHORITY;
    public static final Uri    BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Accepted URI paths
    public static final String PATH_DRZAVA                  = "drzava";
    public static final String PATH_TECAJNA_LISTA           = "tecajna_lista";
    public static final String PATH_TECAJNA_LISTA_PREDICTED = "tecajna_lista_predicted";
    public static final String PATH_DAN                     = "dan";

    public static final String PATH_WITH_DAN_AND_DRZAVA = "with_dan_and_drzava";

    public static Uri buildUri(Uri p_uri, Long p_id) {
        if (p_id == null) {
            p_id = -1l;
        }
        return p_uri.buildUpon()
                .appendPath(p_id.toString())
                .build();
    }

    public static long getId(Uri p_uri) {
        return Long.valueOf(p_uri.getPathSegments().get(1));
    }

    public interface Drzava {

        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DRZAVA)
                .build();

        String _ID      = "_id";
        String SIFRA    = "sifra";
        String VALUTA   = "valuta";
        String JEDINICA = "jedinica";
    }

    public interface TecajnaLista {

        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TECAJNA_LISTA)
                .build();

        String _ID            = "_id";
        String DRZAVA_ID      = "drzava_id";
        String DAN_ID         = "dan_id";
        String KUPOVNI_TECAJ  = "kupovni_tecaj";
        String SREDNJI_TECAJ  = "srednji_tecaj";
        String PRODAJNI_TECAJ = "prodajni_tecaj";
    }

    public interface TecajnaListaPredicted {

        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TECAJNA_LISTA_PREDICTED)
                .build();

        String _ID            = "_id";
        String DRZAVA_ID      = "drzava_id";
        String DAN_ID         = "dan_id";
        String KUPOVNI_TECAJ  = "kupovni_tecaj";
        String SREDNJI_TECAJ  = "srednji_tecaj";
        String PRODAJNI_TECAJ = "prodajni_tecaj";
        String SOLO_PREDICTED = "solo_predicted";
    }

    public interface Dan {

        Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_DAN)
                .build();

        String _ID = "_id";
        String DAN = "dan";
    }
}
