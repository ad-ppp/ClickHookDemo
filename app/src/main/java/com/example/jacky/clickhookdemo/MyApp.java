package com.example.jacky.clickhookdemo;

import android.app.Application;
import com.codeless.tracker.Tracker;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Tracker.init(this);
    }
}
