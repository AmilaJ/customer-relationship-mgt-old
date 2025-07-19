package com.ordersystem.handlers;

import com.ordersystem.database.DatabaseService;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileHandler extends BaseHandler {
    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    public FileHandler() {
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            logger.error("Failed to create upload directory", e);
        }
    }
    
    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String fileId = queryParams.get("id");
            String filename = queryParams.get("filename");
            
            if (fileId != null) {
                // Download file by ID
                downloadFileById(exchange, Long.parseLong(fileId));
            } else if (filename != null) {
                // Download file by filename
                downloadFileByName(exchange, filename);
            } else {
                // List all files
                listFiles(exchange);
            }
            
        } catch (Exception e) {
            logger.error("Error handling file download", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            // Check content type
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.startsWith("multipart/form-data")) {
                sendResponse(exchange, 400, "{\"error\": \"Content-Type must be multipart/form-data\"}");
                return;
            }
            
            // Parse multipart form data
            Map<String, Object> formData = parseMultipartFormData(exchange);
            
            if (!formData.containsKey("file")) {
                sendResponse(exchange, 400, "{\"error\": \"No file provided\"}");
                return;
            }
            
            FileUpload fileUpload = (FileUpload) formData.get("file");
            
            // Validate file size
            if (fileUpload.getSize() > MAX_FILE_SIZE) {
                sendResponse(exchange, 413, "{\"error\": \"File too large. Maximum size is 10MB\"}");
                return;
            }
            
            // Save file
            String savedFilename = saveFile(fileUpload);
            
            // Save file metadata to database
            Long fileId = saveFileMetadata(fileUpload, savedFilename);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileId", fileId);
            response.put("filename", savedFilename);
            response.put("originalFilename", fileUpload.getOriginalFilename());
            response.put("size", fileUpload.getSize());
            response.put("contentType", fileUpload.getContentType());
            
            sendJsonResponse(exchange, 201, response);
            
        } catch (Exception e) {
            logger.error("Error handling file upload", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePut(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
    }
    
    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String fileId = queryParams.get("id");
            
            if (fileId == null) {
                sendResponse(exchange, 400, "{\"error\": \"File ID is required\"}");
                return;
            }
            
            boolean deleted = deleteFile(Long.parseLong(fileId));
            if (deleted) {
                sendResponse(exchange, 200, "{\"message\": \"File deleted successfully\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\": \"File not found\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error deleting file", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    private void downloadFileById(HttpExchange exchange, Long fileId) throws IOException {
        String sql = "SELECT * FROM files WHERE id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, fileId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String filePath = rs.getString("file_path");
                    String originalFilename = rs.getString("original_filename");
                    String contentType = rs.getString("content_type");
                    
                    sendFile(exchange, filePath, originalFilename, contentType);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"File not found\"}");
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting file by ID", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    private void downloadFileByName(HttpExchange exchange, String filename) throws IOException {
        String sql = "SELECT * FROM files WHERE filename = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, filename);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String filePath = rs.getString("file_path");
                    String originalFilename = rs.getString("original_filename");
                    String contentType = rs.getString("content_type");
                    
                    sendFile(exchange, filePath, originalFilename, contentType);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"File not found\"}");
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting file by name", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    private void listFiles(HttpExchange exchange) throws IOException {
        String sql = "SELECT id, filename, original_filename, file_size, content_type, uploaded_at FROM files ORDER BY uploaded_at DESC";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            Map<String, Object> response = new HashMap<>();
            response.put("files", new java.util.ArrayList<>());
            
            while (rs.next()) {
                Map<String, Object> file = new HashMap<>();
                file.put("id", rs.getLong("id"));
                file.put("filename", rs.getString("filename"));
                file.put("originalFilename", rs.getString("original_filename"));
                file.put("size", rs.getLong("file_size"));
                file.put("contentType", rs.getString("content_type"));
                file.put("uploadedAt", rs.getTimestamp("uploaded_at"));
                
                ((java.util.List<Object>) response.get("files")).add(file);
            }
            
            sendJsonResponse(exchange, 200, response);
            
        } catch (SQLException e) {
            logger.error("Database error listing files", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    private String saveFile(FileUpload fileUpload) throws IOException {
        String filename = UUID.randomUUID().toString() + "_" + fileUpload.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, filename);
        
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(fileUpload.getData());
        }
        
        return filename;
    }
    
    private Long saveFileMetadata(FileUpload fileUpload, String savedFilename) {
        String sql = """
            INSERT INTO files (filename, original_filename, file_path, file_size, content_type, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, savedFilename);
            stmt.setString(2, fileUpload.getOriginalFilename());
            stmt.setString(3, Paths.get(UPLOAD_DIR, savedFilename).toString());
            stmt.setLong(4, fileUpload.getSize());
            stmt.setString(5, fileUpload.getContentType());
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error saving file metadata", e);
        }
        
        return null;
    }
    
    private boolean deleteFile(Long fileId) {
        // First get file info
        String selectSql = "SELECT file_path FROM files WHERE id = ?";
        String deleteSql = "DELETE FROM files WHERE id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            
            selectStmt.setLong(1, fileId);
            
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    String filePath = rs.getString("file_path");
                    
                    // Delete physical file
                    try {
                        Files.deleteIfExists(Paths.get(filePath));
                    } catch (IOException e) {
                        logger.error("Error deleting physical file", e);
                    }
                    
                    // Delete database record
                    deleteStmt.setLong(1, fileId);
                    return deleteStmt.executeUpdate() > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error deleting file", e);
        }
        
        return false;
    }
    
    private void sendFile(HttpExchange exchange, String filePath, String originalFilename, String contentType) throws IOException {
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            sendResponse(exchange, 404, "{\"error\": \"File not found on disk\"}");
            return;
        }
        
        byte[] fileData = Files.readAllBytes(path);
        
        exchange.getResponseHeaders().add("Content-Type", contentType != null ? contentType : "application/octet-stream");
        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + originalFilename + "\"");
        exchange.getResponseHeaders().add("Content-Length", String.valueOf(fileData.length));
        
        exchange.sendResponseHeaders(200, fileData.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileData);
        }
    }
    
    private Map<String, Object> parseMultipartFormData(HttpExchange exchange) throws IOException {
        Map<String, Object> formData = new HashMap<>();
        
        try (InputStream is = exchange.getRequestBody()) {
            // Simple multipart parsing (for demonstration purposes)
            // In a production environment, you'd want to use a proper multipart parser
            
            byte[] data = is.readAllBytes();
            String boundary = extractBoundary(exchange.getRequestHeaders().getFirst("Content-Type"));
            
            if (boundary != null) {
                String content = new String(data);
                String[] parts = content.split("--" + boundary);
                
                for (String part : parts) {
                    if (part.trim().isEmpty() || part.contains("--")) {
                        continue;
                    }
                    
                    // Parse part headers and body
                    String[] lines = part.split("\r\n");
                    String headers = "";
                    StringBuilder body = new StringBuilder();
                    boolean inBody = false;
                    
                    for (String line : lines) {
                        if (line.trim().isEmpty() && !inBody) {
                            inBody = true;
                            continue;
                        }
                        
                        if (inBody) {
                            body.append(line).append("\r\n");
                        } else {
                            headers += line + "\r\n";
                        }
                    }
                    
                    // Extract filename and content type
                    String filename = extractFilename(headers);
                    String contentType = extractContentType(headers);
                    
                    if (filename != null) {
                        // Remove trailing \r\n from body
                        String bodyStr = body.toString().trim();
                        byte[] fileData = bodyStr.getBytes();
                        
                        FileUpload fileUpload = new FileUpload();
                        fileUpload.setOriginalFilename(filename);
                        fileUpload.setContentType(contentType);
                        fileUpload.setData(fileData);
                        fileUpload.setSize(fileData.length);
                        
                        formData.put("file", fileUpload);
                    }
                }
            }
        }
        
        return formData;
    }
    
    private String extractBoundary(String contentType) {
        if (contentType != null && contentType.contains("boundary=")) {
            return contentType.split("boundary=")[1];
        }
        return null;
    }
    
    private String extractFilename(String headers) {
        if (headers.contains("filename=")) {
            String[] parts = headers.split("filename=");
            if (parts.length > 1) {
                String filename = parts[1].split("\r\n")[0];
                return filename.replace("\"", "");
            }
        }
        return null;
    }
    
    private String extractContentType(String headers) {
        if (headers.contains("Content-Type:")) {
            String[] parts = headers.split("Content-Type:");
            if (parts.length > 1) {
                return parts[1].split("\r\n")[0].trim();
            }
        }
        return "application/octet-stream";
    }
    
    // Inner class to hold file upload data
    private static class FileUpload {
        private String originalFilename;
        private String contentType;
        private byte[] data;
        private long size;
        
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public byte[] getData() { return data; }
        public void setData(byte[] data) { this.data = data; }
        
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
    }
} 