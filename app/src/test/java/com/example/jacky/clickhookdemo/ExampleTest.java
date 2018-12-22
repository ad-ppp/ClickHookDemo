package com.example.jacky.clickhookdemo;

import org.junit.Test;

public class ExampleTest {
    @Test
    public void letter_index_test() {
        for (int i = 0; i < 10000; i++) {
            Util.println(String.valueOf(i) + "\t\t" + Util.to26(i));
        }
    }

    @Test
    public void append_test() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('1');
        Util.println(stringBuilder.toString());

        int i = '1';
        Util.println(i);
    }
}
