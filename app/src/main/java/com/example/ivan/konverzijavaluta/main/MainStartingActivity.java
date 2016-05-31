package com.example.ivan.konverzijavaluta.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.service.DownloadIntentService;
import com.example.ivan.konverzijavaluta.service.SaveCsvFileToSqlService;
import com.example.ivan.konverzijavaluta.ui.GraphActivity;
import com.example.ivan.konverzijavaluta.ui.PastDataActivity;
import com.example.ivan.konverzijavaluta.ui.PredictedDataActivity;
import com.example.ivan.konverzijavaluta.util.Preferences;
import com.example.ivan.konverzijavaluta.util.ServiceUtils;

import org.joda.time.LocalDate;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainStartingActivity extends AppCompatActivity {

    @InjectView(R.id.toolbar)              Toolbar     m_toolbar;
    @InjectView(R.id.toolbar_progress_bar) ProgressBar m_progressBar;

    private DownloadReceiver m_downloadReceiver;
    private IntentFilter     m_filter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staring_activity);

        ButterKnife.inject(this);

        setupMenu();

        m_filter = new IntentFilter(DownloadReceiver.ACTION_RESP);
        m_filter.addCategory(Intent.CATEGORY_DEFAULT);
        m_downloadReceiver = new DownloadReceiver();
        registerReceiver(m_downloadReceiver, m_filter);

        if (savedInstanceState == null) {
            if (!Preferences.getLastDownloadDate(getApplicationContext()).equals(LocalDate.now())) {
                DownloadIntentService.start(this);
                m_progressBar.setVisibility(View.VISIBLE);
            }

            if (!Preferences.loadBoolean(getApplicationContext(), Preferences.CONVERTED_CSV_TO_SQL, false)) {
                SaveCsvFileToSqlService.start(this);
            }
        }
    }

    private void setupMenu() {
        setSupportActionBar(m_toolbar);
        m_toolbar.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_refresh) {
                    if (!ServiceUtils.isMyServiceRunning(MainStartingActivity.this,
                                                         DownloadIntentService.SERVICE_PATH)) {
                        m_progressBar.setVisibility(View.VISIBLE);
                        DownloadIntentService.start(MainStartingActivity.this);
                        item.setVisible(false);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(m_downloadReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(m_downloadReceiver, m_filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.calculator)
    public void openCalculator() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.past_data)
    public void openPastData() {
        if (checkIfHasLatestData()) return;

        Intent intent = new Intent(this, PastDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.predicted_data)
    public void openPredictedData() {
        if (checkIfHasLatestData()) return;

        Intent intent = new Intent(this, PredictedDataActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.graph_data)
    public void openGraphData() {
        if (checkIfHasLatestData()) return;

        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    private boolean checkIfHasLatestData() {
        if (!Preferences.getLastDownloadDate(getApplicationContext()).equals(LocalDate.now())) {
            if (!ServiceUtils.isMyServiceRunning(this, DownloadIntentService.SERVICE_PATH)) {
                m_progressBar.setVisibility(View.VISIBLE);
                DownloadIntentService.start(this);
            }

            Toast.makeText(this, R.string.need_latest_data, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public class DownloadReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP = "com.example.ivan.konverzijavaluta.DOWNLOAD_SERVICE";

        @Override
        public void onReceive(Context context, Intent intent) {
            m_progressBar.setVisibility(View.INVISIBLE);
            m_toolbar.getMenu().findItem(R.id.action_refresh).setVisible(true);
        }

    }
}
