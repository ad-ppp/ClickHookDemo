package com.example.jacky.clickhookdemo;

import android.app.Application;
import com.codeless.tracker.Tracker;
import com.tencent.bugly.crashreport.CrashReport;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Tracker.init(this);

        CrashReport.initCrashReport(this,"0035626966",true);
    }
}
