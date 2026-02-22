package org.example.filter;

import org.example.entity.Permission;
import java.util.Objects;

public class RoleFilters {

    private RoleFilters() {}

    public static RoleFilter byName(String name) {
        Objects.requireNonNull(name);
        return role -> role.getName().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        Objects.requireNonNull(substring);
        return role -> role.getName().toLowerCase().contains(substring.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission) {
        Objects.requireNonNull(permission);
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        Objects.requireNonNull(permissionName);
        Objects.requireNonNull(resource);
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return role -> role.getPermissions().size() >= n;
    }
}