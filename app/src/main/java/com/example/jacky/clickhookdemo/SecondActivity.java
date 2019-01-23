package com.example.jacky.clickhookdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;

public class SecondActivity extends TestActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, new SecondFragment())
                .replace(R.id.container, new ButterKnifeFragment())
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
