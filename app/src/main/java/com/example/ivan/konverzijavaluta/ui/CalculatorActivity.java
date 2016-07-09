package com.example.ivan.konverzijavaluta.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnTextChanged;

/**
 * Created by ivan on 7/9/2016.
 */
public class CalculatorActivity extends AppCompatActivity {

    @InjectView(R.id.etValuta1) EditText     m_etValuta1;
    @InjectView(R.id.etValuta2) EditText     m_etValuta2;
    @InjectView(R.id.valuta1)   Spinner      m_valuta1;
    @InjectView(R.id.valuta2)   Spinner      m_valuta2;
    @InjectView(R.id.calendar)  CalendarView m_calendar;

    private DanRepository          m_danRepository;
    private DrzavaRepository       m_drzavaRepository;
    private TecajnaListaRepository m_tecajnaListaRepository;

    private Drzava       m_drzava1;
    private Drzava       m_drzava2;
    private Dan          m_dan;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.calculator_activity);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ButterKnife.inject(this);

        initRepositories();
        initSpinner();
        initCalendar();
    }

    private void initCalendar() {
        m_calendar.setDate(DateTime.now().getMillis());
        m_calendar.setOnDateChangeListener(new OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                LocalDate date = new LocalDate(year, month + 1, dayOfMonth);
                m_dan = m_danRepository.getByDate(date);
            }
        });
        m_dan = m_danRepository.getByDate(new LocalDate(m_calendar.getDate()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    public void initSpinner() {
        List<String> list = new ArrayList<>();
        for (Valute valute : Valute.values()) {
            list.add(valute.name());
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_valuta1.setAdapter(dataAdapter);
        m_valuta2.setAdapter(dataAdapter);
        m_drzava1 = m_drzavaRepository.getByValuta(String.valueOf(m_valuta1.getSelectedItem()));
        m_drzava2 = m_drzavaRepository.getByValuta(String.valueOf(m_valuta2.getSelectedItem()));

        m_valuta1.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_drzava1 = m_drzavaRepository.getByValuta(String.valueOf(m_valuta1.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        m_valuta2.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_drzava2 = m_drzavaRepository.getByValuta(String.valueOf(m_valuta2.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void initRepositories() {
        m_drzavaRepository = new DrzavaRepository(getContentResolver());
        m_danRepository = new DanRepository(getContentResolver());
        m_tecajnaListaRepository = new TecajnaListaRepository(getContentResolver());
    }

    @OnTextChanged(R.id.etValuta1)
    public void calculate() {
        TecajnaLista tecaj1 = m_tecajnaListaRepository.getByDanAndDrzava(m_dan.getId(), m_drzava1.getId());
        TecajnaLista tecaj2 = m_tecajnaListaRepository.getByDanAndDrzava(m_dan.getId(), m_drzava2.getId());

        if (m_etValuta1.getText().toString().equals("")) {
            m_etValuta2.setText("");
            return;
        }

        Double value = tecaj2.getSrednjiTecaj().divide(tecaj1.getSrednjiTecaj(), 2, RoundingMode.HALF_UP).doubleValue();
        m_etValuta2.setText(String.valueOf(value * Double.parseDouble(m_etValuta1.getText().toString())));
    }

}
