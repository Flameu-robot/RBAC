package org.example.utils;

import java.util.List;

public class FormatUtils {

    public static String formatTable(String[] headers, List<String[]> rows) {
        int cols = headers.length;
        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {
            widths[i] = safeLength(headers[i]);
        }
        for (String[] row : rows) {
            for (int i = 0; i < cols && i < row.length; i++) {
                widths[i] = Math.max(widths[i], safeLength(row[i]));
            }
        }

        StringBuilder sb = new StringBuilder();
        String separator = buildSeparator(widths);

        sb.append(separator).append("\n");
        sb.append(buildRow(headers, widths)).append("\n");
        sb.append(separator).append("\n");

        for (String[] row : rows) {
            sb.append(buildRow(row, widths)).append("\n");
        }

        sb.append(separator);
        return sb.toString();
    }

    public static String formatBox(String text) {
        if (text == null) text = "";

        String[] lines = text.split("\n");
        int maxLen = 0;
        for (String line : lines) {
            maxLen = Math.max(maxLen, line.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("+-").append("-".repeat(maxLen)).append("-+\n");
        for (String line : lines) {
            sb.append("| ").append(padRight(line, maxLen)).append(" |\n");
        }
        sb.append("+-").append("-".repeat(maxLen)).append("-+");
        return sb.toString();
    }

    public static String formatHeader(String text) {
        if (text == null) text = "";

        int width = text.length() + 4;
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(width)).append("\n");
        sb.append("  ").append(text).append("\n");
        sb.append("=".repeat(width));
        return sb.toString();
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (maxLength < 4) return text.substring(0, Math.min(text.length(), maxLength));
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return " ".repeat(length - text.length()) + text;
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }

    private static String buildRow(String[] cells, int[] widths) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = (i < cells.length && cells[i] != null) ? cells[i] : "";
            sb.append(" ").append(padRight(cell, widths[i])).append(" |");
        }
        return sb.toString();
    }

    private static int safeLength(String text) {
        return text != null ? text.length() : 0;
    }
}
