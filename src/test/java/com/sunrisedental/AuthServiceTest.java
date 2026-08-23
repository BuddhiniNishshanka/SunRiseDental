package com.sunrisedental;

import com.sunrisedental.dao.IAuditLogDAO;
import com.sunrisedental.dao.IUserDAO;
import com.sunrisedental.model.User;
import com.sunrisedental.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Task C: Authentication Service TDD Unit Tests")
public class AuthServiceTest {

    private IUserDAO mockUserDAO;
    private IAuditLogDAO mockAuditLogDAO;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        mockUserDAO = Mockito.mock(IUserDAO.class);
        mockAuditLogDAO = Mockito.mock(IAuditLogDAO.class);
        authService = new AuthService(mockUserDAO, mockAuditLogDAO);
    }

    @Test
    @DisplayName("TC-AUTH-01: Successful login with valid credentials")
    void testSuccessfulLogin() {
        String username = "reception";
        String plainPassword = "password123";
        String hashedPassword = AuthService.hashPassword(plainPassword);

        User mockUser = new User(1, username, hashedPassword, "Kavindi Perera", "RECEPTIONIST", "kavindi@sunrisedental.lk", true);
        when(mockUserDAO.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.login(username, plainPassword, "127.0.0.1");

        assertTrue(result.isPresent(), "User should be authenticated successfully");
        assertEquals("RECEPTIONIST", result.get().getRole());
        assertEquals("Kavindi Perera", result.get().getFullName());
        verify(mockAuditLogDAO, times(1)).logAction(any());
    }

    @Test
    @DisplayName("TC-AUTH-02: Failed login with incorrect password")
    void testFailedLoginIncorrectPassword() {
        String username = "admin";
        String validHash = AuthService.hashPassword("correctPassword");

        User mockUser = new User(1, username, validHash, "Dr. Priyanga", "ADMIN", "admin@sunrisedental.lk", true);
        when(mockUserDAO.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.login(username, "wrongPassword", "127.0.0.1");

        assertFalse(result.isPresent(), "Authentication must fail when password is incorrect");
        verify(mockAuditLogDAO, times(1)).logAction(any());
    }

    @Test
    @DisplayName("TC-AUTH-03: Failed login when user does not exist")
    void testFailedLoginUserNotFound() {
        when(mockUserDAO.findByUsername("unknownUser")).thenReturn(Optional.empty());

        Optional<User> result = authService.login("unknownUser", "anyPassword", "127.0.0.1");

        assertFalse(result.isPresent(), "Authentication must fail when user is not found");
        verify(mockAuditLogDAO, times(1)).logAction(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    @DisplayName("TC-AUTH-04: Empty or blank username/password handling")
    void testEmptyCredentials(String emptyVal) {
        Optional<User> result1 = authService.login(emptyVal, "password", "127.0.0.1");
        Optional<User> result2 = authService.login("user", emptyVal, "127.0.0.1");

        assertFalse(result1.isPresent());
        assertFalse(result2.isPresent());
        verifyNoInteractions(mockUserDAO);
    }

    @Test
    @DisplayName("TC-AUTH-05: Inactive user account login rejection")
    void testInactiveAccountLogin() {
        String username = "disabledUser";
        String password = "pwd";
        String hash = AuthService.hashPassword(password);

        User mockUser = new User(2, username, hash, "Disabled Staff", "RECEPTIONIST", "staff@test.com", false);
        when(mockUserDAO.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.login(username, password, "127.0.0.1");

        assertFalse(result.isPresent(), "Inactive user must not be authenticated");
    }

    @Test
    @DisplayName("TC-AUTH-06: SHA-256 Password Hashing Determinism")
    void testPasswordHashing() {
        String hash1 = AuthService.hashPassword("admin123");
        String hash2 = AuthService.hashPassword("admin123");
        String hashDifferent = AuthService.hashPassword("different123");

        assertEquals(hash1, hash2, "Hashing identical input must produce identical hash");
        assertNotEquals(hash1, hashDifferent, "Different passwords must produce distinct hashes");
        assertEquals(64, hash1.length(), "SHA-256 hash must be 64 hexadecimal characters");
    }
}
