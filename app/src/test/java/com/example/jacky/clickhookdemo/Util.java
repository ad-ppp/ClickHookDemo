package com.example.jacky.clickhookdemo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Util {

    private final int LETTER_LENGTH = 26;

    public static void println(Object o) {
        if (o instanceof String) {
            printlnInternal((String) o);
        } else if (o instanceof Integer) {
            printlnInternal(String.valueOf(o));
        } else {
            printlnInternal(String.valueOf(o));
        }
    }

    private static void printlnInternal(String o) {
        if (o != null) {
            System.out.println("Thread:" + Thread.currentThread() + ",\t" + o);
        }
    }

    public static void suspend(int second) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            if (countDownLatch.await(second, TimeUnit.SECONDS)) {
            } else {
            }
        } catch (InterruptedException e) {
            System.out.println("thread interrupt" + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static void suspend() {
        suspend(10);
    }


    public static String toHex(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position can not be smaller than zero");
        }

        StringBuilder stringBuilder = new StringBuilder();
        int smaller;
        int bigger = position;

        while (bigger >= 0) {
            smaller = bigger % 16;
            bigger = bigger / 16;

            stringBuilder.insert(0, String.valueOf((char) (smaller + 48)));

            if (bigger == 0) {
                break;
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 0->"A"
     * 1->"B'
     * ...
     * 25->Z"
     * 26->"AA"
     * 27->"AB"
     *
     * @param position position
     * @return letter index
     */
    public static String to26(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position can not be smaller than zero");
        }

        position++;
        StringBuilder stringBuilder = new StringBuilder();
        int smaller;
        int bigger = position;

        while (bigger >= 0) {
            bigger--;
            smaller = bigger % 26;
            bigger = bigger / 26;

            stringBuilder.insert(0, String.valueOf((char) (smaller + 65)));

            if (bigger == 0) {
                break;
            }
        }

        return stringBuilder.toString();
    }
}
