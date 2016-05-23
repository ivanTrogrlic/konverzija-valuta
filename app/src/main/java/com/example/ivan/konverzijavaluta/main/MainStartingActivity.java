package com.example.ivan.konverzijavaluta.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.service.ConvertCsvToSqlService;

public class MainStartingActivity extends AppCompatActivity {

    public static Context     context;
    static        ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staring_activity);
        progressBar = (ProgressBar) findViewById(R.id.toolbar_progress_bar);

        context = this;

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        progressBar.setVisibility(View.INVISIBLE);

        if (savedInstanceState == null) {
            DateChooseFragment firstFragment = new DateChooseFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }

        ConvertCsvToSqlService.start(this);
//        TecajnaListaRepository tecajnaListaRepository = new TecajnaListaRepository(getContentResolver());
//        TecajnaLista byId = tecajnaListaRepository.getById(1l);
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
