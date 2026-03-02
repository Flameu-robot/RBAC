package managers;

import org.example.repository.AssignmentManager;
import org.example.entity.*;
import org.example.assignment.*;
import org.example.filter.AssignmentFilter;
import org.example.repository.RoleManager;
import org.example.repository.UserManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    private User alice;
    private User bob;
    private Role admin;
    private Role viewer;
    private Permission readPerm;
    private Permission writePerm;
    private AssignmentMetadata metadata;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        alice = new User("alice", "Alice Smith", "alice@test.com");
        bob = new User("bob_dev", "Bob Johnson", "bob@test.com");
        userManager.add(alice);
        userManager.add(bob);

        readPerm = new Permission("READ", "users", "Read users");
        writePerm = new Permission("WRITE", "users", "Write users");

        admin = new Role("Admin", "Full access");
        admin.addPermission(readPerm);
        admin.addPermission(writePerm);

        viewer = new Role("Viewer", "Read-only");
        viewer.addPermission(readPerm);

        roleManager.add(admin);
        roleManager.add(viewer);

        metadata = AssignmentMetadata.now("system", "test");
    }

    @Test
    void addPermanentAssignment() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void addTemporaryAssignment() {
        String future = LocalDateTime.now().plusDays(30)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        TemporaryAssignment assignment = new TemporaryAssignment(alice, admin, metadata, future, false);
        assignmentManager.add(assignment);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void addNullThrows() {
        assertThrows(NullPointerException.class, () -> assignmentManager.add(null));
    }

    @Test
    void addWithNonExistentUserThrows() {
        User unknown = new User("unknown", "Unknown User", "unknown@test.com");
        PermanentAssignment assignment = new PermanentAssignment(unknown, admin, metadata);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment));
    }

    @Test
    void addWithNonExistentRoleThrows() {
        Role unknown = new Role("Unknown", "Unknown role");
        PermanentAssignment assignment = new PermanentAssignment(alice, unknown, metadata);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(assignment));
    }

    @Test
    void addDuplicateActiveAssignmentThrows() {
        PermanentAssignment a1 = new PermanentAssignment(alice, admin, metadata);
        PermanentAssignment a2 = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(a1);
        assertThrows(IllegalArgumentException.class, () -> assignmentManager.add(a2));
    }

    @Test
    void addAfterRevokeAllowed() {
        PermanentAssignment a1 = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(a1);
        assignmentManager.revokeAssignment(a1.assignmentId());

        PermanentAssignment a2 = new PermanentAssignment(alice, admin, metadata);
        assertDoesNotThrow(() -> assignmentManager.add(a2));
        assertEquals(2, assignmentManager.count());
    }

    @Test
    void removeAssignment() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);
        assertTrue(assignmentManager.remove(assignment));
        assertEquals(0, assignmentManager.count());
    }

    @Test
    void removeNull() {
        assertFalse(assignmentManager.remove(null));
    }

    @Test
    void removeNonExistent() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assertFalse(assignmentManager.remove(assignment));
    }

    @Test
    void findById() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);

        Optional<RoleAssignment> found = assignmentManager.findById(assignment.assignmentId());
        assertTrue(found.isPresent());
        assertEquals(assignment.assignmentId(), found.get().assignmentId());
    }

    @Test
    void findByIdNotFound() {
        assertTrue(assignmentManager.findById("nonexistent").isEmpty());
    }

    @Test
    void findAll() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));
        assertEquals(2, assignmentManager.findAll().size());
    }

    @Test
    void findAllEmpty() {
        assertTrue(assignmentManager.findAll().isEmpty());
    }

    @Test
    void count() {
        assertEquals(0, assignmentManager.count());
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void clear() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));
        assignmentManager.clear();
        assertEquals(0, assignmentManager.count());
    }

    @Test
    void findByUser() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(alice, viewer, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));

        List<RoleAssignment> aliceAssignments = assignmentManager.findByUser(alice);
        assertEquals(2, aliceAssignments.size());

        List<RoleAssignment> bobAssignments = assignmentManager.findByUser(bob);
        assertEquals(1, bobAssignments.size());
    }

    @Test
    void findByUserNone() {
        List<RoleAssignment> result = assignmentManager.findByUser(alice);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByRole() {
        assignmentManager.add(new PermanentAssignment(alice, viewer, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));

        List<RoleAssignment> viewerAssignments = assignmentManager.findByRole(viewer);
        assertEquals(2, viewerAssignments.size());
    }

    @Test
    void findByRoleNone() {
        assertTrue(assignmentManager.findByRole(admin).isEmpty());
    }

    @Test
    void getActiveAssignments() {
        PermanentAssignment active = new PermanentAssignment(alice, admin, metadata);
        PermanentAssignment revoked = new PermanentAssignment(bob, viewer, metadata);
        assignmentManager.add(active);
        assignmentManager.add(revoked);
        assignmentManager.revokeAssignment(revoked.assignmentId());

        List<RoleAssignment> activeList = assignmentManager.getActiveAssignments();
        assertEquals(1, activeList.size());
        assertEquals(active.assignmentId(), activeList.getFirst().assignmentId());
    }

    @Test
    void getExpiredAssignments() {
        PermanentAssignment active = new PermanentAssignment(alice, admin, metadata);
        PermanentAssignment revoked = new PermanentAssignment(bob, viewer, metadata);
        assignmentManager.add(active);
        assignmentManager.add(revoked);
        assignmentManager.revokeAssignment(revoked.assignmentId());

        List<RoleAssignment> expired = assignmentManager.getExpiredAssignments();
        assertEquals(1, expired.size());
        assertEquals(revoked.assignmentId(), expired.getFirst().assignmentId());
    }

    @Test
    void getExpiredTemporaryAssignments() {
        String past = LocalDateTime.now().minusDays(1)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        TemporaryAssignment expired = new TemporaryAssignment(alice, admin, metadata, past, false);
        assignmentManager.add(expired);

        List<RoleAssignment> expiredList = assignmentManager.getExpiredAssignments();
        assertEquals(1, expiredList.size());
    }

    @Test
    void userHasRole() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assertTrue(assignmentManager.userHasRole(alice, admin));
        assertFalse(assignmentManager.userHasRole(alice, viewer));
        assertFalse(assignmentManager.userHasRole(bob, admin));
    }

    @Test
    void userHasRoleAfterRevoke() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);
        assignmentManager.revokeAssignment(assignment.assignmentId());
        assertFalse(assignmentManager.userHasRole(alice, admin));
    }

    @Test
    void userHasPermission() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assertTrue(assignmentManager.userHasPermission(alice, "READ", "users"));
        assertTrue(assignmentManager.userHasPermission(alice, "WRITE", "users"));
        assertFalse(assignmentManager.userHasPermission(alice, "DELETE", "users"));
    }

    @Test
    void userHasPermissionCaseInsensitive() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assertTrue(assignmentManager.userHasPermission(alice, "read", "USERS"));
    }

    @Test
    void userHasPermissionNoAssignments() {
        assertFalse(assignmentManager.userHasPermission(alice, "READ", "users"));
    }

    @Test
    void getUserPermissions() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(alice, viewer, metadata));

        Set<Permission> perms = assignmentManager.getUserPermissions(alice);
        assertEquals(2, perms.size());
        assertTrue(perms.contains(readPerm));
        assertTrue(perms.contains(writePerm));
    }

    @Test
    void getUserPermissionsEmpty() {
        Set<Permission> perms = assignmentManager.getUserPermissions(alice);
        assertTrue(perms.isEmpty());
    }

    @Test
    void getUserPermissionsIgnoresRevoked() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);
        assignmentManager.revokeAssignment(assignment.assignmentId());

        Set<Permission> perms = assignmentManager.getUserPermissions(alice);
        assertTrue(perms.isEmpty());
    }

    @Test
    void revokePermanentAssignment() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);
        assignmentManager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
        assertTrue(assignment.isRevoked());
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void revokeTemporaryAssignment() {
        String future = LocalDateTime.now().plusDays(30)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        TemporaryAssignment assignment = new TemporaryAssignment(alice, admin, metadata, future, false);
        assignmentManager.add(assignment);
        assignmentManager.revokeAssignment(assignment.assignmentId());

        assertEquals(0, assignmentManager.count());
    }

    @Test
    void revokeNonExistentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.revokeAssignment("nonexistent"));
    }

    @Test
    void extendTemporaryAssignment() {
        String future = LocalDateTime.now().plusDays(10)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        TemporaryAssignment assignment = new TemporaryAssignment(alice, admin, metadata, future, false);
        assignmentManager.add(assignment);

        String newDate = LocalDateTime.now().plusDays(60)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), newDate);

        assertEquals(newDate, assignment.getExpiresAt());
    }

    @Test
    void extendPermanentThrows() {
        PermanentAssignment assignment = new PermanentAssignment(alice, admin, metadata);
        assignmentManager.add(assignment);

        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.extendTemporaryAssignment(assignment.assignmentId(), "2030-01-01T00:00:00"));
    }

    @Test
    void extendNonExistentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.extendTemporaryAssignment("nonexistent", "2030-01-01T00:00:00"));
    }

    @Test
    void findByFilter() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));

        AssignmentFilter filter = a -> a.user().username().equals("alice");
        List<RoleAssignment> results = assignmentManager.findByFilter(filter);
        assertEquals(1, results.size());
        assertEquals("alice", results.getFirst().user().username());
    }

    @Test
    void findByFilterNoMatch() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        AssignmentFilter filter = a -> a.user().username().equals("nobody");
        assertTrue(assignmentManager.findByFilter(filter).isEmpty());
    }

    @Test
    void findAllWithFilterAndSorter() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));
        assignmentManager.add(new PermanentAssignment(alice, viewer, metadata));

        AssignmentFilter all = a -> true;
        Comparator<RoleAssignment> byUser = Comparator.comparing(a -> a.user().username());
        List<RoleAssignment> sorted = assignmentManager.findAll(all, byUser);

        assertEquals(3, sorted.size());
        assertEquals("alice", sorted.get(0).user().username());
        assertEquals("alice", sorted.get(1).user().username());
        assertEquals("bob_dev", sorted.get(2).user().username());
    }

    @Test
    void multipleRolesMultipleUsers() {
        assignmentManager.add(new PermanentAssignment(alice, admin, metadata));
        assignmentManager.add(new PermanentAssignment(alice, viewer, metadata));
        assignmentManager.add(new PermanentAssignment(bob, viewer, metadata));

        assertEquals(3, assignmentManager.count());
        assertEquals(2, assignmentManager.findByUser(alice).size());
        assertEquals(1, assignmentManager.findByUser(bob).size());
        assertEquals(2, assignmentManager.findByRole(viewer).size());
        assertEquals(1, assignmentManager.findByRole(admin).size());
    }
}
