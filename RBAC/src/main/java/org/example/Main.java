package org.example;

import org.example.system.RBACSystem;
import org.example.system.commands.CommandParser;
import org.example.system.commands.CommandRegistry;
import org.example.test.StressTest;
import org.example.test.StressTestResult;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        if (args.length > 0 && args[0].equals("--stress")) {
            runStressOnly();
            return;
        }

        runCLI();
    }

    private static void runCLI() {
        RBACSystem system = new RBACSystem();
        system.initialize();

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        Scanner scanner = new Scanner(System.in);

        System.out.println("-+ RBAC System started.");
        System.out.println("-+ Type 'help' to see available commands.");
        System.out.println("-+ Type 'exit' to quit.");
        System.out.println("-+");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                parser.parseAndExecute(input, scanner, system);
            }
        }
    }

    private static void runStressOnly() throws InterruptedException {
        System.out.println("=== Running Stress Tests ===\n");

        System.out.println("--- Test 1: Light (4 threads x 50 iterations) ---");
        StressTestResult r1 = new StressTest(4, 50).run();
        System.out.println(r1.format());

        System.out.println("--- Test 2: Medium (8 threads x 100 iterations) ---");
        StressTestResult r2 = new StressTest(8, 100).run();
        System.out.println(r2.format());

        System.out.println("--- Test 3: Heavy (16 threads x 200 iterations) ---");
        StressTestResult r3 = new StressTest(16, 200).run();
        System.out.println(r3.format());

        System.out.println("=== Summary ===");
        System.out.println("Test 1: " + (r1.isPassed() ? "PASSED" : "FAILED"));
        System.out.println("Test 2: " + (r2.isPassed() ? "PASSED" : "FAILED"));
        System.out.println("Test 3: " + (r3.isPassed() ? "PASSED" : "FAILED"));
    }
}