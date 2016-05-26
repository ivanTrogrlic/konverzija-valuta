package com.example.ivan.konverzijavaluta.ui;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaWrapper;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaPredictedRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;

import org.joda.time.LocalDate;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ivan on 5/26/2016.
 */
public class PredictedDataActivity extends AppCompatActivity {

    @InjectView(R.id.list) RecyclerView m_list;
    @InjectView(R.id.date) TextView     m_date;

    private PredictedDataAdapter            m_adapter;
    private DanRepository                   m_danRepository;
    private DrzavaRepository                m_drzavaRepository;
    private TecajnaListaRepository          m_tecajnaListaRepository;
    private TecajnaListaPredictedRepository m_tecajnaListaPredictedRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_data_activity);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ButterKnife.inject(this);
        initRepositories();
        initAdapter();
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

    @OnClick(R.id.select_date)
    public void setListaForDate() {
        LocalDate now = LocalDate.now();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                setListData(date);
                m_date.setText(date.toString());
            }
        }, now.getYear(), now.getMonthOfYear(), now.getDayOfMonth());
        datePickerDialog.getDatePicker().setMaxDate(now.plusMonths(1).toDate().getTime());
        datePickerDialog.show();
    }

    private void setListData(LocalDate p_date) {
        m_adapter.clear();
        Dan dan = m_danRepository.getByDate(p_date);
        List<TecajnaLista> tecajnaLista = m_tecajnaListaRepository.getByDan(dan.getId());
        List<TecajnaListaPredicted> tecajnaListaPredicted = m_tecajnaListaPredictedRepository.getByDan(dan.getId());
        TecajnaListaWrapper tecajnaListaWrapper = new TecajnaListaWrapper();
        tecajnaListaWrapper.setTecajnaLista(tecajnaLista);
        tecajnaListaWrapper.setTecajnaListaPredicted(tecajnaListaPredicted);

        m_adapter.setItems(tecajnaListaWrapper);
        m_adapter.notifyDataSetChanged();
    }

}
