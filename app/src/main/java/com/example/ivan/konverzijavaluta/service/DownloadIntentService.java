package com.example.ivan.konverzijavaluta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.main.DateChooseFragment;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
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

    public static final String PARAM_OUT_MSG = "lista_skinutih_podataka";
    public static final String NO_DATA       = "NO_DATA";
    public static final int    START_DAY     = 1;
    public static final int    START_MONTH   = 10;
    public static final int    START_YEAR    = 1997;

    DanRepository          m_danRepository;
    DrzavaRepository       m_drzavaRepository;
    TecajnaListaRepository m_tecajnaListaRepository;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    public static void startDownloading(Context p_context) {
        Intent msgIntent = new Intent(p_context, DownloadIntentService.class);
        p_context.startService(msgIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        m_danRepository = new DanRepository(getContentResolver());
        m_drzavaRepository = new DrzavaRepository(getContentResolver());
        m_tecajnaListaRepository = new TecajnaListaRepository(getContentResolver());

        String response = "Done downloading";

        try {
            downloadUrl();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            response = "Unable to retrieve web page. URL may be invalid.";
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(DateChooseFragment.ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, response);
        sendBroadcast(broadcastIntent);
    }

    protected void downloadUrl() throws IOException {
        int staringDay = START_DAY, staringMonth = START_MONTH, startingYear = START_YEAR;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int currentYear = Integer.parseInt(date.substring(0, 4));
        int currentMonth = Integer.parseInt(date.substring(6, 7));
        int currentDay = Integer.parseInt(date.substring(9, 10));


        Dan lastDan = m_danRepository.getLast();
        if (lastDan != null) {
            String lastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(lastDan.getDan());
            staringDay = Integer.parseInt(lastDate.substring(9, 10));
            staringMonth = Integer.parseInt(lastDate.substring(6, 7));
            startingYear = Integer.parseInt(lastDate.substring(0, 4));
        }

        startDownloading(staringDay, staringMonth, startingYear, currentYear, currentMonth,
                         currentDay, lastDan);
    }

    private ArrayList<String> startDownloading(int p_staringDay, int p_staringMonth, int p_startingYear,
                                               int p_currentYear, int p_currentMonth, int p_currentDay,
                                               Dan p_lastDan) throws IOException {
        InputStream inputStream = null;
        ArrayList<String> contentAsString = new ArrayList<>();

        outerloop:
        for (int yearFor = p_startingYear; yearFor <= p_currentYear; yearFor++) {
            if (yearFor == START_YEAR && p_lastDan != null) {
                p_staringMonth = START_MONTH;
            }
            for (int monthFor = p_staringMonth; monthFor <= 12; monthFor++) {
                for (int dayFor = p_staringDay; dayFor <= 31; dayFor++) {
                    if (yearFor == p_currentYear && monthFor == p_currentMonth && dayFor > p_currentDay) {
                        break outerloop;
                    }

                    try {
                        Response response = getTecajnaListaFromUrl(yearFor, monthFor, dayFor);

                        if (response.isSuccessful()) {
                            inputStream = response.body().byteStream();
                            contentAsString = readInputStream(inputStream);
                            addReceivedListToDB(contentAsString);
                            contentAsString.clear();
                        }

                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
                p_staringDay = 1;
            }
            p_staringMonth = 1;
        }
        return contentAsString;
    }

    private Response getTecajnaListaFromUrl(int p_yearFor, int p_monthFor, int p_dayFor) throws IOException {
        URL url = new URL(
                getUrl(String.valueOf(p_dayFor), String.valueOf(p_monthFor - 1), String.valueOf(p_yearFor)));

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(15, TimeUnit.SECONDS);
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        return call.execute();
    }

    private ArrayList<String> readInputStream(InputStream stream) throws IOException {

        ArrayList<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = br.readLine()) != null) {
            list.add(line);
            Timber.i("Line: ", line);
        }
        return list;
    }

    private void addReceivedListToDB(List<String> result) {
        Dan dan = null;

        for (String details : result) {
            StringBuilder builder = new StringBuilder();
            builder.append(details);

            if (builder.toString().matches("\\b\\d{21}\\b")) {
                dan = getDan(builder.substring(11, 19));
                m_danRepository.insert(dan);
            } else {
                String[] dataArray;
                String delimiter = "\\s+";
                dataArray = builder.toString().split(delimiter);
                List<String> list = Arrays.asList(dataArray);

                Drzava drzava = getDrzava(list.get(0));
                Drzava drzavaFromDb = m_drzavaRepository.getByValuta(drzava.getValuta());
                if (drzavaFromDb == null) {
                    m_drzavaRepository.insert(drzava);
                } else {
                    drzava = drzavaFromDb;
                }

                TecajnaLista tecajnaLista = getTecajnaLista(list, dan, drzava);
                m_tecajnaListaRepository.insert(tecajnaLista);
            }
        }
    }

    private TecajnaLista getTecajnaLista(List<String> p_list, Dan p_dan, Drzava p_drzava) {
        String kupovni = p_list.get(1).replaceAll(",", "/./");
        String srednji = p_list.get(2).replaceAll(",", "/./");
        String prodajni = p_list.get(3).replaceAll(",", "/./");
        BigDecimal kupovniDecimal = new BigDecimal(kupovni);
        BigDecimal srednjiDecimal = new BigDecimal(srednji);
        BigDecimal prodajniDecimal = new BigDecimal(prodajni);

        TecajnaLista tecajnaLista = new TecajnaLista();
        tecajnaLista.setDan(p_dan);
        tecajnaLista.setDrzava(p_drzava);
        tecajnaLista.setKupovniTecaj(kupovniDecimal);
        tecajnaLista.setSrednjiTecaj(srednjiDecimal);
        tecajnaLista.setProidajniTecaj(prodajniDecimal);
        return tecajnaLista;
    }

    private Drzava getDrzava(String zemlja) {
        Drzava drzava = new Drzava();
        drzava.setSifra(zemlja.substring(0, 3));
        drzava.setValuta(zemlja.substring(3, 6));
        drzava.setJedinica(Integer.parseInt(zemlja.substring(6, 9)));
        return drzava;
    }

    private Dan getDan(String p_datum) {
        int day = Integer.parseInt(p_datum.substring(0, 2));
        int month = Integer.parseInt(p_datum.substring(2, 4));
        int year = Integer.parseInt(p_datum.substring(4, 8));
        Date date = new Date(year, month, day);
        Dan dan = new Dan();
        dan.setDan(date);
        return dan;
    }

    public String getUrl(String day, String month, String year) {
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
