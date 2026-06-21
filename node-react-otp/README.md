# QuickBite — Real OTP Delivery Setup Guide (Node.js + React + MongoDB)

This sub-project solves actual OTP SMS delivery (using **Twilio**), actual Gmail inbox OTP delivery (using **Nodemailer + App Passwords**), Google Sign-In verification, and session storage with MongoDB TTL expiration.

---

## 📦 Prerequisites

Ensure you have the following installed on your machine:
- **Node.js** (v16+)
- **npm** (v8+)
- **MongoDB** (running locally on port `27017` or via MongoDB Atlas connection URI)

---

## ⚙️ Step-by-Step Services Configuration

### 1. Twilio SMS Setup (Phone OTP)
To send real SMS to mobile devices:
1.  Go to [Twilio.com](https://www.twilio.com) and create a free trial account.
2.  Once logged in, open the **Twilio Console Dashboard**.
3.  Locate your **Account SID** and **Auth Token** in the "Account Info" section. Copy these credentials.
4.  Click **Get a trial phone number** to create an active Twilio SMS sender phone number. Copy the number (with country code).
5.  If using a free trial account, you can only send SMS to your verified phone number. Navigate to **Phone Numbers ➔ Verified Caller IDs** and add the phone number you plan to use for local testing.
6.  Open your `.env` file and input:
    ```env
    TWILIO_ACCOUNT_SID=your_copied_sid
    TWILIO_AUTH_TOKEN=your_copied_auth_token
    TWILIO_PHONE_NUMBER=your_copied_trial_number
    ```

### 2. Gmail App Password Setup (Email OTP)
To send verification emails to users' inboxes:
1.  Log into your **Google Account** settings console.
2.  Navigate to **Security** on the left menu.
3.  Under the "How you sign in to Google" section, verify that **2-Step Verification** is turned **ON** (this is a security requirement for App Passwords).
4.  In the search bar at the top, search for **App passwords** and select it.
5.  In the "App Name" field, enter a description (e.g. `QuickBite Auth`).
6.  Click **Create**.
7.  Copy the generated **16-character password** (shown in a yellow box).
8.  Open your `.env` file and input:
    ```env
    EMAIL_USER=your_full_gmail_address@gmail.com
    EMAIL_PASS=your_copied_16_character_app_password
    ```

---

## 🚀 How to Run Locally

### 1. Setup Backend
1. Open your terminal and navigate to the backend directory:
   ```bash
   cd node-react-otp/backend
   ```
2. Initialize and install dependencies:
   ```bash
   npm init -y
   npm install express mongoose twilio nodemailer jsonwebtoken cookie-parser cors dotenv bcryptjs google-auth-library
   ```
3. Copy `.env.example` to `.env` and fill in your credentials:
   ```bash
   cp ../.env.example .env
   ```
4. Start the server:
   ```bash
   node server.js
   ```

### 2. Setup Frontend
To integrate the premium JSX login card into your React application:
1. Copy [OtpComponent.jsx](file:///Users/ntr/Desktop/qucik%20bite/node-react-otp/frontend/OtpComponent.jsx) to your React components directory (e.g., `src/components/`).
2. Add it to your router or render it inside `App.jsx`:
   ```jsx
   import OtpComponent from './components/OtpComponent';
   
   function App() {
     return <OtpComponent />;
   }
   ```

---

## 🧪 Testing the APIs Locally

### A. Phone SMS OTP Test
To request a real SMS OTP code, send a POST request (using cURL or Postman):
```bash
curl -X POST http://localhost:5000/api/auth/otp/send-phone \
     -H "Content-Type: application/json" \
     -d '{"phone": "+919876543210"}'
```
On success, you will receive the SMS immediately on your phone, and the hashed verification code will be saved in MongoDB with a 5-minute TTL expiration.

To verify the OTP code:
```bash
curl -X POST http://localhost:5000/api/auth/otp/verify-phone \
     -H "Content-Type: application/json" \
     -d '{"phone": "+919876543210", "otp": "YOUR_SMS_OTP_CODE"}'
```

### B. Google OAuth Secondary 2FA Test
To bypass setting up Google OAuth Client keys for local evaluation, use our **Simulation Token** starting with `simulated_token_` to test the full 2FA flow:
```bash
curl -X POST http://localhost:5000/api/auth/google \
     -H "Content-Type: application/json" \
     -d '{"idToken": "simulated_token_recruiter@gmail.com"}'
```
On trigger:
1. Google link synchronization will occur.
2. A real verification code will be sent to the email `recruiter@gmail.com` using Nodemailer and Gmail SMTP.
3. Check the inbox of that email to retrieve the 6-digit code!
4. Submit that code to `http://localhost:5000/api/auth/otp/verify-email` to complete the login.
