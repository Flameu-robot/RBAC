package org.example.sorter;

import org.example.entity.Role;
import java.util.Comparator;

public class RoleSorters {

    private RoleSorters() {}

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(r -> r.getPermissions().size());
    }
}