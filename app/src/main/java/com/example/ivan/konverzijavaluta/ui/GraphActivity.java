package com.example.ivan.konverzijavaluta.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaPredictedRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ivan on 5/31/2016.
 */
public class GraphActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    @InjectView(R.id.chart)    LineChart m_chart;
    @InjectView(R.id.spinner)  Spinner   m_spinner;
    @InjectView(R.id.currency) TextView  m_currency;

    private DanRepository                   m_danRepository;
    private DrzavaRepository                m_drzavaRepository;
    private TecajnaListaRepository          m_tecajnaListaRepository;
    private TecajnaListaPredictedRepository m_tecajnaListaPredictedRepository;
    private String                          m_selectedCurrency;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.graph_activity);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ButterKnife.inject(this);

        initRepositories();
        initGraph();
        initSpinner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    @OnClick(R.id.show)
    public void show() {
        m_selectedCurrency = String.valueOf(m_spinner.getSelectedItem());
        m_currency.setText(m_selectedCurrency);
        setGraphData();
    }

    private void initRepositories() {
        m_drzavaRepository = new DrzavaRepository(getContentResolver());
        m_danRepository = new DanRepository(getContentResolver());
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
        m_selectedCurrency = String.valueOf(m_spinner.getSelectedItem());
    }

    private void initGraph() {
        m_chart.setOnChartValueSelectedListener(this);

        m_chart.setDrawGridBackground(false);
        m_chart.setDescription("");
        m_chart.setDrawBorders(false);

        m_chart.getAxisLeft().setEnabled(false);
        m_chart.getAxisRight().setDrawAxisLine(false);
        m_chart.getAxisRight().setDrawGridLines(false);
        m_chart.getXAxis().setDrawAxisLine(false);
        m_chart.getXAxis().setDrawGridLines(false);

        // enable touch gestures
        m_chart.setTouchEnabled(true);

        // enable scaling and dragging
        m_chart.setDragEnabled(true);
        m_chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        m_chart.setPinchZoom(false);

        Legend l = m_chart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
    }

    private void setGraphData() {
        m_chart.resetTracking();

        LocalDate start = LocalDate.now().minusMonths(1);
        LocalDate end = LocalDate.now();

        Drzava drzava = m_drzavaRepository.getByValuta(m_selectedCurrency);
        List<TecajnaLista> tecajnaLista = m_tecajnaListaRepository.getFromToByDrzava(start, end, drzava.getId());
        List<TecajnaListaPredicted> tecajnaListaPredicted =
                m_tecajnaListaPredictedRepository.getFromToByDrzava(start, end, drzava.getId());

        ArrayList<String> xVals = new ArrayList<>();
        int daysBetween = Days.daysBetween(start, end).getDays();
        for (int i = 0; i <= daysBetween; i++) {
            xVals.add(start.plusDays(i).toString());
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

        addTecajnaListaToDataSet(start, tecajnaLista, dataSets);
        addTecajnaListaPredictedToDataSet(start, tecajnaListaPredicted, dataSets);

        // make the predicted DataSet dashed
        ((LineDataSet) dataSets.get(1)).enableDashedLine(10, 10, 0);

        LineData data = new LineData(xVals, dataSets);
        m_chart.setData(data);
        m_chart.invalidate();
    }

    private void addTecajnaListaToDataSet(LocalDate p_start, List<TecajnaLista> p_tecajnaLista,
                                          ArrayList<ILineDataSet> p_dataSets) {
        ArrayList<Entry> values = new ArrayList<>();

        LocalDate current = p_start;
        Iterator<TecajnaLista> tecajnaListaIterator = p_tecajnaLista.iterator();
        int i = 0;
        double val;
        while (tecajnaListaIterator.hasNext()) {
            TecajnaLista lista = tecajnaListaIterator.next();
            val = lista.getSrednjiTecaj().doubleValue();
            while (current.isBefore(lista.getDan().getDan())) {
                values.add(new Entry((float) val, i));
                current = current.plusDays(1);
                i++;
            }
            current = lista.getDan().getDan().plusDays(1);
            values.add(new Entry((float) val, i));
            i++;
        }

        LineDataSet d = new LineDataSet(values, "Real exchange list");
        d.setLineWidth(2.5f);
        d.setCircleRadius(4f);

        int color = getResources().getColor(R.color.black);
        d.setColor(color);
        d.setCircleColor(color);
        p_dataSets.add(d);
    }

    private void addTecajnaListaPredictedToDataSet(LocalDate p_start,
                                                   List<TecajnaListaPredicted> p_tecajnaListaPredicted,
                                                   ArrayList<ILineDataSet> p_dataSets) {
        ArrayList<Entry> values = new ArrayList<>();

        LocalDate current = p_start;
        Iterator<TecajnaListaPredicted> tecajnaListaIterator = p_tecajnaListaPredicted.iterator();
        int i = 0;
        double val;
        while (tecajnaListaIterator.hasNext()) {
            TecajnaListaPredicted lista = tecajnaListaIterator.next();
            val = lista.getSrednjiTecaj().doubleValue();
            while (current.isBefore(lista.getDan().getDan())) {
                current = current.plusDays(1);
                i++;
            }
            current = lista.getDan().getDan().plusDays(1);
            values.add(new Entry((float) val, i));
            i++;
        }

        LineDataSet d = new LineDataSet(values, "Predicted exchange list");
        d.setLineWidth(2.5f);
        d.setCircleRadius(4f);

        int color = getResources().getColor(R.color.accent);
        d.setColor(color);
        d.setCircleColor(color);
        p_dataSets.add(d);
    }

}
