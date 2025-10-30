# 🔐 Spring Boot JWT Authentication & Authorization System

A production-ready Spring Boot 3.2 application implementing JWT-based authentication with role-based access control (RBAC), refresh token mechanism, comprehensive error handling, and AOP logging.

## 📑 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [Configuration](#%EF%B8%8F-configuration)
- [API Documentation](#-api-documentation)
- [Security Features](#-security-features)
- [Custom JWT Claims](#-custom-jwt-claims)
- [Error Handling](#-error-handling)
- [Logging](#-logging)
- [Testing](#-testing)

---

## ✨ Features

### Authentication & Authorization
- ✅ JWT Access Token (30 min expiration)
- ✅ JWT Refresh Token (7 days expiration)
- ✅ Role-based access control (RBAC)
- ✅ Method-level security with `@PreAuthorize`
- ✅ Custom JWT claims (metadata)
- ✅ Token refresh mechanism
- ✅ Token revocation on logout

### API Design
- ✅ RESTful API endpoints
- ✅ Public routes (no auth required)
- ✅ Protected routes (auth required)
- ✅ Role-specific routes (ADMIN, MODERATOR, USER)
- ✅ Consistent response format with `ApiResponse<T>`
- ✅ HTTP status code best practices

### Error Handling
- ✅ Global exception handler
- ✅ Custom exceptions (ResourceNotFound, BadRequest, etc.)
- ✅ JWT-specific error handling
- ✅ Validation error handling with detailed messages
- ✅ Structured error responses

### Logging & Monitoring
- ✅ AOP-based logging for all controllers
- ✅ AOP-based logging for all services
- ✅ Request/Response logging
- ✅ Execution time tracking
- ✅ Sensitive data masking in logs
- ✅ Exception logging

### Security Best Practices
- ✅ Password encryption with BCrypt
- ✅ Stateless session management
- ✅ CSRF protection (disabled for API)
- ✅ SQL injection prevention (JPA)
- ✅ Input validation
- ✅ Secure JWT signing with HS256

---

## 🏗️ Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ HTTP Request
       ▼
┌─────────────────────────────────────────┐
│      JwtAuthenticationFilter            │
│  (Extract & Validate JWT Token)         │
└──────┬──────────────────────────────────┘
       │ Valid Token
       ▼
┌─────────────────────────────────────────┐
│      SecurityFilterChain                │
│  (Check Roles & Permissions)            │
└──────┬──────────────────────────────────┘
       │ Authorized
       ▼
┌─────────────────────────────────────────┐
│      LoggingAspect (AOP)                │
│  (Log Request/Response)                 │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│      RestController                     │
│  (Handle Request)                       │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│      Service Layer                      │
│  (Business Logic)                       │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│      Repository (JPA)                   │
│  (Database Operations)                  │
└──────┬──────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────┐
│      PostgreSQL Database                │
└─────────────────────────────────────────┘
```

### JWT Authentication Flow

```
1. User Login
   ┌────────┐
   │ Client │──POST /api/v1/auth/login──▶ Server
   └────────┘                                │
                                            ▼
                              ┌──────────────────────────┐
                              │ Validate Credentials     │
                              │ Generate Access Token    │
                              │ Generate Refresh Token   │
                              │ Store Refresh in DB      │
                              └────────┬─────────────────┘
                                       │
   ┌────────┐                          │
   │ Client │◀──────{tokens}───────────┘
   └────────┘

2. Access Protected Resource
   ┌────────┐
   │ Client │──GET /api/v1/user/profile + Bearer Token──▶ Server
   └────────┘                                                │
                                                            ▼
                                              ┌──────────────────────────┐
                                              │ JwtAuthenticationFilter  │
                                              │ Extract & Validate Token │
                                              │ Set Authentication       │
                                              └────────┬─────────────────┘
                                                       │
   ┌────────┐                                          │
   │ Client │◀──────{profile data}─────────────────────┘
   └────────┘

3. Token Refresh
   ┌────────┐
   │ Client │──POST /api/v1/auth/refresh-token + RefreshToken──▶ Server
   └────────┘                                                       │
                                                                   ▼
                                                     ┌──────────────────────────┐
                                                     │ Validate Refresh Token   │
                                                     │ Check DB & Expiry        │
                                                     │ Generate New Access      │
                                                     └────────┬─────────────────┘
                                                              │
   ┌────────┐                                                 │
   │ Client │◀──────{new access token}────────────────────────┘
   └────────┘
```

---

## 📂 Project Structure

```
src/main/java/com/example/
│
├── aspect/
│   └── LoggingAspect.java              # AOP logging for controllers & services
│
├── config/
│   ├── SecurityConfig.java             # Spring Security configuration
│   └── DataInitializer.java            # Initialize roles & default users
│
├── controller/
│   ├── AuthController.java             # Authentication endpoints
│   ├── UserController.java             # User-specific endpoints
│   ├── AdminController.java            # Admin-only endpoints
│   ├── ModeratorController.java        # Moderator endpoints
│   └── PublicController.java           # Public endpoints
│
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── RefreshTokenRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── ApiResponse.java            # Generic response wrapper
│
├── entity/
│   ├── User.java                       # User entity (implements UserDetails)
│   ├── Role.java                       # Role entity
│   └── RefreshToken.java               # Refresh token entity
│
├── exception/
│   ├── GlobalExceptionHandler.java     # Centralized exception handling
│   ├── ResourceNotFoundException.java
│   ├── TokenRefreshException.java
│   ├── BadRequestException.java
│   ├── UnauthorizedException.java
│   └── ForbiddenException.java
│
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   └── RefreshTokenRepository.java
│
├── security/
│   ├── JwtAuthenticationFilter.java    # JWT token validation filter
│   └── JwtAuthenticationEntryPoint.java # Handle auth errors
│
└── service/
    ├── AuthService.java                # Authentication logic
    ├── JwtService.java                 # JWT token generation & validation
    ├── RefreshTokenService.java        # Refresh token management
    └── CustomUserDetailsService.java   # Load user from database
```

---

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 17+ | Programming Language |
| Spring Boot | 3.2.0 | Framework |
| Spring Security | 6.x | Security & Authentication |
| Spring Data JPA | 3.x | Data Persistence |
| Spring AOP | 6.x | Aspect-Oriented Programming |
| JJWT | 0.12.3 | JWT Token Library |
| MySQL | 8.0+ | Database |
| Lombok | 1.18+ | Reduce Boilerplate Code |
| BCrypt | - | Password Encryption |
| Maven | 3.8+ | Build Tool |

---

## 🚀 Getting Started

### Prerequisites

```bash
# Java 17 or higher
java -version

# Maven 3.8+
mvn -version

# MySQL 8.0+
mysql --version
```

### Installation Steps

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd spring-security-jwt
```

#### 2. Setup Database
```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE jwt_db;

-- Optional: Create dedicated user
CREATE USER 'jwt_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON jwt_db.* TO 'jwt_user'@'localhost';
FLUSH PRIVILEGES;

-- Exit
EXIT;
```

**Or using Docker:**
```bash
docker run --name mysql-jwt \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=jwt_db \
  -p 3306:3306 \
  -d mysql:8.0
```

#### 3. Configure Application
Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/jwt_db?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password

jwt:
  secret: your_base64_encoded_secret_key
```

#### 4. Generate JWT Secret
```bash
# Generate secure random key
openssl rand -base64 32

# Or use online generator
# https://generate-random.org/api-token-generator
```

#### 5. Build & Run
```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run

# Or run JAR
java -jar target/spring-security-jwt-1.0.0.jar
```

#### 6. Verify Setup
```bash
# Health check
curl http://localhost:8080/api/v1/public/health

# Expected response:
{
  "success": true,
  "message": "Service is running",
  "data": {
    "status": "UP",
    "timestamp": "2025-10-29T10:00:00",
    "service": "Spring Boot JWT Authentication"
  },
  "statusCode": 200,
  "timestamp": "2025-10-29T10:00:00"
}
```

---

## ⚙️ Configuration

### application.yml Explained

```yaml
# Server Configuration
server:
  port: 8080                          # Application port

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jwt_db
    username: postgres
    password: postgres
  
  jpa:
    hibernate:
      ddl-auto: update                # Auto-create/update tables
    show-sql: true                    # Show SQL in console

# JWT Configuration
jwt:
  secret: <your-secret-key>           # Base64 encoded secret
  access-token-expiration: 1800000    # 30 minutes
  refresh-token-expiration: 604800000 # 7 days

# Logging Configuration
logging:
  level:
    com.example: DEBUG                # Your package log level
    org.springframework.security: DEBUG
```

### Security Configuration

The `SecurityConfig.java` defines route permissions:

```java
// Public routes - No authentication required
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/public/**").permitAll()

// Admin-only routes
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

// Moderator or Admin routes
.requestMatchers("/api/v1/moderator/**").hasAnyRole("MODERATOR", "ADMIN")

// All other routes require authentication
.anyRequest().authenticated()
```

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication Header
```
Authorization: Bearer <access_token>
```

### Response Format

#### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* actual data */ },
  "statusCode": 200,
  "timestamp": "2025-10-29T10:00:00"
}
```

#### Error Response
```json
{
  "success": false,
  "error": "Error message",
  "statusCode": 400,
  "timestamp": "2025-10-29T10:00:00"
}
```

### Endpoints Summary

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/v1/auth/register` | Public | Register new user |
| POST | `/api/v1/auth/login` | Public | Login user |
| POST | `/api/v1/auth/refresh-token` | Public | Refresh access token |
| POST | `/api/v1/auth/logout` | Public | Logout user |
| GET | `/api/v1/public/health` | Public | Health check |
| GET | `/api/v1/user/profile` | Authenticated | Get user profile |
| GET | `/api/v1/admin/users` | ADMIN | Get all users |
| GET | `/api/v1/moderator/dashboard` | MODERATOR/ADMIN | Moderator dashboard |

See [API Testing Guide](API_TESTING_GUIDE.md) for detailed examples.

---

## 🔒 Security Features

### 1. Password Security
- **BCrypt** encryption with salt
- Minimum 6 characters enforced
- Stored securely, never logged

### 2. JWT Token Security
- **HS256** signing algorithm
- **30-minute** access token expiration
- **7-day** refresh token expiration
- Tokens include custom claims
- Stateless authentication

### 3. Refresh Token Management
- Stored in database
- Can be revoked
- One-time use per refresh
- Automatic cleanup of expired tokens

### 4. Role-Based Access Control
```java
// Method level security
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminOnlyMethod() { }

// Multiple roles
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public ResponseEntity<?> moderatorMethod() { }

// Complex expressions
@PreAuthorize("hasRole('USER') and #username == authentication.name")
public ResponseEntity<?> userSpecificMethod(String username) { }
```

### 5. Input Validation
```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 20)
private String username;

@Email(message = "Email should be valid")
private String email;
```

---

## 🎯 Custom JWT Claims

The JWT payload includes custom metadata:

```json
{
  "sub": "john doe",              // Username (standard)
  "userId": 1,                    // Custom: User ID
  "email": "john@example.com",    // Custom: Email
  "firstName": "John",            // Custom: First Name
  "lastName": "Doe",              // Custom: Last Name
  "roles": ["ROLE_USER"],         // Custom: User Roles
  "iat": 1698580200,              // Issued At (standard)
  "exp": 1698582000               // Expiration (standard)
}
```

### Accessing Custom Claims
```java
Claims claims = jwtService.extractAllClaims(token);
Long userId = claims.get("userId", Long.class);
String email = claims.get("email", String.class);
```

---

## ⚠️ Error Handling

### HTTP Status Codes

| Code | Usage | Example |
|------|-------|---------|
| 200 | Success | Data retrieved |
| 201 | Created | User registered |
| 400 | Bad Request | Validation failed |
| 401 | Unauthorized | Invalid credentials |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 500 | Server Error | Unexpected error |

### Exception Handling Flow

```java
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ApiResponse<?>> handleBadCredentials(
        BadCredentialsException ex) {
    ApiResponse<?> response = ApiResponse.error(
        "Invalid username or password", 
        HttpStatus.UNAUTHORIZED.value()
    );
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
}
```

### Validation Errors
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username must be between 3 and 20 characters",
    "email": "Email should be valid"
  },
  "statusCode": 400,
  "timestamp": "2025-10-29T10:00:00"
}
```

---

## 📊 Logging

### AOP Logging Features

The `LoggingAspect` automatically logs:

#### Controller Methods
```
=========================== REQUEST START ===========================
HTTP Method: POST
URI: /api/v1/auth/login
Controller Method: com.example.controller.AuthController.login
IP Address: 127.0.0.1
Request Body: {"username":"admin"}
Response: [CONTAINS TOKENS - HIDDEN]
=========================== REQUEST END =============================
```

#### Service Methods
```
>>> Entering Service: AuthService.login
<<< Exiting Service: AuthService.login - Execution time: 245ms
```

#### Exception Logging
```
Exception in com.example.controller.AuthController.login 
with message: Invalid username or password
```

### Log Levels
```properties
logging.level.com.example=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.root=INFO
```

---

## 🧪 Testing

### Default Test Users

```
Username: admin
Password: admin123
Roles: ROLE_ADMIN

Username: moderator  
Password: moderator123
Roles: ROLE_MODERATOR

Username: user
Password: user123
Roles: ROLE_USER
```

### Testing with cURL

#### 1. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### 2. Access Protected Resource
```bash
curl -X GET http://localhost:8080/api/v1/user/profile \
  -H "Authorization: Bearer <your_access_token>"
```

#### 3. Refresh Token
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<your_refresh_token>"
  }'
```

### Testing with Postman

Import the [Postman Collection](POSTMAN_COLLECTION.json) for ready-to-use requests.

---

## 📝 Best Practices Implemented

1. ✅ **Separation of Concerns** - Clear layer separation
2. ✅ **DRY Principle** - Reusable components
3. ✅ **SOLID Principles** - Clean code architecture
4. ✅ **DTOs** - Separate request/response objects
5. ✅ **Builder Pattern** - Lombok @Builder
6. ✅ **Exception Handling** - Centralized @RestControllerAdvice
7. ✅ **Validation** - @Valid with Bean Validation
8. ✅ **Logging** - AOP-based non-intrusive logging
9. ✅ **Security** - Multiple layers of security
10. ✅ **API Design** - RESTful conventions

---

## 🔧 Troubleshooting

### Issue: 401 Unauthorized
**Cause:** Token expired or invalid  
**Solution:** Use refresh token to get new access token

### Issue: 403 Forbidden
**Cause:** User lacks required role  
**Solution:** Check user roles in database

### Issue: Database Connection Failed
**Cause:** MySQL not running or wrong credentials  
**Solution:** Verify MySQL status and application.yml config

```bash
# Check if MySQL is running
sudo systemctl status mysql  # Linux
brew services list           # macOS

# Start MySQL if needed
sudo systemctl start mysql   # Linux
brew services start mysql    # macOS

# Test connection
mysql -u root -p jwt_db
```

### Issue: Invalid JWT Signature
**Cause:** Wrong secret key or token tampered  
**Solution:** Regenerate token or verify jwt.secret in config

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Email: support@example.com

---

## 🎓 Learning Resources

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [REST API Best Practices](https://restfulapi.net/)

---

**Happy Coding! 🚀**