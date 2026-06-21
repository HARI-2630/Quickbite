-- DDL Database Schema Script for QuickBite Food Delivery (SQLite)

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    google_id VARCHAR(100) UNIQUE,
    avatar_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_phone ON users(phone) WHERE phone IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_google ON users(google_id) WHERE google_id IS NOT NULL;

-- 2. Restaurants Table
CREATE TABLE IF NOT EXISTS restaurants (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    cuisine VARCHAR(255) NOT NULL,
    owner_id INTEGER,
    status VARCHAR(50) DEFAULT 'OPEN'
);

-- 3. Menu Items Table
CREATE TABLE IF NOT EXISTS menu_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    restaurant_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    category VARCHAR(255) NOT NULL,
    image_url VARCHAR(500)
);

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    restaurant_id INTEGER NOT NULL,
    total DOUBLE NOT NULL,
    status VARCHAR(100) DEFAULT 'PLACED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    price DOUBLE NOT NULL
);

-- 6. Cart Table
CREATE TABLE IF NOT EXISTS cart (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL
);

-- 7. OTP Sessions Table
CREATE TABLE IF NOT EXISTS otp_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    phone_or_email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    resend_count INTEGER DEFAULT 0,
    last_requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified INTEGER DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_otp_target ON otp_sessions(phone_or_email);

-- 8. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
