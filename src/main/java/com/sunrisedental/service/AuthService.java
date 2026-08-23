package com.sunrisedental.service;

import com.sunrisedental.dao.IAuditLogDAO;
import com.sunrisedental.dao.IUserDAO;
import com.sunrisedental.factory.DAOFactory;
import com.sunrisedental.model.AuditLog;
import com.sunrisedental.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private final IUserDAO userDAO;
    private final IAuditLogDAO auditLogDAO;

    public AuthService() {
        this(DAOFactory.getInstance().getUserDAO(), DAOFactory.getInstance().getAuditLogDAO());
    }

    public AuthService(IUserDAO userDAO, IAuditLogDAO auditLogDAO) {
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
    }

    /**
     * Authenticates a user using username and plain text password.
     */
    public Optional<User> login(String username, String password, String ipAddress) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (!user.isActive()) {
                LOGGER.warn("User account {} is inactive.", username);
                return Optional.empty();
            }

            String hashed = hashPassword(password);
            if (hashed.equals(user.getPasswordHash())) {
                auditLogDAO.logAction(new AuditLog(username, "USER_LOGIN_SUCCESS", "User successfully logged in", ipAddress));
                LOGGER.info("User {} successfully logged in with role {}.", username, user.getRole());
                return Optional.of(user);
            } else {
                auditLogDAO.logAction(new AuditLog(username, "USER_LOGIN_FAILED", "Invalid password attempt", ipAddress));
                LOGGER.warn("Invalid password for user {}.", username);
            }
        } else {
            auditLogDAO.logAction(new AuditLog(username, "USER_LOGIN_FAILED", "Non-existent username", ipAddress));
            LOGGER.warn("Username {} not found during authentication.", username);
        }

        return Optional.empty();
    }

    /**
     * Hashes password using SHA-256.
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Hashing algorithm error: {}", e.getMessage(), e);
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }
}
