const twilio = require('twilio');
const nodemailer = require('nodemailer');

/**
 * Sends a real 6-digit OTP code to a mobile phone number using Twilio SMS.
 * @param {string} phone - Target phone number in E.164 format.
 * @param {string} code - The 6-digit OTP code.
 */
const sendSMS = async (phone, code) => {
  const accountSid = process.env.TWILIO_ACCOUNT_SID;
  const authToken = process.env.TWILIO_AUTH_TOKEN;
  const fromPhone = process.env.TWILIO_PHONE_NUMBER;

  if (!accountSid || !authToken || !fromPhone) {
    throw new Error('Twilio configuration variables are missing in .env');
  }

  const client = twilio(accountSid, authToken);
  
  await client.messages.create({
    body: `Your QuickBite verification code is: ${code}. It expires in 5 minutes.`,
    from: fromPhone,
    to: phone
  });
};

/**
 * Sends a real 6-digit OTP code to an email address using Nodemailer.
 * Uses secure SMTP settings with a Gmail App Password.
 * @param {string} email - Target email address.
 * @param {string} code - The 6-digit OTP code.
 */
const sendEmail = async (email, code) => {
  const user = process.env.EMAIL_USER;
  const pass = process.env.EMAIL_PASS;

  if (!user || !pass) {
    throw new Error('Nodemailer configuration variables (EMAIL_USER/EMAIL_PASS) are missing in .env');
  }

  const transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    secure: true, // Use SSL/TLS
    auth: {
      user: user,
      pass: pass
    }
  });

  const mailOptions = {
    from: `"QuickBite Security" <${user}>`,
    to: email,
    subject: 'QuickBite Security Verification Code',
    text: `Hello,\n\nYou requested a verification code to access your QuickBite account. Please enter the following 6-digit OTP code to complete your login:\n\n${code}\n\nNote: This verification code will expire in 5 minutes. If you did not make this request, please ignore this email.\n\n© 2026 QuickBite.`,
    html: `
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset="utf-8">
        <style>
          body { font-family: 'Segoe UI', Helvetica, Arial, sans-serif; background-color: #0f0f13; color: #f3f4f6; margin: 0; padding: 40px; }
          .card { background: #151821; border: 1px solid rgba(255,255,255,0.06); padding: 35px; border-radius: 12px; max-width: 440px; margin: 0 auto; box-shadow: 0 10px 40px rgba(0,0,0,0.55); text-align: left; }
          h2 { color: #ff5e3a; margin-top: 0; text-align: center; font-size: 1.5rem; font-weight: 800; border-bottom: 1px solid rgba(255,255,255,0.05); padding-bottom: 14px; }
          p { line-height: 1.6; font-size: 0.95rem; color: #ced6e0; margin: 16px 0; }
          .code { font-size: 2.4rem; font-weight: 900; text-align: center; letter-spacing: 8px; margin: 28px 0; color: #ffffff; background: rgba(255,94,58,0.08); padding: 14px; border-radius: 8px; border: 1px dashed #ff5e3a; text-shadow: 0 2px 10px rgba(255,94,58,0.2); }
          .footer { font-size: 0.8rem; text-align: center; color: #747d8c; margin-top: 32px; border-top: 1px solid rgba(255,255,255,0.06); padding-top: 20px; font-weight: 600; }
        </style>
      </head>
      <body>
        <div class="card">
          <h2>QuickBite Verification Code</h2>
          <p>Hello,</p>
          <p>You requested a verification code to access your QuickBite account. Please enter the following 6-digit OTP code to complete your login:</p>
          <div class="code">${code}</div>
          <p><strong>Note:</strong> This verification code will expire in 5 minutes. If you did not make this request, please ignore this email.</p>
          <div class="footer">© 2026 QuickBite. Secured for recruiter evaluation.</div>
        </div>
      </body>
      </html>
    `,
    headers: {
      'X-Priority': '1',
      'X-MSMail-Priority': 'High',
      'Importance': 'high'
    }
  };

  await transporter.sendMail(mailOptions);
};

module.exports = {
  sendSMS,
  sendEmail
};
