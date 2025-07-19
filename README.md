# CRM Order Management System

A complete CRM system with Java backend and Angular frontend for managing orders, shipments, and files.

## System Architecture

- **Backend**: Java 11+ with embedded HTTP server, MySQL database
- **Frontend**: Angular 20 with TypeScript, SCSS, and RxJS
- **Database**: MySQL with JDBC connectivity
- **Authentication**: Token-based authentication with plain text passwords (for demo)

## Prerequisites

### Backend Requirements
- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+

### Frontend Requirements
- Node.js 18+ (LTS version recommended)
- npm 8+

## Database Setup

1. **Install and start MySQL**
   ```bash
   # macOS (using Homebrew)
   brew install mysql
   brew services start mysql
   
   # Or download from https://dev.mysql.com/downloads/mysql/
   ```

2. **Create database and user**
   ```sql
   CREATE DATABASE order_management;
   CREATE USER 'crm_user'@'localhost' IDENTIFIED BY 'crm_password';
   GRANT ALL PRIVILEGES ON order_management.* TO 'crm_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

## Backend Setup and Startup

### 1. Navigate to backend directory
```bash
cd legacy_crm
```

### 2. Build the project
```bash
mvn clean package
```

### 3. Start the backend server
```bash
java -jar target/order-management-1.0.0.jar
```

The backend will start on `http://localhost:8089`

### Backend API Endpoints

- **Health Check**: `GET /health`
- **Login**: `POST /login`
- **Orders**: `GET/POST/PUT/DELETE /ext/order`
- **Shipments**: `GET/POST/PUT/DELETE /ext/shipment`
- **Files**: `GET/POST/DELETE /files`

### Demo Users (Plain text passwords for demo)
- **Admin**: `admin` / `admin123`
- **User 1**: `user1` / `user123`
- **User 2**: `user2` / `user123`
- **Demo**: `demo` / `demo123`

## Frontend Setup and Startup

### 1. Navigate to frontend directory
```bash
cd crm_front_end
```

### 2. Install dependencies
```bash
npm install
```

### 3. Start the development server
```bash
npm start
```

The frontend will start on `http://localhost:4200`

## Complete Startup Sequence

### Terminal 1 - Backend
```bash
cd legacy_crm
mvn clean package
java -jar target/order-management-1.0.0.jar
```

### Terminal 2 - Frontend
```bash
cd crm_front_end
npm install
npm start
```

## System Features

### Dashboard
- Overview of orders, shipments, and files
- Statistics and recent activity
- Navigation to all modules

### Order Management
- Create, edit, and delete orders
- Add order items with products
- Track order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- Customer information management

### Shipment Management
- Create shipments for orders
- Track shipment status (PENDING, SHIPPED, DELIVERED, CANCELLED)
- Carrier and tracking number management
- Shipping address management

### File Management
- Upload and download files
- File metadata tracking
- File size and type information
- Secure file storage

### Authentication
- Login/logout functionality
- Route protection with guards
- Token-based authentication
- User role management

## Troubleshooting

### Backend Issues
1. **Port 8089 already in use**
   ```bash
   lsof -ti:8089 | xargs kill -9
   ```

2. **Database connection failed**
   - Verify MySQL is running
   - Check database credentials in `DatabaseService.java`
   - Ensure database and user exist

3. **JAR file not found**
   ```bash
   mvn clean package
   ```

### Frontend Issues
1. **Port 4200 already in use**
   ```bash
   npm start -- --port 4201
   ```

2. **Dependencies not found**
   ```bash
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Build errors**
   ```bash
   npm run build
   ```

### Common Issues
1. **CORS errors**: Backend includes CORS headers for localhost:4200
2. **Authentication failures**: Use demo credentials listed above
3. **Database errors**: Check MySQL connection and table creation

## Development

### Backend Development
- Main server: `OrderManagementServer.java`
- Database service: `DatabaseService.java`
- Handlers: `LoginHandler.java`, `OrderHandler.java`, `ShipmentHandler.java`, `FileHandler.java`
- Models: `User.java`, `Order.java`, `Shipment.java`

### Frontend Development
- Main app: `app.component.ts`
- Components: `login/`, `dashboard/`, `orders/`, `shipments/`, `files/`
- Services: `auth.service.ts`, `order.service.ts`, `shipment.service.ts`, `file.service.ts`
- Models: `user.model.ts`, `order.model.ts`, `shipment.model.ts`, `file.model.ts`

## API Documentation

### Authentication
```json
POST /login
{
  "username": "admin",
  "password": "admin123"
}
```

### Create Order
```json
POST /ext/order
{
  "customerName": "John Doe",
  "shippingAddress": "123 Main St",
  "billingAddress": "123 Main St",
  "items": [
    {
      "productId": "PROD001",
      "productName": "Product 1",
      "quantity": 2,
      "price": 29.99
    }
  ]
}
```

### Create Shipment
```json
POST /ext/shipment
{
  "orderId": 1,
  "carrier": "FedEx",
  "trackingNumber": "123456789",
  "shippingAddress": "123 Main St",
  "status": "PENDING"
}
```



## License

This project is for demonstration purposes only. 
