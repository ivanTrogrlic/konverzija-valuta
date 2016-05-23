package com.example.ivan.konverzijavaluta.encog;

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

import java.io.File;
import java.util.Arrays;

import timber.log.Timber;

/**
 * Created by ivan on 5/23/2016.
 */
public class EncogService {

    private String tempPath;
    public static final int WINDOW_SIZE = 40;

    public void run(String[] args) {
        try {
            ErrorCalculation.setMode(ErrorCalculationMode.RMS);
            // Download the data that we will attempt to model.
            File filename = new File("C:\\tecajna-lista.csv");


            // Define the format of the data file.
            // This area will change, depending on the columns and
            // format of the file that you are trying to model.
            CSVFormat format = new CSVFormat();
            VersatileDataSource source = new CSVDataSource(filename, true, format);

            VersatileMLDataSet data = new VersatileMLDataSet(source);
            data.getNormHelper().setFormat(format);

            // ColumnDefinition columnDate = data.defineSourceColumn("Date",ColumnType.ordinal);
            ColumnDefinition columnUSD = data.defineSourceColumn("USD", ColumnType.continuous);
            ColumnDefinition columnJPY = data.defineSourceColumn("JPY", ColumnType.continuous);
            ColumnDefinition columnBGN = data.defineSourceColumn("BGN", ColumnType.continuous);
            ColumnDefinition columnCYP = data.defineSourceColumn("CYP", ColumnType.continuous);
            ColumnDefinition columnCZK = data.defineSourceColumn("CZK", ColumnType.continuous);
            ColumnDefinition columnDKK = data.defineSourceColumn("DKK", ColumnType.continuous);
            ColumnDefinition columnEEK = data.defineSourceColumn("EEK", ColumnType.continuous);
            ColumnDefinition columnGBP = data.defineSourceColumn("GBP", ColumnType.continuous);
            ColumnDefinition columnHUF = data.defineSourceColumn("HUF", ColumnType.continuous);
            ColumnDefinition columnLTL = data.defineSourceColumn("LTL", ColumnType.continuous);
            ColumnDefinition columnLVL = data.defineSourceColumn("LVL", ColumnType.continuous);
            ColumnDefinition columnMTL = data.defineSourceColumn("MTL", ColumnType.continuous);
            ColumnDefinition columnPLN = data.defineSourceColumn("PLN", ColumnType.continuous);
            ColumnDefinition columnROL = data.defineSourceColumn("ROL", ColumnType.continuous);
            ColumnDefinition columnRON = data.defineSourceColumn("RON", ColumnType.continuous);
            ColumnDefinition columnSEK = data.defineSourceColumn("SEK", ColumnType.continuous);
            ColumnDefinition columnSIT = data.defineSourceColumn("SIT", ColumnType.continuous);
            ColumnDefinition columnSKK = data.defineSourceColumn("SKK", ColumnType.continuous);
            ColumnDefinition columnCHF = data.defineSourceColumn("CHF", ColumnType.continuous);
            ColumnDefinition columnISK = data.defineSourceColumn("ISK", ColumnType.continuous);
            ColumnDefinition columnNOK = data.defineSourceColumn("NOK", ColumnType.continuous);
            ColumnDefinition columnHRK = data.defineSourceColumn("HRK", ColumnType.continuous);
            ColumnDefinition columnRUB = data.defineSourceColumn("RUB", ColumnType.continuous);
            ColumnDefinition columnTRL = data.defineSourceColumn("TRL", ColumnType.continuous);
            ColumnDefinition columnTRY = data.defineSourceColumn("TRY", ColumnType.continuous);
            ColumnDefinition columnAUD = data.defineSourceColumn("AUD", ColumnType.continuous);
            ColumnDefinition columnBRL = data.defineSourceColumn("BRL", ColumnType.continuous);
            ColumnDefinition columnCAD = data.defineSourceColumn("CAD", ColumnType.continuous);
            ColumnDefinition columnCNY = data.defineSourceColumn("CNY", ColumnType.continuous);
            ColumnDefinition columnHKD = data.defineSourceColumn("HKD", ColumnType.continuous);
            ColumnDefinition columnIDR = data.defineSourceColumn("IDR", ColumnType.continuous);
            ColumnDefinition columnINR = data.defineSourceColumn("INR", ColumnType.continuous);
            ColumnDefinition columnKRW = data.defineSourceColumn("KRW", ColumnType.continuous);
            ColumnDefinition columnMXN = data.defineSourceColumn("MXN", ColumnType.continuous);
            ColumnDefinition columnMYR = data.defineSourceColumn("MYR", ColumnType.continuous);
            ColumnDefinition columnNZD = data.defineSourceColumn("NZD", ColumnType.continuous);
            ColumnDefinition columnPHP = data.defineSourceColumn("PHP", ColumnType.continuous);
            ColumnDefinition columnSGD = data.defineSourceColumn("SGD", ColumnType.continuous);
            ColumnDefinition columnTHB = data.defineSourceColumn("THB", ColumnType.continuous);
            ColumnDefinition columnZAR = data.defineSourceColumn("ZAR", ColumnType.continuous);
            ColumnDefinition columnILS = data.defineSourceColumn("ILS", ColumnType.continuous);


            // Define how missing values are represented.
            data.getNormHelper().defineUnknownValue("?");
            data.getNormHelper().defineMissingHandler(columnUSD, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnJPY, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnBGN, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnCYP, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnCZK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnDKK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnEEK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnGBP, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnHUF, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnLTL, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnLVL, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnMTL, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnPLN, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnROL, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnRON, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnSEK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnSIT, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnSKK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnISK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnNOK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnHRK, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnRUB, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnTRL, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnTRY, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnAUD, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnBRL, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnCAD, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnCNY, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnHKD, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnIDR, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnINR, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnKRW, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnMXN, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnMYR, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnNZD, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnPHP, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnSGD, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnTHB, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnZAR, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnILS, new MeanMissingHandler());
            data.getNormHelper().defineMissingHandler(columnCHF, new MeanMissingHandler());


            // Analyze the data, determine the min/max/mean/sd of every column.
            data.analyze();

            // Use SSN & DEV to predict SSN. For time-series it is okay to have
            // SSN both as
            // an input and an output.
            data.defineInput(columnUSD);
            data.defineInput(columnJPY);
            data.defineInput(columnBGN);
            data.defineInput(columnCYP);
            data.defineInput(columnCZK);
            data.defineInput(columnDKK);
            data.defineInput(columnEEK);
            data.defineInput(columnGBP);
            data.defineInput(columnHUF);
            data.defineInput(columnLTL);
            data.defineInput(columnLVL);
            data.defineInput(columnMTL);
            data.defineInput(columnPLN);
            data.defineInput(columnROL);
            data.defineInput(columnRON);
            data.defineInput(columnSEK);
            data.defineInput(columnSIT);
            data.defineInput(columnSKK);
            data.defineInput(columnCHF);
            data.defineInput(columnISK);
            data.defineInput(columnNOK);
            data.defineInput(columnHRK);
            data.defineInput(columnRUB);
            data.defineInput(columnTRL);
            data.defineInput(columnTRY);
            data.defineInput(columnAUD);
            data.defineInput(columnBRL);
            data.defineInput(columnCAD);
            data.defineInput(columnCNY);
            data.defineInput(columnHKD);
            data.defineInput(columnIDR);
            data.defineInput(columnINR);
            data.defineInput(columnKRW);
            data.defineInput(columnMXN);
            data.defineInput(columnMYR);
            data.defineInput(columnNZD);
            data.defineInput(columnPHP);
            data.defineInput(columnSGD);
            data.defineInput(columnTHB);
            data.defineInput(columnZAR);
            data.defineInput(columnILS);

            data.defineOutput(columnUSD);
            data.defineOutput(columnJPY);
            data.defineOutput(columnBGN);
            data.defineOutput(columnCYP);
            data.defineOutput(columnCZK);
            data.defineOutput(columnDKK);
            data.defineOutput(columnEEK);
            data.defineOutput(columnGBP);
            data.defineOutput(columnHUF);
            data.defineOutput(columnLTL);
            data.defineOutput(columnLVL);
            data.defineOutput(columnMTL);
            data.defineOutput(columnPLN);
            data.defineOutput(columnROL);
            data.defineOutput(columnRON);
            data.defineOutput(columnSEK);
            data.defineOutput(columnSIT);
            data.defineOutput(columnSKK);
            data.defineOutput(columnCHF);
            data.defineOutput(columnISK);
            data.defineOutput(columnNOK);
            data.defineOutput(columnHRK);
            data.defineOutput(columnRUB);
            data.defineOutput(columnTRL);
            data.defineOutput(columnTRY);
            data.defineOutput(columnAUD);
            data.defineOutput(columnBRL);
            data.defineOutput(columnCAD);
            data.defineOutput(columnCNY);
            data.defineOutput(columnHKD);
            data.defineOutput(columnIDR);
            data.defineOutput(columnINR);
            data.defineOutput(columnKRW);
            data.defineOutput(columnMXN);
            data.defineOutput(columnMYR);
            data.defineOutput(columnNZD);
            data.defineOutput(columnPHP);
            data.defineOutput(columnSGD);
            data.defineOutput(columnTHB);
            data.defineOutput(columnZAR);
            data.defineOutput(columnILS);


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

            // Now normalize the data. Encog will automatically determine the
            // correct normalization
            // type based on the model you chose in the last step.
            data.normalize();

            // Set time series.
            data.setLeadWindowSize(1);
            data.setLagWindowSize(WINDOW_SIZE);

            // Hold back some data for a final validation.
            // Do not shuffle the data into a random ordering. (never shuffle
            // time series)
            // Use a seed of 1001 so that we always use the same holdback and
            // will get more consistent results.
            model.holdBackValidation(0.3, false, 1001);

            // Choose whatever is the default training type for this model.
            model.selectTrainingType(data);

            // Use a 5-fold cross-validated train. Return the best method found.
            // (never shuffle time series)
            MLRegression bestMethod = (MLRegression) model.crossvalidate(5, false);

            // Display the training and validation errors.
            System.out.println("Training error: " + model.calculateError(bestMethod, model.getTrainingDataset()));
            System.out.println("Validation error: " + model.calculateError(bestMethod, model.getValidationDataset()));

            // Display our normalization parameters.
            NormalizationHelper helper = data.getNormHelper();
            System.out.println(helper.toString());

            // Display the final model.
            System.out.println("Final model: " + bestMethod);

            // Loop over the entire, original, dataset and feed it through the
            // model. This also shows how you would process new data, that was
            // not part of your training set. You do not need to retrain, simply
            // use the NormalizationHelper class. After you train, you can save
            // the NormalizationHelper to later normalize and denormalize your
            // data.
            File test = new File("C:\\test.csv");
            ReadCSV csv = new ReadCSV(test, true, format);
            String[] line = new String[41];

            // Create a vector to hold each time-slice, as we build them.
            // These will be grouped together into windows.
            double[] slice = new double[41];
            VectorWindow window = new VectorWindow(WINDOW_SIZE + 1);
            MLData input = helper.allocateInputVector(WINDOW_SIZE + 1);

            // Only display the first 100
            int stopAfter = 100;

            while (csv.next() && stopAfter > 0) {
                StringBuilder result = new StringBuilder();

                line[0] = csv.get(1);// ssn
                line[1] = csv.get(2);// dev
                line[2] = csv.get(3);// dev
                line[3] = csv.get(4);// dev
                line[4] = csv.get(5);// dev
                line[5] = csv.get(6);// dev
                line[6] = csv.get(7);// dev
                line[7] = csv.get(8);// dev
                line[8] = csv.get(9);// dev
                line[9] = csv.get(10);// dev
                line[10] = csv.get(11);// dev
                line[11] = csv.get(12);// dev
                line[12] = csv.get(13);// dev
                line[13] = csv.get(14);// dev
                line[14] = csv.get(15);// dev
                line[15] = csv.get(16);// dev
                line[16] = csv.get(17);// dev
                line[17] = csv.get(18);// dev
                line[18] = csv.get(19);// dev
                line[19] = csv.get(20);// dev
                line[20] = csv.get(21);// dev
                line[21] = csv.get(22);// dev
                line[22] = csv.get(23);// dev
                line[23] = csv.get(24);// dev
                line[24] = csv.get(25);// dev
                line[25] = csv.get(26);// dev
                line[26] = csv.get(27);// dev
                line[27] = csv.get(28);// dev
                line[28] = csv.get(29);// dev
                line[29] = csv.get(30);// dev
                line[30] = csv.get(31);// dev
                line[31] = csv.get(32);// dev
                line[32] = csv.get(33);// dev
                line[33] = csv.get(34);// dev
                line[34] = csv.get(35);// dev
                line[35] = csv.get(36);// dev
                line[36] = csv.get(37);// dev
                line[37] = csv.get(38);// dev
                line[38] = csv.get(39);// dev
                line[39] = csv.get(40);// dev
                line[40] = csv.get(41);// dev

                helper.normalizeInputVector(line, slice, false);

                // enough data to build a full window?
                if (window.isReady()) {
                    window.copyWindow(input.getData(), 0);
                    String correct = csv.get(1); // trying to predict SSN.
                    MLData output = bestMethod.compute(input);
                    String predicted = helper.denormalizeOutputVectorToString(output)[0];

                    result.append(Arrays.toString(line));
                    result.append(" -> predicted: ");
                    result.append(predicted);
                    result.append("(correct: ");
                    result.append(correct);
                    result.append(")");

                    System.out.println(result.toString());
                }

                // Add the normalized slice to the window. We do this just after
                // the after checking to see if the window is ready so that the
                // window is always one behind the current row. This is because
                // we are trying to predict next row.
                window.add(slice);

                stopAfter--;
            }

            // Delete data file and shut down.
            // filename.delete();
            Encog.getInstance().shutdown();

        } catch (Exception ex) {
            Timber.e(ex, "Encog failed");
        }

    }

}