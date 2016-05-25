package com.example.ivan.konverzijavaluta.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
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
import java.text.ParseException;

import timber.log.Timber;

/**
 * Created by ivan on 5/23/2016.
 */
public class ConvertCsvToSqlService extends IntentService {

    public static final String EXCHANGE_LIST_TEMP     = "exchangelisttemp.csv";
    public static final String EXCHANGE_LIST_REVERSED = "exchangelistreversed.csv";
    public static final String DATE_FORMAT            = "M/d/yyyy";
    public static final String DATE                   = "Date";
    public static final String MISSING_VALUE          = "?";

    DanRepository          m_danRepository;
    DrzavaRepository       m_drzavaRepository;
    TecajnaListaRepository m_tecajnaListaRepository;

    public ConvertCsvToSqlService() {
        super("ConvertCsvToSqlService");
    }

    public static void start(Context p_context) {
        Intent msgIntent = new Intent(p_context, ConvertCsvToSqlService.class);
        p_context.startService(msgIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        convert();
    }

    public void convert() {
        m_danRepository = new DanRepository(getContentResolver());
        m_drzavaRepository = new DrzavaRepository(getContentResolver());
        m_tecajnaListaRepository = new TecajnaListaRepository(getContentResolver());

        BufferedReader reader;
        BufferedWriter writer;
        try {
            String path = getExternalFilesDir(null).getPath() + "/" + EXCHANGE_LIST_TEMP;
            File file = new File(path);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            writer = new BufferedWriter(new FileWriter(path));
            reader = new BufferedReader(new InputStreamReader(getAssets().open(EXCHANGE_LIST_REVERSED)));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            String result = sb.toString();

            writer.write(result);
            writer.close();
            reader.close();

            ReadCSV csv = new ReadCSV(file, true, new CSVFormat());
            save(csv);
            file.delete();

            Preferences.saveBoolean(getApplicationContext(), Preferences.CONVERTED_CSV_TO_SQL, true);
        } catch (IOException | ParseException e) {
            Timber.e(e, e.getMessage());
        }
    }

    private void save(ReadCSV p_csv) throws ParseException {
        while (p_csv.next()) {
            String date = p_csv.get(DATE);
            LocalDate parsedDate = LocalDate.parse(date, DateTimeFormat.forPattern(DATE_FORMAT));
            if (m_danRepository.getByDate(parsedDate) != null) continue; // Already inserted

            Dan dan = new Dan();
            dan.setDan(parsedDate);
            dan.setId(m_danRepository.insert(dan));

            for (Valute valuta : Valute.values()) {
                String val = p_csv.get(valuta.name());

                Drzava drzava = m_drzavaRepository.getByValuta(valuta.name());
                if (drzava == null) {
                    drzava = new Drzava();
                    drzava.setJedinica(1); //TODO
                    drzava.setSifra(valuta.name());
                    drzava.setValuta(valuta.name());
                    drzava.setId(m_drzavaRepository.insert(drzava));
                }

                TecajnaLista tecajnaLista = new TecajnaLista();
                tecajnaLista.setDan(dan);
                tecajnaLista.setDrzava(drzava);

                BigDecimal tecaj = new BigDecimal(-1);
                if (!MISSING_VALUE.equals(val)) tecaj = BigDecimal.valueOf(Double.valueOf(val));
                tecajnaLista.setKupovniTecaj(tecaj);
                tecajnaLista.setSrednjiTecaj(tecaj);
                tecajnaLista.setProdajniTecaj(tecaj);
                m_tecajnaListaRepository.insert(tecajnaLista);
            }
        }
    }

}
