package util;

import org.example.utils.FormatUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void formatTableBasic() {
        String[] headers = {"Name", "Age"};
        List<String[]> rows = List.of(
                new String[]{"Alice", "30"},
                new String[]{"Bob", "25"}
        );
        String table = FormatUtils.formatTable(headers, rows);
        assertTrue(table.contains("Name"));
        assertTrue(table.contains("Age"));
        assertTrue(table.contains("Alice"));
        assertTrue(table.contains("Bob"));
        assertTrue(table.contains("+"));
        assertTrue(table.contains("|"));
    }

    @Test
    void formatTableAlignment() {
        String[] headers = {"A", "B"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"short", "longer value"});
        String table = FormatUtils.formatTable(headers, rows);
        String[] lines = table.split("\n");
        int headerLen = lines[1].length();
        int rowLen = lines[3].length();
        assertEquals(headerLen, rowLen);
    }

    @Test
    void formatTableEmptyRows() {
        String[] headers = {"Col1", "Col2"};
        List<String[]> rows = new ArrayList<>();
        String table = FormatUtils.formatTable(headers, rows);
        assertTrue(table.contains("Col1"));
        assertTrue(table.contains("Col2"));
    }

    @Test
    void formatTableNullCells() {
        String[] headers = {"A", "B"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"value", null});
        String table = FormatUtils.formatTable(headers, rows);
        assertTrue(table.contains("value"));
    }

    @Test
    void formatTableSeparators() {
        String[] headers = {"X"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Y"});
        String table = FormatUtils.formatTable(headers, rows);
        String[] lines = table.split("\n");
        assertTrue(lines[0].startsWith("+"));
        assertTrue(lines[0].endsWith("+"));
        assertEquals(lines[0], lines[2]);
        assertEquals(lines[0], lines[lines.length - 1]);
    }

    @Test
    void formatBoxSingleLine() {
        String box = FormatUtils.formatBox("hello");
        assertTrue(box.contains("| hello |"));
        assertTrue(box.startsWith("+-"));
        assertTrue(box.endsWith("-+"));
    }

    @Test
    void formatBoxMultiLine() {
        String box = FormatUtils.formatBox("line1\nline2");
        assertTrue(box.contains("| line1 |"));
        assertTrue(box.contains("| line2 |"));
    }

    @Test
    void formatBoxNull() {
        String box = FormatUtils.formatBox(null);
        assertTrue(box.contains("|"));
    }

    @Test
    void formatBoxDifferentLengths() {
        String box = FormatUtils.formatBox("short\nlonger line here");
        String[] lines = box.split("\n");
        assertEquals(lines[0].length(), lines[1].length());
        assertEquals(lines[1].length(), lines[2].length());
    }

    @Test
    void formatHeaderContainsText() {
        String header = FormatUtils.formatHeader("Title");
        assertTrue(header.contains("Title"));
        assertTrue(header.contains("="));
    }

    @Test
    void formatHeaderSymmetry() {
        String header = FormatUtils.formatHeader("Test");
        String[] lines = header.split("\n");
        assertEquals(lines[0], lines[2]);
    }

    @Test
    void formatHeaderNull() {
        String header = FormatUtils.formatHeader(null);
        assertTrue(header.contains("="));
    }

    @Test
    void truncateShortString() {
        assertEquals("hello", FormatUtils.truncate("hello", 10));
    }

    @Test
    void truncateExactLength() {
        assertEquals("hello", FormatUtils.truncate("hello", 5));
    }

    @Test
    void truncateLongString() {
        assertEquals("hel...", FormatUtils.truncate("hello world", 6));
    }

    @Test
    void truncateVeryShortMax() {
        assertEquals("hel", FormatUtils.truncate("hello", 3));
    }

    @Test
    void truncateNull() {
        assertEquals("", FormatUtils.truncate(null, 10));
    }

    @Test
    void padRightShorter() {
        assertEquals("hi   ", FormatUtils.padRight("hi", 5));
    }

    @Test
    void padRightExact() {
        assertEquals("hello", FormatUtils.padRight("hello", 5));
    }

    @Test
    void padRightLonger() {
        assertEquals("hello world", FormatUtils.padRight("hello world", 5));
    }

    @Test
    void padRightNull() {
        assertEquals("     ", FormatUtils.padRight(null, 5));
    }

    @Test
    void padLeftShorter() {
        assertEquals("   hi", FormatUtils.padLeft("hi", 5));
    }

    @Test
    void padLeftExact() {
        assertEquals("hello", FormatUtils.padLeft("hello", 5));
    }

    @Test
    void padLeftLonger() {
        assertEquals("hello world", FormatUtils.padLeft("hello world", 5));
    }

    @Test
    void padLeftNull() {
        assertEquals("     ", FormatUtils.padLeft(null, 5));
    }
}
