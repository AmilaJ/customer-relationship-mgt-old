package com.ordersystem.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ordersystem.database.DatabaseService;
import com.ordersystem.models.User;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginHandler extends BaseHandler {
    
    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
    }
    
    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            JsonNode requestBody = readJsonRequest(exchange, JsonNode.class);
            String username = requestBody.get("username").asText();
            String password = requestBody.get("password").asText();
            
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Username and password are required\"}");
                return;
            }
            
            User user = authenticateUser(username, password);
            
            if (user != null) {
                // Generate session token
                String sessionToken = UUID.randomUUID().toString();
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("token", sessionToken);
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                ));
                
                sendJsonResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 401, "{\"error\": \"Invalid username or password\"}");
            }
            
        } catch (Exception e) {
            logger.error("Login error", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePut(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
    }
    
    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
    }
    
    private User authenticateUser(String username, String password) {
        String sql = "SELECT id, username, email, password_hash, role, active FROM users WHERE username = ? AND active = true";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    
                    // Verify password (plain text for demo)
                    if (password.equals(storedHash)) {
                        User user = new User();
                        user.setId(rs.getLong("id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setRole(rs.getString("role"));
                        user.setActive(rs.getBoolean("active"));
                        return user;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error during authentication", e);
        }
        
        return null;
    }
} 