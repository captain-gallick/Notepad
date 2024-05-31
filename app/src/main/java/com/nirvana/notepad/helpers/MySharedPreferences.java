package com.nirvana.notepad.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MySharedPreferences {

    private static final String LAYOUT_MODE = "layout_mode";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static int getLAYOUT_MODE(Context context) {
        return getSharedPreferences(context).getInt(LAYOUT_MODE,1);
    }

    public static void setLAYOUT_MODE(Context context, int layoutMode) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(LAYOUT_MODE, layoutMode);
        editor.apply();
    }
}
