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
import com.example.ivan.konverzijavaluta.ui.HrkPredictedActivity;
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
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by ivan on 5/27/2016.
 */
public class EncogHrkService extends IntentService {

    public static final String SERVICE_PATH = "com.example.ivan.konverzijavaluta.encog.EncogHrkService";

    public static final int    WINDOW_SIZE     = 31; // Days to predict
    public static final String HRK_TRAINING    = "treninghrk.csv";
    public static final String HRK_ACTUAL      = "actualhrk.csv";
    public static final String HRK_ACTUAL_TEMP = "actualhrktemp.csv";

    public EncogHrkService() {
        super("EncogHrkService");
    }

    public static void start(Context p_context) {
        Intent msgIntent = new Intent(p_context, EncogHrkService.class);
        p_context.startService(msgIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        run();
    }

    private void sendBroadcast(String p_response) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(HrkPredictedActivity.EncogHrkReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(DownloadIntentService.DOWNLOAD_RESPONSE, p_response);
        sendBroadcast(broadcastIntent);
    }

    private void run() {
        try {
            if (!Preferences.loadBoolean(getApplicationContext(), Preferences.TRAINING_HRK_SAVED, false)) {
                FileUtils.copyFileToExternalDirectory(getApplicationContext(), HRK_TRAINING, HRK_TRAINING);
                Preferences.saveBoolean(getApplicationContext(), Preferences.TRAINING_HRK_SAVED, true);
            }

            Pair<MLRegression, NormalizationHelper> pair = trainData();

            File actual = getActualExchangeListPredictedLines();

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
        String path = getExternalFilesDir(null).getPath() + "/" + HRK_TRAINING;
        File filename = new File(path);

        // Define the format of the data file (CSV format)
        CSVFormat format = new CSVFormat();
        VersatileDataSource source = new CSVDataSource(filename, true, format);

        VersatileMLDataSet data = new VersatileMLDataSet(source);
        data.getNormHelper().setFormat(format);

        // Define how missing values are represented.
        data.getNormHelper().defineUnknownValue("?");

        List<ColumnDefinition> columnDefinitions = new ArrayList<>();

        ColumnDefinition column = data.defineSourceColumn(Valute.HRK.name(), ColumnType.continuous);
        columnDefinitions.add(column);
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
        data.setLeadWindowSize(1);
        data.setLagWindowSize(WINDOW_SIZE + 1);

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
        VectorWindow window = new VectorWindow(WINDOW_SIZE); // Predict 30 days
        MLData input = p_helper.allocateInputVector(WINDOW_SIZE);

        LocalDate parsedDate = LocalDate.now();
        while (csv.next()) {
            line[0] = csv.get(1);
            p_helper.normalizeInputVector(line, slice, false);

            // enough data to build a full window?
            if (window.isReady()) {
                window.copyWindow(input.getData(), 0);
                String date = csv.get(0);

                MLData output = p_bestMethod.compute(input);
                String predicted = p_helper.denormalizeOutputVectorToString(output)[0];

                if (date.equals("")) continue;
                parsedDate = LocalDate.parse(date, DateTimeFormat.forPattern(SaveCsvFileToSqlService.DATE_FORMAT));
                // Save predicted data only from now to the end of the month
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

        Preferences.saveDate(getApplicationContext(), Preferences.LAST_PREDICTED_HRK_DATE, parsedDate.minusDays(1));
    }

    private void insertTecajnaListaPredicted(LocalDate p_parsedDate, String p_predicted) {
        ContentResolver resolver = getContentResolver();
        DrzavaRepository drzavaRepository = new DrzavaRepository(resolver);
        DanRepository danRepository = new DanRepository(resolver);
        TecajnaListaPredictedRepository tecajnaListaPredictedRepository = new TecajnaListaPredictedRepository(
                resolver);

        Dan dan = new Dan();
        dan.setDan(p_parsedDate);
        dan.setId(danRepository.insert(dan));

        Drzava drzava = drzavaRepository.getByValuta(Valute.HRK.name());

        TecajnaListaPredicted tecajnaListaPredicted = new TecajnaListaPredicted();
        tecajnaListaPredicted.setDan(dan);
        tecajnaListaPredicted.setDrzava(drzava);
        BigDecimal tecaj = BigDecimal.valueOf(Double.valueOf(p_predicted));
        tecajnaListaPredicted.setKupovniTecaj(tecaj);
        tecajnaListaPredicted.setSrednjiTecaj(tecaj);
        tecajnaListaPredicted.setProdajniTecaj(tecaj);
        tecajnaListaPredicted.setSoloPredicted(true);
        tecajnaListaPredictedRepository.insert(tecajnaListaPredicted);
    }

    private File getActualExchangeListPredictedLines() {
        BufferedReader reader;
        BufferedWriter writer;
        try {
            String externalStoragePath = getExternalFilesDir(null).getPath();
            String path = externalStoragePath + "/" + HRK_ACTUAL_TEMP;
            String pathActual = externalStoragePath + "/" + HRK_ACTUAL;
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
