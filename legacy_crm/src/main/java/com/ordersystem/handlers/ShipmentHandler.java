package com.ordersystem.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ordersystem.database.DatabaseService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ShipmentHandler extends BaseHandler {
    
    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String orderId = queryParams.get("orderId");
            String trackingNumber = queryParams.get("trackingNumber");
            
            if (orderId != null) {
                // Get shipment by order ID
                Map<String, Object> shipment = getShipmentByOrderId(Long.parseLong(orderId));
                if (shipment != null) {
                    sendJsonResponse(exchange, 200, shipment);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Shipment not found\"}");
                }
            } else if (trackingNumber != null) {
                // Get shipment by tracking number
                Map<String, Object> shipment = getShipmentByTrackingNumber(trackingNumber);
                if (shipment != null) {
                    sendJsonResponse(exchange, 200, shipment);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Shipment not found\"}");
                }
            } else {
                // Get all shipments
                List<Map<String, Object>> shipments = getAllShipments();
                Map<String, Object> response = new HashMap<>();
                response.put("shipments", shipments);
                sendJsonResponse(exchange, 200, response);
            }
            
        } catch (Exception e) {
            logger.error("Error getting shipments", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            logger.info("POST /ext/shipment - Starting shipment creation");
            JsonNode requestBody = readJsonRequest(exchange, JsonNode.class);
            logger.info("POST /ext/shipment - Request body parsed: {}", requestBody);
            
            // Validate required fields
            if (!requestBody.has("orderId")) {
                logger.warn("POST /ext/shipment - Missing orderId in request");
                sendResponse(exchange, 400, "{\"error\": \"Order ID is required\"}");
                return;
            }
            
            Long orderId = requestBody.get("orderId").asLong();
            String trackingNumber = requestBody.has("trackingNumber") ? 
                requestBody.get("trackingNumber").asText() : generateTrackingNumber();
            String carrier = requestBody.has("carrier") ? 
                requestBody.get("carrier").asText() : "DEFAULT_CARRIER";
            String shippingAddress = requestBody.has("shippingAddress") ? 
                requestBody.get("shippingAddress").asText() : null;
            
            logger.info("POST /ext/shipment - Parsed fields: orderId={}, trackingNumber={}, carrier={}, shippingAddress={}", 
                       orderId, trackingNumber, carrier, shippingAddress);
            
            // Check if shipment already exists for this order
            logger.info("POST /ext/shipment - Checking for existing shipment for orderId={}", orderId);
            Map<String, Object> existingShipment = getShipmentByOrderId(orderId);
            if (existingShipment != null) {
                logger.warn("POST /ext/shipment - Shipment already exists for orderId={}", orderId);
                sendResponse(exchange, 409, "{\"error\": \"Shipment already exists for this order\"}");
                return;
            }
            
            // Create shipment
            logger.info("POST /ext/shipment - Creating new shipment");
            Long shipmentId = createShipment(orderId, trackingNumber, carrier, shippingAddress);
            logger.info("POST /ext/shipment - Shipment creation result: shipmentId={}", shipmentId);
            
            if (shipmentId != null) {
                // Fetch the created shipment and return it
                Map<String, Object> createdShipment = getShipmentById(shipmentId);
                
                if (createdShipment != null) {
                    sendJsonResponse(exchange, 201, createdShipment);
                } else {
                    sendResponse(exchange, 500, "{\"error\": \"Shipment created but could not retrieve details\"}");
                }
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create shipment\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error creating shipment", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePut(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String shipmentId = queryParams.get("id");
            
            if (shipmentId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Shipment ID is required\"}");
                return;
            }
            
            JsonNode requestBody = readJsonRequest(exchange, JsonNode.class);
            
            // Update shipment status
            String status = requestBody.has("status") ? requestBody.get("status").asText() : null;
            String trackingNumber = requestBody.has("trackingNumber") ? requestBody.get("trackingNumber").asText() : null;
            String carrier = requestBody.has("carrier") ? requestBody.get("carrier").asText() : null;
            
            boolean updated = updateShipment(Long.parseLong(shipmentId), status, trackingNumber, carrier);
            
            if (updated) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Shipment updated successfully");
                
                sendJsonResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Shipment not found\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error updating shipment", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String shipmentId = queryParams.get("id");
            
            if (shipmentId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Shipment ID is required\"}");
                return;
            }
            
            boolean deleted = deleteShipment(Long.parseLong(shipmentId));
            if (deleted) {
                sendResponse(exchange, 200, "{\"message\": \"Shipment deleted successfully\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Shipment not found\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error deleting shipment", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    private String generateTrackingNumber() {
        return "TRK-" + System.currentTimeMillis() + "-" + new Random().nextInt(1000);
    }
    
    private Map<String, Object> getShipmentByOrderId(Long orderId) {
        String sql = "SELECT * FROM shipments WHERE order_id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, orderId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShipment(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting shipment by order ID", e);
        }
        
        return null;
    }
    
    private Map<String, Object> getShipmentById(Long shipmentId) {
        String sql = "SELECT * FROM shipments WHERE id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, shipmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShipment(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error retrieving shipment by ID", e);
        }
        
        return null;
    }
    
    private Map<String, Object> getShipmentByTrackingNumber(String trackingNumber) {
        String sql = "SELECT * FROM shipments WHERE tracking_number = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, trackingNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShipment(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting shipment by tracking number", e);
        }
        
        return null;
    }
    
    private List<Map<String, Object>> getAllShipments() {
        List<Map<String, Object>> shipments = new ArrayList<>();
        String sql = "SELECT * FROM shipments ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                shipments.add(mapResultSetToShipment(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting all shipments", e);
        }
        
        return shipments;
    }
    
    private Long createShipment(Long orderId, String trackingNumber, String carrier, String shippingAddress) {
        logger.info("createShipment - Starting with orderId={}, trackingNumber={}, carrier={}, shippingAddress={}", 
                   orderId, trackingNumber, carrier, shippingAddress);
        String sql = """
            INSERT INTO shipments (order_id, tracking_number, carrier, status, shipping_address, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            logger.info("createShipment - Preparing SQL statement");
            stmt.setLong(1, orderId);
            stmt.setString(2, trackingNumber);
            stmt.setString(3, carrier);
            stmt.setString(4, "PENDING");
            stmt.setString(5, shippingAddress);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            
            logger.info("createShipment - Executing SQL statement");
            int affectedRows = stmt.executeUpdate();
            logger.info("createShipment - SQL executed, affected rows: {}", affectedRows);
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Long generatedId = rs.getLong(1);
                        logger.info("createShipment - Successfully created shipment with ID: {}", generatedId);
                        return generatedId;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error creating shipment", e);
        }
        
        logger.warn("createShipment - Failed to create shipment, returning null");
        return null;
    }
    
    private boolean updateShipment(Long shipmentId, String status, String trackingNumber, String carrier) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE shipments SET updated_at = ?");
        List<Object> params = new ArrayList<>();
        params.add(Timestamp.valueOf(LocalDateTime.now()));
        
        if (status != null) {
            sqlBuilder.append(", status = ?");
            params.add(status);
            
            // If status is SHIPPED, set shipped_date
            if ("SHIPPED".equals(status)) {
                sqlBuilder.append(", shipped_date = ?");
                params.add(Timestamp.valueOf(LocalDateTime.now()));
            }
            
            // If status is DELIVERED, set delivered_date
            if ("DELIVERED".equals(status)) {
                sqlBuilder.append(", delivered_date = ?");
                params.add(Timestamp.valueOf(LocalDateTime.now()));
            }
        }
        
        if (trackingNumber != null) {
            sqlBuilder.append(", tracking_number = ?");
            params.add(trackingNumber);
        }
        
        if (carrier != null) {
            sqlBuilder.append(", carrier = ?");
            params.add(carrier);
        }
        
        sqlBuilder.append(" WHERE id = ?");
        params.add(shipmentId);
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.error("Database error updating shipment", e);
        }
        
        return false;
    }
    
    private boolean deleteShipment(Long shipmentId) {
        String sql = "DELETE FROM shipments WHERE id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, shipmentId);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.error("Database error deleting shipment", e);
        }
        
        return false;
    }
    
    private Map<String, Object> mapResultSetToShipment(ResultSet rs) throws SQLException {
        Map<String, Object> shipment = new HashMap<>();
        shipment.put("id", rs.getLong("id"));
        shipment.put("orderId", rs.getLong("order_id"));
        shipment.put("trackingNumber", rs.getString("tracking_number"));
        shipment.put("carrier", rs.getString("carrier"));
        shipment.put("status", rs.getString("status"));
        shipment.put("shippingAddress", rs.getString("shipping_address"));
        
        // Use timestamps for proper JSON serialization
        Timestamp shippedDate = rs.getTimestamp("shipped_date");
        if (shippedDate != null) {
            shipment.put("shippedDate", shippedDate.getTime());
        }
        
        Timestamp deliveredDate = rs.getTimestamp("delivered_date");
        if (deliveredDate != null) {
            shipment.put("deliveredDate", deliveredDate.getTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            shipment.put("createdAt", createdAt.getTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            shipment.put("updatedAt", updatedAt.getTime());
        }
        
        return shipment;
    }
} 