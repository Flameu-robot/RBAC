package commands;

import org.example.system.RBACSystem;
import org.example.system.commands.CommandParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {

    private CommandParser parser;
    private RBACSystem system;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private Scanner scannerOf(String input) {
        return new Scanner(input);
    }

    @Test
    void registerCommand() {
        parser.registerCommand("test", "Test command", (s, sys) -> System.out.println("executed"));
        parser.executeCommand("test", scannerOf(""), system);
        assertTrue(getOutput().contains("executed"));
    }

    @Test
    void registerCommandNullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.registerCommand(null, "desc", (s, sys) -> {}));
    }

    @Test
    void registerCommandBlankNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.registerCommand("  ", "desc", (s, sys) -> {}));
    }

    @Test
    void registerCommandNullCommandThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.registerCommand("test", "desc", null));
    }

    @Test
    void registerCommandNullDescription() {
        parser.registerCommand("test", null, (s, sys) -> System.out.println("ok"));
        parser.executeCommand("test", scannerOf(""), system);
        assertTrue(getOutput().contains("ok"));
    }

    @Test
    void executeCommandCaseInsensitive() {
        parser.registerCommand("test", "desc", (s, sys) -> System.out.println("found"));
        parser.executeCommand("TEST", scannerOf(""), system);
        assertTrue(getOutput().contains("found"));
    }

    @Test
    void executeCommandWithSpaces() {
        parser.registerCommand("test", "desc", (s, sys) -> System.out.println("found"));
        parser.executeCommand("  test  ", scannerOf(""), system);
        assertTrue(getOutput().contains("found"));
    }

    @Test
    void executeCommandNull() {
        parser.executeCommand(null, scannerOf(""), system);
        assertTrue(getOutput().contains("empty command"));
    }

    @Test
    void executeCommandBlank() {
        parser.executeCommand("   ", scannerOf(""), system);
        assertTrue(getOutput().contains("empty command"));
    }

    @Test
    void executeCommandUnknown() {
        parser.executeCommand("nonexistent", scannerOf(""), system);
        assertTrue(getOutput().contains("Unknown command"));
    }

    @Test
    void executeCommandExceptionHandled() {
        parser.registerCommand("fail", "desc", (s, sys) -> {
            throw new RuntimeException("test error");
        });
        parser.executeCommand("fail", scannerOf(""), system);
        assertTrue(getOutput().contains("Error executing command"));
        assertTrue(getOutput().contains("test error"));
    }

    @Test
    void parseAndExecuteSingleWord() {
        parser.registerCommand("test", "desc", (s, sys) -> System.out.println("parsed"));
        parser.parseAndExecute("test", scannerOf(""), system);
        assertTrue(getOutput().contains("parsed"));
    }

    @Test
    void parseAndExecuteWithArgs() {
        parser.registerCommand("test", "desc", (s, sys) -> System.out.println("parsed"));
        parser.parseAndExecute("test arg1 arg2", scannerOf(""), system);
        assertTrue(getOutput().contains("parsed"));
    }

    @Test
    void parseAndExecuteNull() {
        parser.parseAndExecute(null, scannerOf(""), system);
        assertTrue(getOutput().contains("empty input"));
    }

    @Test
    void parseAndExecuteBlank() {
        parser.parseAndExecute("   ", scannerOf(""), system);
        assertTrue(getOutput().contains("empty input"));
    }

    @Test
    void parseAndExecuteCaseInsensitive() {
        parser.registerCommand("test", "desc", (s, sys) -> System.out.println("ok"));
        parser.parseAndExecute("TEST extra", scannerOf(""), system);
        assertTrue(getOutput().contains("ok"));
    }

    @Test
    void printHelp() {
        parser.registerCommand("cmd1", "First command", (s, sys) -> {});
        parser.registerCommand("cmd2", "Second command", (s, sys) -> {});
        parser.printHelp();
        String output = getOutput();
        assertTrue(output.contains("cmd1"));
        assertTrue(output.contains("First command"));
        assertTrue(output.contains("cmd2"));
        assertTrue(output.contains("Second command"));
    }

    @Test
    void printHelpEmpty() {
        parser.printHelp();
        assertTrue(getOutput().contains("Available commands"));
    }

    @Test
    void printHelpOrder() {
        parser.registerCommand("bbb", "B command", (s, sys) -> {});
        parser.registerCommand("aaa", "A command", (s, sys) -> {});
        parser.printHelp();
        String output = getOutput();
        int posB = output.indexOf("bbb");
        int posA = output.indexOf("aaa");
        assertTrue(posB < posA, "Commands should appear in registration order");
    }

    @Test
    void overwriteCommand() {
        parser.registerCommand("test", "v1", (s, sys) -> System.out.println("version1"));
        parser.registerCommand("test", "v2", (s, sys) -> System.out.println("version2"));
        parser.executeCommand("test", scannerOf(""), system);
        assertTrue(getOutput().contains("version2"));
    }
}