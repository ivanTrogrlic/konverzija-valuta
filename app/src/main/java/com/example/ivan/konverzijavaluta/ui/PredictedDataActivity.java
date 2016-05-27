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
import android.widget.Toast;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.encog.EncogService;
import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaWrapper;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaPredictedRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
import com.example.ivan.konverzijavaluta.util.Preferences;
import com.example.ivan.konverzijavaluta.util.ServiceUtils;

import org.joda.time.LocalDate;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ivan on 5/26/2016.
 */
public class PredictedDataActivity extends AppCompatActivity {

    @InjectView(R.id.list)    RecyclerView m_list;
    @InjectView(R.id.date)    TextView     m_date;
    @InjectView(R.id.header1) TextView     m_header1;
    @InjectView(R.id.header2) TextView     m_header2;
    @InjectView(R.id.header3) TextView     m_header3;

    private PredictedDataAdapter            m_adapter;
    private DanRepository                   m_danRepository;
    private DrzavaRepository                m_drzavaRepository;
    private TecajnaListaRepository          m_tecajnaListaRepository;
    private TecajnaListaPredictedRepository m_tecajnaListaPredictedRepository;
    private LocalDate                       m_lastPredictedDate;

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

        LocalDate lastPredictedDate = Preferences.loadDate(this, Preferences.LAST_PREDICTED_DATE, LocalDate.now());
        if (LocalDate.now().isAfter(lastPredictedDate.minusDays(1))) {
            // If today is >= last day of the month, make new calculations for the next month
            startServiceIfNotRunning();
        }

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
        m_header1.setText(R.string.currency);
        m_header2.setText(R.string.predicted);
        m_header3.setText(R.string.real);
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
        LocalDate lastPredictedDate = Preferences.loadDate(this, Preferences.LAST_PREDICTED_HRK_DATE, LocalDate.now());
        if (LocalDate.now().isAfter(lastPredictedDate.minusDays(1))) {
            startServiceIfNotRunning();
            Toast.makeText(this, R.string.data_not_predicted, Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate now = LocalDate.now();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
                setListData(date);
                m_date.setText(date.toString());
            }
        }, now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
        datePickerDialog.getDatePicker().setMaxDate(lastPredictedDate.toDate().getTime());
        datePickerDialog.show();
    }

    private void startServiceIfNotRunning() {
        if (!ServiceUtils.isMyServiceRunning(this, EncogService.SERVICE_PATH)) {
            EncogService.start(this);
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

}
