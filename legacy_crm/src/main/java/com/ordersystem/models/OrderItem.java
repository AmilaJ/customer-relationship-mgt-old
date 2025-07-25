package com.ordersystem.models;

import java.math.BigDecimal;

public class OrderItem {
    private Long id;
    private Long orderId;
    private String productId;
    private String productName;
    private String description;
    private int quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    
    public OrderItem() {}
    
    public OrderItem(String productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = calculateTotalPrice();
    }
    
    private BigDecimal calculateTotalPrice() {
        if (this.price != null && this.quantity > 0) {
            return this.price.multiply(new BigDecimal(this.quantity));
        }
        return BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        this.totalPrice = calculateTotalPrice();
    }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price; 
        this.totalPrice = calculateTotalPrice();
    }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
} 