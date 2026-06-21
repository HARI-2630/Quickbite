const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true
  },
  email: {
    type: String,
    required: true,
    unique: true,
    trim: true,
    lowercase: true,
    index: true
  },
  password: {
    type: String
  },
  phone: {
    type: String,
    unique: true,
    sparse: true, // Allows multiple null values for users without phone numbers linked
    index: true
  },
  googleId: {
    type: String,
    unique: true,
    sparse: true,
    index: true
  },
  avatarUrl: {
    type: String
  },
  role: {
    type: String,
    enum: ['CUSTOMER', 'RESTAURANT_ADMIN', 'SUPER_ADMIN'],
    default: 'CUSTOMER'
  },
  status: {
    type: String,
    enum: ['ACTIVE', 'BLOCKED'],
    default: 'ACTIVE'
  }
}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);
