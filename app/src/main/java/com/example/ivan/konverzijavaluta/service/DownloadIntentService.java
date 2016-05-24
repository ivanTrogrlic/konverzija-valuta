package com.example.ivan.konverzijavaluta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.main.DateChooseFragment;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
import com.example.ivan.konverzijavaluta.rest.EcbWebService;
import com.example.ivan.konverzijavaluta.rest.RestClient;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import timber.log.Timber;

public class DownloadIntentService extends IntentService {

    public static final int START_DAY   = 1;
    public static final int START_MONTH = 10;
    public static final int START_YEAR  = 1997;

    public static final String START_PERIOD         = "startPeriod";
    public static final String END_PERIOD           = "endPeriod";
    public static final String TIME_PERIOD          = "TIME_PERIOD";
    public static final String OBS_VALUE            = "OBS_VALUE";
    public static final String UNIT                 = "UNIT";
    public static final String DEFAULT_DATE         = "2016-05-24";
    public static final String DATE_FORMAT          = "yyyy-MM-dd";
    public static final String DAILY_EXCHANGE_RATES = "D..EUR.SP00.A";

    public static final String DOWNLOAD_FINISHED  = "Download finished";
    public static final String DOWNLOAD_FAILED    = "Download failed";
    public static final String DOWNLOAD_RESPONSE  = "download_response";
    public static final String TECAJNA_LISTA_FILE = "tecajnalista.csv";

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
        broadcastIntent.setAction(DateChooseFragment.ResponseReceiver.ACTION_RESP);
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
            String timePeriod = csv.get(TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT));

            String valuta = csv.get(UNIT);
            String value = csv.get(OBS_VALUE);

            Dan dan = findOrInsertDan(date);
            Drzava drzava = findOrInsertDrzava(valuta);
            insertTecajnaLista(value, dan, drzava);
        }

        appendResponseToCsv();
        Preferences.saveDate(getApplicationContext(), Preferences.LAST_DOWNLOADED_EXCHANGE_LIST,
                             LocalDate.now());
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
            writeInitialExchangeListToFile();
        }

        String path = getExternalFilesDir(null).getPath() + "/" + TECAJNA_LISTA_FILE;
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

        p_sb.append(p_currentDate.toString(ConvertCsvToSqlService.DATE_FORMAT)).append(",");
        for (int i = 0; i < Valute.values().length; i++) {
            Valute valuta = Valute.values()[i];

            Drzava drzava = m_drzavaRepository.getByValuta(valuta.name());
            if (drzava == null) {
                p_sb.append(ConvertCsvToSqlService.MISSING_VALUE).append(",");
                continue;
            }

            TecajnaLista tecajnaLista = m_tecajnaListaRepository.getByDanAndDrzava(dan.getId(), drzava.getId());
            if (tecajnaLista == null) {
                p_sb.append(ConvertCsvToSqlService.MISSING_VALUE).append(",");
                continue;
            }
            p_sb.append(tecajnaLista.getSrednjiTecaj());

            if (i != Valute.values().length - 1) p_sb.append(","); // Don't need "," after the last one in a row
        }

        p_sb.append("\n");
        return p_currentDate.plusDays(1);
    }

    private void writeInitialExchangeListToFile() {
        BufferedReader reader;
        BufferedWriter writer;
        try {
            String path = getExternalFilesDir(null).getPath() + "/" + TECAJNA_LISTA_FILE;
            File file = new File(path);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            writer = new BufferedWriter(new FileWriter(path));
            reader = new BufferedReader(new InputStreamReader(getAssets().open(TECAJNA_LISTA_FILE)));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            String result = sb.toString();

            writer.write(result);
            writer.close();
            reader.close();

            Preferences.saveBoolean(getApplicationContext(), Preferences.INITIAL_EXCHANGE_LIST_SAVED, true);
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
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

    //////////////////////////////////////////// Deprecated ////////////////////////////////////////////
    protected void downloadUrl() throws IOException {
        int staringDay = START_DAY, staringMonth = START_MONTH, startingYear = START_YEAR;
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int currentYear = Integer.parseInt(date.substring(0, 4));
        int currentMonth = Integer.parseInt(date.substring(6, 7));
        int currentDay = Integer.parseInt(date.substring(9, 10));


        Dan lastDan = m_danRepository.getLast();
        if (lastDan != null) {
            LocalDate nextDay = lastDan.getDan().plusDays(1);
            String lastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(nextDay.toDate());
            staringDay = Integer.parseInt(lastDate.substring(8, 10));
            staringMonth = Integer.parseInt(lastDate.substring(5, 7));
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

        OkHttpClient client = RestClient.provideHttpClient(getApplicationContext());
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
                Long danId = m_danRepository.insert(dan);
                dan.setId(danId);
            } else {
                String[] dataArray;
                String delimiter = "\\s+";
                dataArray = builder.toString().split(delimiter);
                List<String> list = Arrays.asList(dataArray);

                Drzava drzava = getDrzava(list.get(0));
                Drzava drzavaFromDb = m_drzavaRepository.getByValuta(drzava.getValuta());
                if (drzavaFromDb == null) {
                    Long drzavaId = m_drzavaRepository.insert(drzava);
                    drzava.setId(drzavaId);
                } else {
                    drzava = drzavaFromDb;
                }

                TecajnaLista tecajnaLista = getTecajnaLista(list, dan, drzava);
                m_tecajnaListaRepository.insert(tecajnaLista);
            }
        }
    }

    private TecajnaLista getTecajnaLista(List<String> p_list, Dan p_dan, Drzava p_drzava) {
        String kupovni = p_list.get(1).replaceAll(",", ".");
        String srednji = p_list.get(2).replaceAll(",", ".");
        String prodajni = p_list.get(3).replaceAll(",", ".");
        BigDecimal kupovniDecimal = new BigDecimal(kupovni);
        BigDecimal srednjiDecimal = new BigDecimal(srednji);
        BigDecimal prodajniDecimal = new BigDecimal(prodajni);

        TecajnaLista tecajnaLista = new TecajnaLista();
        tecajnaLista.setDan(p_dan);
        tecajnaLista.setDrzava(p_drzava);
        tecajnaLista.setKupovniTecaj(kupovniDecimal);
        tecajnaLista.setSrednjiTecaj(srednjiDecimal);
        tecajnaLista.setProdajniTecaj(prodajniDecimal);
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
        LocalDate date = new LocalDate(year, month, day);
        Dan dan = new Dan();
        dan.setDan(date);
        return dan;
    }

    public String getUrl(String day, String month, String year) {
        String URL = "http://old.hnb.hr/tecajn/f";

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
