package com.codeless.tracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

class ActivityAgent {
    private final static String TAG = "ActivityAgent";

    static void onActivityCreate(Object object, @Nullable Bundle bundle) {
        if (bundle == null) {
            Log.d(TAG, "enter activity " + object.getClass().getName());
        }
    }

    static void onActivityResume(Object object) {
        Log.d(TAG, "current activity " + object.getClass().getName());
    }

    static void onActivityPause(Object object) {
        Log.d(TAG, "leave activity " + object.getClass().getName());
    }
}
