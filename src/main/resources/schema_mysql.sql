-- MySQL Database Schema Script for QuickBite Food Delivery
-- Use this script to set up your local MySQL database.

CREATE DATABASE IF NOT EXISTS quickbite;
USE quickbite;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
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

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_google ON users(google_id);

-- 2. Restaurants Table
CREATE TABLE IF NOT EXISTS restaurants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    cuisine VARCHAR(255) NOT NULL,
    owner_id INT,
    status VARCHAR(50) DEFAULT 'OPEN',
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 3. Menu Items Table
CREATE TABLE IF NOT EXISTS menu_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL,
    category VARCHAR(255) NOT NULL,
    image_url VARCHAR(500),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- 4. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    restaurant_id INT NOT NULL,
    total DOUBLE NOT NULL,
    status VARCHAR(100) DEFAULT 'PLACED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
);

-- 5. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
);

-- 6. Cart Table
CREATE TABLE IF NOT EXISTS cart (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
);

-- 7. OTP Sessions Table
CREATE TABLE IF NOT EXISTS otp_sessions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    phone_or_email VARCHAR(255) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    resend_count INT DEFAULT 0,
    last_requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    verified TINYINT DEFAULT 0
);
CREATE INDEX idx_otp_target ON otp_sessions(phone_or_email);

-- 8. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_audit_user ON audit_logs(user_id);

-- Insert Seed Data for testing (with mock values)
-- Note: Default passwords here will be validated by the system.
INSERT INTO users (id, name, email, password, role, status) VALUES 
(1, 'Alice Customer', 'customer@quickbite.com', 'password', 'CUSTOMER', 'ACTIVE'),
(2, 'Bob Owner', 'owner@quickbite.com', 'password', 'RESTAURANT_ADMIN', 'ACTIVE'),
(3, 'Charlie Admin', 'admin@quickbite.com', 'password', 'SUPER_ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO restaurants (id, name, cuisine, owner_id, status) VALUES 
(1, 'Royal Snacks & Bites', 'Snacks & Finger Foods', 2, 'OPEN'),
(2, 'Burger Craft & Co.', 'Burgers & Fries', 2, 'OPEN'),
(3, 'Pizzeria Napoli', 'Italian & Pizzas', 2, 'OPEN'),
(5, 'The Campus Maggi Point', 'Maggi & Quick Bites', 2, 'OPEN'),
(6, 'The Campus Ice Cream Parlour', 'Ice Cream & Shakes', 2, 'OPEN')
ON DUPLICATE KEY UPDATE name=VALUES(name), cuisine=VALUES(cuisine);

INSERT INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES 
-- Snacks
(1, 1, 'Mumbai Vada Pav (Double)', 60.00, 'snacks', 'assets/dish_vada_pav.png'),
(2, 1, 'Crispy Samosa Chole Chaat', 75.00, 'snacks', 'assets/dish_samosa.png'),
(3, 1, 'Paneer Pakora Platter', 110.00, 'snacks', 'assets/dish_pakora.png'),
(14, 1, 'Aloo Tikki Burger Pav', 80.00, 'snacks', 'assets/dish_aloo_tikki_burger.png'),
(15, 1, 'Cheesy Loaded Fries', 130.00, 'snacks', 'assets/dish_aloo_tikki_burger.png'),
(32, 1, 'Crispy Onion Rings', 90.00, 'snacks', 'assets/dish_pakora.png'),
(33, 1, 'Paneer Tikka Roll', 120.00, 'snacks', 'assets/dish_paneer_burger.png'),
(34, 1, 'Cheesy Nachos Bowl', 140.00, 'snacks', 'assets/dish_samosa.png'),
-- Burgers
(4, 2, 'Truffle Umami Burger', 350.00, 'burgers', 'assets/dish_truffle_burger.png'),
(20, 2, 'Crispy Paneer & Jalapeno Burger', 240.00, 'burgers', 'assets/dish_paneer_burger.png'),
-- Pizza
(7, 3, 'Truffle Wild Mushroom Pizza', 450.00, 'pizza', 'assets/dish_truffle_pizza.png'),
(8, 3, 'Tandoori Paneer Tikka Pizza', 230.00, 'pizza', 'assets/dish_paneer_pizza.png'),
(21, 3, 'Classic Margherita Extra Virgin', 320.00, 'pizza', 'assets/dish_margherita_pizza.png'),
(39, 3, 'Chicken Tikka Butter Masala Pizza', 230.00, 'pizza', 'assets/dish_chicken_pizza.png'),
(40, 3, 'Spicy Schezwan Paneer Pizza', 210.00, 'pizza', 'assets/dish_paneer_pizza.png'),
(41, 3, 'Double Cheese Margherita Indian Style', 180.00, 'pizza', 'assets/dish_margherita_pizza.png'),
-- Drinks
(11, 1, 'Sweet Mango Lassi', 80.00, 'drinks', 'assets/dish_mango_lassi.png'),
(12, 1, 'Masala Kadak Chai', 45.00, 'drinks', 'assets/dish_masala_chai.png'),
(13, 1, 'Cold Coffee with Chocolate Scoop', 120.00, 'drinks', 'assets/dish_cold_coffee.png'),
(24, 1, 'Iced Hazelnut Latte', 140.00, 'drinks', 'assets/dish_hazelnut_latte.png'),
(42, 1, 'Kesaria Thandai Shahi', 95.00, 'drinks', 'assets/dish_badam_kheer.png'),
(43, 5, 'Mint Masala Chaas (Buttermilk)', 50.00, 'drinks', 'assets/dish_lime_soda.png'),
(44, 1, 'Rose Lassi garnished with Almonds', 90.00, 'drinks', 'assets/dish_rose_lassi.png'),
(45, 5, 'Fresh Lime Soda (Sweet & Salted)', 60.00, 'drinks', 'assets/dish_lime_soda.png'),
-- Maggi
(16, 5, 'Cheese Masala Maggi', 90.00, 'maggi', 'assets/dish_cheese_maggi.png'),
(17, 5, 'Veg Double Masala Maggi', 80.00, 'maggi', 'assets/dish_veg_maggi.png'),
(18, 5, 'Egg & Chicken Spicy Maggi', 120.00, 'maggi', 'assets/dish_chicken_maggi.png'),
(19, 5, 'Spicy Schezwan Vegetable Maggi', 85.00, 'maggi', 'assets/dish_schezwan_maggi.png'),
(26, 5, 'Paneer Tikka Butter Maggi', 110.00, 'maggi', 'assets/dish_paneer_butter_maggi.png'),
(35, 5, 'Crispy Samosa Chole Chaat', 75.00, 'maggi', 'assets/dish_samosa.png'),
(36, 5, 'Mumbai Vada Pav (Double)', 60.00, 'maggi', 'assets/dish_vada_pav.png'),
(37, 5, 'Paneer Pakora Platter', 110.00, 'maggi', 'assets/dish_pakora.png'),
(38, 5, 'Aloo Tikki Burger Pav', 80.00, 'maggi', 'assets/dish_aloo_tikki_burger.png'),
-- Ice Cream / Desserts
(27, 6, 'Gulab Jamun with Ice Cream', 95.00, 'icecream', 'assets/dish_gulab_jamun.png'),
(28, 6, 'Mango Kulfi Slice', 65.00, 'icecream', 'assets/dish_mango_kulfi.png'),
(29, 6, 'Chocolate Fudge Ice Cream', 110.00, 'icecream', 'assets/dish_chocolate_icecream.png'),
(30, 6, 'Saffron Badam Kheer', 85.00, 'icecream', 'assets/dish_badam_kheer.png'),
(31, 6, 'Royal Rose Falooda', 130.00, 'icecream', 'assets/dish_rose_lassi.png'),
(46, 6, 'Kesar Pista Kulfi Stick', 70.00, 'icecream', 'assets/dish_mango_kulfi.png'),
(47, 6, 'Gajar Ka Halwa with Dry Fruits', 120.00, 'icecream', 'assets/dish_gulab_jamun.png'),
(48, 6, 'Rabri Malai Falooda Bowl', 150.00, 'icecream', 'assets/dish_rose_lassi.png'),
(49, 6, 'Hot Rasgulla in Sweet Syrup (2 Pcs)', 80.00, 'icecream', 'assets/dish_gulab_jamun.png'),
(50, 6, 'Double Ka Meetha (Bread Pudding)', 110.00, 'icecream', 'assets/dish_gulab_jamun.png')
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), category=VALUES(category), image_url=VALUES(image_url);
