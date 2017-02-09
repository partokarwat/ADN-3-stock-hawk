package com.udacity.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.udacity.stockhawk.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PrefUtils {

    private PrefUtils() {
    }

    /**
     * Helper method to get the stocks of the Database, or create default stocks
     * if the database is empty.
     *
     * @param context The context used to access the stocks
     * @return the stock symbols
     */
    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // load stocks from database, if empty database cursor is null
        Cursor cursor = context.getContentResolver().query(
                Contract.Quote.URI,
                new String[] {Contract.Quote.COLUMN_SYMBOL},
                null,
                null,
                null
        );

        if (cursor == null || cursor.getCount() == 0) {
            //show default stocks
            String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);
            return prefs.getStringSet(stocksKey, new HashSet<>(Arrays.asList(defaultStocksList)));
        } else {
            //show stocks from database
            ArrayList<String> data = new ArrayList<>();
            while(cursor.moveToNext()){
                data.add(cursor.getString(0)); //always get the row element of the 1 column table
            }
            cursor.close();

            HashSet<String> stocks = new HashSet<>(data);
            return prefs.getStringSet(stocksKey, stocks);
        }

    }

    private static void editStockPref(Context context, String symbol, Boolean add) {
        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = getStocks(context);

        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(key, stocks);
        editor.apply();
    }

    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

}
