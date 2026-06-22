-- -- RMS Database Setup Script
-- -- Run this in MySQL before starting the application
--
-- CREATE DATABASE IF NOT EXISTS rms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE rms;
--
-- -- Insert default admin user (password: admin123)
-- -- The application uses spring.jpa.hibernate.ddl-auto=update so tables auto-create
-- -- After tables are created, run this insert:
-- INSERT IGNORE INTO users (name, password, type) VALUES
-- ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin');
-- -- Default password is: admin123

-- RMS Database Setup Script
-- Run this in MySQL before starting the application

CREATE DATABASE IF NOT EXISTS rms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rms;

-- NOTE: the default admin user (admin / admin123) is now created/repaired
-- automatically at application startup by com.rms.config.DataSeeder.
-- No manual INSERT is needed (the bcrypt hash previously hard-coded here
-- did not actually correspond to "admin123" and caused 401 login errors).