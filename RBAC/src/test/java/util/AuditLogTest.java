package util;

import org.example.utils.AuditEntry;
import org.example.utils.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void logAddsEntry() {
        auditLog.log("CREATE", "admin", "user1", "Created user");
        assertEquals(1, auditLog.getAll().size());
    }

    @Test
    void logMultipleEntries() {
        auditLog.log("CREATE", "admin", "user1", "Created");
        auditLog.log("DELETE", "admin", "user2", "Deleted");
        auditLog.log("UPDATE", "manager", "role1", "Updated");
        assertEquals(3, auditLog.getAll().size());
    }

    @Test
    void logSetsTimestamp() {
        auditLog.log("CREATE", "admin", "user1", "test");
        AuditEntry entry = auditLog.getAll().getFirst();
        assertNotNull(entry.timestamp());
        assertTrue(entry.timestamp().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void logStoresCorrectData() {
        auditLog.log("CREATE", "admin", "user1", "details here");
        AuditEntry entry = auditLog.getAll().getFirst();
        assertEquals("CREATE", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("user1", entry.target());
        assertEquals("details here", entry.details());
    }

    @Test
    void getAllReturnsCopy() {
        auditLog.log("CREATE", "admin", "user1", "test");
        List<AuditEntry> list = auditLog.getAll();
        list.clear();
        assertEquals(1, auditLog.getAll().size());
    }

    @Test
    void getAllEmpty() {
        assertTrue(auditLog.getAll().isEmpty());
    }

    @Test
    void getByPerformer() {
        auditLog.log("CREATE", "admin", "user1", "test1");
        auditLog.log("UPDATE", "manager", "user2", "test2");
        auditLog.log("DELETE", "admin", "user3", "test3");

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());
    }

    @Test
    void getByPerformerCaseInsensitive() {
        auditLog.log("CREATE", "Admin", "user1", "test");
        List<AuditEntry> result = auditLog.getByPerformer("admin");
        assertEquals(1, result.size());
    }

    @Test
    void getByPerformerNoMatch() {
        auditLog.log("CREATE", "admin", "user1", "test");
        assertTrue(auditLog.getByPerformer("nobody").isEmpty());
    }

    @Test
    void getByAction() {
        auditLog.log("CREATE", "admin", "user1", "test1");
        auditLog.log("CREATE", "manager", "user2", "test2");
        auditLog.log("DELETE", "admin", "user3", "test3");

        List<AuditEntry> creates = auditLog.getByAction("CREATE");
        assertEquals(2, creates.size());
    }

    @Test
    void getByActionCaseInsensitive() {
        auditLog.log("CREATE", "admin", "user1", "test");
        List<AuditEntry> result = auditLog.getByAction("create");
        assertEquals(1, result.size());
    }

    @Test
    void getByActionNoMatch() {
        auditLog.log("CREATE", "admin", "user1", "test");
        assertTrue(auditLog.getByAction("UPDATE").isEmpty());
    }

    @Test
    void printLogEmpty() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        auditLog.printLog();
        assertTrue(out.toString().contains("No audit entries"));
    }

    @Test
    void printLogWithEntries() {
        auditLog.log("CREATE", "admin", "user1", "test");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        auditLog.printLog();
        String output = out.toString();
        assertTrue(output.contains("CREATE"));
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("user1"));
        assertTrue(output.contains("Total: 1"));
    }

    @Test
    void saveToFile(@TempDir Path tempDir) throws IOException {
        auditLog.log("CREATE", "admin", "user1", "test details");
        auditLog.log("DELETE", "manager", "user2", "removed");

        Path file = tempDir.resolve("audit.log");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        auditLog.saveToFile(file.toString());

        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("CREATE"));
        assertTrue(content.contains("admin"));
        assertTrue(content.contains("test details"));
        assertTrue(content.contains("DELETE"));
        assertTrue(out.toString().contains("saved"));
    }
}
