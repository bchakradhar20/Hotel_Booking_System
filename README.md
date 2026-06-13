# Hotel Room Reservation System

A production-quality full-stack Hotel Reservation System built with Spring Boot 3 + React, featuring microservices architecture.

---

## Tech Stack

**Backend:** Java 17, Spring Boot 3.2, Spring Security, JWT, JPA/Hibernate, MySQL, ModelMapper, Swagger/OpenAPI 3, JUnit 5, Mockito

**Microservices:** Spring Cloud Gateway, Netflix Eureka, OpenFeign

**Frontend:** React 18, React Router 6, Axios, Bootstrap 5, Vite

---

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+

---

## Database Setup

MySQL will auto-create the databases on first startup via:
```
createDatabaseIfNotExist=true
```
Password is configured as `victus` in `application.yml` files.

**Databases Created:**
- `hotel_auth_db` - Users and authentication
- `hotel_room_db` - Room management
- `hotel_reservation_db` - Reservations

---

## Running the Microservices

### Quick Start (All Services)
```bash
# Build all microservices
cd microservices
mvn clean install -DskipTests

# Start services (run each in separate terminal)
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd auth-service && mvn spring-boot:run
cd room-service && mvn spring-boot:run
cd reservation-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
```

### Using Scripts
```bash
# Windows
start-microservices.bat
stop-microservices.bat
check-microservices-status.bat
```

**Service URLs:**
- Eureka Server: http://localhost:8761
- API Gateway: http://localhost:8085
- Auth Service: http://localhost:8081
- Room Service: http://localhost:8082
- Reservation Service: http://localhost:8083
- User Service: http://localhost:8084

---

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on: http://localhost:3000

---

## Creating an Admin User

1. Register a user via `POST /api/auth/signup` or the Register page.
2. In MySQL, run:
```sql
INSERT INTO user_roles (user_id, role_id)
SELECT u.user_id, r.role_id
FROM users u, roles r
WHERE u.username = 'yourusername' AND r.role_name = 'ROLE_ADMIN';
```

---

## Microservices API Endpoints - ALL THROUGH API GATEWAY

### 🚨 IMPORTANT: Use API Gateway for ALL Requests
**All endpoints must be accessed through API Gateway at `http://localhost:8085`**

The API Gateway handles:
- Authentication and JWT token validation
- Service discovery and load balancing
- Request routing to appropriate microservices
- Adding user context headers (X-User-Id, X-Username)

---

### 🔐 Authentication Service - Via API Gateway

#### Public Endpoints (No Authentication Required)

**Register User**
```http
POST http://localhost:8085/api/auth/signup
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Login User**
```http
POST http://localhost:8085/api/auth/signin
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}

# Response includes JWT token:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "roles": ["ROLE_USER"]
}
```

**Create Admin User**
```http
POST http://localhost:8085/api/auth/create-admin
Content-Type: application/json

{
  "firstName": "Admin",
  "lastName": "User",
  "username": "admin",
  "email": "admin@hotel.com",
  "password": "admin123",
  "adminSecret": "HotelAdmin@2024"
}
```

---

### 🏨 Room Service - Via API Gateway

#### Public Endpoints (No Authentication Required)

**Get All Rooms**
```http
GET http://localhost:8085/api/rooms
```

**Get Room by ID**
```http
GET http://localhost:8085/api/rooms/1
```

#### Protected Endpoints (Requires JWT Token)

**Create Room (ADMIN only)**
```http
POST http://localhost:8085/api/rooms
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
  "roomNumber": "101",
  "roomType": "DELUXE",
  "capacity": 2,
  "pricePerNight": 150.00,
  "description": "Spacious deluxe room with city view",
  "amenities": ["WiFi", "AC", "TV", "Mini Bar"]
}
```

**Update Room (ADMIN only)**
```http
PUT http://localhost:8085/api/rooms/1
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
  "roomNumber": "101",
  "roomType": "DELUXE",
  "capacity": 2,
  "pricePerNight": 175.00,
  "description": "Updated deluxe room with ocean view",
  "amenities": ["WiFi", "AC", "TV", "Mini Bar", "Balcony"]
}
```

**Delete Room (ADMIN only)**
```http
DELETE http://localhost:8085/api/rooms/1
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

### 📅 Reservation Service - Via API Gateway

#### All Endpoints Require Authentication

**Create Reservation (USER/ADMIN)**
```http
POST http://localhost:8085/api/reservations
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
  "roomId": 1,
  "checkInDate": "2024-12-15",
  "checkOutDate": "2024-12-18"
}

# Gateway automatically adds X-User-Id and X-Username headers
```

**Get My Reservations**
```http
GET http://localhost:8085/api/reservations/my
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Get All Reservations (ADMIN only)**
```http
GET http://localhost:8085/api/reservations
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Cancel My Reservation**
```http
DELETE http://localhost:8085/api/reservations/my/1
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Delete Any Reservation (ADMIN only)**
```http
DELETE http://localhost:8085/api/reservations/1
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

### 👤 User Service - Via API Gateway

#### All Endpoints Require Authentication

**Get My Profile**
```http
GET http://localhost:8085/api/users/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Update My Profile**
```http
PUT http://localhost:8085/api/users/profile
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

{
  "firstName": "John",
  "lastName": "Smith",
  "email": "johnsmith@example.com"
}
```

**Get All Users (ADMIN only)**
```http
GET http://localhost:8085/api/users
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## Sample Room Types

```json
{
  "roomTypes": [
    {
      "type": "STANDARD",
      "capacity": 2,
      "basePrice": 100.00
    },
    {
      "type": "DELUXE",
      "capacity": 2,
      "basePrice": 150.00
    },
    {
      "type": "SUITE",
      "capacity": 4,
      "basePrice": 250.00
    },
    {
      "type": "FAMILY",
      "capacity": 6,
      "basePrice": 300.00
    }
  ]
}
```

---

## Swagger Documentation - Through API Gateway

- **API Gateway (All Services):** http://localhost:8085/swagger-ui.html
- **Individual Service Docs (Direct Access):**
  - Auth Service: http://localhost:8081/swagger-ui/index.html
  - Room Service: http://localhost:8082/swagger-ui/index.html
  - Reservation Service: http://localhost:8083/swagger-ui/index.html
  - User Service: http://localhost:8084/swagger-ui/index.html
- **Eureka Dashboard:** http://localhost:8761

**⚠️ Note:** For production use, always use the API Gateway endpoints in your applications.

---

## 📋 Complete Testing Workflow with Postman

### Step 1: Register a User
```http
POST http://localhost:8085/api/auth/signup
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Step 2: Login and Get JWT Token
```http
POST http://localhost:8085/api/auth/signin
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}

# Copy the token from response for next requests
```

### Step 3: Create Admin User (Optional)
```http
POST http://localhost:8085/api/auth/create-admin
Content-Type: application/json

{
  "firstName": "Admin",
  "lastName": "User",
  "username": "admin",
  "email": "admin@hotel.com",
  "password": "admin123",
  "adminSecret": "HotelAdmin@2024"
}
```

### Step 4: Create Rooms (Admin Required)
```http
POST http://localhost:8085/api/rooms
Content-Type: application/json
Authorization: Bearer ADMIN_JWT_TOKEN

{
  "roomNumber": "101",
  "roomType": "STANDARD",
  "capacity": 2,
  "pricePerNight": 100.00,
  "description": "Standard room with basic amenities",
  "amenities": ["WiFi", "AC", "TV"]
}
```

### Step 5: Get All Rooms (Public)
```http
GET http://localhost:8085/api/rooms
```

### Step 6: Book a Room
```http
POST http://localhost:8085/api/reservations
Content-Type: application/json
Authorization: Bearer USER_JWT_TOKEN

{
  "roomId": 1,
  "checkInDate": "2024-12-15",
  "checkOutDate": "2024-12-18"
}
```

### Step 7: View My Reservations
```http
GET http://localhost:8085/api/reservations/my
Authorization: Bearer USER_JWT_TOKEN
```

### Step 8: Update User Profile
```http
PUT http://localhost:8085/api/users/profile
Content-Type: application/json
Authorization: Bearer USER_JWT_TOKEN

{
  "firstName": "Johnny",
  "lastName": "Doe",
  "email": "johnny.doe@example.com"
}
```

---

## 🚀 Postman Collection Import

Create a Postman collection with these environment variables:

**Environment Variables:**
- `gateway_url`: `http://localhost:8085`
- `user_token`: `{{token from login response}}`
- `admin_token`: `{{token from admin login response}}`

**Pre-request Script for Authentication:**
```javascript
// Add this to collection pre-request script
if (pm.request.headers.has("Authorization")) {
    pm.request.headers.upsert({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("user_token")
    });
}
```

---

## Running Tests

```bash
cd microservices
mvn test
```