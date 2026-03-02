package org.example.utils;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print("-+" + message);
            String input = scanner.nextLine().trim();
            if (!required || !input.isEmpty()) {
                return input;
            }
            System.out.println("-+" + "This field is required. Please try again.");
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print("-+" + message);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("-+" + "Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("-+" + "Invalid number. Please try again.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print("-+" + message + " (да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("да") || input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("нет") || input.equals("no") || input.equals("n")) {
                return false;
            }
            System.out.println("-+" + "Please enter 'да' or 'нет'.");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            System.out.println("-+" + "No options available.");
            return null;
        }

        System.out.println("-+" + message);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("-+" + "  " + (i + 1) + ". " + options.get(i).toString());
        }

        int choice = promptInt(scanner, "Choose (1-" + options.size() + "): ", 1, options.size());
        return options.get(choice - 1);
    }
}
