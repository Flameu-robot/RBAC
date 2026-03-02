package util;

import org.example.assignment.PermanentAssignment;
import org.example.entity.*;
import org.example.repository.*;
import org.example.utils.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private ReportGenerator generator;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        generator = new ReportGenerator();
    }

    private void setupTestData() {
        Permission readUsers = new Permission("READ", "users", "Read users");
        Permission writeUsers = new Permission("WRITE", "users", "Write users");
        Permission readReports = new Permission("READ", "reports", "Read reports");

        Role admin = new Role("Admin", "Full access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(readReports);
        roleManager.add(admin);

        Role viewer = new Role("Viewer", "Read only");
        viewer.addPermission(readUsers);
        viewer.addPermission(readReports);
        roleManager.add(viewer);

        User alice = new User("alice", "Alice Smith", "alice@test.com");
        User bob = new User("bob_dev", "Bob Johnson", "bob@test.com");
        userManager.add(alice);
        userManager.add(bob);

        AssignmentMetadata meta = AssignmentMetadata.now("system", "test");
        assignmentManager.add(new PermanentAssignment(alice, admin, meta));
        assignmentManager.add(new PermanentAssignment(bob, viewer, meta));
    }

    @Test
    void userReportEmpty() {
        String report = generator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("No users"));
    }

    @Test
    void userReportWithData() {
        setupTestData();
        String report = generator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("User Report"));
        assertTrue(report.contains("alice"));
        assertTrue(report.contains("bob_dev"));
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Total users: 2"));
    }

    @Test
    void userReportShowsPermissions() {
        setupTestData();
        String report = generator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("READ"));
        assertTrue(report.contains("WRITE"));
    }

    @Test
    void userReportUserWithoutRoles() {
        userManager.add(new User("lonely", "Lonely User", "lonely@test.com"));
        String report = generator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("lonely"));
        assertTrue(report.contains("none"));
    }

    @Test
    void roleReportEmpty() {
        String report = generator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("No roles"));
    }

    @Test
    void roleReportWithData() {
        setupTestData();
        String report = generator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("Role Report"));
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Total roles: 2"));
    }

    @Test
    void roleReportShowsPermissionCount() {
        setupTestData();
        String report = generator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.contains("Permissions"));
        assertTrue(report.contains("Active Users"));
    }

    @Test
    void permissionMatrixEmpty() {
        String matrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(matrix.contains("No users"));
    }

    @Test
    void permissionMatrixNoPermissions() {
        userManager.add(new User("testuser", "Test User", "test@test.com"));
        String matrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(matrix.contains("No permissions assigned"));
    }

    @Test
    void permissionMatrixWithData() {
        setupTestData();
        String matrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(matrix.contains("Permission Matrix"));
        assertTrue(matrix.contains("alice"));
        assertTrue(matrix.contains("bob_dev"));
        assertTrue(matrix.contains("users"));
        assertTrue(matrix.contains("reports"));
    }

    @Test
    void permissionMatrixShowsPermissionNames() {
        setupTestData();
        String matrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(matrix.contains("READ"));
    }

    @Test
    void permissionMatrixDashForMissing() {
        setupTestData();
        String matrix = generator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(matrix.contains("-"));
    }

    @Test
    void exportToFile(@TempDir Path tempDir) throws IOException {
        String report = "Test report content\nLine 2";
        Path file = tempDir.resolve("report.txt");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        generator.exportToFile(report, file.toString());

        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertEquals(report, content);
        assertTrue(out.toString().contains("saved"));
    }

    @Test
    void exportToFileInvalidPath() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        generator.exportToFile("test", "/invalid/path/that/does/not/exist/file.txt");
        assertTrue(out.toString().contains("Error"));
    }
}
