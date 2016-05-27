package com.example.ivan.konverzijavaluta.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.encog.EncogService;
import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaWrapper;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaPredictedRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
import com.example.ivan.konverzijavaluta.util.ServiceUtils;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ivan on 5/26/2016.
 */
public class PredictedDataActivity extends AppCompatActivity {

    @InjectView(R.id.list)                 RecyclerView m_list;
    @InjectView(R.id.toolbar_progress_bar) ProgressBar  m_progressBar;
    @InjectView(R.id.currency)             TextView     m_currency;
    @InjectView(R.id.spinner)              Spinner      m_spinner;
    @InjectView(R.id.header1)              TextView     m_header1;
    @InjectView(R.id.header2)              TextView     m_header2;
    @InjectView(R.id.header3)              TextView     m_header3;

    private PredictedDataAdapter            m_adapter;
    private DanRepository                   m_danRepository;
    private DrzavaRepository                m_drzavaRepository;
    private TecajnaListaRepository          m_tecajnaListaRepository;
    private TecajnaListaPredictedRepository m_tecajnaListaPredictedRepository;
    private LocalDate                       m_lastPredictedDate;

    private EncogReceiver m_receiver;
    private IntentFilter  m_filter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_data_activity);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ButterKnife.inject(this);
        initRepositories();
        initAdapter();
        setHeader();
        initSpinner();
        initReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(m_receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(m_receiver, m_filter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void setHeader() {
        m_header1.setText(R.string.date);
        m_header2.setText(R.string.predicted);
        m_header3.setText(R.string.real);
    }

    private void initReceiver() {
        m_filter = new IntentFilter(EncogReceiver.ACTION_RESP);
        m_filter.addCategory(Intent.CATEGORY_DEFAULT);
        m_receiver = new EncogReceiver();
        registerReceiver(m_receiver, m_filter);
    }

    private void initAdapter() {
        m_adapter = new PredictedDataAdapter(this);
        m_list.setAdapter(m_adapter);
        m_list.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initRepositories() {
        m_danRepository = new DanRepository(getContentResolver());
        m_drzavaRepository = new DrzavaRepository(getContentResolver());
        m_tecajnaListaRepository = new TecajnaListaRepository(getContentResolver());
        m_tecajnaListaPredictedRepository = new TecajnaListaPredictedRepository(getContentResolver());
    }

    public void initSpinner() {
        List<String> list = new ArrayList<>();
        for (Valute valute : Valute.values()) {
            list.add(valute.name());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinner.setAdapter(dataAdapter);
    }

    @OnClick(R.id.predict)
    public void predict() {
        String valuta = String.valueOf(m_spinner.getSelectedItem());
        m_currency.setText(valuta);
//        LocalDate lastPredictedDate = Preferences.loadDate(this, Preferences.LAST_PREDICTED_DATE, LocalDate.now());
//        if (LocalDate.now().isAfter(lastPredictedDate.minusDays(1))) {
        // If today is >= last day of the month, make new calculations for the next month
        //TODO check if today is before last predicted date for selected value, if it is, dont start service, just query predict data for the last month
        startServiceIfNotRunning(valuta);
//        }
    }

    private void startServiceIfNotRunning(String p_valuta) {
        if (!ServiceUtils.isMyServiceRunning(this, EncogService.SERVICE_PATH)) {
            EncogService.start(this, p_valuta);
            m_progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void setListData(LocalDate p_date) {
        m_adapter.clear();
        Dan dan = m_danRepository.getByDate(p_date);
        List<TecajnaLista> tecajnaLista = m_tecajnaListaRepository.getByDan(dan.getId());
        List<TecajnaListaPredicted> tecajnaListaPredicted = m_tecajnaListaPredictedRepository.getByDan(dan.getId(),
                                                                                                       false);
        TecajnaListaWrapper tecajnaListaWrapper = new TecajnaListaWrapper();
        tecajnaListaWrapper.setTecajnaLista(tecajnaLista);
        tecajnaListaWrapper.setTecajnaListaPredicted(tecajnaListaPredicted);

        m_adapter.setItems(tecajnaListaWrapper);
        m_adapter.notifyDataSetChanged();
    }

    public class EncogReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP = "com.example.ivan.konverzijavaluta.ENCOG";

        @Override
        public void onReceive(Context context, Intent intent) {
            m_progressBar.setVisibility(View.INVISIBLE);
            setListData(LocalDate.now().plusDays(1));
        }

    }

}
