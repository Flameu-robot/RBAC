package org.example;

import org.example.entity.AssignmentMetadata;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.entity.User;

public class Main {
    static void main(String[] args) {
        System.out.println("User Tests");
        testUser();

        System.out.println("\nRole Demonstration");
        demonstrateRole();

        System.out.println("\nAssignmentMetadata Demonstration");
        demonstrateMetadata();
    }

    private static void testUser() {
        testCase("Valid User", () -> {
            User user = new User("john_doe", "John Doe", "john@example.com");
            System.out.println("Created: " + user.format());
        });

        testCase("Invalid Username (short)", () ->
                new User("ab", "Short Name", "test@mail.com"));

        testCase("Invalid Email", () ->
                new User("john_doe", "John Doe", "invalid-email"));
    }

    private static void demonstrateRole() {

        Role adminRole = new Role("Administrator", "Full system access");

        Permission readUsers = new Permission("READ", "users", "Can view user list");
        Permission writeUsers = new Permission("WRITE", "users", "Can create and edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Can delete users");

        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);

        System.out.println("Role with permissions");
        System.out.println(adminRole.format());

        System.out.println("\nChecks");
        System.out.println("Has permission (READ, users): " + adminRole.hasPermission("READ", "users"));
        System.out.println("Has permission object (writeUsers): " + adminRole.hasPermission(writeUsers));
        System.out.println("Has permission (EXECUTE, scripts): " + adminRole.hasPermission("EXECUTE", "scripts"));

        Role emptyRole = new Role("Guest", "Read-only access");
        System.out.println("\nEmpty Role");
        System.out.println(emptyRole.format());
    }

    private static void demonstrateMetadata() {
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial role setup");
        System.out.println("Metadata 1 (Current time):");
        System.out.println(meta1.format());

        AssignmentMetadata meta2 = new AssignmentMetadata("super_admin", "2023-10-05T14:30:00", "System migration");
        System.out.println("\nMetadata 2 (Historical date):");
        System.out.println(meta2.format());

        AssignmentMetadata meta3 = AssignmentMetadata.now("system", null);
        System.out.println("\nMetadata 3 (No reason):");
        System.out.println(meta3.format());
    }

    private static void testCase(String testName, Runnable test) {
        System.out.println(">> Test: " + testName);
        try {
            test.run();
            System.out.println("   Result: SUCCESS");
        } catch (IllegalArgumentException e) {
            System.out.println("   Result: FAILED (Expected) -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("   Result: ERROR (Unexpected) -> " + e.getClass().getSimpleName());
        }
        System.out.println();
    }
}