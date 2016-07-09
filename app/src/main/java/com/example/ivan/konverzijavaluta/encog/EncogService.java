package com.example.ivan.konverzijavaluta.encog;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.entitet.WorldBankModel;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaPredictedRepository;
import com.example.ivan.konverzijavaluta.rest.EcbWebService;
import com.example.ivan.konverzijavaluta.rest.RestClient;
import com.example.ivan.konverzijavaluta.rest.WorldBankWebService;
import com.example.ivan.konverzijavaluta.service.DownloadIntentService;
import com.example.ivan.konverzijavaluta.service.SaveCsvFileToSqlService;
import com.example.ivan.konverzijavaluta.ui.PredictedDataActivity;
import com.example.ivan.konverzijavaluta.util.FileUtils;
import com.example.ivan.konverzijavaluta.util.Preferences;
import com.example.ivan.konverzijavaluta.util.ServiceUtils;

import org.encog.ConsoleStatusReportable;
import org.encog.Encog;
import org.encog.mathutil.error.ErrorCalculation;
import org.encog.mathutil.error.ErrorCalculationMode;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.missing.MeanMissingHandler;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.arrayutil.VectorWindow;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by ivan on 5/23/2016.
 */
public class EncogService extends IntentService {

    public static final String SERVICE_PATH = "com.example.ivan.konverzijavaluta.encog.EncogService";

    public static final int    WINDOW_SIZE               = 31;
    public static final String EXCHANGE_LIST_ACTUAL_TEMP = "exchangelistactualtemp.csv";
    public static final String PREDICTING_VALUTA         = "predicting_valuta";

    public static final String DEFAULT_DATE        = "2015-04-1";
    public static final String START_DATE          = "1999-01-1";
    public static final String TRAINING_START_YEAR = "1999";
    public static final String ACTUAL_START_YEAR   = "2014";
    public static final String DATE_FORMAT_MONTHLY = "yyyy-MM";
    public static final String DATE_FORMAT         = "M/d/yyyy";

    public static final String PARAM_DATE   = "date";
    public static final String PARAM_FORMAT = "format";
    public static final String JSON         = "json";

    public static final String COLUMN_DATE       = "Date";
    public static final String COLUMN_NEER       = "NEER";
    public static final String COLUMN_OIL        = "OIL";
    public static final String COLUMN_RHCI       = "RHCI";
    public static final String COLUMN_NHCI       = "NHCI";
    public static final String COLUMN_DEBT       = "DEBT";
    public static final String COLUMN_CORRUPTION = "CORRUPTION";

    private String m_valuta;
    private String m_valutaLowerCase;
    private Map<LocalDate, Double> m_neer       = new HashMap<>();
    private Map<LocalDate, Double> m_oil        = new HashMap<>();
    private Map<LocalDate, Double> m_rhci       = new HashMap<>();
    private Map<LocalDate, Double> m_nhci       = new HashMap<>();
    private Map<String, Double>    m_debt       = new HashMap<>();
    private Map<String, Double>    m_corruption = new HashMap<>();

    public EncogService() {
        super("EncogService");
    }

    public static void start(Context p_context, String p_valuta) {
        Intent msgIntent = new Intent(p_context, EncogService.class);
        msgIntent.putExtra(PREDICTING_VALUTA, p_valuta);
        p_context.startService(msgIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        m_valuta = intent.getStringExtra(PREDICTING_VALUTA);
        m_valutaLowerCase = m_valuta.toLowerCase();
        run();
    }

    private void sendBroadcast(String p_response) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(PredictedDataActivity.EncogReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(DownloadIntentService.DOWNLOAD_RESPONSE, p_response);
        sendBroadcast(broadcastIntent);
    }

    private void run() {
        try {
            if (!Preferences.loadBoolean(getApplicationContext(), m_valutaLowerCase + "_saved", false)) {
                FileUtils.copyFileToExternalDirectory(getApplicationContext(), "trening" + m_valutaLowerCase + ".csv",
                                                      "trening" + m_valutaLowerCase + ".csv");
                Preferences.saveBoolean(getApplicationContext(), m_valutaLowerCase + "_saved", true);
            }

            downloadTrainingData();
        } catch (Exception ex) {
            Timber.e(ex, "Encog failed. " + ex.getMessage());
            sendBroadcast(DownloadIntentService.DOWNLOAD_FAILED);
        }

    }

    private Pair<MLRegression, NormalizationHelper> trainData() {
        ErrorCalculation.setMode(ErrorCalculationMode.RMS);
        String path = getExternalFilesDir(null).getPath() + "/" + "fulltrening" + m_valutaLowerCase + ".csv";
        File filename = new File(path);

        // Define the format of the data file (CSV format)
        CSVFormat format = new CSVFormat();
        VersatileDataSource source = new CSVDataSource(filename, true, format);

        VersatileMLDataSet data = new VersatileMLDataSet(source);
        data.getNormHelper().setFormat(format);

        // Define how missing values are represented.
        data.getNormHelper().defineUnknownValue("?");

        ColumnDefinition value = data.defineSourceColumn(m_valuta, ColumnType.continuous);
        ColumnDefinition neer = data.defineSourceColumn(COLUMN_NEER, ColumnType.continuous);
        ColumnDefinition oil = data.defineSourceColumn(COLUMN_OIL, ColumnType.continuous);
        ColumnDefinition rhci = data.defineSourceColumn(COLUMN_RHCI, ColumnType.continuous);
        ColumnDefinition nhci = data.defineSourceColumn(COLUMN_NHCI, ColumnType.continuous);
        ColumnDefinition debt = data.defineSourceColumn(COLUMN_DEBT, ColumnType.continuous);
        ColumnDefinition corruption = data.defineSourceColumn(COLUMN_CORRUPTION, ColumnType.continuous);

        data.getNormHelper().defineMissingHandler(value, new MeanMissingHandler());
        data.getNormHelper().defineMissingHandler(neer, new MeanMissingHandler());
        data.getNormHelper().defineMissingHandler(oil, new MeanMissingHandler());
        data.getNormHelper().defineMissingHandler(rhci, new MeanMissingHandler());
        data.getNormHelper().defineMissingHandler(nhci, new MeanMissingHandler());
        data.getNormHelper().defineMissingHandler(debt, new MeanMissingHandler());
        data.getNormHelper().defineMissingHandler(corruption, new MeanMissingHandler());

        // Analyze the data, determine the min/max/mean/sd of every column.
        data.analyze();

        data.defineInput(value);
        data.defineInput(neer);
        data.defineInput(oil);
        data.defineInput(rhci);
        data.defineInput(nhci);
        data.defineInput(debt);
        data.defineInput(corruption);

        data.defineOutput(value);

        // Create feedforward neural network as the model type.
        // MLMethodFactory.TYPE_FEEDFORWARD.
        // You could also other model types, such as:
        // MLMethodFactory.SVM: Support Vector Machine (SVM)
        // MLMethodFactory.TYPE_RBFNETWORK: RBF Neural Network
        // MLMethodFactor.TYPE_NEAT: NEAT Neural Network
        // MLMethodFactor.TYPE_PNN: Probabilistic Neural Network
        EncogModel model = new EncogModel(data);
        model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);

        // Send any output to the console.
        model.setReport(new ConsoleStatusReportable());

        // Now normalize the data. Encog will automatically determine the correct normalization
        // type based on the model you set in the last step.
        data.normalize();

        // Set time series.
        data.setLeadWindowSize(WINDOW_SIZE);
        data.setLagWindowSize(WINDOW_SIZE);

        // Hold back some data for a final validation.
        // Do not shuffle the data into a random ordering. (never shuffle time series)
        // Use a seed of 1001 so that we always use the same holdback and
        // will get more consistent results.
        model.holdBackValidation(0.3, false, 1001);

        // Choose whatever is the default training type for this model.
        model.selectTrainingType(data);

        // Use a 5-fold cross-validated train.
        // Return the best method found. (never shuffle time series)
        MLRegression bestMethod = (MLRegression) model.crossvalidate(5, false);

        // Display the training and validation errors.
        Timber.d("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
        Timber.d("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));

        // Display our normalization parameters.
        NormalizationHelper helper = data.getNormHelper();
        Timber.d(helper.toString());

        // Display the final model.
        Timber.d("Final model: " + bestMethod);

        return new Pair<>(bestMethod, helper);
    }

    private void predictData(MLRegression p_bestMethod, NormalizationHelper p_helper, File p_actual) {
        ReadCSV csv = new ReadCSV(p_actual, true, new CSVFormat());
        String[] line = new String[7];

        // Create a vector to hold each time-slice, as we build them.
        // These will be grouped together into windows.
        double[] slice = new double[7];
        VectorWindow window = new VectorWindow(WINDOW_SIZE);
        MLData input = p_helper.allocateInputVector(WINDOW_SIZE);

        while (csv.next()) {
            line[0] = csv.get(1);
            line[1] = csv.get(2);
            line[2] = csv.get(3);
            line[3] = csv.get(4);
            line[4] = csv.get(5);
            line[5] = csv.get(6);
            line[6] = csv.get(7);
            p_helper.normalizeInputVector(line, slice, false);

            // enough data to build a full window?
            if (window.isReady()) {
                window.copyWindow(input.getData(), 0);
                String date = csv.get(0);

                MLData output = p_bestMethod.compute(input);
                String predicted = p_helper.denormalizeOutputVectorToString(output)[0];

                if (date.equals("")) continue;
                LocalDate parsedDate = LocalDate.parse(date,
                                                       DateTimeFormat.forPattern(SaveCsvFileToSqlService.DATE_FORMAT));
                if (parsedDate.isAfter(LocalDate.now())) {
                    Timber.d(parsedDate.toString());
                    insertTecajnaListaPredicted(parsedDate, predicted);
                }
            }

            // Add the normalized slice to the window. We do this just after
            // the after checking to see if the window is ready so that the
            // window is always one behind the current row. This is because
            // we are trying to predict next row.
            window.add(slice);
        }
    }

    private void insertTecajnaListaPredicted(LocalDate p_parsedDate, String p_predicted) {
        ContentResolver resolver = getContentResolver();
        DrzavaRepository drzavaRepository = new DrzavaRepository(resolver);
        DanRepository danRepository = new DanRepository(resolver);
        TecajnaListaPredictedRepository tecajnaListaPredictedRepository = new TecajnaListaPredictedRepository(
                resolver);

        Dan dan = danRepository.getByDate(p_parsedDate);
        if (dan == null) {
            dan = new Dan();
            dan.setDan(p_parsedDate);
            dan.setId(danRepository.insert(dan));
        }

        Drzava drzava = drzavaRepository.getByValuta(m_valuta);

        TecajnaListaPredicted tecajnaListaPredicted = new TecajnaListaPredicted();
        tecajnaListaPredicted.setDan(dan);
        tecajnaListaPredicted.setDrzava(drzava);
        BigDecimal tecaj = BigDecimal.valueOf(Double.valueOf(p_predicted));
        tecajnaListaPredicted.setKupovniTecaj(tecaj);
        tecajnaListaPredicted.setSrednjiTecaj(tecaj);
        tecajnaListaPredicted.setProdajniTecaj(tecaj);
        tecajnaListaPredictedRepository.insert(tecajnaListaPredicted);
    }

    private File getActualExchangeListPredictedLines() {
        BufferedReader reader;
        BufferedWriter writer;
        try {
            String externalStoragePath = getExternalFilesDir(null).getPath();
            String path = externalStoragePath + "/" + EXCHANGE_LIST_ACTUAL_TEMP;
            String pathActual = externalStoragePath + "/" + "fullactual" + m_valutaLowerCase + ".csv";
            File tempFile = new File(path);
            File actualFile = new File(pathActual);

            if (!tempFile.exists()) tempFile.getParentFile().mkdirs();

            writer = new BufferedWriter(new FileWriter(path));
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(actualFile)));

            StringBuilder sb = new StringBuilder();
            String line;
            String lastLine = "";

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                lastLine = line;
            }

            String date = lastLine.substring(0, lastLine.indexOf(","));
            LocalDate parsedDate = LocalDate.parse(date,
                                                   DateTimeFormat.forPattern(SaveCsvFileToSqlService.DATE_FORMAT));
            StringBuilder predictedLine = new StringBuilder(lastLine);

            // Add 31 more lines to the file, with new dates, which will be used as predicted
            for (int i = 0; i < 31; i++) {
                predictedLine.replace(0, predictedLine.indexOf(","), parsedDate.plusDays(i + 1).toString(
                        SaveCsvFileToSqlService.DATE_FORMAT));
                sb.append(predictedLine).append("\n");
            }

            String result = sb.toString();

            writer.write(result);
            writer.close();
            reader.close();
            return tempFile;
        } catch (IOException e) {
            Timber.e(e, e.getMessage());
            return null;
        }
    }

    private void startEncogService() {
        Pair<MLRegression, NormalizationHelper> pair = trainData();

        File actual = getActualExchangeListPredictedLines();
        if (actual == null) return; //Shouldn't ever happen

        predictData(pair.first, pair.second, actual);

        // Delete data file and shut down.
        actual.delete();
        Encog.getInstance().shutdown();
        sendBroadcast(DownloadIntentService.DOWNLOAD_FINISHED);
    }


    ///////////////////// DOWNLOADING TRAINING DATA /////////////////////////////////////////////
    // TODO Lots of duplicated code below, refactor!!!

    private void downloadTrainingData() {
        Map<String, String> params = getTrainingEcbParams();
        RestClient.createEcb(EcbWebService.class).getOil(params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleOilResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleOilResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT_MONTHLY));
            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);

            m_oil.put(date, value);
        }
        file.delete();

        String query = "M.H42." + m_valuta + ".NRC0.A";
        Map<String, String> params = getTrainingEcbParams();
        RestClient.createEcb(EcbWebService.class).get(query, params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleRhciResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleRhciResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT_MONTHLY));
            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);

            m_rhci.put(date, value);
        }
        file.delete();

        String query = "M.H42." + m_valuta + ".NN00.A";
        Map<String, String> params = getTrainingEcbParams();
        RestClient.createEcb(EcbWebService.class).get(query, params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleNhciResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleNhciResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT_MONTHLY));
            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);

            m_nhci.put(date, value);
        }
        file.delete();

        String query = ServiceUtils.countryForCurrency(m_valuta) + "/indicators/GFDD.DM.07";
        Map<String, String> params = getTrainingWorldParams();
        RestClient.createWorld(WorldBankWebService.class).get(query, params).enqueue(
                new Callback<List<WorldBankModel>[]>() {
                    @Override
                    public void onResponse(Call<List<WorldBankModel>[]> call,
                                           Response<List<WorldBankModel>[]> response) {
                        try {
                            handleDebtResponse(response.body()[1]);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<WorldBankModel>[]> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleDebtResponse(List<WorldBankModel> p_models) throws IOException {
        for (WorldBankModel model : p_models) m_debt.put(model.getDate(), model.getValue());

        String query = ServiceUtils.countryForCurrency(m_valuta) + "/indicators/CC.NO.SRC";
        Map<String, String> params = getTrainingWorldParams();
        RestClient.createWorld(WorldBankWebService.class).get(query, params).enqueue(
                new Callback<List<WorldBankModel>[]>() {
                    @Override
                    public void onResponse(Call<List<WorldBankModel>[]> call,
                                           Response<List<WorldBankModel>[]> response) {
                        try {
                            handleCorruptionResponse(response.body()[1]);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<WorldBankModel>[]> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleCorruptionResponse(List<WorldBankModel> p_models) throws IOException {
        for (WorldBankModel model : p_models) m_corruption.put(model.getDate(), model.getValue());

        insertTrainingDataToFile();
    }

    private void insertTrainingDataToFile() throws IOException {
        String path = getExternalFilesDir(null).getPath() + "/" + "trening" + m_valutaLowerCase + ".csv";
        File file = new File(path);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        String pathFull = getExternalFilesDir(null).getPath() + "/" + "fulltrening" + m_valutaLowerCase + ".csv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(pathFull, false));
        StringBuilder sb = new StringBuilder();

        sb.append(COLUMN_DATE).append(",")
                .append(m_valuta).append(",")
                .append(COLUMN_NEER).append(",")
                .append(COLUMN_OIL).append(",")
                .append(COLUMN_RHCI).append(",")
                .append(COLUMN_NHCI).append(",")
                .append(COLUMN_DEBT).append(",")
                .append(COLUMN_CORRUPTION).append(",");
        sb.append("\n");

        while (csv.next()) appendLineToCsv(csv, sb);

        String result = sb.toString();
        writer.write(result);
        writer.close();

        // Now download actual data
        downloadActualData();
    }

    private void appendLineToCsv(ReadCSV p_csv, StringBuilder p_sb) {
        String timePeriod = p_csv.get(COLUMN_DATE);
        LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT));

        String value = p_csv.get(m_valuta);
        String neer = p_csv.get(COLUMN_NEER);
        double oil = m_oil.get(date.withDayOfMonth(1));
        double rhci = m_rhci.get(date.withDayOfMonth(1));
        double nhci = m_nhci.get(date.withDayOfMonth(1));

        String year = String.valueOf(date.getYear());
        String debt = m_debt.containsKey(year) ? String.valueOf(m_debt.get(year)) : "?";
        debt = getYearBefore(date, debt, m_debt);

        String corruption = m_corruption.containsKey(year) ? String.valueOf(m_corruption.get(year)) : "?";
        corruption = getYearBefore(date, corruption, m_corruption);

        p_sb.append(timePeriod).append(",")
                .append(value).append(",")
                .append(neer).append(",")
                .append(oil).append(",")
                .append(rhci).append(",")
                .append(nhci).append(",")
                .append(debt).append(",")
                .append(corruption).append(",");

        p_sb.append("\n");
    }

    private String getYearBefore(LocalDate p_date, String p_value, Map<String, Double> p_map) {
        for (int i = 0; i < 3; i++) {
            String yearBefore = String.valueOf(p_date.getYear() - i);
            if (p_map.containsKey(yearBefore)) {
                p_value = String.valueOf(p_map.get(yearBefore));
                break;
            }
        }
        return p_value;
    }

    @NonNull
    private Map<String, String> getTrainingEcbParams() {
        Map<String, String> params = new HashMap<>();
        LocalDate start = LocalDate.parse(START_DATE,
                                          DateTimeFormat.forPattern(DownloadIntentService.DATE_FORMAT_SQLITE));
        params.put(DownloadIntentService.START_PERIOD, start.toString(DownloadIntentService.DATE_FORMAT_SQLITE));
        params.put(DownloadIntentService.END_PERIOD,
                   LocalDate.now().toString(DownloadIntentService.DATE_FORMAT_SQLITE));
        return params;
    }

    @NonNull
    private Map<String, String> getTrainingWorldParams() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_DATE, TRAINING_START_YEAR + ":" + LocalDate.now().getYear());
        params.put(PARAM_FORMAT, JSON);
        return params;
    }


    ///////////////////// DOWNLOADING ACTUAL DATA /////////////////////////////////////////////
    // TODO Lots of duplicated code below, refactor!!!

    private void downloadActualData() {
        Map<String, String> params = getTrainingEcbParams();
        RestClient.createEcb(EcbWebService.class).getOil(params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleActualOilResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleActualOilResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT_MONTHLY));
            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);

            m_oil.put(date, value);
        }
        file.delete();

        String e5orE7 = ServiceUtils.getE5orE7(m_valuta);
        String query = "D." + e5orE7 + "." + m_valuta + ".EN00.A";
        Map<String, String> params = getActualEcbParams();
        RestClient.createEcb(EcbWebService.class).get(query, params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleNeerResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleNeerResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod,
                                             DateTimeFormat.forPattern(DownloadIntentService.DATE_FORMAT_SQLITE));

            if (csv.get(DownloadIntentService.COLUMN_OBS_VALUE).equals("")) continue;

            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);
            m_neer.put(date, value);
        }
        file.delete();

        String query = "M.H42." + m_valuta + ".NRC0.A";
        Map<String, String> params = getActualEcbParams();
        RestClient.createEcb(EcbWebService.class).get(query, params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleActualRhciResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleActualRhciResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT_MONTHLY));
            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);

            m_rhci.put(date, value);
        }
        file.delete();

        String query = "M.H42." + m_valuta + ".NN00.A";
        Map<String, String> params = getActualEcbParams();
        RestClient.createEcb(EcbWebService.class).get(query, params).enqueue(
                new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (!response.isSuccessful()) {
                            Timber.e(new Exception(), response.message());
                            return;
                        }

                        try {
                            handleActualNhciResponse(response);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleActualNhciResponse(Response<ResponseBody> p_response) throws IOException {
        File file = FileUtils.convertResponseToCsvFile(getApplicationContext(), p_response);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        while (csv.next()) {
            String timePeriod = csv.get(DownloadIntentService.COLUMN_TIME_PERIOD);
            LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT_MONTHLY));
            double value = csv.getDouble(DownloadIntentService.COLUMN_OBS_VALUE);

            m_nhci.put(date, value);
        }
        file.delete();

        String query = ServiceUtils.countryForCurrency(m_valuta) + "/indicators/GFDD.DM.07";
        Map<String, String> params = getActualWorldParams();
        RestClient.createWorld(WorldBankWebService.class).get(query, params).enqueue(
                new Callback<List<WorldBankModel>[]>() {
                    @Override
                    public void onResponse(Call<List<WorldBankModel>[]> call,
                                           Response<List<WorldBankModel>[]> response) {
                        try {
                            handleActualDebtResponse(response.body()[1]);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<WorldBankModel>[]> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleActualDebtResponse(List<WorldBankModel> p_models) throws IOException {
        for (WorldBankModel model : p_models) m_debt.put(model.getDate(), model.getValue());

        String query = ServiceUtils.countryForCurrency(m_valuta) + "/indicators/CC.NO.SRC";
        Map<String, String> params = getActualWorldParams();
        RestClient.createWorld(WorldBankWebService.class).get(query, params).enqueue(
                new Callback<List<WorldBankModel>[]>() {
                    @Override
                    public void onResponse(Call<List<WorldBankModel>[]> call,
                                           Response<List<WorldBankModel>[]> response) {
                        try {
                            handleActualCorruptionResponse(response.body()[1]);
                        } catch (IOException e) {
                            Timber.e(e, e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<WorldBankModel>[]> call, Throwable t) {
                        Timber.e(t, t.getMessage());
                    }
                });
    }

    private void handleActualCorruptionResponse(List<WorldBankModel> p_models) throws IOException {
        for (WorldBankModel model : p_models) m_corruption.put(model.getDate(), model.getValue());

        insertActualDataToFile();
    }

    private void insertActualDataToFile() throws IOException {
        String path = getExternalFilesDir(null).getPath() + "/" + DownloadIntentService.EXCHANGE_LIST_ACTUAL;
        File file = new File(path);
        ReadCSV csv = new ReadCSV(file, true, new CSVFormat());

        String pathFull = getExternalFilesDir(null).getPath() + "/" + "fullactual" + m_valutaLowerCase + ".csv";
        BufferedWriter writer = new BufferedWriter(new FileWriter(pathFull, false));
        StringBuilder sb = new StringBuilder();

        sb.append(COLUMN_DATE).append(",")
                .append(m_valuta).append(",")
                .append(COLUMN_NEER).append(",")
                .append(COLUMN_OIL).append(",")
                .append(COLUMN_RHCI).append(",")
                .append(COLUMN_NHCI).append(",")
                .append(COLUMN_DEBT).append(",")
                .append(COLUMN_CORRUPTION).append(",");
        sb.append("\n");

        while (csv.next()) appendActualLineToCsv(csv, sb);

        String result = sb.toString();
        writer.write(result);
        writer.close();

        // Now start training and predicting data
        startEncogService();
    }

    private void appendActualLineToCsv(ReadCSV p_csv, StringBuilder p_sb) {
        String timePeriod = p_csv.get(0);
        LocalDate date = LocalDate.parse(timePeriod, DateTimeFormat.forPattern(DATE_FORMAT));

        String value = p_csv.get(Valute.valueOf(m_valuta).ordinal() + 1);

        double neer = m_neer.get(date);
        for (int i = 0; i < 3; i++) {
            LocalDate dayBefore = date.minusDays(i);
            if (m_neer.containsKey(dayBefore)) {
                neer = m_neer.get(dayBefore);
                break;
            }
        }

        LocalDate dateWithStartOfMonth = date.withDayOfMonth(1);
        String oil = m_oil.containsKey(dateWithStartOfMonth) ? String.valueOf(m_oil.get(dateWithStartOfMonth)) : "?";
        oil = getMonthBefore(date, oil, m_oil);

        String rhci = m_rhci.containsKey(dateWithStartOfMonth) ? String.valueOf(m_rhci.get(dateWithStartOfMonth)) : "?";
        rhci = getMonthBefore(date, rhci, m_rhci);

        String nhci = m_nhci.containsKey(dateWithStartOfMonth) ? String.valueOf(m_nhci.get(dateWithStartOfMonth)) : "?";
        nhci = getMonthBefore(date, nhci, m_nhci);

        String year = String.valueOf(date.getYear());
        String debt = m_debt.containsKey(year) ? String.valueOf(m_debt.get(year)) : "?";
        debt = getYearBefore(date, debt, m_debt);

        String corruption = m_corruption.containsKey(year) ? String.valueOf(m_corruption.get(year)) : "?";
        corruption = getYearBefore(date, corruption, m_corruption);

        p_sb.append(timePeriod).append(",")
                .append(value).append(",")
                .append(neer).append(",")
                .append(oil).append(",")
                .append(rhci).append(",")
                .append(nhci).append(",")
                .append(debt).append(",")
                .append(corruption).append(",");

        p_sb.append("\n");
    }

    private String getMonthBefore(LocalDate p_date, String p_neer, Map<LocalDate, Double> p_data) {
        for (int i = 0; i < 24; i++) {
            LocalDate dayBefore = p_date.minusMonths(i).withDayOfMonth(1);
            if (p_data.containsKey(dayBefore)) {
                p_neer = String.valueOf(p_data.get(dayBefore));
                break;
            }
        }
        return p_neer;
    }

    @NonNull
    private Map<String, String> getActualEcbParams() {
        Map<String, String> params = new HashMap<>();
        LocalDate start = LocalDate.parse(DEFAULT_DATE,
                                          DateTimeFormat.forPattern(DownloadIntentService.DATE_FORMAT_SQLITE));
        params.put(DownloadIntentService.START_PERIOD, start.toString(DownloadIntentService.DATE_FORMAT_SQLITE));
        params.put(DownloadIntentService.END_PERIOD,
                   LocalDate.now().toString(DownloadIntentService.DATE_FORMAT_SQLITE));
        return params;
    }

    @NonNull
    private Map<String, String> getActualWorldParams() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_DATE, ACTUAL_START_YEAR + ":" + LocalDate.now().getYear());
        params.put(PARAM_FORMAT, JSON);
        return params;
    }
}