package com.gdm.alphageeksales.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private static SharedPreferences mSharedPref;

    public static void init(Context context) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences("com.gdm.alphageeksales", Context.MODE_PRIVATE);
    }

    public static String read(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }

    public static String getUserID() {
        return mSharedPref.getString("USER_ID", "");
    }
    public static String getUserInfo() {
        return mSharedPref.getString("PROFILE", "");
    }
    public static String getJWTToken() {
        return mSharedPref.getString("JWT_TOKEN", "");
    }

    public static void write(String key, String value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }
}
