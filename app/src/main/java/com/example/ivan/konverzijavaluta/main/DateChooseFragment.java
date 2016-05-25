package com.example.ivan.konverzijavaluta.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.appcompat.BuildConfig;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivan.konverzijavaluta.R;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase;
import com.example.ivan.konverzijavaluta.service.DownloadIntentService;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

public class DateChooseFragment extends Fragment {

    @InjectView(R.id.tvShowData)                  TextView showData;
    @InjectView(R.id.tvIzlaznaVrijednost)         TextView izlaznaVrijednostSrednja;
    @InjectView(R.id.tvIzlaznaVrijednostKupovna)  TextView izlaznaVrijednostKupovna;
    @InjectView(R.id.tvIzlaznaVrijednostProdajna) TextView izlaznaVrijednostProdajna;
    @InjectView(R.id.etUlaznaVrijednost)          EditText ulaznaVrijednost;
    @InjectView(R.id.sUlaznaValuta)               Spinner  ulaznaValutaSpinner;
    @InjectView(R.id.sIzlaznaValuta)              Spinner  izlaznaValutaSpinner;
    @InjectView(R.id.sDan)                        Spinner  spinnerDan;
    @InjectView(R.id.sMjesec)                     Spinner  spinnerMjesec;
    @InjectView(R.id.sGodina)                     Spinner  spinnerGodina;

    public static final String SHOW_DATA = "show data";
    public static final String SREDNJA   = "srednja";
    public static final String KUPOVNA   = "kupovna";
    public static final String PRODAJNA  = "prodajna";
    public static final String HRK       = "HRK";
    public static KonverzijaDatabase db;
    private boolean isTaskRunning = false;
    private ResponseReceiver receiver;
    IntentFilter filter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.konverzija_valuta, container, false);
        ButterKnife.inject(this, view);

        if (savedInstanceState != null) {
            showData.setText(savedInstanceState.getString(SHOW_DATA));
            izlaznaVrijednostProdajna.setText(savedInstanceState.getString(PRODAJNA));
            izlaznaVrijednostSrednja.setText(savedInstanceState.getString(SREDNJA));
            izlaznaVrijednostKupovna.setText(savedInstanceState.getString(KUPOVNA));
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        db = new KonverzijaDatabase(getActivity());

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new ResponseReceiver();
        getActivity().registerReceiver(receiver, filter);

    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, filter);
        if (isMyServiceRunning(getActivity())) {
            MainStartingActivity.progressBar.setVisibility(View.VISIBLE);
        } else {
//            startDownloadService();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isTaskRunning) {
            MainStartingActivity.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        if (MainStartingActivity.progressBar != null && MainStartingActivity.progressBar.isShown()) {
            MainStartingActivity.progressBar.setVisibility(View.INVISIBLE);
        }

        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SHOW_DATA, showData.getText().toString());
        outState.putString(SREDNJA, izlaznaVrijednostSrednja.getText().toString());
        outState.putString(KUPOVNA, izlaznaVrijednostKupovna.getText().toString());
        outState.putString(PRODAJNA, izlaznaVrijednostProdajna.getText().toString());
    }

    @OnClick(R.id.bIzracunaj)
    public void submit() {
    }

    private void startDownloadService() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            if (!isTaskRunning) {

                isTaskRunning = true;

                MainStartingActivity.progressBar.setVisibility(View.VISIBLE);
                Intent msgIntent = new Intent(getActivity(), DownloadIntentService.class);
                getActivity().startService(msgIntent);

            }

        } else {
            Toast.makeText(getActivity(), "Nema internet konekcije", Toast.LENGTH_SHORT).show();
        }
    }

    public class ResponseReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP = "com.example.ivan.konverzijavaluta.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {

            MainStartingActivity.progressBar.setVisibility(View.INVISIBLE);
            loadValuteSpinnerData();
            loadGodineSpinnerData();

            isTaskRunning = false;

        }

    }

    public static boolean isMyServiceRunning(Context p_context) {
        ActivityManager manager = (ActivityManager) p_context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.ivan.konverzijavaluta.service.DownloadIntentService".equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void loadValuteSpinnerData() {

//        ArrayAdapter<String> dataAdapter;
//        List<String> lables = new ArrayList<>();
//        lables.add(HRK);
//        lables.addAll(db.getValute());
//
//        if (lables.size() > getResources().getStringArray(R.array.valute).length) {
//
//            dataAdapter = new ArrayAdapter<>(getActivity().getBaseContext(),
//                                             android.R.layout.simple_spinner_item, lables);
//
//        } else {
//
//            dataAdapter = new ArrayAdapter<>(getActivity().getBaseContext(),
//                                             android.R.layout.simple_spinner_item,
//                                             getResources().getStringArray(R.array.valute));
//
//        }
//
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        ulaznaValutaSpinner.setAdapter(dataAdapter);
//        izlaznaValutaSpinner.setAdapter(dataAdapter);

    }

    protected void loadGodineSpinnerData() {

//        ArrayAdapter<String> dataAdapter;
//        List<String> lables = new ArrayList<>();
//        lables.addAll(db.getGodine());
//
//        if (lables.size() > getResources().getStringArray(R.array.godine).length) {
//
//            dataAdapter = new ArrayAdapter<>(getActivity().getBaseContext(),
//                                             android.R.layout.simple_spinner_item, lables);
//
//        } else {
//
//            dataAdapter = new ArrayAdapter<>(getActivity().getBaseContext(),
//                                             android.R.layout.simple_spinner_item,
//                                             getResources().getStringArray(R.array.godine));
//
//        }
//
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        spinnerGodina.setAdapter(dataAdapter);

    }

    private boolean valutaPostoji(String datum) {

//        String ulaznaValuta = ulaznaValutaSpinner.getSelectedItem().toString();
//        String izlaznaValuta = izlaznaValutaSpinner.getSelectedItem().toString();
//        boolean ulaznaPostoji = false;
//        boolean izlaznaPostoji = false;
//        String valuta;
//
//        String[] projection = {
//                KonverzijaDatabase.ID,
//                KonverzijaDatabase.COL_DATE,
//                KonverzijaDatabase.COL_ZEMLJA_VALUTA,
//                KonverzijaDatabase.COL_KUPOVNI,
//                KonverzijaDatabase.COL_SREDNJI,
//                KonverzijaDatabase.COL_PRODAJNI};
//        Uri uri = Uri.parse(KonverzijaProvider.CONTENT_URI + "/" + 1);
//
//        Cursor cursor = getActivity().getContentResolver().query(uri, projection, "datum=?", new String[]{datum}, null);
//
//        if (cursor != null) {
//            cursor.moveToNext();
//            if (cursor.getCount() > 0) {
//                ulaznaPostoji = false;
//                for (cursor.moveToFirst(); cursor.getPosition() < cursor.getCount(); cursor.moveToNext()) {
//                    valuta = cursor.getString(cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA));
//                    if (valuta.substring(3, 6).equals(ulaznaValuta) || ulaznaValuta.equals("HRK")) {
//                        ulaznaPostoji = true;
//                        break;
//                    }
//                }
//                izlaznaPostoji = false;
//                for (cursor.moveToFirst(); cursor.getPosition() < cursor.getCount(); cursor.moveToNext()) {
//                    valuta = cursor.getString(cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA));
//                    if (valuta.substring(3, 6).equals(izlaznaValuta) || izlaznaValuta.equals("HRK")) {
//                        izlaznaPostoji = true;
//                        break;
//                    }
//                }
//
//            }
//            cursor.close();
//        }
//
//        return (!ulaznaPostoji || !izlaznaPostoji);
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:

                Toast.makeText(getActivity(), "Jon Snow died BibleThump", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_refresh:

                if (!isMyServiceRunning(getActivity())) {
                    ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.isConnected()) {

                        MainStartingActivity.progressBar.setVisibility(View.VISIBLE);
                        Intent msgIntent = new Intent(getActivity(), DownloadIntentService.class);
                        getActivity().startService(msgIntent);

                    } else {
                        Toast.makeText(getActivity(), "Nema internet konekcije", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Podatci se već skidaju", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
