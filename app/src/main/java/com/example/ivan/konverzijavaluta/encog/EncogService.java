package com.example.ivan.konverzijavaluta.encog;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;

import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.entitet.Valute;
import com.example.ivan.konverzijavaluta.repository.DanRepository;
import com.example.ivan.konverzijavaluta.repository.DrzavaRepository;
import com.example.ivan.konverzijavaluta.repository.TecajnaListaPredictedRepository;
import com.example.ivan.konverzijavaluta.service.DownloadIntentService;
import com.example.ivan.konverzijavaluta.service.SaveCsvFileToSqlService;
import com.example.ivan.konverzijavaluta.ui.PredictedDataActivity;
import com.example.ivan.konverzijavaluta.util.FileUtils;
import com.example.ivan.konverzijavaluta.util.Preferences;

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

import timber.log.Timber;

/**
 * Created by ivan on 5/23/2016.
 */
public class EncogService extends IntentService {

    public static final String SERVICE_PATH = "com.example.ivan.konverzijavaluta.encog.EncogService";

    public static final int    WINDOW_SIZE               = 31;
    public static final String EXCHANGE_LIST_ACTUAL_TEMP = "exchangelistactualtemp.csv";
    public static final String PREDICTING_VALUTA         = "predicting_valuta";

    private String m_valuta;
    private String m_valutaLowerCase;

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

            Pair<MLRegression, NormalizationHelper> pair = trainData();

            File actual = getActualExchangeListPredictedLines();
            if (actual == null) return; //Shouldn't ever happen

            predictData(pair.first, pair.second, actual);

            // Delete data file and shut down.
            actual.delete();
            Encog.getInstance().shutdown();
            sendBroadcast(DownloadIntentService.DOWNLOAD_FINISHED);
        } catch (Exception ex) {
            Timber.e(ex, "Encog failed. " + ex.getMessage());
            sendBroadcast(DownloadIntentService.DOWNLOAD_FAILED);
        }

    }

    private Pair<MLRegression, NormalizationHelper> trainData() {
        ErrorCalculation.setMode(ErrorCalculationMode.RMS);
        String path = getExternalFilesDir(null).getPath() + "/" + "trening" + m_valutaLowerCase + ".csv";
        File filename = new File(path);

        // Define the format of the data file (CSV format)
        CSVFormat format = new CSVFormat();
        VersatileDataSource source = new CSVDataSource(filename, true, format);

        VersatileMLDataSet data = new VersatileMLDataSet(source);
        data.getNormHelper().setFormat(format);

        // Define how missing values are represented.
        data.getNormHelper().defineUnknownValue("?");


        ColumnDefinition column = data.defineSourceColumn(m_valuta, ColumnType.continuous);
        data.getNormHelper().defineMissingHandler(column, new MeanMissingHandler());

        // Analyze the data, determine the min/max/mean/sd of every column.
        data.analyze();

        data.defineInput(column);
        data.defineOutput(column);

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
        String[] line = new String[1];

        // Create a vector to hold each time-slice, as we build them.
        // These will be grouped together into windows.
        double[] slice = new double[1];
        VectorWindow window = new VectorWindow(WINDOW_SIZE);
        MLData input = p_helper.allocateInputVector(WINDOW_SIZE);

        while (csv.next()) {
            line[0] = csv.get(Valute.valueOf(m_valuta).ordinal() + 1);
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
            String pathActual = externalStoragePath + "/" + DownloadIntentService.EXCHANGE_LIST_ACTUAL;
            File tempFile = new File(path);
            File actualFile = new File(pathActual);

            if (!tempFile.exists()) {
                tempFile.getParentFile().mkdirs();
            }

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
}