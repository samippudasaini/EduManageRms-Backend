# RMS Backend — Spring Boot

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.x

## Setup Steps

### 1. Create MySQL Database
```sql
CREATE DATABASE rms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure application.properties
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rms?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
```

### 3. Run the Application
```bash
mvn spring-boot:run
```
The backend starts at: http://localhost:8080

### 4. Create Default Admin User
After first run (tables auto-created), run in MySQL:
```sql
USE rms;
INSERT INTO users (name, password, type) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin');
```
Default login: username=`admin`, password=`admin123`

## API Base URL
http://localhost:8080/api
