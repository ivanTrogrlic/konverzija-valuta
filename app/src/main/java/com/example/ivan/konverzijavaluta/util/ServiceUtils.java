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

    public static String countryForCurrency(String currency) {
        switch (currency) {
            case "USD":
                return "USA";
            case "JPY":
                return "JPN";
            case "BGN":
                return "BGR";
            case "CYP":
                return "CYP";
            case "CZK":
                return "CZE";
            case "DKK":
                return "DNK";
            case "EEK":
                return "EST";
            case "GBP":
                return "GBR";
            case "HUF":
                return "HUN";
            case "LTL":
                return "LTU";
            case "LVL":
                return "LVA";
            case "MTL":
                return "MLT";
            case "PLN":
                return "POL";
            case "ROL":
                return "ROU";
            case "RON":
                return "ROU";
            case "SEK":
                return "SWE";
            case "SIT":
                return "SVN";
            case "SKK":
                return "CHE";
            case "ISK":
                return "ISL";
            case "NOK":
                return "NOR";
            case "HRK":
                return "HRV";
            case "RUB":
                return "RUS";
            case "TRL":
                return "TUR";
            case "TRY":
                return "TUR";
            case "AUD":
                return "AUS";
            case "BRL":
                return "BRA";
            case "CAD":
                return "CAN";
            case "CNY":
                return "CHN";
            case "HKD":
                return "HKG";
            case "IDR":
                return "IDN";
            case "INR":
                return "IND";
            case "KRW":
                return "KOR";
            case "MXN":
                return "MEX";
            case "MYR":
                return "MYS";
            case "NZD":
                return "NZL";
            case "PHP":
                return "PHL";
            case "SGD":
                return "SGP";
            case "THB":
                return "THA";
            case "ZAR":
                return "ZAF";
            case "ILS":
                return "ISR";
            default:
                throw new IllegalArgumentException("Unknown currency");
        }
    }

    public static String getE5orE7(String p_currency) {
        if (p_currency.equals("BRL")
                || p_currency.equals("IRD")
                || p_currency.equals("ILS")
                || p_currency.equals("INR")
                || p_currency.equals("ISK")
                || p_currency.equals("MXN")
                || p_currency.equals("MYR")
                || p_currency.equals("NZD")
                || p_currency.equals("PHP")
                || p_currency.equals("RUB")
                || p_currency.equals("SEK")
                || p_currency.equals("THB")
                || p_currency.equals("TRY")
                || p_currency.equals("ZAR")) {
            return "E7";
        }
        return "E5";
    }
}
