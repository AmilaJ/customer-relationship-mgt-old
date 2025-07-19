package com.ordersystem;

import com.sun.net.httpserver.HttpServer;
import com.ordersystem.handlers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class OrderManagementServer {
    private static final Logger logger = LoggerFactory.getLogger(OrderManagementServer.class);
    private static final int PORT = 8089;
    
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            
            // Create context handlers for each endpoint
            server.createContext("/login", new LoginHandler());
            server.createContext("/orders", new OrderHandler());
            server.createContext("/files", new FileHandler());
            server.createContext("/ext/shipment", new ShipmentHandler());
            
            // Set executor for handling requests
            server.setExecutor(Executors.newFixedThreadPool(10));
            
            // Start the server
            server.start();
            logger.info("Order Management Server started on port {}", PORT);
            logger.info("Available endpoints:");
            logger.info("  POST /login - User authentication");
            logger.info("  GET/POST/PUT/DELETE /orders - Order management");
            logger.info("  GET/POST /files - File upload/download");
            logger.info("  GET/POST /ext/shipment - External shipment integration");
            
        } catch (IOException e) {
            logger.error("Failed to start server", e);
            System.exit(1);
        }
    }
} 