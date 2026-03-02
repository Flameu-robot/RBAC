package commands;

import org.example.system.RBACSystem;
import org.example.system.commands.CommandParser;
import org.example.system.commands.CommandRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandRegistryTest {

    private CommandParser parser;
    private RBACSystem system;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        system.initialize();
        CommandRegistry.registerAll(parser);

        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    private String getOutput() {
        return outputStream.toString();
    }

    private void execute(String command, String input) {
        parser.executeCommand(command, new Scanner(input), system);
    }

    @Test
    void userListShowsInitialAdmin() {
        execute("user-list", "");
        String output = getOutput();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("System Administrator"));
    }

    @Test
    void userCreateSuccess() {
        execute("user-create", "testuser\nTest User\ntest@mail.com\n");
        String output = getOutput();
        assertTrue(output.contains("User created"));
        assertTrue(system.getUserManager().exists("testuser"));
    }

    @Test
    void userCreateInvalidUsername() {
        execute("user-create", "ab\nTest\ntest@mail.com\n");
        assertTrue(getOutput().contains("Error"));
        assertFalse(system.getUserManager().exists("ab"));
    }

    @Test
    void userCreateInvalidEmail() {
        execute("user-create", "testuser\nTest User\ninvalid\n");
        assertTrue(getOutput().contains("Error"));
    }

    @Test
    void userCreateDuplicate() {
        execute("user-create", "admin\nAnother Admin\nadmin2@mail.com\n");
        assertTrue(getOutput().contains("Error"));
    }

    @Test
    void userViewExisting() {
        execute("user-view", "admin\n");
        String output = getOutput();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("Roles"));
        assertTrue(output.contains("Permissions"));
    }

    @Test
    void userViewNonExistent() {
        execute("user-view", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void userUpdateSuccess() {
        execute("user-create", "testuser\nOld Name\nold@mail.com\n");
        outputStream.reset();
        execute("user-update", "testuser\nNew Name\nnew@mail.com\n");
        assertTrue(getOutput().contains("updated"));
        assertEquals("New Name", system.getUserManager().findByUsername("testuser").get().fullname());
    }

    @Test
    void userUpdateNonExistent() {
        execute("user-update", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void userDeleteConfirmed() {
        execute("user-create", "todelete\nTo Delete\ndel@mail.com\n");
        outputStream.reset();
        execute("user-delete", "todelete\nда\n");
        assertTrue(getOutput().contains("deleted"));
        assertFalse(system.getUserManager().exists("todelete"));
    }

    @Test
    void userDeleteCancelled() {
        execute("user-create", "tokeep\nTo Keep\nkeep@mail.com\n");
        outputStream.reset();
        execute("user-delete", "tokeep\nнет\n");
        assertTrue(getOutput().contains("Cancelled"));
        assertTrue(system.getUserManager().exists("tokeep"));
    }

    @Test
    void userDeleteNonExistent() {
        execute("user-delete", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void userSearchByUsername() {
        execute("user-search", "1\nadmin\n");
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void userSearchByEmail() {
        execute("user-search", "2\nsystem.com\n");
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void userSearchByDomain() {
        execute("user-search", "3\nsystem.com\n");
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void userSearchByFullName() {
        execute("user-search", "4\nAdministrator\n");
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void userSearchNoResults() {
        execute("user-search", "1\nzzzzz\n");
        assertTrue(getOutput().contains("No users found"));
    }

    @Test
    void userSearchInvalidChoice() {
        execute("user-search", "9\n");
        assertTrue(getOutput().contains("Invalid choice"));
    }

    @Test
    void roleListShowsInitialRoles() {
        execute("role-list", "");
        String output = getOutput();
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("Manager"));
        assertTrue(output.contains("Viewer"));
    }

    @Test
    void roleCreateSuccess() {
        execute("role-create", "Tester\nTest role\nнет\n");
        assertTrue(getOutput().contains("created"));
        assertTrue(system.getRoleManager().exists("Tester"));
    }

    @Test
    void roleCreateWithPermission() {
        execute("role-create", "Tester\nTest role\nда\nREAD\nfiles\nRead files\nнет\n");
        String output = getOutput();
        assertTrue(output.contains("created"));
        assertTrue(output.contains("Permission added"));
    }

    @Test
    void roleCreateDuplicate() {
        execute("role-create", "Admin\nDuplicate\nнет\n");
        assertTrue(getOutput().contains("Error"));
    }

    @Test
    void roleViewExisting() {
        execute("role-view", "Admin\n");
        String output = getOutput();
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("Permissions"));
    }

    @Test
    void roleViewNonExistent() {
        execute("role-view", "NonExistent\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void roleUpdateDescription() {
        execute("role-update", "Viewer\n\nUpdated description\n");
        assertTrue(getOutput().contains("updated"));
        assertEquals("Updated description",
                system.getRoleManager().findByName("Viewer").get().getDescription());
    }

    @Test
    void roleUpdateNonExistent() {
        execute("role-update", "NonExistent\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void roleDeleteUnassigned() {
        execute("role-create", "ToDelete\nTemp role\nнет\n");
        outputStream.reset();
        execute("role-delete", "ToDelete\nда\n");
        assertTrue(getOutput().contains("deleted"));
        assertFalse(system.getRoleManager().exists("ToDelete"));
    }

    @Test
    void roleDeleteCancelled() {
        execute("role-create", "ToKeep\nKeep role\nнет\n");
        outputStream.reset();
        execute("role-delete", "ToKeep\nнет\n");
        assertTrue(getOutput().contains("Cancelled"));
        assertTrue(system.getRoleManager().exists("ToKeep"));
    }

    @Test
    void roleDeleteAssignedShowsWarning() {
        execute("role-delete", "Admin\nнет\n");
        String output = getOutput();
        assertTrue(output.contains("WARNING"));
        assertTrue(output.contains("admin"));
    }

    @Test
    void roleAddPermission() {
        execute("role-add-permission", "Viewer\nEXECUTE\nscripts\nRun scripts\n");
        assertTrue(getOutput().contains("Permission added"));
        assertTrue(system.getRoleManager().findByName("Viewer").get()
                .hasPermission("EXECUTE", "scripts"));
    }

    @Test
    void roleAddPermissionNonExistentRole() {
        execute("role-add-permission", "NonExistent\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void roleRemovePermissionNonExistentRole() {
        execute("role-remove-permission", "NonExistent\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void roleSearchByName() {
        execute("role-search", "1\nAdmin\n");
        assertTrue(getOutput().contains("Admin"));
    }

    @Test
    void roleSearchByPermission() {
        execute("role-search", "2\nREAD\nusers\n");
        String output = getOutput();
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("Viewer"));
    }

    @Test
    void roleSearchByMinPermissions() {
        execute("role-search", "3\n5\n");
        String output = getOutput();
        assertTrue(output.contains("Admin"));
    }

    @Test
    void roleSearchInvalidChoice() {
        execute("role-search", "9\n");
        assertTrue(getOutput().contains("Invalid choice"));
    }

    @Test
    void assignRolePermanent() {
        execute("user-create", "testuser\nTest User\ntest@mail.com\n");
        outputStream.reset();
        execute("assign-role", "testuser\n3\n1\ntest reason\n");
        assertTrue(getOutput().contains("Permanent assignment created"));
    }

    @Test
    void assignRoleTemporary() {
        execute("user-create", "testuser\nTest User\ntest@mail.com\n");
        outputStream.reset();
        execute("assign-role", "testuser\n3\n2\ntest reason\n2030-12-31\n");
        assertTrue(getOutput().contains("Temporary assignment created"));
    }

    @Test
    void assignRoleUserNotFound() {
        execute("assign-role", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void revokeRoleSuccess() {
        execute("revoke-role", "admin\n1\n");
        assertTrue(getOutput().contains("revoked"));
    }

    @Test
    void revokeRoleUserNotFound() {
        execute("revoke-role", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void assignmentListShowsInitial() {
        execute("assignment-list", "");
        String output = getOutput();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("PERMANENT"));
    }

    @Test
    void assignmentListUserExisting() {
        execute("assignment-list-user", "admin\n");
        String output = getOutput();
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("ACTIVE"));
    }

    @Test
    void assignmentListUserNotFound() {
        execute("assignment-list-user", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void assignmentListRoleExisting() {
        execute("assignment-list-role", "Admin\n");
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void assignmentListRoleNotFound() {
        execute("assignment-list-role", "NonExistent\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void assignmentActiveShowsActive() {
        execute("assignment-active", "");
        String output = getOutput();
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("ACTIVE"));
    }

    @Test
    void assignmentExpiredEmpty() {
        execute("assignment-expired", "");
        assertTrue(getOutput().contains("No expired"));
    }

    @Test
    void assignmentSearchByUsername() {
        execute("assignment-search", "1\nadmin\n");
        assertTrue(getOutput().contains("admin"));
    }

    @Test
    void assignmentSearchByRole() {
        execute("assignment-search", "2\nAdmin\n");
        assertTrue(getOutput().contains("Admin"));
    }

    @Test
    void assignmentSearchByTypePermanent() {
        execute("assignment-search", "3\npermanent\n");
        assertTrue(getOutput().contains("PERMANENT"));
    }

    @Test
    void assignmentSearchByStatusActive() {
        execute("assignment-search", "4\nactive\n");
        assertTrue(getOutput().contains("ACTIVE"));
    }

    @Test
    void assignmentSearchInvalidChoice() {
        execute("assignment-search", "9\n");
        assertTrue(getOutput().contains("Invalid choice"));
    }

    @Test
    void permissionsUserExisting() {
        execute("permissions-user", "admin\n");
        String output = getOutput();
        assertTrue(output.contains("READ"));
        assertTrue(output.contains("WRITE"));
        assertTrue(output.contains("DELETE"));
    }

    @Test
    void permissionsUserNotFound() {
        execute("permissions-user", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void permissionsCheckHasPermission() {
        execute("permissions-check", "admin\nREAD\nusers\n");
        String output = getOutput();
        assertTrue(output.contains("YES"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    void permissionsCheckNoPermission() {
        execute("permissions-check", "admin\nEXECUTE\nscripts\n");
        assertTrue(getOutput().contains("NO"));
    }

    @Test
    void permissionsCheckUserNotFound() {
        execute("permissions-check", "nobody\n");
        assertTrue(getOutput().contains("not found"));
    }

    @Test
    void helpShowsCommands() {
        execute("help", "");
        String output = getOutput();
        assertTrue(output.contains("user-list"));
        assertTrue(output.contains("role-list"));
        assertTrue(output.contains("assign-role"));
        assertTrue(output.contains("help"));
    }

    @Test
    void statsShowsStatistics() {
        execute("stats", "");
        String output = getOutput();
        assertTrue(output.contains("Users"));
        assertTrue(output.contains("Roles"));
        assertTrue(output.contains("Assignments"));
    }

    @Test
    void clearProducesOutput() {
        execute("clear", "");
        String output = getOutput();
        assertTrue(output.length() > 40);
    }

    @Test
    void saveNotImplemented() {
        execute("save", "");
        assertTrue(getOutput().contains("not implemented"));
    }

    @Test
    void loadNotImplemented() {
        execute("load", "");
        assertTrue(getOutput().contains("not implemented"));
    }
}
