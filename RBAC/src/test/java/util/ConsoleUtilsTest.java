package util;

import org.example.utils.ConsoleUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private Scanner scannerOf(String input) {
        return new Scanner(input);
    }

    @Test
    void promptStringNotRequired() {
        String result = ConsoleUtils.promptString(scannerOf("\n"), "Enter: ", false);
        assertEquals("", result);
    }

    @Test
    void promptStringNotRequiredWithValue() {
        String result = ConsoleUtils.promptString(scannerOf("hello\n"), "Enter: ", false);
        assertEquals("hello", result);
    }

    @Test
    void promptStringRequiredFirstTry() {
        String result = ConsoleUtils.promptString(scannerOf("value\n"), "Enter: ", true);
        assertEquals("value", result);
    }

    @Test
    void promptStringRequiredRetry() {
        String result = ConsoleUtils.promptString(scannerOf("\nactual\n"), "Enter: ", true);
        assertEquals("actual", result);
        assertTrue(outputStream.toString().contains("required"));
    }

    @Test
    void promptStringTrimsInput() {
        String result = ConsoleUtils.promptString(scannerOf("  hello  \n"), "Enter: ", false);
        assertEquals("hello", result);
    }

    @Test
    void promptIntValid() {
        int result = ConsoleUtils.promptInt(scannerOf("5\n"), "Number: ", 1, 10);
        assertEquals(5, result);
    }

    @Test
    void promptIntMin() {
        int result = ConsoleUtils.promptInt(scannerOf("1\n"), "Number: ", 1, 10);
        assertEquals(1, result);
    }

    @Test
    void promptIntMax() {
        int result = ConsoleUtils.promptInt(scannerOf("10\n"), "Number: ", 1, 10);
        assertEquals(10, result);
    }

    @Test
    void promptIntRetryOutOfRange() {
        int result = ConsoleUtils.promptInt(scannerOf("99\n5\n"), "Number: ", 1, 10);
        assertEquals(5, result);
        assertTrue(outputStream.toString().contains("between"));
    }

    @Test
    void promptIntRetryInvalidFormat() {
        int result = ConsoleUtils.promptInt(scannerOf("abc\n5\n"), "Number: ", 1, 10);
        assertEquals(5, result);
        assertTrue(outputStream.toString().contains("Invalid number"));
    }

    @Test
    void promptYesNoYes() {
        assertTrue(ConsoleUtils.promptYesNo(scannerOf("да\n"), "Confirm?"));
    }

    @Test
    void promptYesNoNo() {
        assertFalse(ConsoleUtils.promptYesNo(scannerOf("нет\n"), "Confirm?"));
    }

    @Test
    void promptYesNoEnglishYes() {
        assertTrue(ConsoleUtils.promptYesNo(scannerOf("yes\n"), "Confirm?"));
    }

    @Test
    void promptYesNoEnglishNo() {
        assertFalse(ConsoleUtils.promptYesNo(scannerOf("no\n"), "Confirm?"));
    }

    @Test
    void promptYesNoShortY() {
        assertTrue(ConsoleUtils.promptYesNo(scannerOf("y\n"), "Confirm?"));
    }

    @Test
    void promptYesNoShortN() {
        assertFalse(ConsoleUtils.promptYesNo(scannerOf("n\n"), "Confirm?"));
    }

    @Test
    void promptYesNoCaseInsensitive() {
        assertTrue(ConsoleUtils.promptYesNo(scannerOf("ДА\n"), "Confirm?"));
    }

    @Test
    void promptYesNoRetryInvalid() {
        boolean result = ConsoleUtils.promptYesNo(scannerOf("maybe\nда\n"), "Confirm?");
        assertTrue(result);
        assertTrue(outputStream.toString().contains("Please enter"));
    }

    @Test
    void promptChoiceValid() {
        List<String> options = List.of("Apple", "Banana", "Cherry");
        String result = ConsoleUtils.promptChoice(scannerOf("2\n"), "Pick fruit:", options);
        assertEquals("Banana", result);
    }

    @Test
    void promptChoiceFirst() {
        List<String> options = List.of("A", "B", "C");
        String result = ConsoleUtils.promptChoice(scannerOf("1\n"), "Pick:", options);
        assertEquals("A", result);
    }

    @Test
    void promptChoiceLast() {
        List<String> options = List.of("A", "B", "C");
        String result = ConsoleUtils.promptChoice(scannerOf("3\n"), "Pick:", options);
        assertEquals("C", result);
    }

    @Test
    void promptChoiceNullList() {
        String result = ConsoleUtils.promptChoice(scannerOf(""), "Pick:", null);
        assertNull(result);
        assertTrue(outputStream.toString().contains("No options"));
    }

    @Test
    void promptChoiceEmptyList() {
        String result = ConsoleUtils.promptChoice(scannerOf(""), "Pick:", List.of());
        assertNull(result);
        assertTrue(outputStream.toString().contains("No options"));
    }

    @Test
    void promptChoiceDisplaysOptions() {
        List<String> options = List.of("Opt1", "Opt2");
        ConsoleUtils.promptChoice(scannerOf("1\n"), "Choose:", options);
        String output = outputStream.toString();
        assertTrue(output.contains("1. Opt1"));
        assertTrue(output.contains("2. Opt2"));
    }
}
