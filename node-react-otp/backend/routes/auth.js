const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { OAuth2Client } = require('google-auth-library');

const Otp = require('../models/Otp');
const User = require('../models/User');
const { sendSMS, sendEmail } = require('../services/otpService');

// Google OAuth 2.0 Client
const googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

/**
 * Generate Access and Refresh JWT Tokens
 */
const generateTokens = (user) => {
  const payload = { id: user._id, email: user.email, role: user.role };
  
  const accessToken = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '15m' });
  const refreshToken = jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '7d' });
  
  return { accessToken, refreshToken };
};

/**
 * Configure secure token cookies on response
 */
const setAuthCookies = (res, accessToken, refreshToken) => {
  res.cookie('jwt_access', accessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: 15 * 60 * 1000 // 15 mins
  });

  res.cookie('jwt_refresh', refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict',
    maxAge: 7 * 24 * 60 * 60 * 1000 // 7 days
  });
};

/**
 * Helper to generate 6-digit OTP code
 */
const generateNumericOtp = () => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

/**
 * 1. Send SMS OTP
 */
router.post('/otp/send-phone', async (req, res) => {
  try {
    const { phone } = req.body;
    if (!phone || !phone.trim()) {
      return res.status(400).json({ error: 'Phone number is required' });
    }
    const sanitizedPhone = phone.trim();

    // E.164 phone check
    if (!/^\+?[1-9]\d{1,14}$/.test(sanitizedPhone)) {
      return res.status(400).json({ error: 'Invalid phone format. Use E.164 (e.g. +919876543210)' });
    }

    let otpSession = await Otp.findOne({ phoneOrEmail: sanitizedPhone });
    const now = new Date();
    let resendCount = 1;

    if (otpSession) {
      // Check 60-second cooldown
      const elapsedMs = now.getTime() - otpSession.lastRequestedAt.getTime();
      if (elapsedMs < 60 * 1000) {
        return res.status(400).json({ error: `Please wait ${Math.ceil(60 - elapsedMs/1000)}s before requesting another OTP` });
      }

      // Check 3 attempts limit per 10 minutes
      const windowMs = now.getTime() - otpSession.createdAt.getTime();
      if (windowMs < 10 * 60 * 1000) {
        if (otpSession.resendCount >= 3) {
          return res.status(429).json({ error: 'Resend limit reached (max 3 per 10 mins). Try again later.' });
        }
        resendCount = otpSession.resendCount + 1;
      } else {
        // Reset rate limit window
        otpSession.createdAt = now;
        resendCount = 1;
      }
    }

    const rawCode = generateNumericOtp();
    const hashedCode = await bcrypt.hash(rawCode, 10);
    const expiresAt = new Date(now.getTime() + 5 * 60 * 1000); // 5 mins expiry

    if (otpSession) {
      otpSession.otp = hashedCode;
      otpSession.resendCount = resendCount;
      otpSession.lastRequestedAt = now;
      otpSession.expiresAt = expiresAt;
      await otpSession.save();
    } else {
      await Otp.create({
        phoneOrEmail: sanitizedPhone,
        otp: hashedCode,
        resendCount,
        lastRequestedAt: now,
        expiresAt
      });
    }

    // Call Twilio REST integration
    await sendSMS(sanitizedPhone, rawCode);
    
    // For local evaluation logs printout
    console.log(`[REAL OTP SMS DELIVERED] Code for ${sanitizedPhone} is: ${rawCode}`);

    res.status(200).json({ success: true, message: 'OTP sent successfully via SMS' });
  } catch (err) {
    console.error('Phone OTP Error:', err);
    res.status(500).json({ error: 'Failed to send OTP via SMS. ' + err.message });
  }
});

/**
 * 2. Send Email OTP
 */
router.post('/otp/send-email', async (req, res) => {
  try {
    const { email } = req.body;
    if (!email || !email.trim()) {
      return res.status(400).json({ error: 'Email is required' });
    }
    const sanitizedEmail = email.trim().toLowerCase();

    let otpSession = await Otp.findOne({ phoneOrEmail: sanitizedEmail });
    const now = new Date();
    let resendCount = 1;

    if (otpSession) {
      const elapsedMs = now.getTime() - otpSession.lastRequestedAt.getTime();
      if (elapsedMs < 60 * 1000) {
        return res.status(400).json({ error: `Please wait ${Math.ceil(60 - elapsedMs/1000)}s before requesting another OTP` });
      }

      const windowMs = now.getTime() - otpSession.createdAt.getTime();
      if (windowMs < 10 * 60 * 1000) {
        if (otpSession.resendCount >= 3) {
          return res.status(429).json({ error: 'Resend limit reached (max 3 per 10 mins). Try again later.' });
        }
        resendCount = otpSession.resendCount + 1;
      } else {
        otpSession.createdAt = now;
        resendCount = 1;
      }
    }

    const rawCode = generateNumericOtp();
    const hashedCode = await bcrypt.hash(rawCode, 10);
    const expiresAt = new Date(now.getTime() + 5 * 60 * 1000); 

    if (otpSession) {
      otpSession.otp = hashedCode;
      otpSession.resendCount = resendCount;
      otpSession.lastRequestedAt = now;
      otpSession.expiresAt = expiresAt;
      await otpSession.save();
    } else {
      await Otp.create({
        phoneOrEmail: sanitizedEmail,
        otp: hashedCode,
        resendCount,
        lastRequestedAt: now,
        expiresAt
      });
    }

    // Call Nodemailer SMTP transporter integration
    await sendEmail(sanitizedEmail, rawCode);
    console.log(`[REAL OTP EMAIL DELIVERED] Code for ${sanitizedEmail} is: ${rawCode}`);

    res.status(200).json({ success: true, message: 'OTP sent successfully to email' });
  } catch (err) {
    console.error('Email OTP Error:', err);
    res.status(500).json({ error: 'Failed to send OTP via Email. ' + err.message });
  }
});

/**
 * 3. Verify Phone OTP
 */
router.post('/otp/verify-phone', async (req, res) => {
  try {
    const { phone, otp } = req.body;
    if (!phone || !otp) {
      return res.status(400).json({ error: 'Phone and OTP code are required' });
    }
    const sanitizedPhone = phone.trim();

    const otpSession = await Otp.findOne({ phoneOrEmail: sanitizedPhone });
    if (!otpSession) {
      return res.status(400).json({ error: 'Invalid or expired OTP session' });
    }

    // Check expiry
    if (new Date() > otpSession.expiresAt) {
      await Otp.deleteOne({ _id: otpSession._id });
      return res.status(400).json({ error: 'OTP code has expired' });
    }

    // Compare bcrypt hash
    const isMatch = await bcrypt.compare(otp, otpSession.otp);
    if (!isMatch) {
      return res.status(400).json({ error: 'Invalid OTP code entered' });
    }

    // OTP verified successfully - clear session
    await Otp.deleteOne({ _id: otpSession._id });

    // Login or auto-register user
    let user = await User.findOne({ phone: sanitizedPhone });
    if (!user) {
      // Register new profile
      user = await User.create({
        name: `User-${sanitizedPhone.slice(-4)}`,
        email: `phone_${Date.now()}@quickbite.com`, // placeholder email
        phone: sanitizedPhone,
        status: 'ACTIVE'
      });
    }

    if (user.status === 'BLOCKED') {
      return res.status(403).json({ error: 'This user account is blocked by administrative rules' });
    }

    const { accessToken, refreshToken } = generateTokens(user);
    setAuthCookies(res, accessToken, refreshToken);

    res.status(200).json({
      success: true,
      message: 'OTP verification successful',
      user: { id: user._id, name: user.name, email: user.email, role: user.role }
    });
  } catch (err) {
    console.error('Phone Verify Error:', err);
    res.status(500).json({ error: 'Internal verification failure' });
  }
});

/**
 * 4. Verify Email OTP
 */
router.post('/otp/verify-email', async (req, res) => {
  try {
    const { email, otp } = req.body;
    if (!email || !otp) {
      return res.status(400).json({ error: 'Email and OTP code are required' });
    }
    const sanitizedEmail = email.trim().toLowerCase();

    const otpSession = await Otp.findOne({ phoneOrEmail: sanitizedEmail });
    if (!otpSession) {
      return res.status(400).json({ error: 'Invalid or expired OTP session' });
    }

    if (new Date() > otpSession.expiresAt) {
      await Otp.deleteOne({ _id: otpSession._id });
      return res.status(400).json({ error: 'OTP code has expired' });
    }

    const isMatch = await bcrypt.compare(otp, otpSession.otp);
    if (!isMatch) {
      return res.status(400).json({ error: 'Invalid OTP code entered' });
    }

    await Otp.deleteOne({ _id: otpSession._id });

    let user = await User.findOne({ email: sanitizedEmail });
    if (!user) {
      // Auto-register
      user = await User.create({
        name: sanitizedEmail.split('@')[0],
        email: sanitizedEmail,
        status: 'ACTIVE'
      });
    }

    if (user.status === 'BLOCKED') {
      return res.status(403).json({ error: 'This user account is blocked' });
    }

    const { accessToken, refreshToken } = generateTokens(user);
    setAuthCookies(res, accessToken, refreshToken);

    res.status(200).json({
      success: true,
      message: 'OTP verification successful',
      user: { id: user._id, name: user.name, email: user.email, role: user.role }
    });
  } catch (err) {
    console.error('Email Verify Error:', err);
    res.status(500).json({ error: 'Internal verification failure' });
  }
});

/**
 * 5. Google Sign In & Secondary 2FA dispatch
 */
router.post('/google', async (req, res) => {
  try {
    const { idToken } = req.body;
    if (!idToken) {
      return res.status(400).json({ error: 'Google ID Token is required' });
    }

    let email, name, picture, googleId;

    // Simulation check for testing
    if (idToken.startsWith('simulated_token_')) {
      email = idToken.replace('simulated_token_', '');
      name = 'Google Recruiter';
      picture = 'https://api.dicebear.com/7.x/bottts/svg?seed=google';
      googleId = 'google_simulated_' + email.hashCode;
    } else {
      // Real Google Verification
      const ticket = await googleClient.verifyIdToken({
        idToken,
        audience: process.env.GOOGLE_CLIENT_ID
      });
      const payload = ticket.getPayload();
      email = payload.email;
      name = payload.name;
      picture = payload.picture;
      googleId = payload.sub;
    }

    let user = await User.findOne({ googleId });
    if (!user) {
      // Try by email linking
      user = await User.findOne({ email });
      if (user) {
        user.googleId = googleId;
        if (!user.avatarUrl) user.avatarUrl = picture;
        await user.save();
      } else {
        // Create new
        user = await User.create({
          name,
          email,
          googleId,
          avatarUrl: picture,
          status: 'ACTIVE'
        });
      }
    }

    if (user.status === 'BLOCKED') {
      return res.status(403).json({ error: 'This user account is blocked' });
    }

    // Google Sign-in successful -> Dispatch secondary 2FA Gmail verification OTP code
    const rawCode = generateNumericOtp();
    const hashedCode = await bcrypt.hash(rawCode, 10);
    const now = new Date();
    const expiresAt = new Date(now.getTime() + 5 * 60 * 1000);

    // Save Gmail verification code
    await Otp.findOneAndUpdate(
      { phoneOrEmail: email },
      {
        otp: hashedCode,
        resendCount: 1,
        lastRequestedAt: now,
        expiresAt
      },
      { upsert: true, new: true }
    );

    // Dispatch real email via SMTP
    await sendEmail(email, rawCode);
    console.log(`[REAL GOOGLE 2FA OTP DISPATCHED] Code for ${email} is: ${rawCode}`);

    res.status(200).json({
      success: true,
      message: 'Google Sign-in complete. Verification code sent to your Gmail account.',
      email,
      requiresMFA: true
    });
  } catch (err) {
    console.error('Google Sign-in Error:', err);
    res.status(500).json({ error: 'Google authentication exchange failed: ' + err.message });
  }
});

/**
 * 6. Logout Endpoint
 */
router.post('/logout', (req, res) => {
  res.clearCookie('jwt_access');
  res.clearCookie('jwt_refresh');
  res.status(200).json({ success: true, message: 'Session closed successfully' });
});

// String hashCode helper for simulated Google ID hashing
String.prototype.hashCode = function() {
  var hash = 0, i, chr;
  if (this.length === 0) return hash;
  for (i = 0; i < this.length; i++) {
    chr   = this.charCodeAt(i);
    hash  = ((hash << 5) - hash) + chr;
    hash |= 0;
  }
  return hash;
};

module.exports = router;
