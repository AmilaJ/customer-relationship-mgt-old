# Order Management System

A monolithic order management system built in pure Java with a simple HTTP server. This system provides stable but basic endpoints for order management, file handling, and shipment tracking.

## Features

- **User Authentication** (`/login`) - Secure login with password hashing
- **Order Management** (`/orders`) - Full CRUD operations for orders
- **File Management** (`/files`) - Upload, download, and manage files
- **Shipment Tracking** (`/ext/shipment`) - External shipment integration

## Technology Stack

- **Java 11+** - Core language
- **H2 Database** - Embedded database for data persistence
- **Jackson** - JSON processing
- **BCrypt** - Password hashing
- **SLF4J + Logback** - Logging
- **Java HTTP Server** - Built-in HTTP server (no Spring Boot)

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Quick Start

1. **Clone and build the project:**
   ```bash
   mvn clean package
   ```

2. **Run the application:**
   ```bash
   java -jar target/order-management-1.0.0.jar
   ```

3. **The server will start on port 8089:**
   ```
   Order Management Server started on port 8089
   Available endpoints:
     POST /login - User authentication
     GET/POST/PUT/DELETE /orders - Order management
     GET/POST /files - File upload/download
     GET/POST /ext/shipment - External shipment integration
   ```

## API Documentation

### Authentication

#### POST /login
Authenticate a user and receive a session token.

**Request Body:**
```json
{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "uuid-session-token",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@ordersystem.com",
    "role": "ADMIN"
  }
}
```

**Default Users:**
- Username: `admin`, Password: `password`
- Username: `user1`, Password: `password`
- Username: `user2`, Password: `password`

### Order Management

#### GET /orders
Get all orders or a specific order.

**Query Parameters:**
- `id` - Get order by ID
- `orderNumber` - Get order by order number

**Response:**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-001",
    "customerId": 1,
    "customerName": "John Doe",
    "status": "PENDING",
    "totalAmount": 299.99,
    "currency": "USD",
    "items": [...]
  }
]
```

#### POST /orders
Create a new order.

**Request Body:**
```json
{
  "customerId": 1,
  "customerName": "John Doe",
  "shippingAddress": "123 Main St, City, State",
  "billingAddress": "123 Main St, City, State",
  "notes": "Handle with care",
  "items": [
    {
      "productId": "PROD-001",
      "productName": "Laptop",
      "quantity": 1,
      "price": 299.99,
      "description": "High-performance laptop"
    }
  ]
}
```

#### PUT /orders?id={orderId}
Update an existing order.

**Request Body:**
```json
{
  "status": "SHIPPED",
  "shippingAddress": "456 Oak Ave, City, State",
  "notes": "Updated notes"
}
```

#### DELETE /orders?id={orderId}
Delete an order.

### File Management

#### GET /files
List all files or download a specific file.

**Query Parameters:**
- `id` - Download file by ID
- `filename` - Download file by filename

#### POST /files
Upload a file (multipart/form-data).

**Form Data:**
- `file` - The file to upload (max 10MB)

**Response:**
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "fileId": 1,
  "filename": "uuid_original_filename.ext",
  "originalFilename": "original_filename.ext",
  "size": 1024,
  "contentType": "text/plain"
}
```

#### DELETE /files?id={fileId}
Delete a file.

### Shipment Management

#### GET /ext/shipment
Get all shipments or a specific shipment.

**Query Parameters:**
- `orderId` - Get shipment by order ID
- `trackingNumber` - Get shipment by tracking number

#### POST /ext/shipment
Create a new shipment.

**Request Body:**
```json
{
  "orderId": 1,
  "trackingNumber": "TRK-123456789",
  "carrier": "FedEx",
  "shippingAddress": "123 Main St, City, State"
}
```

#### PUT /ext/shipment?id={shipmentId}
Update shipment status.

**Request Body:**
```json
{
  "status": "SHIPPED",
  "trackingNumber": "TRK-123456789",
  "carrier": "FedEx"
}
```

#### DELETE /ext/shipment?id={shipmentId}
Delete a shipment.

## Database Schema

The system uses an H2 embedded database with the following tables:

- **users** - User accounts and authentication
- **orders** - Order information
- **order_items** - Individual items within orders
- **files** - File metadata and storage information
- **shipments** - Shipment tracking information

## File Storage

Uploaded files are stored in the `uploads/` directory with UUID-based filenames to prevent conflicts.

## Logging

Logs are written to both console and file (`logs/ordersystem.log`) with daily rotation.

## Development

### Project Structure
```
src/main/java/com/ordersystem/
├── OrderManagementServer.java    # Main server class
├── handlers/                     # HTTP request handlers
│   ├── BaseHandler.java         # Base handler with common functionality
│   ├── LoginHandler.java        # Authentication endpoint
│   ├── OrderHandler.java        # Order management endpoint
│   ├── FileHandler.java         # File management endpoint
│   └── ShipmentHandler.java     # Shipment tracking endpoint
├── models/                      # Data models
│   ├── User.java               # User entity
│   ├── Order.java              # Order entity
│   └── OrderItem.java          # Order item entity
└── database/                   # Database layer
    └── DatabaseService.java    # Database connection and initialization
```

### Adding New Endpoints

1. Create a new handler class extending `BaseHandler`
2. Implement the required HTTP methods
3. Register the handler in `OrderManagementServer.java`

### Database Changes

1. Modify the `createTables()` method in `DatabaseService.java`
2. Update the sample data insertion if needed
3. The database will be automatically recreated on startup

## Security Notes

- Passwords are hashed using BCrypt
- CORS headers are enabled for all origins (configure as needed)
- File uploads are limited to 10MB
- No authentication middleware is implemented (add as needed)

## Production Considerations

- Replace H2 with a production database (PostgreSQL, MySQL, etc.)
- Implement proper authentication and authorization
- Add input validation and sanitization
- Configure proper CORS policies
- Add rate limiting
- Implement proper error handling
- Add health checks and monitoring
- Use HTTPS in production
- Configure proper logging levels

## Troubleshooting

### Common Issues

1. **Port already in use**: Change the port in `OrderManagementServer.java`
2. **Database errors**: Delete the `ordersystem.mv.db` file to reset the database
3. **File upload issues**: Ensure the `uploads/` directory is writable

### Logs

Check the console output and `logs/ordersystem.log` for detailed error information. 