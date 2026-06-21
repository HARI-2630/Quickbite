const path = require('path');
// Load local backend .env first (if any)
require('dotenv').config({ path: path.join(__dirname, '.env') });
// Fall back to root .env for missing keys
require('dotenv').config({ path: path.join(__dirname, '../../.env') });

const express = require('express');
const mongoose = require('mongoose');
const cookieParser = require('cookie-parser');
const cors = require('cors');

const authRoutes = require('./routes/auth');

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware Setup
app.use(cors({
  origin: process.env.CLIENT_URL || 'http://localhost:3000',
  credentials: true
}));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());

// REST Route Registration
app.use('/api/auth', authRoutes);

// Custom route: /api/send-otp
const { sendEmail } = require('./services/otpService');
const bcrypt = require('bcryptjs');
const Otp = require('./models/Otp');

const generateNumericOtp = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

app.post('/api/send-otp', async (req, res) => {
  try {
    const { email } = req.body;
    if (!email || !email.trim()) {
      return res.status(400).json({ error: 'Email is required' });
    }
    const sanitizedEmail = email.trim().toLowerCase();
    const rawCode = generateNumericOtp();
    const hashedCode = await bcrypt.hash(rawCode, 10);
    const now = new Date();
    const expiresAt = new Date(now.getTime() + 5 * 60 * 1000); // 5 mins expiry

    // Save to DB
    await Otp.findOneAndUpdate(
      { phoneOrEmail: sanitizedEmail },
      {
        otp: hashedCode,
        resendCount: 1,
        lastRequestedAt: now,
        expiresAt
      },
      { upsert: true, new: true }
    );

    // Send real email
    await sendEmail(sanitizedEmail, rawCode);
    console.log(`[REAL OTP EMAIL SENT] Code for ${sanitizedEmail} is: ${rawCode}`);

    res.status(200).json({ success: true, message: 'OTP sent successfully to email' });
  } catch (err) {
    console.error('API Send OTP Error:', err);
    res.status(500).json({ error: 'Failed to send OTP. ' + err.message });
  }
});

// Root Ping Endpoint
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'OK', message: 'QuickBite Enterprise Authentication service is healthy.' });
});

// Database Connection & Server Listening
const mongoUri = process.env.MONGO_URI || 'mongodb://localhost:27017/quickbite';

mongoose.connect(mongoUri)
  .then(() => {
    console.log('Successfully connected to MongoDB.');
    app.listen(PORT, () => {
      console.log(`Server is running in ${process.env.NODE_ENV || 'development'} mode on port ${PORT}`);
    });
  })
  .catch((err) => {
    console.error('Database connection failed:', err.message);
    process.exit(1);
  });
