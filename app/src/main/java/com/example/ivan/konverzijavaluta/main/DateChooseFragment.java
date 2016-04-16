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
        if (isMyServiceRunning()) {
            MainStartingActivity.progressBar.setVisibility(View.VISIBLE);
        } else {
            startDownloadService();
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
//        String ulaznaValuta = ulaznaValutaSpinner.getSelectedItem().toString();
//        String izlaznaValuta = izlaznaValutaSpinner.getSelectedItem().toString();
//        String dan = spinnerDan.getSelectedItem().toString();
//        String mjesec = spinnerMjesec.getSelectedItem().toString();
//        String godina = spinnerGodina.getSelectedItem().toString();
//        Double unos, rjesenjeSrednjiTecaj, rjesenjeKupovniTecaj, rjesenjeProdajniTecaj;
//        Double kupovniUlaznaValuta = 0d;
//        Double srednjiUlaznaValuta = 0d;
//        Double prodajniUlaznaValuta = 0d;
//        Double kupovniIzlaznaaValuta = 0d;
//        Double srednjiIzlaznaValuta = 0d;
//        Double prodajniIzlaznaValuta = 0d;
//        Double jedinicaUlazneValute = 0d;
//        Double jedinicaIzlazneValute = 0d;
//        String ispisSrednji, ispisKupovni, ispisProdajni, datum;
//        boolean postoji = false;
//
//        NumberFormat formater = NumberFormat.getInstance();
//        formater.setMinimumFractionDigits(2);
//        formater.setMaximumFractionDigits(2);
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
//        if (dan.length() == 1) {
//            dan = "0" + dan;
//        }
//        if (mjesec.length() == 1) {
//            mjesec = "0" + mjesec;
//        }
//
//        datum = dan + mjesec + godina;
//
//        Cursor cursor = getActivity().getContentResolver().query(uri, projection, "datum=?", new String[]{datum}, null);
//
//        if (cursor != null) {
//
//            boolean noData = false;
//            cursor.moveToFirst();
//            if (cursor.getCount() > 0) {
//                noData = cursor.getString(
//                        cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA)).equals(
//                        DownloadIntentService.NO_DATA);
//            }
//
//            //ako tečajna lista postoju u bazi za odabrani datum
//            if (cursor.getCount() > 0 && !noData && !(Integer.parseInt(mjesec) < 10 && Integer.parseInt(
//                    godina) == 1997)) {
//                cursor.close();
//                Cursor cursorNew = getActivity().getContentResolver().query(uri, projection, "datum=?",
//                                                                            new String[]{datum}, null);
//                while (cursorNew.moveToNext()) {
//
//                    String zemlja = cursorNew.getString(
//                            cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA));
//
//                    if (zemlja.substring(3, 6).equals(ulaznaValuta)) {
//
//                        kupovniUlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                         "."));
//                        srednjiUlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                         "."));
//                        prodajniUlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                          "."));
//                        jedinicaUlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                        Timber.i("ZEMLJA", zemlja);
//                        Timber.i("ID", cursorNew.getString(0));
//
//                    } else if (zemlja.substring(3, 6).equals(izlaznaValuta)) {
//
//                        kupovniIzlaznaaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                         "."));
//                        srednjiIzlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                         "."));
//                        prodajniIzlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                          "."));
//                        jedinicaIzlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                        Timber.i("ZEMLJA", zemlja);
//                        Timber.i("ID", cursorNew.getString(0));
//
//                    }
//                }
//                showData.setText("Datum tečajne liste: " + dan + ". " + mjesec + ". " + godina + ".");
//                cursorNew.close();
//
//                postoji = valutaPostoji(datum);
//
//                //ako za odabrani datum lista nije jos skinuta (ili ne postoji jer je odabran npr. sutrasnji datum)
//                //koristi se zadnja skinuta tečajna lista
//            } else if (cursor.getCount() == 0 && !(Integer.parseInt(mjesec) < 10 && Integer.parseInt(godina) == 1997)) {
//
//                String lastDate = "";
//
//                cursor.close();
//                Cursor cursorNew = getActivity().getContentResolver().query(uri, projection, null, null, null);
//
//                for (cursorNew.moveToLast(); !cursorNew.isBeforeFirst(); cursorNew.moveToPrevious()) {
//                    if (!cursorNew.getString(
//                            cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA)).equals(
//                            DownloadIntentService.NO_DATA)) {
//                        lastDate = cursorNew.getString(cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_DATE));
//                        break;
//                    }
//                }
//                cursorNew.close();
//
//                Cursor cursorLastDate = getActivity().getContentResolver().query(uri, projection, "datum=?",
//                                                                                 new String[]{lastDate}, null);
//
//                if (cursorLastDate != null) {
//                    while (cursorLastDate.moveToNext()) {
//
//                        String zemlja = cursorLastDate.getString(
//                                cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA));
//
//                        if (zemlja.substring(3, 6).equals(ulaznaValuta)) {
//
//                            kupovniUlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                          "."));
//                            srednjiUlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                          "."));
//                            prodajniUlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                           "."));
//                            jedinicaUlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                            Timber.i("ZEMLJA", zemlja);
//                            Timber.i("ID", cursorLastDate.getString(0));
//
//                        } else if (zemlja.substring(3, 6).equals(izlaznaValuta)) {
//
//                            kupovniIzlaznaaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                          "."));
//                            srednjiIzlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                          "."));
//                            prodajniIzlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                           "."));
//                            jedinicaIzlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                            Timber.i("ZEMLJA", zemlja);
//                            Timber.i("ID", cursorLastDate.getString(0));
//
//                        }
//                    }
//                    cursorLastDate.close();
//                }
//                showData.setText("Tečajna lista za taj dan još nije skinuta. Koristim zadnju dostupnu listu:" + "\n"
//                                         + lastDate.substring(0, 2) + ". " + lastDate.substring(2,
//                                                                                                4) + ". " + lastDate.substring(
//                        4, 8) + ".");
//
//                postoji = valutaPostoji(lastDate);
//
//                //ako odabrani datum postoji u bazi, ali nema tačejne liste za taj dan,
//                //onda se koristi prva dostupna tečajna lista prije tog datuma
//            } else if (!(Integer.parseInt(mjesec) < 10 && Integer.parseInt(godina) == 1997)) {
//
//                String date = "";
//                int position = 0;
//                String id;
//
//                cursor.moveToFirst();
//                id = cursor.getString(cursor.getColumnIndexOrThrow(KonverzijaDatabase.ID));
//                cursor.close();
//
//                Cursor cursorNew = getActivity().getContentResolver().query(uri, projection, null, null, null);
//
//                while (cursorNew.moveToNext()) {
//
//                    String idNew = cursorNew.getString(cursor.getColumnIndexOrThrow(KonverzijaDatabase.ID));
//                    if (idNew.equals(id)) {
//                        position = cursorNew.getPosition();
//                    }
//
//                }
//
//                for (cursorNew.moveToPosition(position); !cursorNew.isBeforeFirst(); cursorNew.moveToPrevious()) {
//                    if (!cursorNew.getString(
//                            cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA)).equals(
//                            DownloadIntentService.NO_DATA)) {
//                        date = cursorNew.getString(cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_DATE));
//                        break;
//                    }
//                }
//                cursorNew.close();
//
//                Cursor cursorLastDate = getActivity().getContentResolver().query(uri, projection, "datum=?",
//                                                                                 new String[]{date}, null);
//
//                if (cursorLastDate != null) {
//                    while (cursorLastDate.moveToNext()) {
//
//                        String zemlja = cursorLastDate.getString(
//                                cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA));
//
//                        if (zemlja.substring(3, 6).equals(ulaznaValuta)) {
//
//                            kupovniUlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                          "."));
//                            srednjiUlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                          "."));
//                            prodajniUlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                           "."));
//                            jedinicaUlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                            Timber.i("ZEMLJA", zemlja);
//                            Timber.i("ID", cursorLastDate.getString(0));
//                            Log.i("datum", String.valueOf(
//                                    cursorLastDate.getColumnIndexOrThrow(KonverzijaDatabase.COL_DATE)));
//
//                        } else if (zemlja.substring(3, 6).equals(izlaznaValuta)) {
//
//                            kupovniIzlaznaaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                          "."));
//                            srednjiIzlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                          "."));
//                            prodajniIzlaznaValuta = Double.parseDouble(cursorLastDate.getString(
//                                    cursor.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                           "."));
//                            jedinicaIzlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                            Timber.i("ZEMLJA", zemlja);
//                            Timber.i("ID", cursorLastDate.getString(0));
//                            Log.i("datum", String.valueOf(
//                                    cursorLastDate.getColumnIndexOrThrow(KonverzijaDatabase.COL_DATE)));
//
//                        }
//                    }
//                    cursorLastDate.close();
//                }
//                showData.setText(
//                        "Nema tečajne liste za taj dan, koristim najbližu tečajnu listu prije odabranog datuma." + "\n"
//                                + date.substring(0, 2) + ". " + date.substring(2, 4) + ". " + date.substring(4,
//                                                                                                             8) + ".");
//
//                postoji = valutaPostoji(date);
//
//                //ako je odabran datum za koji tečajne liste ne postoje
//                //(ako je odabran datum prije 1.10.1997.), koristi se prva dostupna lista, tj. 1.10.1997.
//            } else if (Integer.parseInt(mjesec) < 10 && Integer.parseInt(godina) == 1997) {
//
//                Cursor cursorNew = getActivity().getContentResolver().query(uri, projection, "datum=?",
//                                                                            new String[]{"01101997"}, null);
//                while (cursorNew.moveToNext()) {
//
//                    String zemlja = cursorNew.getString(
//                            cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_ZEMLJA_VALUTA));
//
//                    if (zemlja.substring(3, 6).equals(ulaznaValuta)) {
//
//                        kupovniUlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                         "."));
//                        srednjiUlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                         "."));
//                        prodajniUlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                          "."));
//                        jedinicaUlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                        Timber.i("ZEMLJA", zemlja);
//                        Timber.i("ID", cursorNew.getString(0));
//
//                    } else if (zemlja.substring(3, 6).equals(izlaznaValuta)) {
//
//                        kupovniIzlaznaaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_KUPOVNI)).replace(",",
//                                                                                                         "."));
//                        srednjiIzlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_SREDNJI)).replace(",",
//                                                                                                         "."));
//                        prodajniIzlaznaValuta = Double.parseDouble(cursorNew.getString(
//                                cursorNew.getColumnIndexOrThrow(KonverzijaDatabase.COL_PRODAJNI)).replace(",",
//                                                                                                          "."));
//                        jedinicaIzlazneValute = Double.parseDouble(zemlja.substring(6, 9));
//                        Timber.i("ZEMLJA", zemlja);
//                        Timber.i("ID", cursorNew.getString(0));
//
//                    }
//                }
//                cursorNew.close();
//                showData.setText("Nema tečejne liste za taj dan, koristim prvu dostupnu listu:" + "\n"
//                                         + "01" + ". " + "10" + ". " + "1997" + ".");
//                postoji = valutaPostoji("01101997");
//
//            }
//
//            if (ulaznaVrijednost.getText().toString().equals("")) {
//
//                ulaznaVrijednost.setError("Unesite željeni iznos");
//
//            } else if (postoji) {
//
//                izlaznaVrijednostKupovna.setText("");
//                izlaznaVrijednostKupovna.setTextSize(0);
//                izlaznaVrijednostSrednja.setText("Za taj dan nema podataka za tu valutu!");
//                izlaznaVrijednostProdajna.setText("");
//                izlaznaVrijednostProdajna.setTextSize(0);
//
//            } else {
//
//                izlaznaVrijednostKupovna.setTextSize(20);
//                izlaznaVrijednostProdajna.setTextSize(20);
//
//                unos = Double.parseDouble(ulaznaVrijednost.getText().toString());
//
//                //ispis ako je ulazna valuta HRK a izlazna nije
//                if (ulaznaValuta.equals(HRK) && !izlaznaValuta.equals(HRK)) {
//
//                    rjesenjeSrednjiTecaj = unos / (srednjiIzlaznaValuta / jedinicaIzlazneValute);
//                    ispisSrednji = formater.format(rjesenjeSrednjiTecaj) + " " + izlaznaValuta;
//
//                    if (!kupovniIzlaznaaValuta.toString().equals("0.0")) {
//                        rjesenjeKupovniTecaj = unos / (kupovniIzlaznaaValuta / jedinicaIzlazneValute);
//                        ispisKupovni = formater.format(rjesenjeKupovniTecaj) + " " + izlaznaValuta;
//                    } else {
//                        ispisKupovni = ispisSrednji;
//                    }
//
//                    if (!kupovniIzlaznaaValuta.toString().equals("0.0")) {
//                        rjesenjeProdajniTecaj = unos / (prodajniIzlaznaValuta / jedinicaIzlazneValute);
//                        ispisProdajni = formater.format(rjesenjeProdajniTecaj) + " " + izlaznaValuta;
//                    } else {
//                        ispisProdajni = ispisSrednji;
//                    }
//
//                    izlaznaVrijednostKupovna.setText("Kupovni tečaj: " + ispisKupovni);
//                    izlaznaVrijednostSrednja.setText("Srednji tečaj: " + ispisSrednji);
//                    izlaznaVrijednostProdajna.setText("Prodajni tečaj: " + ispisProdajni);
//
//                    //ispis ako je izlazna valuta HRK a ulazna nije
//                } else if (izlaznaValuta.equals(HRK) && !ulaznaValuta.equals(HRK)) {
//
//                    rjesenjeSrednjiTecaj = unos * (srednjiUlaznaValuta / jedinicaUlazneValute);
//                    ispisSrednji = formater.format(rjesenjeSrednjiTecaj) + " " + izlaznaValuta;
//
//                    if (!kupovniUlaznaValuta.toString().equals("0.0")) {
//                        rjesenjeKupovniTecaj = unos * (kupovniUlaznaValuta / jedinicaUlazneValute);
//                        ispisKupovni = formater.format(rjesenjeKupovniTecaj) + " " + izlaznaValuta;
//                    } else {
//                        ispisKupovni = ispisSrednji;
//                    }
//
//                    if (!prodajniUlaznaValuta.toString().equals("0.0")) {
//                        rjesenjeProdajniTecaj = unos * (prodajniUlaznaValuta / jedinicaUlazneValute);
//                        ispisProdajni = formater.format(rjesenjeProdajniTecaj) + " " + izlaznaValuta;
//                    } else {
//                        ispisProdajni = ispisSrednji;
//                    }
//
//                    izlaznaVrijednostKupovna.setText("Kupovni tečaj: " + ispisKupovni);
//                    izlaznaVrijednostSrednja.setText("Srednji tečaj: " + ispisSrednji);
//                    izlaznaVrijednostProdajna.setText("Prodajni tečaj: " + ispisProdajni);
//
//
//                    //ispis ako ni ulazna ni izlazna valuta nisu HRK
//                } else if (!ulaznaValuta.equals(HRK) && !izlaznaValuta.equals(HRK) && !ulaznaValuta.equals(
//                        izlaznaValuta)) {
//
//                    rjesenjeSrednjiTecaj = unos * (srednjiUlaznaValuta / jedinicaUlazneValute) / (srednjiIzlaznaValuta / jedinicaIzlazneValute);
//                    ispisSrednji = formater.format(rjesenjeSrednjiTecaj) + " " + izlaznaValuta;
//
//                    if (!kupovniUlaznaValuta.toString().equals("0.0") && !kupovniIzlaznaaValuta.toString().equals(
//                            "0.0")) {
//                        rjesenjeKupovniTecaj = unos * (kupovniUlaznaValuta / jedinicaUlazneValute) / (kupovniIzlaznaaValuta / jedinicaIzlazneValute);
//                        ispisKupovni = formater.format(rjesenjeKupovniTecaj) + " " + izlaznaValuta;
//                    } else {
//                        ispisKupovni = ispisSrednji;
//                    }
//
//                    if (!prodajniUlaznaValuta.toString().equals("0.0") && !prodajniIzlaznaValuta.toString().equals(
//                            "0.0")) {
//                        rjesenjeProdajniTecaj = unos * (prodajniUlaznaValuta / jedinicaUlazneValute) / (prodajniIzlaznaValuta / jedinicaIzlazneValute);
//                        ispisProdajni = formater.format(rjesenjeProdajniTecaj) + " " + izlaznaValuta;
//                    } else {
//                        ispisProdajni = ispisSrednji;
//                    }
//
//                    izlaznaVrijednostKupovna.setText("Kupovni tečaj: " + ispisKupovni);
//                    izlaznaVrijednostSrednja.setText("Srednji tečaj: " + ispisSrednji);
//                    izlaznaVrijednostProdajna.setText("Prodajni tečaj: " + ispisProdajni);
//
//                    //ispis ako su obje valuta iste
//                } else {
//
//                    ispisSrednji = unos + " " + izlaznaValuta;
//
//                    izlaznaVrijednostKupovna.setText("Kupovni tečaj: " + ispisSrednji);
//                    izlaznaVrijednostSrednja.setText("Srednji tečaj: " + ispisSrednji);
//                    izlaznaVrijednostProdajna.setText("Prodajni tečaj: " + ispisSrednji);
//
//                }
//            }
//        }
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

    public static boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) MainStartingActivity.context.getSystemService(
                Context.ACTIVITY_SERVICE);
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

                if (!isMyServiceRunning()) {
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
