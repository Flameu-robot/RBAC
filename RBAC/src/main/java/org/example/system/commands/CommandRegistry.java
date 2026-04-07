package org.example.system.commands;

import org.example.entity.*;
import org.example.repository.*;
import org.example.assignment.*;
import org.example.filter.*;
import org.example.system.RBACSystem;
import org.example.utils.AuditLog;
import org.example.utils.ReportGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {

    public static void registerAll(CommandParser parser) {
        registerUserCommands(parser);
        registerRoleCommands(parser);
        registerAssignmentCommands(parser);
        registerPermissionCommands(parser);
        registerServiceCommands(parser);
    }

    private static String ask(Scanner scanner, String prompt) {
        System.out.print("-+ " + prompt);
        return scanner.nextLine().trim();
    }

    private static void out(String message) {
        System.out.println("-+ " + message);
    }

    private static void outIndent(String message) {
        System.out.println("-+ " + "  " + message);
    }

    private static boolean confirm(Scanner scanner, String message) {
        String answer = ask(scanner, message + " (да/нет): ");
        return answer.equalsIgnoreCase("да");
    }

    private static int askNumber(Scanner scanner, String prompt, int min, int max) {
        String input = ask(scanner, prompt);
        try {
            int num = Integer.parseInt(input);
            if (num < min || num > max) {
                out("Invalid number.");
                return -1;
            }
            return num;
        } catch (NumberFormatException e) {
            out("Invalid input.");
            return -1;
        }
    }

    private static Optional<User> findUser(Scanner scanner, RBACSystem system) {
        String username = ask(scanner, "Enter username: ");
        Optional<User> opt = system.getUserManager().findByUsername(username);
        if (opt.isEmpty()) {
            out("User '" + username + "' not found.");
        }
        return opt;
    }

    private static Optional<Role> findRole(Scanner scanner, RBACSystem system) {
        String name = ask(scanner, "Enter role name: ");
        Optional<Role> opt = system.getRoleManager().findByName(name);
        if (opt.isEmpty()) {
            out("Role '" + name + "' not found.");
        }
        return opt;
    }

    private static Permission askPermission(Scanner scanner) {
        String name = ask(scanner, "Permission name: ");
        String resource = ask(scanner, "Resource: ");
        String description = ask(scanner, "Description: ");
        return new Permission(name, resource, description);
    }

    private static String resolveAssignedBy(RBACSystem system) {
        return system.getCurrentUser() != null ? system.getCurrentUser() : "system";
    }

    private static void printUserTable(List<User> users) {
        System.out.printf("-+ " + "%-20s %-25s %-30s%n", "Username", "Full Name", "Email");
        out("-".repeat(75));
        for (User u : users) {
            System.out.printf("-+ " + "%-20s %-25s %-30s%n", u.username(), u.fullname(), u.email());
        }
        out("Total: " + users.size());
    }

    private static void printRoleTable(List<Role> roles) {
        System.out.printf("-+ " + "%-20s %-15s %-20s%n", "Name", "Permissions", "ID");
        out("-".repeat(55));
        for (Role r : roles) {
            System.out.printf("-+ " + "%-20s %-15d %-20s%n", r.getName(), r.getPermissions().size(), r.getId());
        }
    }

    private static void printAssignmentTable(List<RoleAssignment> assignments) {
        System.out.printf("-+ " + "%-15s %-15s %-12s %-10s %-25s%n",
                "Username", "Role", "Type", "Status", "Assigned At");
        out("-".repeat(77));
        for (RoleAssignment a : assignments) {
            System.out.printf("-+ " + "%-15s %-15s %-12s %-10s %-25s%n",
                    a.user().username(),
                    a.role().getName(),
                    a.assignmentType(),
                    a.isActive() ? "ACTIVE" : "INACTIVE",
                    a.metadata().assignedAt());
        }
        out("Total: " + assignments.size());
    }

    private static void registerUserCommands(CommandParser parser) {

        parser.registerCommand("user-list", "List all users", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                out("No users found.");
                return;
            }
            printUserTable(users);
        });

        parser.registerCommand("user-create", "Create a new user", (scanner, system) -> {
            String username = ask(scanner, "Enter username: ");
            String fullName = ask(scanner, "Enter full name: ");
            String email = ask(scanner, "Enter email: ");
            try {
                User user = new User(username, fullName, email);
                system.getUserManager().add(user);
                out("User created: " + user.format());
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View user details", (scanner, system) -> {
            Optional<User> opt = findUser(scanner, system);
            if (opt.isEmpty()) return;
            User user = opt.get();

            out("User: " + user.format());

            List<RoleAssignment> active = system.getAssignmentManager().findByUser(user).stream()
                    .filter(RoleAssignment::isActive).toList();
            out("Roles (" + active.size() + "):");
            if (active.isEmpty()) {
                outIndent("No roles assigned.");
            } else {
                for (RoleAssignment a : active) {
                    outIndent("- " + a.role().getName() + " [" + a.assignmentType() + "]");
                }
            }

            Set<Permission> perms = system.getAssignmentManager().getUserPermissions(user);
            out("Permissions (" + perms.size() + "):");
            if (perms.isEmpty()) {
                outIndent("No permissions.");
            } else {
                for (Permission p : perms) {
                    outIndent("- " + p.format());
                }
            }
        });

        parser.registerCommand("user-update", "Update user data", (scanner, system) -> {
            String username = ask(scanner, "Enter username: ");
            if (!system.getUserManager().exists(username)) {
                out("User '" + username + "' not found.");
                return;
            }
            String fullName = ask(scanner, "Enter new full name: ");
            String email = ask(scanner, "Enter new email: ");
            try {
                system.getUserManager().update(username, fullName, email);
                out("User '" + username + "' updated.");
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete a user", (scanner, system) -> {
            Optional<User> opt = findUser(scanner, system);
            if (opt.isEmpty()) return;
            User user = opt.get();

            if (!confirm(scanner, "Confirm deletion of '" + user.username() + "'?")) {
                out("Cancelled.");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            for (RoleAssignment a : assignments) {
                system.getAssignmentManager().remove(a);
            }
            system.getUserManager().remove(user);
            out("User '" + user.username() + "' deleted. Removed " + assignments.size() + " assignment(s).");
        });

        parser.registerCommand("user-search", "Search users by filters", (scanner, system) -> {
            out("Search by:");
            outIndent("1. Username (contains)");
            outIndent("2. Email (contains)");
            outIndent("3. Email domain");
            outIndent("4. Full name (contains)");
            String choice = ask(scanner, "Choose: ");

            UserFilter filter;
            switch (choice) {
                case "1" -> {
                    String p = ask(scanner, "Username pattern: ").toLowerCase();
                    filter = u -> u.username().toLowerCase().contains(p);
                }
                case "2" -> {
                    String p = ask(scanner, "Email pattern: ").toLowerCase();
                    filter = u -> u.email().toLowerCase().contains(p);
                }
                case "3" -> {
                    String d = ask(scanner, "Email domain: ").toLowerCase();
                    filter = u -> u.email().toLowerCase().endsWith("@" + d);
                }
                case "4" -> {
                    String p = ask(scanner, "Name pattern: ").toLowerCase();
                    filter = u -> u.fullname().toLowerCase().contains(p);
                }
                default -> {
                    out("Invalid choice.");
                    return;
                }
            }

            List<User> results = system.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                out("No users found.");
            } else {
                printUserTable(results);
            }
        });
    }

    private static void registerRoleCommands(CommandParser parser) {

        parser.registerCommand("role-list", "List all roles", (scanner, system) -> {
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                out("No roles found.");
                return;
            }
            printRoleTable(roles);
            out("Total: " + roles.size());
        });

        parser.registerCommand("role-create", "Create a new role", (scanner, system) -> {
            String name = ask(scanner, "Enter role name: ");
            String desc = ask(scanner, "Enter description: ");
            try {
                Role role = new Role(name, desc);
                system.getRoleManager().add(role);
                out("Role '" + name + "' created.");

                while (confirm(scanner, "Add permission?")) {
                    try {
                        Permission perm = askPermission(scanner);
                        role.addPermission(perm);
                        out("Permission added: " + perm.format());
                    } catch (IllegalArgumentException e) {
                        out("Error: " + e.getMessage());
                    }
                }
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "View role details", (scanner, system) -> {
            Optional<Role> opt = findRole(scanner, system);
            if (opt.isEmpty()) return;
            out(opt.get().format().replace("\n", "\n" + "-+ "));
        });

        parser.registerCommand("role-update", "Update role name/description", (scanner, system) -> {
            String oldName = ask(scanner, "Enter current role name: ");
            Optional<Role> opt = system.getRoleManager().findByName(oldName);
            if (opt.isEmpty()) {
                out("Role '" + oldName + "' not found.");
                return;
            }
            Role role = opt.get();
            String newName = ask(scanner, "New name (empty to keep '" + role.getName() + "'): ");
            String newDesc = ask(scanner, "New description (empty to keep current): ");

            try {
                boolean nameChanged = !newName.isEmpty() && !newName.equals(role.getName());
                if (nameChanged) {
                    system.getRoleManager().remove(role);
                    role.setName(newName);
                    if (!newDesc.isEmpty()) role.setDescription(newDesc);
                    system.getRoleManager().add(role);
                } else if (!newDesc.isEmpty()) {
                    role.setDescription(newDesc);
                }
                out("Role updated.");
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Delete a role", (scanner, system) -> {
            Optional<Role> opt = findRole(scanner, system);
            if (opt.isEmpty()) return;
            Role role = opt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            List<RoleAssignment> active = assignments.stream().filter(RoleAssignment::isActive).toList();

            if (!active.isEmpty()) {
                out("WARNING: Role assigned to " + active.size() + " user(s):");
                for (RoleAssignment a : active) {
                    outIndent("- " + a.user().username());
                }
            }

            if (!confirm(scanner, "Confirm deletion of role '" + role.getName() + "'?")) {
                out("Cancelled.");
                return;
            }
            for (RoleAssignment a : assignments) {
                system.getAssignmentManager().remove(a);
            }
            system.getRoleManager().remove(role);
            out("Role '" + role.getName() + "' deleted. Removed " + assignments.size() + " assignment(s).");
        });

        parser.registerCommand("role-add-permission", "Add permission to role", (scanner, system) -> {
            String roleName = ask(scanner, "Enter role name: ");
            if (!system.getRoleManager().exists(roleName)) {
                out("Role '" + roleName + "' not found.");
                return;
            }
            try {
                Permission perm = askPermission(scanner);
                system.getRoleManager().addPermissionToRole(roleName, perm);
                out("Permission added: " + perm.format());
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Remove permission from role", (scanner, system) -> {
            Optional<Role> opt = findRole(scanner, system);
            if (opt.isEmpty()) return;

            List<Permission> permList = new ArrayList<>(opt.get().getPermissions());
            if (permList.isEmpty()) {
                out("Role has no permissions.");
                return;
            }
            out("Permissions:");
            for (int i = 0; i < permList.size(); i++) {
                outIndent((i + 1) + ". " + permList.get(i).format());
            }

            int num = askNumber(scanner, "Enter number to remove: ", 1, permList.size());
            if (num == -1) return;

            Permission toRemove = permList.get(num - 1);
            system.getRoleManager().removePermissionFromRole(opt.get().getName(), toRemove);
            out("Permission removed: " + toRemove.format());
        });

        parser.registerCommand("role-search", "Search roles", (scanner, system) -> {
            out("Search by:");
            outIndent("1. Name (contains)");
            outIndent("2. Has specific permission");
            outIndent("3. Minimum number of permissions");
            String choice = ask(scanner, "Choose: ");

            RoleFilter filter;
            switch (choice) {
                case "1" -> {
                    String p = ask(scanner, "Name pattern: ").toLowerCase();
                    filter = r -> r.getName().toLowerCase().contains(p);
                }
                case "2" -> {
                    String pName = ask(scanner, "Permission name: ");
                    String pRes = ask(scanner, "Resource: ");
                    filter = r -> r.hasPermission(pName, pRes);
                }
                case "3" -> {
                    int min = askNumber(scanner, "Minimum permissions: ", 0, Integer.MAX_VALUE);
                    if (min == -1) return;
                    filter = r -> r.getPermissions().size() >= min;
                }
                default -> {
                    out("Invalid choice.");
                    return;
                }
            }

            List<Role> results = system.getRoleManager().findByFilter(filter);
            if (results.isEmpty()) {
                out("No roles found.");
            } else {
                printRoleTable(results);
                out("Found: " + results.size());
            }
        });
    }


    private static void registerAssignmentCommands(CommandParser parser) {

        parser.registerCommand("assign-role", "Assign role to user", (scanner, system) -> {
            Optional<User> userOpt = findUser(scanner, system);
            if (userOpt.isEmpty()) return;
            User user = userOpt.get();

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                out("No roles available.");
                return;
            }
            out("Available roles:");
            for (int i = 0; i < roles.size(); i++) {
                outIndent((i + 1) + ". " + roles.get(i).getName());
            }

            int num = askNumber(scanner, "Choose role number: ", 1, roles.size());
            if (num == -1) return;
            Role role = roles.get(num - 1);

            String typeChoice = ask(scanner, "Type (1 - permanent, 2 - temporary): ");
            String reason = ask(scanner, "Reason: ");
            if (reason.isEmpty()) reason = "No reason provided";

            AssignmentMetadata metadata = AssignmentMetadata.now(resolveAssignedBy(system), reason);

            try {
                if (typeChoice.equals("2")) {
                    String dateStr = ask(scanner, "Expiration date (yyyy-MM-dd): ");
                    TemporaryAssignment assignment = new TemporaryAssignment(
                            user, role, metadata, dateStr + "T23:59:59", false);
                    system.getAssignmentManager().add(assignment);
                    out("Temporary assignment created.");
                } else {
                    PermanentAssignment assignment = new PermanentAssignment(user, role, metadata);
                    system.getAssignmentManager().add(assignment);
                    out("Permanent assignment created.");
                }
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Revoke role from user", (scanner, system) -> {
            Optional<User> userOpt = findUser(scanner, system);
            if (userOpt.isEmpty()) return;

            List<RoleAssignment> active = system.getAssignmentManager()
                    .findByUser(userOpt.get()).stream()
                    .filter(RoleAssignment::isActive).toList();
            if (active.isEmpty()) {
                out("No active assignments for '" + userOpt.get().username() + "'.");
                return;
            }

            out("Active assignments:");
            for (int i = 0; i < active.size(); i++) {
                RoleAssignment a = active.get(i);
                outIndent((i + 1) + ". " + a.role().getName()
                        + " [" + a.assignmentType() + "] ID: " + a.assignmentId());
            }

            int num = askNumber(scanner, "Choose number to revoke: ", 1, active.size());
            if (num == -1) return;

            system.getAssignmentManager().revokeAssignment(active.get(num - 1).assignmentId());
            out("Assignment revoked.");
        });

        parser.registerCommand("assignment-list", "List all assignments", (scanner, system) -> {
            List<RoleAssignment> all = system.getAssignmentManager().findAll();
            if (all.isEmpty()) {
                out("No assignments found.");
                return;
            }
            printAssignmentTable(all);
        });

        parser.registerCommand("assignment-list-user", "Assignments for a user", (scanner, system) -> {
            Optional<User> userOpt = findUser(scanner, system);
            if (userOpt.isEmpty()) return;

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(userOpt.get());
            if (assignments.isEmpty()) {
                out("No assignments for '" + userOpt.get().username() + "'.");
                return;
            }
            for (RoleAssignment a : assignments) {
                if (a instanceof AbstractRoleAssignment abs) {
                    out(abs.summary().replace("\n", "\n" + "-+ "));
                }
                out("");
            }
        });

        parser.registerCommand("assignment-list-role", "Users with a specific role", (scanner, system) -> {
            Optional<Role> roleOpt = findRole(scanner, system);
            if (roleOpt.isEmpty()) return;

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(roleOpt.get());
            if (assignments.isEmpty()) {
                out("No users have role '" + roleOpt.get().getName() + "'.");
                return;
            }
            out("Users with role '" + roleOpt.get().getName() + "':");
            for (RoleAssignment a : assignments) {
                outIndent("- " + a.user().username()
                        + " [" + a.assignmentType() + "] "
                        + (a.isActive() ? "ACTIVE" : "INACTIVE"));
            }
        });

        parser.registerCommand("assignment-active", "List active assignments", (scanner, system) -> {
            List<RoleAssignment> active = system.getAssignmentManager().getActiveAssignments();
            if (active.isEmpty()) {
                out("No active assignments.");
                return;
            }
            printAssignmentTable(active);
        });

        parser.registerCommand("assignment-expired", "List expired temporary assignments", (scanner, system) -> {
            List<RoleAssignment> expired = system.getAssignmentManager().getExpiredAssignments().stream()
                    .filter(TemporaryAssignment.class::isInstance)
                    .toList();
            if (expired.isEmpty()) {
                out("No expired temporary assignments.");
                return;
            }
            printAssignmentTable(expired);
        });

        parser.registerCommand("assignment-extend", "Extend temporary assignment", (scanner, system) -> {
            out("Find assignment by:");
            outIndent("1. Assignment ID");
            outIndent("2. Username + Role");
            String choice = ask(scanner, "Choose: ");

            String assignmentId;

            if (choice.equals("1")) {
                assignmentId = ask(scanner, "Enter assignment ID: ");
            } else if (choice.equals("2")) {
                Optional<User> userOpt = findUser(scanner, system);
                if (userOpt.isEmpty()) return;
                Optional<Role> roleOpt = findRole(scanner, system);
                if (roleOpt.isEmpty()) return;

                Optional<RoleAssignment> found = system.getAssignmentManager()
                        .findByUser(userOpt.get()).stream()
                        .filter(a -> a.role().equals(roleOpt.get())
                                && a instanceof TemporaryAssignment)
                        .findFirst();
                if (found.isEmpty()) {
                    out("Temporary assignment not found.");
                    return;
                }
                assignmentId = found.get().assignmentId();
            } else {
                out("Invalid choice.");
                return;
            }

            String dateStr = ask(scanner, "New expiration date (yyyy-MM-dd): ");
            try {
                system.getAssignmentManager().extendTemporaryAssignment(
                        assignmentId, dateStr + "T23:59:59");
                out("Assignment extended to " + dateStr + ".");
            } catch (IllegalArgumentException e) {
                out("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Search assignments by filters", (scanner, system) -> {
            out("Search by:");
            outIndent("1. Username");
            outIndent("2. Role");
            outIndent("3. Type (permanent/temporary)");
            outIndent("4. Status (active/inactive)");
            outIndent("5. Assigned after date");
            outIndent("6. Expiring before date");
            String choice = ask(scanner, "Choose: ");

            AssignmentFilter filter;
            switch (choice) {
                case "1" -> {
                    String u = ask(scanner, "Username: ");
                    filter = a -> a.user().username().equalsIgnoreCase(u);
                }
                case "2" -> {
                    String r = ask(scanner, "Role name: ");
                    filter = a -> a.role().getName().equalsIgnoreCase(r);
                }
                case "3" -> {
                    String t = ask(scanner, "Type (permanent/temporary): ").toLowerCase();
                    filter = a -> a.assignmentType().toLowerCase().contains(t);
                }
                case "4" -> {
                    String s = ask(scanner, "Status (active/inactive): ").toLowerCase();
                    boolean wantActive = s.equals("active");
                    filter = a -> a.isActive() == wantActive;
                }
                case "5" -> {
                    String dateStr = ask(scanner, "After date (yyyy-MM-dd): ");
                    LocalDateTime afterDate;
                    try {
                        afterDate = LocalDate.parse(dateStr).atStartOfDay();
                    } catch (DateTimeParseException e) {
                        out("Invalid date format.");
                        return;
                    }
                    filter = a -> {
                        try {
                            return LocalDateTime.parse(a.metadata().assignedAt()).isAfter(afterDate);
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    };
                }
                case "6" -> {
                    String dateStr = ask(scanner, "Before date (yyyy-MM-dd): ");
                    LocalDateTime beforeDate;
                    try {
                        beforeDate = LocalDate.parse(dateStr).atStartOfDay();
                    } catch (DateTimeParseException e) {
                        out("Invalid date format.");
                        return;
                    }
                    filter = a -> {
                        if (!(a instanceof TemporaryAssignment temp)) return false;
                        try {
                            return LocalDateTime.parse(temp.getExpiresAt()).isBefore(beforeDate);
                        } catch (DateTimeParseException e) {
                            return false;
                        }
                    };
                }
                default -> {
                    out("Invalid choice.");
                    return;
                }
            }

            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                out("No assignments found.");
            } else {
                printAssignmentTable(results);
            }
        });
    }


    private static void registerPermissionCommands(CommandParser parser) {

        parser.registerCommand("permissions-user", "All permissions for a user", (scanner, system) -> {
            Optional<User> userOpt = findUser(scanner, system);
            if (userOpt.isEmpty()) return;
            User user = userOpt.get();

            Set<Permission> perms = system.getAssignmentManager().getUserPermissions(user);
            if (perms.isEmpty()) {
                out("No permissions for '" + user.username() + "'.");
                return;
            }

            Map<String, List<Permission>> grouped = perms.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            out("Permissions for '" + user.username() + "':");
            for (Map.Entry<String, List<Permission>> entry : grouped.entrySet()) {
                outIndent("Resource: " + entry.getKey());
                for (Permission p : entry.getValue()) {
                    System.out.println("-+ " + "    - " + p.name() + ": " + p.description());
                }
            }
        });

        parser.registerCommand("permissions-check", "Check if user has specific permission", (scanner, system) -> {
            Optional<User> userOpt = findUser(scanner, system);
            if (userOpt.isEmpty()) return;
            User user = userOpt.get();

            String permName = ask(scanner, "Permission name: ");
            String resource = ask(scanner, "Resource: ");

            boolean has = system.getAssignmentManager().userHasPermission(user, permName, resource);

            if (has) {
                out("YES: '" + user.username() + "' has " + permName + " on " + resource);
                List<RoleAssignment> grantedBy = system.getAssignmentManager().findByUser(user).stream()
                        .filter(RoleAssignment::isActive)
                        .filter(a -> a.role().hasPermission(permName, resource))
                        .toList();
                out("Granted by role(s):");
                for (RoleAssignment a : grantedBy) {
                    outIndent("- " + a.role().getName());
                }
            } else {
                out("NO: '" + user.username() + "' does NOT have " + permName + " on " + resource);
            }
        });
    }

    private static void registerServiceCommands(CommandParser parser) {

        parser.registerCommand("help", "Show available commands", (scanner, system) ->
                parser.printHelp());

        parser.registerCommand("stats", "Show system statistics", (scanner, system) ->
                System.out.println(system.generateStatistics()));

        parser.registerCommand("clear", "Clear screen", (scanner, system) -> {
            for (int i = 0; i < 50; i++) System.out.println();
        });

        parser.registerCommand("exit", "Exit the program", (scanner, system) -> {
            if (confirm(scanner, "Are you sure you want to exit?")) {
                out("Shutting down...");
                system.shutdown();
                out("Goodbye!");
                System.exit(0);
            } else {
                out("Exit cancelled.");
            }
        });

        parser.registerCommand("save", "Save data to file (not implemented)", (scanner, system) ->
                out("Save feature is not implemented."));

        parser.registerCommand("load", "Load data from file (not implemented)", (scanner, system) ->
                out("Load feature is not implemented."));

        parser.registerCommand("report-users-async", "Generate user report asynchronously", (scanner, system) -> {
            out("Starting async user report generation...");

            system.getAuditLog().logAsync("REPORT_START",
                    system.getCurrentUser(),
                    "reports",
                    "Async user report generation started");

            system.getBackgroundExecutor().submit(() -> {
                try {
                    ReportGenerator generator = new ReportGenerator();
                    String report = generator.generateUserReportParallel(
                            system.getUserManager(),
                            system.getAssignmentManager()
                    );

                    System.out.println("\n" + report);

                    system.getAuditLog().logAsync("REPORT_COMPLETE",
                            system.getCurrentUser(),
                            "reports",
                            "User report generated successfully");

                } catch (Exception e) {
                    System.out.println("-+ Error generating report: " + e.getMessage());
                    system.getAuditLog().logAsync("REPORT_ERROR",
                            system.getCurrentUser(),
                            "reports",
                            "Report generation failed: " + e.getMessage());
                }
            });

            out("Report generation started in background. Result will appear shortly.");
        });

        parser.registerCommand("report-matrix-async", "Generate permission matrix asynchronously", (scanner, system) -> {
            out("Starting async permission matrix generation...");

            system.getAuditLog().logAsync("MATRIX_START",
                    system.getCurrentUser(),
                    "reports",
                    "Async permission matrix generation started");

            system.getBackgroundExecutor().submit(() -> {
                try {
                    ReportGenerator generator = new ReportGenerator();
                    String matrix = generator.generatePermissionMatrixParallel(
                            system.getUserManager(),
                            system.getAssignmentManager()
                    );

                    System.out.println("\n" + matrix);

                    system.getAuditLog().logAsync("MATRIX_COMPLETE",
                            system.getCurrentUser(),
                            "reports",
                            "Permission matrix generated successfully");

                } catch (Exception e) {
                    System.out.println("-+ Error generating matrix: " + e.getMessage());
                    system.getAuditLog().logAsync("MATRIX_ERROR",
                            system.getCurrentUser(),
                            "reports",
                            "Matrix generation failed: " + e.getMessage());
                }
            });

            out("Matrix generation started in background.");
        });

        parser.registerCommand("save-async", "Save data to file asynchronously", (scanner, system) -> {
            String filename = ask(scanner, "Enter filename (default: rbac_data.txt): ");
            if (filename.isEmpty()) {
                filename = "rbac_data.txt";
            }

            final String finalFilename = filename;
            out("Starting async save to '" + finalFilename + "'...");

            system.getAuditLog().logAsync("SAVE_START",
                    system.getCurrentUser(),
                    finalFilename,
                    "Async save started");

            system.getBackgroundExecutor().submit(() -> {
                try (PrintWriter writer = new PrintWriter(new FileWriter(finalFilename))) {

                    writer.println("=== USERS ===");
                    writer.println("Total: " + system.getUserManager().count());
                    writer.println();
                    for (User u : system.getUserManager().findAll()) {
                        writer.println(u.format());
                    }

                    writer.println("\n=== ROLES ===");
                    writer.println("Total: " + system.getRoleManager().count());
                    writer.println();
                    for (Role r : system.getRoleManager().findAll()) {
                        writer.println(r.getName() + " - " + r.getDescription());
                        writer.println("  Permissions: " + r.getPermissions().size());
                        for (Permission p : r.getPermissions()) {
                            writer.println("    - " + p.format());
                        }
                        writer.println();
                    }

                    writer.println("=== ASSIGNMENTS ===");
                    writer.println("Total: " + system.getAssignmentManager().count());
                    writer.println();
                    for (RoleAssignment a : system.getAssignmentManager().findAll()) {
                        writer.printf("%s -> %s [%s] %s%n",
                                a.user().username(),
                                a.role().getName(),
                                a.assignmentType(),
                                a.isActive() ? "ACTIVE" : "INACTIVE");
                        writer.println("  " + a.metadata().format());
                        writer.println();
                    }

                    System.out.println("-+ Data saved to '" + finalFilename + "'");

                    system.getAuditLog().logAsync("SAVE_COMPLETE",
                            system.getCurrentUser(),
                            finalFilename,
                            "Data saved successfully");

                } catch (IOException e) {
                    System.out.println("-+ Error saving data: " + e.getMessage());
                    system.getAuditLog().logAsync("SAVE_ERROR",
                            system.getCurrentUser(),
                            finalFilename,
                            "Save failed: " + e.getMessage());
                }
            });

            out("Save started in background.");
        });

        parser.registerCommand("audit-status", "Show audit log status", (scanner, system) -> {
            AuditLog auditLog = system.getAuditLog();
            out("Audit Log Status:");
            outIndent("Processor running: " + auditLog.isProcessorRunning());
            outIndent("Pending entries: " + auditLog.getPendingCount());
            outIndent("Total entries: " + auditLog.getAll().size());
        });

        parser.registerCommand("audit-log", "Show audit log", (scanner, system) -> {
            system.getAuditLog().printLog();
        });

        parser.registerCommand("audit-save", "Save audit log to file", (scanner, system) -> {
            String filename = ask(scanner, "Enter filename (default: audit.log): ");
            if (filename.isEmpty()) {
                filename = "audit.log";
            }
            system.getAuditLog().saveToFile(filename);
        });
    }
}
