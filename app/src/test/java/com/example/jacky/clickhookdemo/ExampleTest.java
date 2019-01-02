package com.example.jacky.clickhookdemo;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExampleTest {
    private final static Pattern pattern = Pattern.compile("\\[[^\\]]+\\]", Pattern.CASE_INSENSITIVE);

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

    @Test
    public void regex_test() {
        String source = "[大笑][大哭]hello[123]";
        final Matcher matcher = pattern.matcher(source);

        int start;
        for (start = 0; start < source.length(); start++) {
            if (matcher.find()) {
                String key = matcher.group();
                start = matcher.start() + key.length();
                Util.println("start=" + start + ",\t key=" + key);
                Util.println("result=" + matcher.replaceFirst("AB"));
            }
        }
    }
}
