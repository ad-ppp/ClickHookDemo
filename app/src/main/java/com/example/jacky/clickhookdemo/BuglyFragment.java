package com.example.jacky.clickhookdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.tencent.bugly.crashreport.CrashReport;

public class BuglyFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button button = view.findViewById(R.id.button);
        button.setText("bugly test");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setText("clicked...");
                CrashReport.setUserId("debug | apply plugin ");
                CrashReport.testJavaCrash();
            }
        });

        // throw new IllegalArgumentException("this is a test for crash");
        // Log.d("123", String.valueOf(1 / 0));
    }
}
