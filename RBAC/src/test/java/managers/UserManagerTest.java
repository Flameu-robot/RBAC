package managers;

import org.example.repository.UserManager;
import org.example.entity.User;
import org.example.filter.UserFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager manager;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
        user1 = new User("alice", "Alice Smith", "alice@test.com");
        user2 = new User("bob_dev", "Bob Johnson", "bob@company.com");
        user3 = new User("charlie", "Charlie Brown", "charlie@test.com");
    }

    @Test
    void addUser() {
        manager.add(user1);
        assertEquals(1, manager.count());
        assertTrue(manager.exists("alice"));
    }

    @Test
    void addNullThrows() {
        assertThrows(NullPointerException.class, () -> manager.add(null));
    }

    @Test
    void addDuplicateThrows() {
        manager.add(user1);
        User duplicate = new User("alice", "Another Alice", "another@test.com");
        assertThrows(IllegalArgumentException.class, () -> manager.add(duplicate));
    }

    @Test
    void removeUser() {
        manager.add(user1);
        assertTrue(manager.remove(user1));
        assertEquals(0, manager.count());
        assertFalse(manager.exists("alice"));
    }

    @Test
    void removeNull() {
        assertFalse(manager.remove(null));
    }

    @Test
    void removeNonExistent() {
        assertFalse(manager.remove(user1));
    }

    @Test
    void findById() {
        manager.add(user1);
        Optional<User> found = manager.findById("alice");
        assertTrue(found.isPresent());
        assertEquals(user1, found.get());
    }

    @Test
    void findByIdNotFound() {
        Optional<User> found = manager.findById("nonexistent");
        assertTrue(found.isEmpty());
    }

    @Test
    void findByUsername() {
        manager.add(user1);
        Optional<User> found = manager.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().username());
    }

    @Test
    void findByEmail() {
        manager.add(user1);
        manager.add(user2);
        Optional<User> found = manager.findByEmail("bob@company.com");
        assertTrue(found.isPresent());
        assertEquals("bob_dev", found.get().username());
    }

    @Test
    void findByEmailCaseInsensitive() {
        manager.add(user1);
        Optional<User> found = manager.findByEmail("ALICE@TEST.COM");
        assertTrue(found.isPresent());
        assertEquals("alice", found.get().username());
    }

    @Test
    void findByEmailNotFound() {
        manager.add(user1);
        Optional<User> found = manager.findByEmail("nobody@test.com");
        assertTrue(found.isEmpty());
    }

    @Test
    void findAll() {
        manager.add(user1);
        manager.add(user2);
        List<User> all = manager.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void findAllEmpty() {
        List<User> all = manager.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void count() {
        assertEquals(0, manager.count());
        manager.add(user1);
        assertEquals(1, manager.count());
        manager.add(user2);
        assertEquals(2, manager.count());
    }

    @Test
    void clear() {
        manager.add(user1);
        manager.add(user2);
        manager.clear();
        assertEquals(0, manager.count());
    }

    @Test
    void exists() {
        manager.add(user1);
        assertTrue(manager.exists("alice"));
        assertFalse(manager.exists("nobody"));
    }

    @Test
    void update() {
        manager.add(user1);
        manager.update("alice", "Alice Updated", "alice_new@test.com");
        Optional<User> found = manager.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("Alice Updated", found.get().fullname());
        assertEquals("alice_new@test.com", found.get().email());
    }

    @Test
    void updateNonExistentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.update("nobody", "Name", "email@test.com"));
    }

    @Test
    void findByFilter() {
        manager.add(user1);
        manager.add(user2);
        manager.add(user3);

        UserFilter emailFilter = u -> u.email().endsWith("@test.com");
        List<User> results = manager.findByFilter(emailFilter);
        assertEquals(2, results.size());
    }

    @Test
    void findByFilterNoMatch() {
        manager.add(user1);
        UserFilter filter = u -> u.username().contains("zzz");
        List<User> results = manager.findByFilter(filter);
        assertTrue(results.isEmpty());
    }

    @Test
    void findAllWithFilterAndSorter() {
        manager.add(user1);
        manager.add(user2);
        manager.add(user3);

        UserFilter all = u -> true;
        Comparator<User> byUsername = Comparator.comparing(User::username);
        List<User> sorted = manager.findAll(all, byUsername);

        assertEquals(3, sorted.size());
        assertEquals("alice", sorted.get(0).username());
        assertEquals("bob_dev", sorted.get(1).username());
        assertEquals("charlie", sorted.get(2).username());
    }

    @Test
    void findAllReturnsCopy() {
        manager.add(user1);
        List<User> list = manager.findAll();
        list.clear();
        assertEquals(1, manager.count());
    }
}
