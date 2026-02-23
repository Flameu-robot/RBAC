package org.example.repository;

import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.filter.RoleFilter;

import java.util.*;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {

    private final Map<String, Role> rolesById = new HashMap<>();

    private final Map<String, Role> rolesByName = new HashMap<>();

    @Override
    public void add(Role item) {
        Objects.requireNonNull(item, "Role cannot be null");

        if (rolesById.containsKey(item.getId())) {
            throw new IllegalArgumentException("Role with ID '" + item.getId() + "' already exists");
        }
        if (rolesByName.containsKey(item.getName())) {
            throw new IllegalArgumentException("Role with name '" + item.getName() + "' already exists");
        }

        rolesById.put(item.getId(), item);
        rolesByName.put(item.getName(), item);
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) return false;

        Role removed = rolesById.remove(item.getId());
        if (removed != null) {
            rolesByName.remove(removed.getName());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role '" + roleName + "' not found");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalArgumentException("Role '" + roleName + "' not found");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }
}
