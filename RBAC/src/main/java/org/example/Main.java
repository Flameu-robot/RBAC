package org.example;
import org.example.entity.User;

public class Main {
    public static void main(String[] args) {

        // проверка на Успешное создание
        testCase("Valid User", () -> {
            User user = new User("john_doe", "John Doe", "john@example.com");
            System.out.println("Created: " + user.format());
        });

        // проверка на граничные случаи длины
        testCase("Valid User (3 chars)", () -> {
            User user = new User("abc", "Short Name", "a@b.c");
            System.out.println("Created: " + user.format());
        });

        // проверка на Ошибка длины username < 3 символов
        testCase("Invalid Username (too short)", () -> {
            new User("ab", "Too Short", "test@mail.com");
        });

        // проверка на Ошибка длины username > 20 символов
        testCase("Invalid Username (too long)", () -> {
            new User("very_long_username_that_exceeds_limit", "Too Long", "test@mail.com");
        });

        // проверка на Ошибка формата username (недопустимые символы)
        testCase("Invalid Username (invalid chars)", () -> {
            new User("john-doe!", "Invalid Chars", "test@mail.com");
        });

        // проверка на Ошибка формата email
        testCase("Invalid Email (no @)", () -> {
            new User("john_doe", "John Doe", "invalid-email.com");
        });

        // проверка на Ошибка формата email (нет точки после @)
        testCase("Invalid Email (no dot)", () -> {
            new User("john_doe", "John Doe", "john@localhost");
        });

        // проверка на Ошибка пустых полей
        testCase("Empty Fullname", () -> {
            new User("john_doe", "", "john@example.com");
        });

        // проверка на Null поля
        testCase("Null Username", () -> {
            new User(null, "John Doe", "john@example.com");
        });

        System.out.println("\nTests Completed");
    }
    private static void testCase(String testName, Runnable test) {
        System.out.println(">> Test: " + testName);
        try {
            test.run();
            System.out.println("   Result: SUCCESS");
        }
        catch (IllegalArgumentException e) {
            System.out.println("   Result: FAILED (Expected) -> " + e.getMessage());
        }
        catch (Exception e) {
            System.out.println("   Result: ERROR (Unexpected) -> " + e.getClass().getSimpleName());
        }
        System.out.println();
    }
}
