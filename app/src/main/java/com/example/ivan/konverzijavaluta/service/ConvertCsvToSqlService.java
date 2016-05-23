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
            String path1 = getExternalFilesDir(null).getPath();
            String path2 = "tecajnalista.csv";
            String path = path1 + "/" + path2;
            File file = new File(path);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }

            writer = new BufferedWriter(new FileWriter(path));
            reader = new BufferedReader(new InputStreamReader(getAssets().open("tecajnalista.csv")));
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
        } catch (IOException | ParseException e) {
            Timber.e(e, e.getMessage());
        }
    }

    private void save(ReadCSV p_csv) throws ParseException {
        Dan dan = new Dan();
        while (p_csv.next()) {
            String date = p_csv.get("Date");
            dan.setDan(LocalDate.parse(date, DateTimeFormat.forPattern("MM/dd/yyyy")));
            dan.setId(m_danRepository.insert(dan));
            Timber.d("DAN: " + LocalDate.parse(date, DateTimeFormat.forPattern("MM/dd/yyyy")));

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
                if (!val.equals("?")) tecaj = BigDecimal.valueOf(Double.valueOf(val));
                tecajnaLista.setKupovniTecaj(tecaj);
                tecajnaLista.setSrednjiTecaj(tecaj);
                tecajnaLista.setProdajniTecaj(tecaj);
                m_tecajnaListaRepository.insert(tecajnaLista);
            }
        }
    }

}
