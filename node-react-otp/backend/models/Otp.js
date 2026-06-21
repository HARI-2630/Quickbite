const mongoose = require('mongoose');

const otpSchema = new mongoose.Schema({
  phoneOrEmail: {
    type: String,
    required: true,
    index: true
  },
  otp: {
    type: String,
    required: true
  },
  resendCount: {
    type: Number,
    default: 1
  },
  lastRequestedAt: {
    type: Date,
    default: Date.now
  },
  expiresAt: {
    type: Date,
    required: true
  }
}, { timestamps: true });

// TTL Index: Auto-delete the document when the expiresAt date is reached (5 minutes from generation)
otpSchema.index({ expiresAt: 1 }, { expireAfterSeconds: 0 });

module.exports = mongoose.model('Otp', otpSchema);
