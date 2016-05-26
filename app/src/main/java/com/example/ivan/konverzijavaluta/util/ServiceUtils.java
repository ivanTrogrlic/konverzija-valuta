package com.example.ivan.konverzijavaluta.util;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by ivan on 5/26/2016.
 */
public class ServiceUtils {

    public static boolean isMyServiceRunning(Context p_context, String p_service) {
        ActivityManager manager = (ActivityManager) p_context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.service.getClassName().equals(p_service)) {
                return true;
            }
        }
        return false;
    }

}
