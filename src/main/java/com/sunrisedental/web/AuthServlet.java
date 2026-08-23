package com.sunrisedental.web;

import com.google.gson.JsonObject;
import com.sunrisedental.model.User;
import com.sunrisedental.service.AuthService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/*"})
public class AuthServlet extends BaseServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/session".equals(pathInfo) || pathInfo == null || "/".equals(pathInfo)) {
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                User user = (User) session.getAttribute("currentUser");
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("fullName", user.getFullName());
                userData.put("role", user.getRole());
                userData.put("email", user.getEmail());
                sendSuccess(resp, "Session active", userData);
            } else {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "No active session found.");
            }
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if ("/login".equals(pathInfo)) {
            JsonObject body = parseRequestJsonObject(req);
            String username = body.has("username") ? body.get("username").getAsString() : "";
            String password = body.has("password") ? body.get("password").getAsString() : "";

            if (username.isEmpty() || password.isEmpty()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required.");
                return;
            }

            Optional<User> userOpt = authService.login(username, password, getClientIp(req));
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", user);
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                session.setMaxInactiveInterval(30 * 60); // 30 minutes

                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("fullName", user.getFullName());
                userData.put("role", user.getRole());
                userData.put("email", user.getEmail());

                sendSuccess(resp, "Login successful", userData);
            } else {
                sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password.");
            }
        } else if ("/logout".equals(pathInfo)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            sendSuccess(resp, "Successfully logged out.", null);
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
        }
    }
}
