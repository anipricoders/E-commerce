# E-Commerce REST API (Spring Boot + Neon PostgreSQL + JWT)

A production-ready E-Commerce REST API built using **Spring Boot 3**, **Spring Security 6**, **JSON Web Tokens (JWT)**, **Spring Data JPA**, and **Neon PostgreSQL**.

---

## 🚀 Features

- **Customer Authentication & Authorization**:
  - Secure registration with BCrypt password hashing.
  - JWT token generation upon login and registration.
  - Role-based authorization (`ROLE_CUSTOMER`, `ROLE_ADMIN`).
- **Product Catalog & Search**:
  - Show all products.
  - Real-time keyword search across product name, description, and category.
  - Filter products by category.
  - Product details by ID.
  - Pre-seeded diverse sample catalog (Electronics, Footwear, Fashion, Home, etc.).
- **Shopping Cart Management**:
  - Dedicated cart per customer.
  - Add products to cart with stock validation.
  - Update quantities or remove line items.
  - Auto-calculated item subtotals and overall cart totals.
  - Clear cart.
- **Order Placement & Purchase**:
  - **Cart Checkout**: Buy all items currently in cart, automatically validate inventory, deduct product stock quantities, and clear customer cart.
  - **Buy Now (Instant Purchase)**: Directly purchase any product with custom quantity and shipping address without touching the cart.
  - Order history tracking and detailed order lookup.
- **Interactive API Documentation**:
  - Swagger UI / OpenAPI 3 with JWT Bearer authentication support.

---

## 🛠 Tech Stack

- **Java**: 17 LTS
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6 + JJWT 0.12.5
- **Database**: PostgreSQL (Hosted on Neon Tech)
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Jakarta Validation API
- **Documentation**: SpringDoc OpenAPI 2.5.0 (Swagger UI)
- **Build Tool**: Maven / Maven Wrapper

---

## ⚙️ Database Configuration

The application is configured to connect to the provided Neon PostgreSQL database:

```properties
spring.datasource.url=jdbc:postgresql://ep-super-glitter-anoiluf4-pooler.c-6.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
spring.datasource.username=neondb_owner
spring.datasource.password=npg_Ovk3wnoX6DtG
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
server.port=8081
```

---

## 🏃 How to Run the Application

From the project root (`d:/D Data/Testing/E-commerce`):

### Windows (PowerShell / Command Prompt):
```powershell
.\mvnw.cmd spring-boot:run
```

### Linux / macOS:
```bash
./mvnw spring-boot:run
```

---

## 📖 Interactive Swagger UI Documentation

Once running, access the interactive Swagger UI in your browser:
👉 **[http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)**

You can click **Authorize** at the top right, enter `Bearer <your_token>`, and test protected endpoints directly.

---

## 👤 Pre-Seeded Test Credentials

| Role | Email | Password |
|---|---|---|
| Customer | `customer@ecommerce.com` | `password123` |
| Admin | `admin@ecommerce.com` | `admin123` |

---

## 📡 REST API Endpoints Overview

### 1. Authentication (`/api/auth`)

#### A. Customer Registration
- **Endpoint**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
```json
{
  "fullName": "Alice Smith",
  "email": "alice@example.com",
  "password": "Password123!",
  "phone": "+1-555-0144",
  "address": "456 Elm Avenue, Seattle, WA"
}
```
- **Response** (`201 Created`):
```json
{
  "success": true,
  "message": "Customer registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "fullName": "Alice Smith",
    "email": "alice@example.com",
    "role": "ROLE_CUSTOMER"
  }
}
```

#### B. Customer Login
- **Endpoint**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
```json
{
  "email": "customer@ecommerce.com",
  "password": "password123"
}
```
- **Response** (`200 OK`):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "tokenType": "Bearer",
    "id": 1,
    "fullName": "John Customer",
    "email": "customer@ecommerce.com",
    "role": "ROLE_CUSTOMER"
  }
}
```

#### C. Current Customer Profile
- **Endpoint**: `GET /api/auth/me`
- **Access**: Protected (`Authorization: Bearer <token>`)

---

### 2. Product Catalog (`/api/products`)

#### A. Show All Products
- **Endpoint**: `GET /api/products`
- **Access**: Public
- **Response**: List of products with price, stock, category, and image URL.

#### B. Search Products
- **Endpoint**: `GET /api/products/search?keyword=MacBook`
- **Access**: Public
- **Description**: Searches case-insensitively across product `name`, `description`, and `category`.

#### C. Get Product by ID
- **Endpoint**: `GET /api/products/{id}`
- **Access**: Public

#### D. Get Product Categories
- **Endpoint**: `GET /api/products/categories`
- **Access**: Public

#### E. Get Products by Category
- **Endpoint**: `GET /api/products/category/{category}`
- **Access**: Public

---

### 3. Shopping Cart (`/api/cart`)
*All cart endpoints require `Authorization: Bearer <token>`.*

#### A. View Current Cart
- **Endpoint**: `GET /api/cart`
- **Response**:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "customerId": 1,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Apple MacBook Pro 16\"",
        "productCategory": "Electronics",
        "imageUrl": "https://images.unsplash.com/...",
        "unitPrice": 2499.00,
        "quantity": 2,
        "subtotal": 4998.00
      }
    ],
    "totalItems": 2,
    "totalAmount": 4998.00
  }
}
```

#### B. Add Product to Cart
- **Endpoint**: `POST /api/cart/items`
- **Request Body**:
```json
{
  "productId": 1,
  "quantity": 2
}
```

#### C. Update Cart Item Quantity
- **Endpoint**: `PUT /api/cart/items/{itemId}`
- **Request Body**:
```json
{
  "quantity": 3
}
```

#### D. Remove Item from Cart
- **Endpoint**: `DELETE /api/cart/items/{itemId}`

#### E. Clear Cart
- **Endpoint**: `DELETE /api/cart`

---

### 4. Orders & Buying Products (`/api/orders`)
*All order endpoints require `Authorization: Bearer <token>`.*

#### A. Buy Cart Items (Checkout)
- **Endpoint**: `POST /api/orders/checkout`
- **Description**: Checks inventory stock, deducts stock quantities, creates order line items, places the order, and clears the cart.
- **Request Body**:
```json
{
  "shippingAddress": "456 Elm Avenue, Seattle, WA",
  "paymentMethod": "CREDIT_CARD"
}
```
- **Response** (`201 Created`):
```json
{
  "success": true,
  "message": "Order placed successfully from cart items!",
  "data": {
    "id": 1,
    "orderNumber": "ORD-A4AD2CC1",
    "customerId": 1,
    "customerEmail": "customer@ecommerce.com",
    "totalAmount": 4998.00,
    "status": "CONFIRMED",
    "shippingAddress": "456 Elm Avenue, Seattle, WA",
    "paymentMethod": "CREDIT_CARD",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "Apple MacBook Pro 16\"",
        "unitPrice": 2499.00,
        "quantity": 2,
        "subtotal": 4998.00
      }
    ],
    "orderDate": "2026-09-30T15:03:22"
  }
}
```

#### B. Buy Now (Direct Instant Purchase)
- **Endpoint**: `POST /api/orders/buy-now`
- **Description**: Directly purchases a product without needing to add it to the cart first.
- **Request Body**:
```json
{
  "productId": 2,
  "quantity": 1,
  "shippingAddress": "456 Elm Avenue, Seattle, WA",
  "paymentMethod": "UPI"
}
```

#### C. View Customer Order History
- **Endpoint**: `GET /api/orders`
- **Description**: Retrieves all orders placed by the currently authenticated customer, ordered by newest first.

#### D. View Specific Order by ID
- **Endpoint**: `GET /api/orders/{id}`
