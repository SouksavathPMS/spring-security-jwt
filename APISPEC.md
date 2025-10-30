# Spring Boot JWT Authentication - API Testing Guide

## 📋 Table of Contents
1. [Setup Instructions](#setup-instructions)
2. [API Endpoints](#api-endpoints)
3. [Testing Scenarios](#testing-scenarios)
4. [Postman Collection Examples](#postman-collection-examples)

---

## 🚀 Setup Instructions

### 1. Database Setup
```sql
-- Create PostgreSQL database
CREATE DATABASE jwt_db;

-- Or use H2 in-memory database (for testing)
-- Just change in application.yml
```

### 2. Generate JWT Secret
```bash
# Generate a secure random key (256-bit)
openssl rand -base64 32
```

### 3. Run Application
```bash
mvn clean install
mvn spring-boot:run
```

### 4. Default Users
```
Admin:     admin / admin123
Moderator: moderator / moderator123
User:      user / user123
```

---

## 🔌 API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Health Check
```http
GET http://localhost:8080/api/v1/public/health
```

#### 2. Application Info
```http
GET http://localhost:8080/api/v1/public/info
```

#### 3. Welcome
```http
GET http://localhost:8080/api/v1/public/welcome
```

---

### Authentication Endpoints

#### 1. Register New User
```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
    "tokenType": "Bearer",
    "expiresIn": 1800000,
    "username": "johndoe",
    "email": "john@example.com",
    "roles": ["ROLE_USER"]
  },
  "statusCode": 201,
  "timestamp": "2025-10-29T10:30:00"
}
```

#### 2. Login
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7",
    "tokenType": "Bearer",
    "expiresIn": 1800000,
    "username": "admin",
    "email": "admin@example.com",
    "roles": ["ROLE_ADMIN"]
  },
  "statusCode": 200,
  "timestamp": "2025-10-29T10:31:00"
}
```

#### 3. Refresh Access Token
```http
POST http://localhost:8080/api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.NEW_TOKEN...",
    "refreshToken": "b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7",
    "tokenType": "Bearer",
    "expiresIn": 1800000,
    "username": "admin",
    "email": "admin@example.com",
    "roles": ["ROLE_ADMIN"]
  },
  "statusCode": 200,
  "timestamp": "2025-10-29T10:32:00"
}
```

#### 4. Logout
```http
POST http://localhost:8080/api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "b2c3d4e5-f6g7-h8i9-j0k1-l2m3n4o5p6q7"
}
```

---

### Protected Endpoints (Authentication Required)

#### User Endpoints (ROLE_USER)

##### 1. Get Profile
```http
GET http://localhost:8080/api/v1/user/profile
Authorization: Bearer {accessToken}
```

##### 2. User Dashboard
```http
GET http://localhost:8080/api/v1/user/dashboard
Authorization: Bearer {accessToken}
```

##### 3. Get User Data
```http
GET http://localhost:8080/api/v1/user/data
Authorization: Bearer {accessToken}
```

---

#### Admin Endpoints (ROLE_ADMIN Only)

##### 1. Get All Users
```http
GET http://localhost:8080/api/v1/admin/users
Authorization: Bearer {accessToken}
```

##### 2. Get User by ID
```http
GET http://localhost:8080/api/v1/admin/users/1
Authorization: Bearer {accessToken}
```

##### 3. Delete User
```http
DELETE http://localhost:8080/api/v1/admin/users/1
Authorization: Bearer {accessToken}
```

##### 4. Admin Dashboard
```http
GET http://localhost:8080/api/v1/admin/dashboard
Authorization: Bearer {accessToken}
```

---

#### Moderator Endpoints (ROLE_MODERATOR or ROLE_ADMIN)

##### 1. Moderator Dashboard
```http
GET http://localhost:8080/api/v1/moderator/dashboard
Authorization: Bearer {accessToken}
```

##### 2. Moderate Content
```http
POST http://localhost:8080/api/v1/moderator/moderate
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "content": "Some content to moderate"
}
```

---

## 🧪 Testing Scenarios

### Scenario 1: Register and Login Flow
```bash
# 1. Register new user
POST /api/v1/auth/register

# 2. Login with credentials
POST /api/v1/auth/login

# 3. Access protected resource
GET /api/v1/user/profile (with Bearer token)
```

### Scenario 2: Token Refresh Flow
```bash
# 1. Login
POST /api/v1/auth/login
# Save accessToken and refreshToken

# 2. Wait 30 minutes (or manually expire token)

# 3. Try to access protected resource
GET /api/v1/user/profile
# Response: 401 Unauthorized

# 4. Refresh the token
POST /api/v1/auth/refresh-token
{
  "refreshToken": "saved_refresh_token"
}

# 5. Use new access token
GET /api/v1/user/profile (with new Bearer token)
```

### Scenario 3: Role-Based Access Control
```bash
# 1. Login as regular user
POST /api/v1/auth/login
{
  "username": "user",
  "password": "user123"
}

# 2. Try to access admin endpoint
GET /api/v1/admin/users
# Response: 403 Forbidden

# 3. Login as admin
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "admin123"
}

# 4. Access admin endpoint
GET /api/v1/admin/users
# Response: 200 OK with users list
```

### Scenario 4: Error Handling
```bash
# 1. Invalid credentials
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "wrongpassword"
}
# Response: 401 Unauthorized

# 2. Expired token
GET /api/v1/user/profile
Authorization: Bearer expired_token
# Response: 401 Unauthorized

# 3. Invalid token format
GET /api/v1/user/profile
Authorization: Bearer invalid_token
# Response: 401 Unauthorized

# 4. Missing required fields
POST /api/v1/auth/register
{
  "username": "test"
}
# Response: 400 Bad Request with validation errors
```

---

## 📮 Postman Collection Examples

### Environment Variables
```json
{
  "baseUrl": "http://localhost:8080",
  "accessToken": "",
  "refreshToken": ""
}
```

### Pre-request Script (Auto Token Refresh)
```javascript
// Save this in Postman collection settings
const moment = require('moment');

// Get token expiry from environment
const tokenExpiry = pm.environment.get('tokenExpiry');
const refreshToken = pm.environment.get('refreshToken');

// Check if token is expired or about to expire
if (!tokenExpiry || moment().isAfter(moment(tokenExpiry).subtract(1, 'minute'))) {
    if (refreshToken) {
        // Refresh token
        const refreshRequest = {
            url: pm.environment.get('baseUrl') + '/api/v1/auth/refresh-token',
            method: 'POST',
            header: {
                'Content-Type': 'application/json',
            },
            body: {
                mode: 'raw',
                raw: JSON.stringify({ refreshToken: refreshToken })
            }
        };
        
        pm.sendRequest(refreshRequest, (err, response) => {
            if (!err) {
                const jsonData = response.json();
                pm.environment.set('accessToken', jsonData.data.accessToken);
                pm.environment.set('tokenExpiry', moment().add(30, 'minutes').toISOString());
            }
        });
    }
}
```

### Test Scripts (Save Token After Login)
```javascript
// Add this to Login/Register request Tests tab
if (pm.response.code === 200 || pm.response.code === 201) {
    const jsonData = pm.response.json();
    pm.environment.set('accessToken', jsonData.data.accessToken);
    pm.environment.set('refreshToken', jsonData.data.refreshToken);
    pm.environment.set('tokenExpiry', moment().add(30, 'minutes').toISOString());
}
```

---

## 🔍 JWT Token Structure

### Decoded JWT Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Decoded JWT Payload (Custom Claims)
```json
{
  "sub": "admin",
  "userId": 1,
  "email": "admin@example.com",
  "firstName": "Admin",
  "lastName": "User",
  "roles": ["ROLE_ADMIN"],
  "iat": 1698580200,
  "exp": 1698582000
}
```

---

## 📊 Status Codes Reference

| Status Code | Meaning | When Used |
|------------|---------|-----------|
| 200 | OK | Successful request |
| 201 | Created | User registered successfully |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Invalid credentials or token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server-side error |

---

## 🛡️ Security Best Practices Implemented

1. ✅ **Password Encryption** - BCrypt with salt
2. ✅ **JWT Token Signing** - HS256 algorithm
3. ✅ **Token Expiration** - 30 minutes for access, 7 days for refresh
4. ✅ **Refresh Token Rotation** - New tokens on refresh
5. ✅ **Role-Based Access Control** - @PreAuthorize annotations
6. ✅ **Input Validation** - @Valid with custom error messages
7. ✅ **SQL Injection Prevention** - JPA with parameterized queries
8. ✅ **CORS Configuration** - Can be customized
9. ✅ **Sensitive Data Logging** - Passwords/tokens hidden in logs
10. ✅ **Token Revocation** - Refresh tokens can be revoked

---

## 🐛 Common Issues & Solutions

### Issue 1: 401 Unauthorized
**Solution:** Check if token is valid and not expired. Use refresh token endpoint.

### Issue 2: 403 Forbidden
**Solution:** User doesn't have required role. Check role assignment.

### Issue 3: Token Expired
**Solution:** Use refresh token to get new access token.

### Issue 4: Database Connection Error
**Solution:** Check PostgreSQL is running and credentials are correct.

---

## 📝 Notes

- Access tokens expire in **30 minutes**
- Refresh tokens expire in **7 days**
- Tokens are **stateless** (no server-side storage for access tokens)
- Refresh tokens are **stored in database** and can be revoked
- All endpoints (except public) require valid JWT token
- Role hierarchy: ADMIN > MODERATOR > USER

---

## 🔗 Additional Resources

- JWT.io - Decode and verify JWT tokens: https://jwt.io
- Postman - API testing tool: https://www.postman.com
- Spring Security Docs: https://docs.spring.io/spring-security/reference/