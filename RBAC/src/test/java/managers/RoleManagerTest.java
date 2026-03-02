package managers;

import org.example.repository.RoleManager;
import org.example.entity.Permission;
import org.example.entity.Role;
import org.example.filter.RoleFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {

    private RoleManager manager;
    private Role admin;
    private Role viewer;
    private Role editor;
    private Permission readPerm;
    private Permission writePerm;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
        admin = new Role("Admin", "Full access");
        viewer = new Role("Viewer", "Read-only access");
        editor = new Role("Editor", "Edit access");
        readPerm = new Permission("READ", "users", "Read users");
        writePerm = new Permission("WRITE", "users", "Write users");
    }

    @Test
    void addRole() {
        manager.add(admin);
        assertEquals(1, manager.count());
        assertTrue(manager.exists("Admin"));
    }

    @Test
    void addNullThrows() {
        assertThrows(NullPointerException.class, () -> manager.add(null));
    }

    @Test
    void addDuplicateNameThrows() {
        manager.add(admin);
        Role duplicate = new Role("Admin", "Another admin");
        assertThrows(IllegalArgumentException.class, () -> manager.add(duplicate));
    }

    @Test
    void removeRole() {
        manager.add(admin);
        assertTrue(manager.remove(admin));
        assertEquals(0, manager.count());
        assertFalse(manager.exists("Admin"));
    }

    @Test
    void removeNull() {
        assertFalse(manager.remove(null));
    }

    @Test
    void removeNonExistent() {
        assertFalse(manager.remove(admin));
    }

    @Test
    void findById() {
        manager.add(admin);
        Optional<Role> found = manager.findById(admin.getId());
        assertTrue(found.isPresent());
        assertEquals("Admin", found.get().getName());
    }

    @Test
    void findByIdNotFound() {
        assertTrue(manager.findById("nonexistent").isEmpty());
    }

    @Test
    void findByName() {
        manager.add(admin);
        manager.add(viewer);
        Optional<Role> found = manager.findByName("Viewer");
        assertTrue(found.isPresent());
        assertEquals("Viewer", found.get().getName());
    }

    @Test
    void findByNameNotFound() {
        assertTrue(manager.findByName("NonExistent").isEmpty());
    }

    @Test
    void findAll() {
        manager.add(admin);
        manager.add(viewer);
        assertEquals(2, manager.findAll().size());
    }

    @Test
    void findAllEmpty() {
        assertTrue(manager.findAll().isEmpty());
    }

    @Test
    void count() {
        assertEquals(0, manager.count());
        manager.add(admin);
        assertEquals(1, manager.count());
    }

    @Test
    void clear() {
        manager.add(admin);
        manager.add(viewer);
        manager.clear();
        assertEquals(0, manager.count());
        assertFalse(manager.exists("Admin"));
    }

    @Test
    void exists() {
        manager.add(admin);
        assertTrue(manager.exists("Admin"));
        assertFalse(manager.exists("Nobody"));
    }

    @Test
    void addPermissionToRole() {
        manager.add(admin);
        manager.addPermissionToRole("Admin", readPerm);
        assertTrue(admin.hasPermission(readPerm));
    }

    @Test
    void addPermissionToNonExistentRoleThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.addPermissionToRole("Nobody", readPerm));
    }

    @Test
    void removePermissionFromRole() {
        admin.addPermission(readPerm);
        manager.add(admin);
        manager.removePermissionFromRole("Admin", readPerm);
        assertFalse(admin.hasPermission(readPerm));
    }

    @Test
    void removePermissionFromNonExistentRoleThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.removePermissionFromRole("Nobody", readPerm));
    }

    @Test
    void findRolesWithPermission() {
        admin.addPermission(readPerm);
        admin.addPermission(writePerm);
        viewer.addPermission(readPerm);
        manager.add(admin);
        manager.add(viewer);

        List<Role> withRead = manager.findRolesWithPermission("READ", "users");
        assertEquals(2, withRead.size());

        List<Role> withWrite = manager.findRolesWithPermission("WRITE", "users");
        assertEquals(1, withWrite.size());
        assertEquals("Admin", withWrite.get(0).getName());
    }

    @Test
    void findRolesWithPermissionNone() {
        manager.add(admin);
        List<Role> result = manager.findRolesWithPermission("DELETE", "users");
        assertTrue(result.isEmpty());
    }

    @Test
    void findByFilter() {
        manager.add(admin);
        manager.add(viewer);
        manager.add(editor);

        RoleFilter filter = r -> r.getName().length() == 6;
        List<Role> results = manager.findByFilter(filter);
        assertEquals(2, results.size());
    }

    @Test
    void findByFilterNoMatch() {
        manager.add(admin);
        RoleFilter filter = r -> r.getName().contains("zzz");
        assertTrue(manager.findByFilter(filter).isEmpty());
    }

    @Test
    void findAllWithFilterAndSorter() {
        manager.add(admin);
        manager.add(viewer);
        manager.add(editor);

        RoleFilter all = r -> true;
        Comparator<Role> byName = Comparator.comparing(Role::getName);
        List<Role> sorted = manager.findAll(all, byName);

        assertEquals(3, sorted.size());
        assertEquals("Admin", sorted.get(0).getName());
        assertEquals("Editor", sorted.get(1).getName());
        assertEquals("Viewer", sorted.get(2).getName());
    }

    @Test
    void findAllReturnsCopy() {
        manager.add(admin);
        List<Role> list = manager.findAll();
        list.clear();
        assertEquals(1, manager.count());
    }
}