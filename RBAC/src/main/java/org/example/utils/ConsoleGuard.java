package org.example.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleGuard {

    private static final AtomicBoolean inputActive = new AtomicBoolean(false);
    private static final AtomicBoolean enabled     = new AtomicBoolean(true);

    private ConsoleGuard() {}

    public static void enable()  { enabled.set(true); }
    public static void disable() { enabled.set(false); }

    public static void beginInput() {
        if (enabled.get()) inputActive.set(true);
    }

    public static void endInput() {
        inputActive.set(false);
    }

    public static boolean isInputActive() {
        return enabled.get() && inputActive.get();
    }

    public static void backgroundPrintln(String message) {
        if (!isInputActive()) {
            System.out.println(message);
        }
    }
}
