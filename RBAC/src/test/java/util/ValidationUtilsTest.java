package util;

import org.example.utils.ValidationUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void isValidUsernameValid() {
        assertTrue(ValidationUtils.isValidUsername("alice"));
        assertTrue(ValidationUtils.isValidUsername("bob_dev"));
        assertTrue(ValidationUtils.isValidUsername("User123"));
        assertTrue(ValidationUtils.isValidUsername("abc"));
        assertTrue(ValidationUtils.isValidUsername("a".repeat(20)));
    }

    @Test
    void isValidUsernameTooShort() {
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("a"));
    }

    @Test
    void isValidUsernameTooLong() {
        assertFalse(ValidationUtils.isValidUsername("a".repeat(21)));
    }

    @Test
    void isValidUsernameInvalidChars() {
        assertFalse(ValidationUtils.isValidUsername("user name"));
        assertFalse(ValidationUtils.isValidUsername("user@name"));
        assertFalse(ValidationUtils.isValidUsername("user-name"));
        assertFalse(ValidationUtils.isValidUsername("user.name"));
    }

    @Test
    void isValidUsernameNull() {
        assertFalse(ValidationUtils.isValidUsername(null));
    }

    @Test
    void isValidUsernameEmpty() {
        assertFalse(ValidationUtils.isValidUsername(""));
    }

    @Test
    void isValidEmailValid() {
        assertTrue(ValidationUtils.isValidEmail("test@mail.com"));
        assertTrue(ValidationUtils.isValidEmail("user@domain.org"));
        assertTrue(ValidationUtils.isValidEmail("a@b.c"));
    }

    @Test
    void isValidEmailInvalid() {
        assertFalse(ValidationUtils.isValidEmail("noatsign"));
        assertFalse(ValidationUtils.isValidEmail("no@dot"));
        assertFalse(ValidationUtils.isValidEmail("@domain.com"));
    }

    @Test
    void isValidEmailNull() {
        assertFalse(ValidationUtils.isValidEmail(null));
    }

    @Test
    void isValidEmailEmpty() {
        assertFalse(ValidationUtils.isValidEmail(""));
    }

    @Test
    void isValidDateValid() {
        assertTrue(ValidationUtils.isValidDate("2024-01-15"));
        assertTrue(ValidationUtils.isValidDate("2000-12-31"));
        assertTrue(ValidationUtils.isValidDate("1999-06-01"));
    }

    @Test
    void isValidDateInvalidFormat() {
        assertFalse(ValidationUtils.isValidDate("15-01-2024"));
        assertFalse(ValidationUtils.isValidDate("2024/01/15"));
        assertFalse(ValidationUtils.isValidDate("2024-1-15"));
        assertFalse(ValidationUtils.isValidDate("not-a-date"));
    }

    @Test
    void isValidDateInvalidMonth() {
        assertFalse(ValidationUtils.isValidDate("2024-00-15"));
        assertFalse(ValidationUtils.isValidDate("2024-13-15"));
    }

    @Test
    void isValidDateInvalidDay() {
        assertFalse(ValidationUtils.isValidDate("2024-01-00"));
        assertFalse(ValidationUtils.isValidDate("2024-01-32"));
    }

    @Test
    void isValidDateNull() {
        assertFalse(ValidationUtils.isValidDate(null));
    }

    @Test
    void normalizeStringTrimsAndCollapsesSpaces() {
        assertEquals("hello world", ValidationUtils.normalizeString("  hello   world  "));
    }

    @Test
    void normalizeStringSingleWord() {
        assertEquals("hello", ValidationUtils.normalizeString("  hello  "));
    }

    @Test
    void normalizeStringAlreadyNormal() {
        assertEquals("hello world", ValidationUtils.normalizeString("hello world"));
    }

    @Test
    void normalizeStringNull() {
        assertEquals("", ValidationUtils.normalizeString(null));
    }

    @Test
    void normalizeStringEmpty() {
        assertEquals("", ValidationUtils.normalizeString(""));
    }

    @Test
    void requireNonEmptyValid() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("value", "field"));
    }

    @Test
    void requireNonEmptyNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "field"));
        assertTrue(ex.getMessage().contains("field"));
    }

    @Test
    void requireNonEmptyBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("   ", "field"));
    }

    @Test
    void requireNonEmptyEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "field"));
    }
}
