package com.ordersystem.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.ordersystem.database.DatabaseService;
import com.ordersystem.models.Order;
import com.ordersystem.models.OrderItem;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OrderHandler extends BaseHandler {
    
    @Override
    protected void handleGet(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String orderId = queryParams.get("id");
            String orderNumber = queryParams.get("orderNumber");
            
            if (orderId != null) {
                // Get specific order by ID
                Order order = getOrderById(Long.parseLong(orderId));
                if (order != null) {
                    sendJsonResponse(exchange, 200, order);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Order not found\"}");
                }
            } else if (orderNumber != null) {
                // Get specific order by order number
                Order order = getOrderByNumber(orderNumber);
                if (order != null) {
                    sendJsonResponse(exchange, 200, order);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Order not found\"}");
                }
            } else {
                // Get all orders
                List<Order> orders = getAllOrders();
                sendJsonResponse(exchange, 200, orders);
            }
            
        } catch (Exception e) {
            logger.error("Error getting orders", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            JsonNode requestBody = readJsonRequest(exchange, JsonNode.class);
            
            Order order = new Order();
            order.setOrderNumber(generateOrderNumber());
            order.setCustomerId(requestBody.get("customerId").asLong());
            order.setCustomerName(requestBody.get("customerName").asText());
            order.setStatus("PENDING");
            
            if (requestBody.has("shippingAddress")) {
                order.setShippingAddress(requestBody.get("shippingAddress").asText());
            }
            if (requestBody.has("billingAddress")) {
                order.setBillingAddress(requestBody.get("billingAddress").asText());
            }
            if (requestBody.has("notes")) {
                order.setNotes(requestBody.get("notes").asText());
            }
            
            // Add order items if provided
            if (requestBody.has("items") && requestBody.get("items").isArray()) {
                for (JsonNode itemNode : requestBody.get("items")) {
                    OrderItem item = new OrderItem(
                        itemNode.get("productId").asText(),
                        itemNode.get("productName").asText(),
                        itemNode.get("quantity").asInt(),
                        new BigDecimal(itemNode.get("price").asText())
                    );
                    if (itemNode.has("description")) {
                        item.setDescription(itemNode.get("description").asText());
                    }
                    order.addItem(item);
                }
            }
            
            order.calculateTotal();
            
            Order savedOrder = createOrder(order);
            if (savedOrder != null) {
                sendJsonResponse(exchange, 201, savedOrder);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create order\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error creating order", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handlePut(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String orderId = queryParams.get("id");
            
            if (orderId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Order ID is required\"}");
                return;
            }
            
            JsonNode requestBody = readJsonRequest(exchange, JsonNode.class);
            
            Order order = getOrderById(Long.parseLong(orderId));
            if (order == null) {
                sendResponse(exchange, 404, "{\"error\": \"Order not found\"}");
                return;
            }
            
            // Update order fields
            if (requestBody.has("status")) {
                order.setStatus(requestBody.get("status").asText());
            }
            if (requestBody.has("shippingAddress")) {
                order.setShippingAddress(requestBody.get("shippingAddress").asText());
            }
            if (requestBody.has("billingAddress")) {
                order.setBillingAddress(requestBody.get("billingAddress").asText());
            }
            if (requestBody.has("notes")) {
                order.setNotes(requestBody.get("notes").asText());
            }
            if (requestBody.has("expectedDeliveryDate")) {
                order.setExpectedDeliveryDate(LocalDateTime.parse(requestBody.get("expectedDeliveryDate").asText()));
            }
            
            order.setUpdatedAt(LocalDateTime.now());
            
            boolean updated = updateOrder(order);
            if (updated) {
                sendJsonResponse(exchange, 200, order);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to update order\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error updating order", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    @Override
    protected void handleDelete(HttpExchange exchange) throws IOException {
        try {
            Map<String, String> queryParams = parseQueryParams(exchange);
            String orderId = queryParams.get("id");
            
            if (orderId == null) {
                sendResponse(exchange, 400, "{\"error\": \"Order ID is required\"}");
                return;
            }
            
            boolean deleted = deleteOrder(Long.parseLong(orderId));
            if (deleted) {
                sendResponse(exchange, 200, "{\"message\": \"Order deleted successfully\"}");
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Order not found\"}");
            }
            
        } catch (Exception e) {
            logger.error("Error deleting order", e);
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
    
    private Order getOrderById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting order by ID", e);
        }
        
        return null;
    }
    
    private Order getOrderByNumber(String orderNumber) {
        String sql = "SELECT * FROM orders WHERE order_number = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, orderNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting order by number", e);
        }
        
        return null;
    }
    
    private List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Database error getting all orders", e);
        }
        
        return orders;
    }
    
    private Order createOrder(Order order) {
        String sql = """
            INSERT INTO orders (order_number, customer_id, customer_name, status, total_amount, 
                               currency, shipping_address, billing_address, notes, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, order.getOrderNumber());
            stmt.setLong(2, order.getCustomerId());
            stmt.setString(3, order.getCustomerName());
            stmt.setString(4, order.getStatus());
            stmt.setBigDecimal(5, order.getTotalAmount());
            stmt.setString(6, order.getCurrency());
            stmt.setString(7, order.getShippingAddress());
            stmt.setString(8, order.getBillingAddress());
            stmt.setString(9, order.getNotes());
            stmt.setTimestamp(10, Timestamp.valueOf(order.getCreatedAt()));
            stmt.setTimestamp(11, Timestamp.valueOf(order.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setId(rs.getLong(1));
                        
                        // Save order items
                        saveOrderItems(order);
                        
                        return order;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error creating order", e);
        }
        
        return null;
    }
    
    private boolean updateOrder(Order order) {
        String sql = """
            UPDATE orders SET status = ?, shipping_address = ?, billing_address = ?, 
                             notes = ?, expected_delivery_date = ?, updated_at = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, order.getStatus());
            stmt.setString(2, order.getShippingAddress());
            stmt.setString(3, order.getBillingAddress());
            stmt.setString(4, order.getNotes());
            stmt.setTimestamp(5, order.getExpectedDeliveryDate() != null ? 
                Timestamp.valueOf(order.getExpectedDeliveryDate()) : null);
            stmt.setTimestamp(6, Timestamp.valueOf(order.getUpdatedAt()));
            stmt.setLong(7, order.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.error("Database error updating order", e);
        }
        
        return false;
    }
    
    private boolean deleteOrder(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            logger.error("Database error deleting order", e);
        }
        
        return false;
    }
    
    private void saveOrderItems(Order order) {
        String sql = """
            INSERT INTO order_items (order_id, product_id, product_name, description, 
                                    quantity, price, total_price)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (OrderItem item : order.getItems()) {
                stmt.setLong(1, order.getId());
                stmt.setString(2, item.getProductId());
                stmt.setString(3, item.getProductName());
                stmt.setString(4, item.getDescription());
                stmt.setInt(5, item.getQuantity());
                stmt.setBigDecimal(6, item.getPrice());
                stmt.setBigDecimal(7, item.getTotalPrice());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            
        } catch (SQLException e) {
            logger.error("Database error saving order items", e);
        }
    }
    
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNumber(rs.getString("order_number"));
        order.setCustomerId(rs.getLong("customer_id"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCurrency(rs.getString("currency"));
        order.setShippingAddress(rs.getString("shipping_address"));
        order.setBillingAddress(rs.getString("billing_address"));
        order.setNotes(rs.getString("notes"));
        
        Timestamp orderDate = rs.getTimestamp("order_date");
        if (orderDate != null) {
            order.setOrderDate(orderDate.toLocalDateTime());
        }
        
        Timestamp expectedDelivery = rs.getTimestamp("expected_delivery_date");
        if (expectedDelivery != null) {
            order.setExpectedDeliveryDate(expectedDelivery.toLocalDateTime());
        }
        
        Timestamp actualDelivery = rs.getTimestamp("actual_delivery_date");
        if (actualDelivery != null) {
            order.setActualDeliveryDate(actualDelivery.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Load order items
        loadOrderItems(order);
        
        return order;
    }
    
    private void loadOrderItems(Order order) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (Connection conn = DatabaseService.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, order.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setDescription(rs.getString("description"));
                    // Set price before quantity to avoid null pointer exception
                    item.setPrice(rs.getBigDecimal("price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    
                    order.addItem(item);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database error loading order items", e);
        }
    }
} 