# QuickBite — Premium Enterprise-Grade Java Web Application

QuickBite is a state-of-the-art, production-ready food delivery web application built using **Java Servlets (Jakarta EE)**, **JavaServer Pages (JSP)**, **JDBC**, and a dual database layer (**MySQL / SQLite**). 

This upgraded version is engineered to demonstrate **enterprise-level web architecture** and advanced security integrations to HR and technical recruiters.

---

## 🔐 Advanced Security & Auth Architecture

1.  **Multi-Factor Authentication (MFA)**:
    *   **Phone Login with OTP**: Users authenticate via SMS verification codes using the **Twilio SMS REST API** (with a graceful logging sandbox simulation when API keys are absent).
    *   **Google OAuth 2.0 with Gmail Verification**: Seamless Google account linkage with a secondary verification code dispatched to their Gmail account.
2.  **JWT Session Management**:
    *   Dependency-free, custom-built Base64url **HMAC-SHA256 JWT builder**.
    *   Generates short-lived **Access Tokens (15-minute expiration)** and persistent **Refresh Tokens (7-day expiration)**.
    *   Stored securely in **HttpOnly, SameSite=Strict, Secure cookies** to completely prevent XSS and CSRF token theft.
3.  **Password Protection**:
    *   Implements **PBKDF2 Hashing** (`PBKDF2WithHmacSHA256`) with a cryptographically secure 16-byte random salt and 65,536 iterations.
    *   Includes constant-time comparison to prevent timing attacks.
4.  **IP Rate Limiting**:
    *   Thread-safe, in-memory **Token Bucket Rate Limiter** to prevent brute-force attacks on sensitive endpoints (maximum 5 auth requests/minute per IP, returning standard `429 Too Many Requests`).
5.  **Role-Based Access Control (RBAC)**:
    *   Strictly protected servlet mapping filters validating JWT claims.
    *   System roles: `CUSTOMER`, `RESTAURANT_ADMIN` (Manager), and `SUPER_ADMIN`.
6.  **Security Audit Logs**:
    *   Tracks all operations (logins, registrations, profile updates, status changes, and admin directives) alongside timestamps, client user agents, and IP addresses.

---

## 🎨 Clean UI/UX & Localizations

*   **Responsive Obsidion Design**: Fully mobile-responsive UI with smooth animation transitions, glassmorphic navigation panels, and interactive HTML5 particle backgrounds.
*   **Notifications & Toast Alerts**: Modern sliding toast notifications for real-time status updates and action confirmations.
*   **Dual Language Support**: Instant client-side localization toggle (supports **English** 🇬🇧 and **Hindi** 🇮🇳).
*   **Dark Mode Selector**: LocalStorage-persisted dark mode toggle.
*   **Dynamic Avatar System**: Interactive profile photo uploads via Multipart servlet handlers, with DiceBear bot identifiers fallback.
*   **Defensive Error Pages**: Beautiful custom `error-404.jsp` (Page Not Found) and `error-500.jsp` (Server Error) configurations.

---

## 🏗️ Technical Stack & Project Layout

*   **Language & Platform**: Java 17+ (LTS) & Maven
*   **Web Server**: Apache Tomcat 10.1 (Jakarta EE 10 / Servlet 6.0)
*   **UI Engine**: JSP (JSTL Core) + Vanilla CSS Variables + JavaScript
*   **Testing Suite**: JUnit Jupiter (JUnit 5)
*   **Container Support**: Docker & Docker Compose

### Package Directory Architecture
```
quickbite/
├── src/main/java/com/quickbite/
│   ├── connection/
│   │   └── DBConnection.java         # Connection management & auto-migrations
│   ├── models/
│   │   ├── User.java                 # User profile model
│   │   ├── AuditLog.java            # Audit record model
│   │   └── OtpSession.java          # SMS/Email verification model
│   ├── dao/
│   │   ├── UserDAO.java              # CRUD operations for profile updates
│   │   ├── AuditLogDAO.java          # Audit logs repository
│   │   └── OtpDAO.java               # Active OTP credentials tracker
│   ├── security/
│   │   ├── JwtUtil.java              # HMAC-SHA256 JWT builder & claims parser
│   │   ├── PasswordHash.java         # PBKDF2 cryptography suite
│   │   ├── RateLimiter.java          # Token Bucket rate limiter
│   │   ├── SmsEmailService.java      # Twilio API client & sandbox logging simulator
│   │   └── Config.java               # Loads environment parameters from .env
│   ├── filters/
│   │   ├── RateLimitFilter.java      # Brute-force block filter
│   │   └── JwtAuthFilter.java        # Security filter mapping cookies to session
│   └── servlets/
│       ├── AuthOtpServlet.java       # OTP lifecycle handler
│       ├── GoogleLoginServlet.java   # Google OAuth callback & sync
│       ├── UserDashboardServlet.java # Profile and upload handler
│       ├── AdminUserServlet.java     # User directory controls (RBAC)
│       └── AuthServlet.java          # Session logouts & credential matching
```

---

## ⚙️ Configuration Setup & Steps

### 1. Configure Secrets (`.env`)
Create a `.env` file in the root workspace folder (a template is preloaded):
```env
db.type=sqlite
JWT_SECRET=super-secret-key-quickbite-2026-production-ready

# Twilio Configuration (Optional - defaults to simulated console logs)
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
TWILIO_PHONE_NUMBER=your_twilio_phone

# Google OAuth 2.0 Credentials (Optional - defaults to simulator dashboard)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_REDIRECT_URI=http://localhost:8080/quickbite/google-login
```

### 2. Standard Local Compilation & Run
1.  **Import to Eclipse**:
    *   File ➔ Import... ➔ Maven ➔ Existing Maven Projects ➔ Choose directory containing `pom.xml`.
2.  **Deploy to Tomcat**:
    *   Add **Apache Tomcat v10.1** in Servers tab.
    *   Right-click project ➔ Run As ➔ Run on Server.
    *   Navigate to **`http://localhost:8080/quickbite`**.

### 3. Run with Docker Compose (Production Environment)
To launch the Tomcat application container alongside a MySQL server instance:
```bash
# Build war and spin up Tomcat + MySQL containers
docker-compose up --build
```
The init script automatically executes `schema_mysql.sql` inside the DB container, configures tables, and binds the web app to port `8080`.

### 4. Running Verification Unit Tests
To execute all auth security JUnit 5 tests, run:
```bash
mvn test
```

---

## 🧪 API Testing with Postman
A pre-configured Postman request suite is exported in the workspace:
*   Import **`quickbite_postman_collection.json`** into Postman.
*   Includes requests for traditional logins, OTP codes dispatching, password resets, profile edits, and admin directories.

---

## 👥 Seed Accounts for Testing
Use these pre-loaded accounts to log in:

| Role | Email | Password |
| :--- | :--- | :--- |
| **Super Admin** | `admin@quickbite.com` | `password` |
| **Restaurant Manager** | `owner@quickbite.com` | `password` |
| **Customer** | `customer@quickbite.com` | `password` |
