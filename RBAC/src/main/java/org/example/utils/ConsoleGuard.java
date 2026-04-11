package org.example.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleGuard {

    private static final AtomicBoolean inputActive = new AtomicBoolean(false);

    private ConsoleGuard() {}

    public static void beginInput() {
        inputActive.set(true);
    }

    public static void endInput() {
        inputActive.set(false);
    }

    public static boolean isInputActive() {
        return inputActive.get();
    }

    public static void backgroundPrintln(String message) {
        if (!inputActive.get()) {
            System.out.println(message);
        }
    }
}
