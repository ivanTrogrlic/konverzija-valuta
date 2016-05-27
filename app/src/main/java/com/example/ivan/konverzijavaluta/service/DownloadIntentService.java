package com.example.ivan.konverzijavaluta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.main.MainStartingActivity;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
import com.example.ivan.konverzijavaluta.rest.EcbWebService;
import com.example.ivan.konverzijavaluta.rest.RestClient;
import com.example.ivan.konverzijavaluta.util.FileUtils;
import com.example.ivan.konverzijavaluta.util.Preferences;

import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import timber.log.Timber;

public class DownloadIntentService extends IntentService {

    public static final String SERVICE_PATH = "com.example.ivan.konverzijavaluta.service.DownloadIntentService";

    public static final String COLUMN_TIME_PERIOD   = "TIME_PERIOD";
    public static final String COLUMN_OBS_VALUE     = "OBS_VALUE";
    public static final String COLUMN_UNIT          = "UNIT";
    public static final String START_PERIOD         = "startPeriod";
    public static final String END_PERIOD           = "endPeriod";
    public static final String DEFAULT_DATE         = "2016-05-24";
    public static final String DATE_FORMAT          = "yyyy-MM-dd";
    public static final String DAILY_EXCHANGE_RATES = "D..EUR.SP00.A";

    public static final String DOWNLOAD_FINISHED    = "Download finished";
    public static final String DOWNLOAD_FAILED      = "Download failed";
    public static final String DOWNLOAD_RESPONSE    = "download_response";
    public static final String EXCHANGE_LIST_ACTUAL = "exchangelistactual.csv";

    DanRepository          m_danRepository;
    DrzavaRepository       m_drzavaRepository;
    TecajnaListaRepository m_tecajnaListaRepository;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    public static void start(Context p_context) {
        Intent msgIntent = new Intent(p_context, DownloadIntentService.class);
        p_context.startService(msgIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        m_danRepository = new DanRepository(getContentResolver());
        m_drzavaRepository = new DrzavaRepository(getContentResolver());
        m_tecajnaListaRepository = new TecajnaListaRepository(getContentResolver());

        downloadLatestExchangeValues();
    }

    private void downloadLatestExchangeValues() {
        Map<String, String> params = getDownloadDatesParams();

        RestClient.create(EcbWebService.class).get(DAILY_EXCHANGE_RATES, params).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Timber.e(new Exception(), response.message());
                    sendDownloadResponse(DOWNLOAD_FAILED);
                    return;
                }

                try {
                    handleResponse(response);
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                    sendDownloadResponse(DOWNLOAD_FAILED);
                }

                sendDownloadResponse(DOWNLOAD_FINISHED);
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Timber.e(t, t.getMessage());
                sendDownloadResponse(DOWNLOAD_FAILED);
            }
        });
    }

    private void sendDownloadResponse(String p_response) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainStartingActivity.DownloadReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(DOWNLOAD_RESPONSE, p_response);
        sendBroadcast(broadcastIntent);
    }

    @NonNull
    private Map<String, String> getDownloadDatesParams() {
        Map<String, String> params = new HashMap<>();
        LocalDate downloadDate = Preferences.getLastDownloadDate(getApplicationContext());
        String date = downloadDate.toString(DATE_FORMAT);
        params.put(START_PERIOD, date);
        params.put(END_PERIOD, LocalDate.now().toString(DATE_FORMAT));
        return params;
    }

    private void handleResponse(retrofit2.Response<ResponseBody> response) throws IOException {
        File file = convertResponseToCsvFile(response);

        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT));

            String valuta = csv.get(COLUMN_UNIT);
            String value = csv.get(COLUMN_OBS_VALUE);

            Dan dan = findOrInsertDan(date);
            Drzava drzava = findOrInsertDrzava(valuta);
            insertTecajnaLista(value, dan, drzava);
        }

        appendResponseToCsv();
        Preferences.saveDate(getApplicationContext(), Preferences.LAST_DOWNLOADED_EXCHANGE_LIST, LocalDate.now());
        file.delete();
    }

    @NonNull
    private File convertResponseToCsvFile(retrofit2.Response<ResponseBody> response) throws IOException {
        BufferedWriter writer;
        BufferedReader reader;
        String path1 = getExternalFilesDir(null).getPath();
        String path2 = "dataSet.csv";
        String path = path1 + "/" + path2;
        File file = new File(path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        writer = new BufferedWriter(new FileWriter(path));
        reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            Timber.d(line);
            sb.append(line + "\n");
        }

        String result = sb.toString();

        writer.write(result);
        writer.close();
        reader.close();
        return file;
    }

    private void appendResponseToCsv() throws IOException {
        if (!Preferences.loadBoolean(getApplicationContext(), Preferences.INITIAL_EXCHANGE_LIST_SAVED, false)) {
            FileUtils.copyFileToExternalDirectory(getApplicationContext(), EXCHANGE_LIST_ACTUAL, EXCHANGE_LIST_ACTUAL);
            Preferences.saveBoolean(getApplicationContext(), Preferences.INITIAL_EXCHANGE_LIST_SAVED, true);
        }

        String path = getExternalFilesDir(null).getPath() + "/" + EXCHANGE_LIST_ACTUAL;
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));

        LocalDate currentDate = Preferences.getLastDownloadDate(getApplicationContext());
        StringBuilder sb = new StringBuilder();
        while (currentDate.isBefore(LocalDate.now().plusDays(1))) {
            currentDate = appendLineToCsvFile(currentDate, sb);
        }

        String result = sb.toString();
        writer.write(result);
        writer.close();
    }

    private LocalDate appendLineToCsvFile(LocalDate p_currentDate, StringBuilder p_sb) {
        Dan dan = m_danRepository.getByDate(p_currentDate);
        if (dan == null) return p_currentDate.plusDays(1); // No exchange list available for today yet

        p_sb.append(p_currentDate.toString(SaveCsvFileToSqlService.DATE_FORMAT)).append(",");
        for (int i = 0; i < Valute.values().length; i++) {
            Valute valuta = Valute.values()[i];

            Drzava drzava = m_drzavaRepository.getByValuta(valuta.name());
            if (drzava == null) {
                p_sb.append(SaveCsvFileToSqlService.MISSING_VALUE).append(",");
                continue;
            }

            TecajnaLista tecajnaLista = m_tecajnaListaRepository.getByDanAndDrzava(dan.getId(), drzava.getId());
            if (tecajnaLista == null) {
                p_sb.append(SaveCsvFileToSqlService.MISSING_VALUE).append(",");
                continue;
            }
            p_sb.append(tecajnaLista.getSrednjiTecaj());

            if (i != Valute.values().length - 1) p_sb.append(","); // Don't need "," after the last one in a row
        }

        p_sb.append("\n");
        return p_currentDate.plusDays(1);
    }

    private void insertTecajnaLista(String p_value, Dan p_dan, Drzava p_drzava) {
        TecajnaLista tecajnaLista = new TecajnaLista();
        tecajnaLista.setDan(p_dan);
        tecajnaLista.setDrzava(p_drzava);

        BigDecimal tecaj = BigDecimal.valueOf(Double.valueOf(p_value));
        tecajnaLista.setKupovniTecaj(tecaj);
        tecajnaLista.setSrednjiTecaj(tecaj);
        tecajnaLista.setProdajniTecaj(tecaj);
        m_tecajnaListaRepository.insert(tecajnaLista);
    }

    @NonNull
    private Drzava findOrInsertDrzava(String p_valuta) {
        Drzava drzava = m_drzavaRepository.getByValuta(p_valuta);
        if (drzava == null) {
            drzava = new Drzava();
            drzava.setJedinica(1); //TODO
            drzava.setSifra(p_valuta);
            drzava.setValuta(p_valuta);
            drzava.setId(m_drzavaRepository.insert(drzava));
        }
        return drzava;
    }

    @NonNull
    private Dan findOrInsertDan(LocalDate p_date) {
        Dan dan = m_danRepository.getByDate(p_date);
        if (dan == null) {
            dan = new Dan();
            dan.setDan(p_date);
            dan.setId(m_danRepository.insert(dan));
        }
        return dan;
    }

}
