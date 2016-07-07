package com.example.ivan.konverzijavaluta.util;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by ivan on 5/25/2016.
 */
public class FileUtils {

    public static File copyFileToExternalDirectory(Context p_context, String p_initFile,
                                                   String p_newFile) throws IOException {
        BufferedReader reader;
        BufferedWriter writer;
        String path = p_context.getExternalFilesDir(null).getPath() + "/" + p_newFile;
        File file = new File(path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        writer = new BufferedWriter(new FileWriter(path));
        reader = new BufferedReader(new InputStreamReader(p_context.getAssets().open(p_initFile)));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        String result = sb.toString();

        writer.write(result);
        writer.close();
        reader.close();

        return file;
    }

    @NonNull
    public static File convertResponseToCsvFile(Context p_context, Response<ResponseBody> response) throws IOException {
        BufferedWriter writer;
        BufferedReader reader;
        String path1 = p_context.getExternalFilesDir(null).getPath();
        String path2 = "dataSet.csv";
        String path = path1 + "/" + path2;
        File file = new File(path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        writer = new BufferedWriter(new FileWriter(path));
        reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            Timber.d(line);
            sb.append(line + "\n");
        }

        String result = sb.toString();

        writer.write(result);
        writer.close();
        reader.close();
        return file;
    }

}
