package com.example.jacky.clickhookdemo;

import org.junit.Test;

public class ExampleTest {
    @Test
    public void index_test() {
        for (int i = 0; i < 500; i++) {
            Util.println(String.valueOf(i) + "\t" + Util.getLetterIndex(i));
        }

        Util.println(String.valueOf(26) + "\t" + Util.getLetterIndex(26));
        Util.println(Util.toHex(917));
        Util.suspend(20);
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
