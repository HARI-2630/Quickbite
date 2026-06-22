package com.quickbite.connection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnection {
    private static Properties props = new Properties();
    private static HikariDataSource ds;

    static {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("Could not find db.properties!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String dbType = props.getProperty("db.type", "sqlite");
            HikariConfig config = new HikariConfig();
            
            if ("sqlite".equalsIgnoreCase(dbType)) {
                String driver = props.getProperty("db.sqlite.driver", "org.sqlite.JDBC");
                String url = props.getProperty("db.sqlite.url", "jdbc:sqlite:quickbite.db");
                
                config.setDriverClassName(driver);
                config.setJdbcUrl(url);
                config.setMaximumPoolSize(10); 
                config.setConnectionTimeout(30000); 
                config.setIdleTimeout(60000);
            } else {
                String driver = props.getProperty("db.mysql.driver", "com.mysql.cj.jdbc.Driver");
                String url = props.getProperty("db.mysql.url");
                String user = props.getProperty("db.mysql.username");
                String pass = props.getProperty("db.mysql.password");
                
                config.setDriverClassName(driver);
                config.setJdbcUrl(url);
                config.setUsername(user);
                config.setPassword(pass);
                config.setMaximumPoolSize(20); 
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
            }
            
            config.setPoolName("QuickBiteConnectionPool");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            ds = new HikariDataSource(config);
            System.out.println("[DBConnection] HikariCP Connection Pool '" + config.getPoolName() + "' initialized successfully.");
            
            // Check SQLite DB initialization on startup
            try (Connection conn = ds.getConnection()) {
                if ("sqlite".equalsIgnoreCase(dbType)) {
                    initializeSqliteDatabase(conn);
                }
            }
        } catch (Exception e) {
            System.err.println("[DBConnection Error] Failed to initialize connection pool:");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try {
            if (ds == null) {
                return null;
            }
            return ds.getConnection();
        } catch (Exception e) {
            System.err.println("[DBConnection Error] Failed to acquire connection from pool: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static synchronized void initializeSqliteDatabase(Connection conn) {
        // Run DDL script to create tables and seed mock data if tables don't exist
        try (Statement stmt = conn.createStatement()) {
            // Check if otp_sessions table exists to see if DB is updated
            ResultSet rs = conn.getMetaData().getTables(null, null, "otp_sessions", null);
            boolean needsInitialization = !rs.next();
            
            if (needsInitialization) {
                System.out.println("Initializing SQLite tables (new schema)...");
                // Let's drop existing tables if they exist to start fresh with clean columns
                stmt.execute("DROP TABLE IF EXISTS users;");
                stmt.execute("DROP TABLE IF EXISTS restaurants;");
                stmt.execute("DROP TABLE IF EXISTS menu_items;");
                stmt.execute("DROP TABLE IF EXISTS orders;");
                stmt.execute("DROP TABLE IF EXISTS order_items;");
                stmt.execute("DROP TABLE IF EXISTS cart;");
                stmt.execute("DROP TABLE IF EXISTS otp_sessions;");
                stmt.execute("DROP TABLE IF EXISTS audit_logs;");
                
                // Load schema.sql resource
                InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql");
                if (is == null) {
                    System.err.println("Could not find schema.sql!");
                    return;
                }
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder sqlBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                            continue;
                        }
                        sqlBuilder.append(line).append(" ");
                        if (line.trim().endsWith(";")) {
                            stmt.execute(sqlBuilder.toString());
                            sqlBuilder.setLength(0);
                        }
                    }
                }
            }
            
            // Seed SQLite data (runs on every startup, safe because of INSERT OR IGNORE)
            String customerHash = com.quickbite.security.PasswordHash.hash("password");
            String ownerHash = com.quickbite.security.PasswordHash.hash("password");
            String adminHash = com.quickbite.security.PasswordHash.hash("password");
            
            stmt.execute("INSERT OR IGNORE INTO users (id, name, email, password, role, status) VALUES (1, 'Alice Customer', 'customer@quickbite.com', '" + customerHash + "', 'CUSTOMER', 'ACTIVE');");
            stmt.execute("INSERT OR IGNORE INTO users (id, name, email, password, role, status) VALUES (2, 'Bob Owner', 'owner@quickbite.com', '" + ownerHash + "', 'RESTAURANT_ADMIN', 'ACTIVE');");
            stmt.execute("INSERT OR IGNORE INTO users (id, name, email, password, role, status) VALUES (3, 'Charlie Admin', 'admin@quickbite.com', '" + adminHash + "', 'SUPER_ADMIN', 'ACTIVE');");
            
            // Force re-seed of restaurant 1 and menu items (to replace Biryani with Snacks)
            stmt.execute("DELETE FROM menu_items WHERE id IN (1, 2, 3, 14, 15, 32, 33, 34);");
            stmt.execute("UPDATE restaurants SET name = 'Royal Snacks & Bites', cuisine = 'Snacks & Finger Foods' WHERE id = 1;");

            stmt.execute("INSERT OR IGNORE INTO restaurants (id, name, cuisine, owner_id, status) VALUES (1, 'Royal Snacks & Bites', 'Snacks & Finger Foods', 2, 'OPEN');");
            stmt.execute("INSERT OR IGNORE INTO restaurants (id, name, cuisine, owner_id, status) VALUES (2, 'Burger Craft & Co.', 'Burgers & Fries', 2, 'OPEN');");
            stmt.execute("INSERT OR IGNORE INTO restaurants (id, name, cuisine, owner_id, status) VALUES (3, 'Pizzeria Napoli', 'Italian & Pizzas', 2, 'OPEN');");
            stmt.execute("INSERT OR IGNORE INTO restaurants (id, name, cuisine, owner_id, status) VALUES (5, 'The Campus Maggi Point', 'Maggi & Quick Bites', 2, 'OPEN');");
            stmt.execute("INSERT OR IGNORE INTO restaurants (id, name, cuisine, owner_id, status) VALUES (6, 'The Campus Ice Cream Parlour', 'Ice Cream & Shakes', 2, 'OPEN');");
            
            // Snacks
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (1, 1, 'Mumbai Vada Pav (Double)', 60.00, 'snacks', 'assets/dish_vada_pav.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (2, 1, 'Crispy Samosa Chole Chaat', 75.00, 'snacks', 'assets/dish_samosa.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (3, 1, 'Paneer Pakora Platter', 110.00, 'snacks', 'assets/dish_pakora.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (14, 1, 'Aloo Tikki Burger Pav', 80.00, 'snacks', 'assets/dish_aloo_tikki_burger.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (15, 1, 'Cheesy Loaded Fries', 130.00, 'snacks', 'assets/dish_aloo_tikki_burger.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (32, 1, 'Crispy Onion Rings', 90.00, 'snacks', 'assets/dish_pakora.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (33, 1, 'Paneer Tikka Roll', 120.00, 'snacks', 'assets/dish_paneer_burger.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (34, 1, 'Cheesy Nachos Bowl', 140.00, 'snacks', 'assets/dish_samosa.png');");
            
            // Burgers
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (4, 2, 'Truffle Umami Burger', 350.00, 'burgers', 'assets/dish_truffle_burger.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (20, 2, 'Crispy Paneer & Jalapeno Burger', 240.00, 'burgers', 'assets/dish_paneer_burger.png');");
            
            // Pizza
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (7, 3, 'Truffle Wild Mushroom Pizza', 450.00, 'pizza', 'assets/dish_truffle_pizza.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (8, 3, 'Tandoori Paneer Tikka Pizza', 230.00, 'pizza', 'assets/dish_paneer_pizza.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (21, 3, 'Classic Margherita Extra Virgin', 320.00, 'pizza', 'assets/dish_margherita_pizza.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (39, 3, 'Chicken Tikka Butter Masala Pizza', 230.00, 'pizza', 'assets/dish_chicken_pizza.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (40, 3, 'Spicy Schezwan Paneer Pizza', 210.00, 'pizza', 'assets/dish_paneer_pizza.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (41, 3, 'Double Cheese Margherita Indian Style', 180.00, 'pizza', 'assets/dish_margherita_pizza.png');");
            
            // Drinks
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (11, 1, 'Sweet Mango Lassi', 80.00, 'drinks', 'assets/dish_mango_lassi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (12, 1, 'Masala Kadak Chai', 45.00, 'drinks', 'assets/dish_masala_chai.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (13, 1, 'Cold Coffee with Chocolate Scoop', 120.00, 'drinks', 'assets/dish_cold_coffee.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (24, 1, 'Iced Hazelnut Latte', 140.00, 'drinks', 'assets/dish_hazelnut_latte.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (42, 1, 'Kesaria Thandai Shahi', 95.00, 'drinks', 'assets/dish_badam_kheer.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (43, 5, 'Mint Masala Chaas (Buttermilk)', 50.00, 'drinks', 'assets/dish_lime_soda.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (44, 1, 'Rose Lassi garnished with Almonds', 90.00, 'drinks', 'assets/dish_rose_lassi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (45, 5, 'Fresh Lime Soda (Sweet & Salted)', 60.00, 'drinks', 'assets/dish_lime_soda.png');");
            
            // Maggi & Quick Bites
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (16, 5, 'Cheese Masala Maggi', 90.00, 'maggi', 'assets/dish_cheese_maggi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (17, 5, 'Veg Double Masala Maggi', 80.00, 'maggi', 'assets/dish_veg_maggi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (18, 5, 'Egg & Chicken Spicy Maggi', 120.00, 'maggi', 'assets/dish_chicken_maggi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (19, 5, 'Spicy Schezwan Vegetable Maggi', 85.00, 'maggi', 'assets/dish_schezwan_maggi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (26, 5, 'Paneer Tikka Butter Maggi', 110.00, 'maggi', 'assets/dish_paneer_butter_maggi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (35, 5, 'Crispy Samosa Chole Chaat', 75.00, 'maggi', 'assets/dish_samosa.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (36, 5, 'Mumbai Vada Pav (Double)', 60.00, 'maggi', 'assets/dish_vada_pav.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (37, 5, 'Paneer Pakora Platter', 110.00, 'maggi', 'assets/dish_pakora.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (38, 5, 'Aloo Tikki Burger Pav', 80.00, 'maggi', 'assets/dish_aloo_tikki_burger.png');");
            
            // Ice Cream / Desserts
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (27, 6, 'Gulab Jamun with Ice Cream', 95.00, 'icecream', 'assets/dish_gulab_jamun.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (28, 6, 'Mango Kulfi Slice', 65.00, 'icecream', 'assets/dish_mango_kulfi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (29, 6, 'Chocolate Fudge Ice Cream', 110.00, 'icecream', 'assets/dish_chocolate_icecream.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (30, 6, 'Saffron Badam Kheer', 85.00, 'icecream', 'assets/dish_badam_kheer.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (31, 6, 'Royal Rose Falooda', 130.00, 'icecream', 'assets/dish_rose_lassi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (46, 6, 'Kesar Pista Kulfi Stick', 70.00, 'icecream', 'assets/dish_mango_kulfi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (47, 6, 'Gajar Ka Halwa with Dry Fruits', 120.00, 'icecream', 'assets/dish_gulab_jamun.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (48, 6, 'Rabri Malai Falooda Bowl', 150.00, 'icecream', 'assets/dish_rose_lassi.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (49, 6, 'Hot Rasgulla in Sweet Syrup (2 Pcs)', 80.00, 'icecream', 'assets/dish_gulab_jamun.png');");
            stmt.execute("INSERT OR IGNORE INTO menu_items (id, restaurant_id, name, price, category, image_url) VALUES (50, 6, 'Double Ka Meetha (Bread Pudding)', 110.00, 'icecream', 'assets/dish_gulab_jamun.png');");
            
            System.out.println("SQLite tables created and seeded successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        if (ds != null) {
            System.out.println("[DBConnection] Shutting down HikariCP connection pool...");
            ds.close();
            System.out.println("[DBConnection] Connection pool shut down cleanly.");
        }
    }
}
