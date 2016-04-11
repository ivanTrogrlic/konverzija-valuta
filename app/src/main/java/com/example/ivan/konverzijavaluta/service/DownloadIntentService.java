package com.example.ivan.konverzijavaluta.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.main.DateChooseFragment;
import com.example.ivan.konverzijavaluta.main.MainStartingActivity;
import com.example.ivan.konverzijavaluta.provider.KonverzijaProvider;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class DownloadIntentService extends IntentService {

    public static final String PARAM_OUT_MSG = "lista skinutih podataka";
    public static final String NO_DATA       = "NO_DATA";
    static KonverzijaDatabase db;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        db = DateChooseFragment.db;

        ArrayList<String> list = new ArrayList<>();

        try {
            list = downloadUrl();
        } catch (IOException e) {
            list.add("Unable to retrieve web page. URL may be invalid.");
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(DateChooseFragment.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putStringArrayListExtra(PARAM_OUT_MSG, list);
        sendBroadcast(broadcastIntent);
    }

    protected static ArrayList<String> downloadUrl() throws IOException {
        InputStream is = null;
        ArrayList<String> contentAsString = new ArrayList<>();

        int staringDay = 1, staringMonth = 10, startingYear = 1997;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(6, 7));
        int day = Integer.parseInt(date.substring(9, 10));

        if (db.chkDB()) {

            String datumZadnjeUneseneListe = db.getLastEntry();
            staringDay = Integer.parseInt(datumZadnjeUneseneListe.substring(0, 2));
            staringMonth = Integer.parseInt(datumZadnjeUneseneListe.substring(2, 4));
            startingYear = Integer.parseInt(datumZadnjeUneseneListe.substring(4, 8));
            Log.i("Last entery", datumZadnjeUneseneListe);

            MainStartingActivity.context.getContentResolver().delete(KonverzijaProvider.CONTENT_URI, "datum=?", new String[]{datumZadnjeUneseneListe});

        }

        outerloop:
        for (int yearFor = startingYear; yearFor <= year; yearFor++) {
            if (yearFor == 1997 && db.chkDB()) {
                staringMonth = 10;
            }
            Log.i("Year", String.valueOf(yearFor));
            for (int monthFor = staringMonth; monthFor <= 12; monthFor++) {
                Log.i("Month", String.valueOf(monthFor));
                for (int dayFor = staringDay; dayFor <= 31; dayFor++) {
                    if (yearFor == year && monthFor == month && dayFor > day) {
                        break outerloop;
                    }

                    try {
                        URL url = new URL(getUrl(String.valueOf(dayFor), String.valueOf(monthFor - 1), String.valueOf(yearFor)));

                        OkHttpClient client = new OkHttpClient();
                        client.setConnectTimeout(10, TimeUnit.SECONDS);
                        client.setReadTimeout(15, TimeUnit.SECONDS);
                        Request request = new Request.Builder().url(url).build();
                        Call call = client.newCall(request);
                        Response response = call.execute();

                        if (response.isSuccessful()) {

                            is = response.body().byteStream();
                            contentAsString = readIt(is);

                        } else {

                            if (dayFor < 10) {
                                contentAsString.add("0" + String.valueOf(dayFor));
                            } else {
                                contentAsString.add(String.valueOf(dayFor));
                            }
                            if (monthFor < 10) {
                                contentAsString.add("0" + String.valueOf(monthFor));
                            } else {
                                contentAsString.add(String.valueOf(monthFor));
                            }
                            contentAsString.add(String.valueOf(yearFor));
                            contentAsString.add(NO_DATA);

                        }

                        addRecievedListToDB(contentAsString);
                        contentAsString.clear();

                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }

                }
                staringDay = 1;
            }
            staringMonth = 1;
        }

        return contentAsString;

    }

    protected static ArrayList<String> readIt(InputStream stream) throws IOException {

        ArrayList<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(line);
            Timber.i("Lajna", line);
        }
        return list;
    }

    protected static void addRecievedListToDB(List<String> result) {

        String datum = "";

        if (result.get(3).equals(NO_DATA)) {

            ContentValues values = new ContentValues();
            values.put(KonverzijaDatabase.COL_DATE, result.get(0) + result.get(1) + result.get(2));
            values.put(KonverzijaDatabase.COL_ZEMLJA_VALUTA, result.get(3));
            values.put(KonverzijaDatabase.COL_KUPOVNI, result.get(3));
            values.put(KonverzijaDatabase.COL_SREDNJI, result.get(3));
            values.put(KonverzijaDatabase.COL_PRODAJNI, result.get(3));
            MainStartingActivity.context.getContentResolver().insert(KonverzijaProvider.CONTENT_URI, values);

        } else {

            for (String details : result) {
                StringBuilder builder = new StringBuilder();
                builder.append(details);

                if (builder.toString().matches("\\b\\d{21}\\b")) {

                    datum = builder.substring(11, 19);

                } else {

                    String[] dataArray;
                    String delimiter = "\\s+";
                    dataArray = builder.toString().split(delimiter);
                    List<String> list = Arrays.asList(dataArray);
                    Timber.i("Lista", String.valueOf(list.size()));
                    Timber.i("Array", String.valueOf(dataArray.length));

                    Valute valute = new Valute(datum,
                            list.get(0),
                            list.get(1),
                            list.get(2),
                            list.get(3));

                    ContentValues values = new ContentValues();
                    values.put(KonverzijaDatabase.COL_DATE, valute.getDatum());
                    values.put(KonverzijaDatabase.COL_ZEMLJA_VALUTA, valute.getZemlja());
                    values.put(KonverzijaDatabase.COL_KUPOVNI, valute.getKupovni());
                    values.put(KonverzijaDatabase.COL_SREDNJI, valute.getSrednji());
                    values.put(KonverzijaDatabase.COL_PRODAJNI, valute.getProdajni());
                    MainStartingActivity.context.getContentResolver().insert(KonverzijaProvider.CONTENT_URI, values);

                    if (!db.valutaVecPostoji(valute.getZemlja().substring(3, 6))) {

                        db.addSamoValuta(valute.getZemlja().substring(3, 6));
                        Log.i("Valuta", valute.getZemlja());

                    }

                }
            }
        }
    }

    public static String getUrl(String day, String month, String year) {

        String URL = "http://www.hnb.hr/tecajn/f";

        int intMonth = Integer.parseInt(month) + 1;
        month = String.valueOf(intMonth);
        if (day.length() == 1) {
            day = "0" + day;
        }
        if (month.length() == 1) {
            month = "0" + month;
        }
        year = year.substring(2, 4);

        return URL + day + month + year + ".dat";

    }

}

