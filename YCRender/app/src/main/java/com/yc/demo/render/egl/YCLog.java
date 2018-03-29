package com.yc.demo.render.egl;

import android.util.Log;

/**
 * Created by lidm on 18/2/26.
 * LOG
 */
public class YCLog {

    private static final boolean M_DEGUB = true;
    private static final String M_LOG_TAG = "YC_RENDER";


    public static void i(String log) {
        if (M_DEGUB) {
            Log.i(M_LOG_TAG, log);
        }
    }

    public static void d(String log) {
        if (M_DEGUB) {
            Log.d(M_LOG_TAG, log);
        }
    }

    public static void e(String log) {
        if (M_DEGUB) {
            Log.e(M_LOG_TAG, log);
        }
    }

}
