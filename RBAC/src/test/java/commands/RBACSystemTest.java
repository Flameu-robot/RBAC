package commands;


import org.example.entity.Role;
import org.example.entity.User;
import org.example.system.RBACSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    void managersNotNull() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
    }

    @Test
    void initializeCreatesAdmin() {
        system.initialize();
        assertTrue(system.getUserManager().exists("admin"));
    }

    @Test
    void initializeCreatesRoles() {
        system.initialize();
        assertTrue(system.getRoleManager().exists("Admin"));
        assertTrue(system.getRoleManager().exists("Manager"));
        assertTrue(system.getRoleManager().exists("Viewer"));
    }

    @Test
    void initializeCreatesAssignment() {
        system.initialize();
        assertEquals(1, system.getAssignmentManager().count());
    }

    @Test
    void initializeSetsCurrentUser() {
        system.initialize();
        assertEquals("admin", system.getCurrentUser());
    }

    @Test
    void initializeAdminHasPermissions() {
        system.initialize();
        User admin = system.getUserManager().findByUsername("admin").get();
        assertTrue(system.getAssignmentManager().userHasPermission(admin, "READ", "users"));
        assertTrue(system.getAssignmentManager().userHasPermission(admin, "WRITE", "users"));
        assertTrue(system.getAssignmentManager().userHasPermission(admin, "DELETE", "users"));
    }

    @Test
    void initializeAdminRoleHas9Permissions() {
        system.initialize();
        Optional<Role> admin = system.getRoleManager().findByName("Admin");
        assertTrue(admin.isPresent());
        assertEquals(9, admin.get().getPermissions().size());
    }

    @Test
    void initializeManagerRoleHas5Permissions() {
        system.initialize();
        Optional<Role> manager = system.getRoleManager().findByName("Manager");
        assertTrue(manager.isPresent());
        assertEquals(5, manager.get().getPermissions().size());
    }

    @Test
    void initializeViewerRoleHas3Permissions() {
        system.initialize();
        Optional<Role> viewer = system.getRoleManager().findByName("Viewer");
        assertTrue(viewer.isPresent());
        assertEquals(3, viewer.get().getPermissions().size());
    }

    @Test
    void setCurrentUser() {
        system.setCurrentUser("testuser");
        assertEquals("testuser", system.getCurrentUser());
    }

    @Test
    void currentUserInitiallyNull() {
        assertNull(system.getCurrentUser());
    }

    @Test
    void generateStatisticsEmpty() {
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("0"));
    }

    @Test
    void generateStatisticsAfterInitialize() {
        system.initialize();
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Users"));
        assertTrue(stats.contains("Roles"));
        assertTrue(stats.contains("Assignments"));
        assertTrue(stats.contains("Admin"));
    }

    @Test
    void generateStatisticsContainsAvgRoles() {
        system.initialize();
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Avg roles per user"));
    }

    @Test
    void generateStatisticsContainsTop3() {
        system.initialize();
        String stats = system.generateStatistics();
        assertTrue(stats.contains("Top-3"));
    }
}
