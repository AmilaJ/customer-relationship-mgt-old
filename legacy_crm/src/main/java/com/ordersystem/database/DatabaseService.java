package com.ordersystem.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static final String DB_URL = "jdbc:mysql://localhost:3306/order_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "crm_user";
    private static final String DB_PASSWORD = "crm_password";
    
    private static DatabaseService instance;
    
    private DatabaseService() {
        initializeDatabase();
    }
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create tables and sample data using a temporary connection
            try (Connection tempConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                createTables(tempConnection);
                insertSampleData(tempConnection);
            }
            
            logger.info("Database initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    private void createTables(Connection connection) throws SQLException {
        // Users table
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                active BOOLEAN DEFAULT TRUE
            )
        """;
        
        // Orders table
        String createOrdersTable = """
            CREATE TABLE IF NOT EXISTS orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                order_number VARCHAR(50) UNIQUE NOT NULL,
                customer_id BIGINT NOT NULL,
                customer_name VARCHAR(100) NOT NULL,
                status VARCHAR(20) NOT NULL,
                total_amount DECIMAL(10,2),
                currency VARCHAR(3) DEFAULT 'USD',
                shipping_address TEXT,
                billing_address TEXT,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                expected_delivery_date TIMESTAMP NULL,
                actual_delivery_date TIMESTAMP NULL,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        // Order items table
        String createOrderItemsTable = """
            CREATE TABLE IF NOT EXISTS order_items (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                order_id BIGINT NOT NULL,
                product_id VARCHAR(50) NOT NULL,
                product_name VARCHAR(100) NOT NULL,
                description TEXT,
                quantity INT NOT NULL,
                price DECIMAL(10,2) NOT NULL,
                total_price DECIMAL(10,2) NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
            )
        """;
        
        // Files table
        String createFilesTable = """
            CREATE TABLE IF NOT EXISTS files (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                filename VARCHAR(255) NOT NULL,
                original_filename VARCHAR(255) NOT NULL,
                file_path VARCHAR(500) NOT NULL,
                file_size BIGINT NOT NULL,
                content_type VARCHAR(100),
                uploaded_by BIGINT,
                uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
            )
        """;
        
        // Shipments table
        String createShipmentsTable = """
            CREATE TABLE IF NOT EXISTS shipments (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                order_id BIGINT NOT NULL,
                tracking_number VARCHAR(100) UNIQUE,
                carrier VARCHAR(50),
                status VARCHAR(20) DEFAULT 'PENDING',
                shipped_date TIMESTAMP NULL,
                delivered_date TIMESTAMP NULL,
                shipping_address TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createOrdersTable);
            stmt.execute(createOrderItemsTable);
            stmt.execute(createFilesTable);
            stmt.execute(createShipmentsTable);
        }
    }
    
    private void insertSampleData(Connection connection) throws SQLException {
        // Clear existing users and data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM shipments");
            stmt.execute("DELETE FROM files");
            stmt.execute("DELETE FROM order_items");
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM users");
        }
        
        // Insert new users with plain text passwords (for demo)
        String insertUsers = """
            INSERT INTO users (username, email, password_hash, role) VALUES
            ('admin', 'admin@ordersystem.com', 'admin123', 'ADMIN'),
            ('user1', 'user1@example.com', 'user123', 'USER'),
            ('user2', 'user2@example.com', 'user123', 'USER'),
            ('demo', 'demo@ordersystem.com', 'demo123', 'USER')
        """;
        
        // Insert sample orders
        String insertOrders = """
            INSERT INTO orders (order_number, customer_id, customer_name, status, total_amount) VALUES
            ('ORD-001', 1, 'John Doe', 'PENDING', 299.99),
            ('ORD-002', 2, 'Jane Smith', 'SHIPPED', 149.50),
            ('ORD-003', 1, 'John Doe', 'DELIVERED', 89.99)
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(insertUsers);
            stmt.execute(insertOrders);
        }
        
        logger.info("Sample data inserted successfully");
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
} 