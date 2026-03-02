package org.example.utils;

import org.example.entity.*;
import org.example.repository.*;
import org.example.assignment.RoleAssignment;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== User Report ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users in system.\n");
            return sb.toString();
        }

        for (User user : users) {
            sb.append("User: ").append(user.format()).append("\n");

            List<RoleAssignment> active = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive).toList();

            sb.append("  Roles (").append(active.size()).append("):");
            if (active.isEmpty()) {
                sb.append(" none");
            } else {
                for (RoleAssignment a : active) {
                    sb.append("\n    - ").append(a.role().getName())
                            .append(" [").append(a.assignmentType()).append("]");
                }
            }
            sb.append("\n");

            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            sb.append("  Permissions (").append(perms.size()).append("):");
            if (perms.isEmpty()) {
                sb.append(" none");
            } else {
                for (Permission p : perms) {
                    sb.append("\n    - ").append(p.format());
                }
            }
            sb.append("\n\n");
        }

        sb.append("Total users: ").append(users.size()).append("\n");
        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Role Report ===\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("No roles in system.\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s %-15s %-15s %-20s%n", "Role", "Permissions", "Active Users", "ID"));
        sb.append("-".repeat(70)).append("\n");

        for (Role role : roles) {
            long activeUsers = assignmentManager.findByRole(role).stream()
                    .filter(RoleAssignment::isActive).count();
            sb.append(String.format("%-20s %-15d %-15d %-20s%n",
                    role.getName(),
                    role.getPermissions().size(),
                    activeUsers,
                    role.getId()));
        }

        sb.append("\nTotal roles: ").append(roles.size()).append("\n");
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Permission Matrix ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users in system.\n");
            return sb.toString();
        }

        Set<String> allResources = new TreeSet<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            for (Permission p : perms) {
                allResources.add(p.resource());
            }
        }

        if (allResources.isEmpty()) {
            sb.append("No permissions assigned.\n");
            return sb.toString();
        }

        List<String> resourceList = new ArrayList<>(allResources);

        int userColWidth = 15;
        int resColWidth = 20;

        sb.append(String.format("%-" + userColWidth + "s", "User"));
        for (String res : resourceList) {
            sb.append(String.format("%-" + resColWidth + "s", res));
        }
        sb.append("\n");
        sb.append("-".repeat(userColWidth + resColWidth * resourceList.size())).append("\n");

        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            sb.append(String.format("%-" + userColWidth + "s", user.username()));

            for (String resource : resourceList) {
                String permNames = perms.stream()
                        .filter(p -> p.resource().equals(resource))
                        .map(Permission::name)
                        .sorted()
                        .collect(Collectors.joining(","));
                sb.append(String.format("%-" + resColWidth + "s",
                        permNames.isEmpty() ? "-" : permNames));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.print(report);
            System.out.println("-+ Report saved to " + filename);
        } catch (IOException e) {
            System.out.println("-+ Error saving report: " + e.getMessage());
        }
    }
}

