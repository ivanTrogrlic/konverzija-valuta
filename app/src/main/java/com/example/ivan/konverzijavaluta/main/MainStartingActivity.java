package com.example.ivan.konverzijavaluta.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.service.ConvertCsvToSqlService;
import com.example.ivan.konverzijavaluta.service.DownloadIntentService;
import com.example.ivan.konverzijavaluta.util.Preferences;

import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import timber.log.Timber;

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

        if (savedInstanceState == null) {
            DateChooseFragment firstFragment = new DateChooseFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();

            if (!Preferences.getLastDownloadDate(getApplicationContext()).equals(LocalDate.now())) {
                // We already have the latest exchange list
                DownloadIntentService.start(this);
            }

            if (!Preferences.loadBoolean(getApplicationContext(), Preferences.CONVERTED_CSV_TO_SQL, false)) {
                ConvertCsvToSqlService.start(this);
            }
        }
    }

    //TODO delete this
    public void exportFile() {
        String path = getExternalFilesDir(null).getPath() + "/" + DownloadIntentService.TECAJNA_LISTA_FILE;
        File file = new File(path);


        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                Timber.d(line);
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
