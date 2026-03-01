package org.example.system.commands;

import org.example.system.RBACSystem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Map<String, String> commandDescriptions = new LinkedHashMap<>();

    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Command name cannot be null or empty");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }

        String key = name.toLowerCase().trim();
        commands.put(key, command);
        commandDescriptions.put(key, description != null ? description : "No description");
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        if (commandName == null || commandName.isBlank()) {
            System.out.println("-+ Error: empty command. Type 'help' for list of commands.");
            return;
        }

        String key = commandName.toLowerCase().trim();
        Command command = commands.get(key);

        if (command == null) {
            System.out.println("-+ Unknown command: '" + commandName + "'");
            System.out.println("-+ Type 'help' for list of available commands.");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("-+ Error executing command '" + commandName + "': " + e.getMessage());
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.isBlank()) {
            System.out.println("-+ Error: empty input. Type 'help' for list of commands.");
            return;
        }

        String trimmed = input.trim();
        String commandName;
        int spaceIndex = trimmed.indexOf(' ');

        if (spaceIndex == -1) {
            commandName = trimmed;
        } else {
            commandName = trimmed.substring(0, spaceIndex);
        }

        executeCommand(commandName, scanner, system);
    }

    public void printHelp() {
        System.out.println("-+ Available commands:");
        System.out.println("-+");

        int maxLen = commandDescriptions.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        String format = "-+   %-" + (maxLen + 2) + "s %s";

        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf(format + "%n", entry.getKey(), entry.getValue());
        }

        System.out.println("-+");
        System.out.println("-+ Usage: type command name and press Enter.");
    }

}
