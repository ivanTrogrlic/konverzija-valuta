package com.example.ivan.konverzijavaluta.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.encog.EncogService;
import com.example.ivan.konverzijavaluta.service.ConvertCsvToSqlService;
import com.example.ivan.konverzijavaluta.service.DownloadIntentService;
import com.example.ivan.konverzijavaluta.ui.PastDataActivity;
import com.example.ivan.konverzijavaluta.ui.PredictedDataActivity;
import com.example.ivan.konverzijavaluta.util.Preferences;

import org.joda.time.LocalDate;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainStartingActivity extends AppCompatActivity {

    static ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staring_activity);
        progressBar = (ProgressBar) findViewById(R.id.toolbar_progress_bar);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        progressBar.setVisibility(View.INVISIBLE);

        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            if (!Preferences.getLastDownloadDate(getApplicationContext()).equals(LocalDate.now())) {
                DownloadIntentService.start(this);
            }

            if (!Preferences.loadBoolean(getApplicationContext(), Preferences.CONVERTED_CSV_TO_SQL, false)) {
                ConvertCsvToSqlService.start(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.calculator)
    public void openCalculator() {
        LocalDate lastPredictedDate = Preferences.loadDate(this, Preferences.LAST_PREDICTED_DATE, LocalDate.now());
        if (LocalDate.now().isAfter(lastPredictedDate.minusDays(1))) {
            // If today is >= last day of the month, make new calculations for the next month
            EncogService.start(this); //TODO move this elsewhere
        }
    }

    @OnClick(R.id.past_data)
    public void openPastData() {
        Intent intent = new Intent(this, PastDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.predicted_data)
    public void openPredictedData() {
        Intent intent = new Intent(this, PredictedDataActivity.class);
        startActivity(intent);
    }
}
