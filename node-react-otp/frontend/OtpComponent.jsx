import React, { useState, useEffect } from 'react';

// API Base URL config
const API_BASE = 'http://localhost:5000/api/auth';

export default function OtpComponent() {
  const [view, setView] = useState('login'); // login, phone, register, phone-otp, google-otp, forgot, reset
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('CUSTOMER');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);

  // Cooldown Countdown Timer State
  const [cooldown, setCooldown] = useState(0);

  useEffect(() => {
    if (cooldown > 0) {
      const timer = setTimeout(() => setCooldown(cooldown - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [cooldown]);

  // Toast Notification auto-dismiss helper
  const triggerToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  // 1. Send SMS OTP
  const handleSendPhoneOtp = async (e) => {
    if (e) e.preventDefault();
    if (!phone) return triggerToast('Phone number is required.', 'error');

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/otp/send-phone`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone })
      });
      const data = await res.json();
      setLoading(false);

      if (res.ok) {
        triggerToast('SMS OTP verification code sent.', 'success');
        setView('phone-otp');
        setCooldown(60); // 60 seconds cooldown limit
      } else {
        triggerToast(data.error || 'Failed to send SMS OTP.', 'error');
      }
    } catch (err) {
      setLoading(false);
      triggerToast('Network connection error.', 'error');
    }
  };

  // 2. Verify Phone OTP
  const handleVerifyPhoneOtp = async (e) => {
    e.preventDefault();
    if (otp.length !== 6) return triggerToast('Please enter a valid 6-digit code.', 'error');

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/otp/verify-phone`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ phone, otp })
      });
      const data = await res.json();
      setLoading(false);

      if (res.ok) {
        triggerToast('OTP verified successfully!', 'success');
        setOtp('');
        // Handle successful session setup (e.g. redirect to customer dashboard)
        console.log('User signed in:', data.user);
      } else {
        triggerToast(data.error || 'Incorrect code entered.', 'error');
      }
    } catch (err) {
      setLoading(false);
      triggerToast('Verification network error.', 'error');
    }
  };

  // 3. Send Email OTP (Optional manual trigger or fallback)
  const handleSendEmailOtp = async (e) => {
    e.preventDefault();
    if (!email) return triggerToast('Email address is required.', 'error');

    setLoading(true);
    try {
      const res = await fetch(`http://localhost:5000/api/send-otp`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });
      const data = await res.json();
      setLoading(false);

      if (res.ok) {
        triggerToast('Verification code sent to Gmail.', 'success');
        setView('google-otp'); // share same view format for Gmail
        setCooldown(60);
      } else {
        triggerToast(data.error || 'Failed to send Email OTP.', 'error');
      }
    } catch (err) {
      setLoading(false);
      triggerToast('Network connection error.', 'error');
    }
  };

  // 4. Verify Google Email OTP
  const handleVerifyGoogleOtp = async (e) => {
    e.preventDefault();
    if (otp.length !== 6) return triggerToast('Please enter a valid 6-digit code.', 'error');

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/otp/verify-email`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp })
      });
      const data = await res.json();
      setLoading(false);

      if (res.ok) {
        triggerToast('Session verified. Redirecting...', 'success');
        setOtp('');
        console.log('Authenticated User:', data.user);
      } else {
        triggerToast(data.error || 'Incorrect code entered.', 'error');
      }
    } catch (err) {
      setLoading(false);
      triggerToast('Verification network error.', 'error');
    }
  };

  // 5. Google Sign-In Simulation
  const handleGoogleSignIn = async () => {
    let targetEmail = email;
    if (!targetEmail || !targetEmail.trim()) {
      targetEmail = window.prompt("Enter your Google Account email for simulation:", "recruiter@gmail.com");
      if (!targetEmail) {
        return; // User cancelled
      }
      targetEmail = targetEmail.trim();
      setEmail(targetEmail);
    }
    setLoading(true);
    try {
      // Simulate Google Sign-In exchanging Token
      const mockToken = `simulated_token_${targetEmail}`;
      const res = await fetch(`${API_BASE}/google`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idToken: mockToken })
      });
      const data = await res.json();
      setLoading(false);

      if (res.ok && data.requiresMFA) {
        triggerToast('Google account linked. Verification code sent to your Gmail inbox.', 'info');
        setEmail(data.email);
        setView('google-otp');
        setCooldown(60);
      } else {
        triggerToast(data.error || 'Google Sign-in failed.', 'error');
      }
    } catch (err) {
      setLoading(false);
      triggerToast('Google authentication exchange error.', 'error');
    }
  };

  // Resend Trigger Handler
  const handleResendOtp = () => {
    if (cooldown > 0) return;
    if (view === 'phone-otp') {
      handleSendPhoneOtp();
    } else {
      triggerToast('Dispaching code...');
      // Re-trigger email/Google codes send
      handleGoogleSignIn();
    }
  };

  return (
    <div style={styles.bodyWrapper}>
      {/* Toast Notice */}
      {toast && (
        <div style={{ ...styles.toast, ...(toast.type === 'error' ? styles.toastError : styles.toastSuccess) }}>
          {toast.message}
        </div>
      )}

      {/* Loading Spinner */}
      {loading && (
        <div style={styles.spinnerOverlay}>
          <div style={styles.spinner}></div>
        </div>
      )}

      <div style={styles.card}>
        <div style={styles.header}>
          <h3 style={styles.title}>🛡️ Two-Factor OTP</h3>
        </div>

        <div style={styles.body}>
          {/* Google Sign-in button */}
          {['login', 'phone', 'register'].includes(view) && (
            <>
              <button style={styles.googleBtn} onClick={handleGoogleSignIn}>
                <svg viewBox="0 0 24 24" width="18" height="18" style={{ marginRight: 10 }}>
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
                </svg>
                <span>Sign in with Google</span>
              </button>
              <div style={styles.divider}>OR CONTINUE WITH</div>
            </>
          )}

          {/* Authentication tabs */}
          {['login', 'phone', 'register'].includes(view) && (
            <div style={styles.tabsContainer}>
              <button style={{ ...styles.tab, ...(view === 'login' && styles.activeTab) }} onClick={() => setView('login')}>Email</button>
              <button style={{ ...styles.tab, ...(view === 'phone' && styles.activeTab) }} onClick={() => setView('phone')}>Phone</button>
              <button style={{ ...styles.tab, ...(view === 'register' && styles.activeTab) }} onClick={() => setView('register')}>Register</button>
            </div>
          )}

          {/* 1. Traditional Email Password Login */}
          {view === 'login' && (
            <form onSubmit={handleSendEmailOtp}>
              <div style={styles.formGroup}>
                <label style={styles.label}>Email Address</label>
                <input type="email" style={styles.input} placeholder="recruiter@quickbite.com" value={email} onChange={e => setEmail(e.target.value)} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Password</label>
                <input type="password" style={styles.input} placeholder="••••••••" value={password} onChange={e => setPassword(e.target.value)} required />
              </div>
              <button type="submit" style={styles.submitBtn}>Sign In & Request OTP ⚡</button>
            </form>
          )}

          {/* 2. Phone Number Entry Form */}
          {view === 'phone' && (
            <form onSubmit={handleSendPhoneOtp}>
              <div style={styles.formGroup}>
                <label style={styles.label}>Phone Number (E.164)</label>
                <input type="tel" style={styles.input} placeholder="+919876543210" value={phone} onChange={e => setPhone(e.target.value)} required />
              </div>
              <button type="submit" style={styles.submitBtn}>Send SMS OTP 📲</button>
            </form>
          )}

          {/* 3. Phone OTP Verify Form */}
          {view === 'phone-otp' && (
            <form onSubmit={handleVerifyPhoneOtp}>
              <div style={{ textAlign: 'center', marginBottom: 20 }}>
                <p style={{ color: '#ced6e0', fontSize: '0.88rem' }}>
                  We sent a 6-digit OTP code to <strong style={{ color: '#fff' }}>{phone}</strong>
                </p>
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Verification Code</label>
                <input type="text" style={styles.otpInput} placeholder="000000" maxLength="6" value={otp} onChange={e => setOtp(e.target.value.replace(/\D/g,''))} required />
              </div>
              <button type="submit" style={styles.submitBtn}>Verify Code 🔓</button>
              <div style={{ textAlign: 'center', marginTop: 18 }}>
                <button type="button" style={{ ...styles.resendBtn, ...(cooldown > 0 && { pointerEvents: 'none', opacity: 0.5 }) }} onClick={handleResendOtp}>
                  {cooldown > 0 ? `Resend Code in ${cooldown}s` : 'Resend Code'}
                </button>
              </div>
            </form>
          )}

          {/* 4. Google OTP Verify Form */}
          {view === 'google-otp' && (
            <form onSubmit={handleVerifyGoogleOtp}>
              <div style={{ textAlign: 'center', marginBottom: 20 }}>
                <p style={{ color: '#ced6e0', fontSize: '0.88rem' }}>
                  A 6-digit code was sent to <strong style={{ color: '#fff' }}>{email}</strong>
                </p>
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Enter Verification Code</label>
                <input type="text" style={styles.otpInput} placeholder="000000" maxLength="6" value={otp} onChange={e => setOtp(e.target.value.replace(/\D/g,''))} required />
              </div>
              <button type="submit" style={styles.submitBtn}>Verify & Continue 🛡️</button>
              <div style={{ textAlign: 'center', marginTop: 18 }}>
                <button type="button" style={{ ...styles.resendBtn, ...(cooldown > 0 && { pointerEvents: 'none', opacity: 0.5 }) }} onClick={handleResendOtp}>
                  {cooldown > 0 ? `Resend Code in ${cooldown}s` : 'Resend Code'}
                </button>
              </div>
            </form>
          )}

          {/* 5. User Registration Form */}
          {view === 'register' && (
            <form onSubmit={(e) => { e.preventDefault(); triggerToast('Simulated signup completed. Access authorized.', 'success'); }}>
              <div style={styles.formGroup}>
                <label style={styles.label}>Full Name</label>
                <input type="text" style={styles.input} placeholder="Alice Smith" value={name} onChange={e => setName(e.target.value)} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Email Address</label>
                <input type="email" style={styles.input} placeholder="alice@example.com" value={email} onChange={e => setEmail(e.target.value)} required />
              </div>
              <div style={styles.formGroup}>
                <label style={styles.label}>Select Role</label>
                <select style={styles.select} value={role} onChange={e => setRole(e.target.value)}>
                  <option value="CUSTOMER">🍽️ Order Delicious Foods</option>
                  <option value="RESTAURANT_ADMIN">👨‍🍳 Manage my Restaurant</option>
                </select>
              </div>
              <button type="submit" style={styles.submitBtn}>Create Account 🎉</button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

// Inline luxury dark CSS rules to match QuickBite obsidian UI style
const styles = {
  bodyWrapper: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%',
    fontFamily: "'Segoe UI', system-ui, sans-serif",
    color: '#f3f4f6'
  },
  card: {
    width: '420px',
    background: '#151821',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: '16px',
    boxShadow: '0 10px 40px rgba(0,0,0,0.45)',
    overflow: 'hidden'
  },
  header: {
    padding: '20px 24px',
    borderBottom: '1px solid rgba(255,255,255,0.06)'
  },
  title: {
    fontSize: '1.25rem',
    fontWeight: '800',
    color: '#fff',
    margin: 0
  },
  body: {
    padding: '24px'
  },
  googleBtn: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    padding: '12px',
    borderRadius: '8px',
    border: '1px solid rgba(255,255,255,0.06)',
    background: '#0e1017',
    color: '#fff',
    fontWeight: '700',
    fontSize: '0.9rem',
    cursor: 'pointer',
    transition: 'all 0.2s',
    marginBottom: '16px'
  },
  divider: {
    textAlign: 'center',
    fontSize: '0.75rem',
    color: '#747d8c',
    fontWeight: '700',
    margin: '16px 0',
    letterSpacing: '0.5px'
  },
  tabsContainer: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr 1fr',
    gap: '8px',
    background: 'rgba(255,255,255,0.02)',
    padding: '4px',
    borderRadius: '8px',
    border: '1px solid rgba(255,255,255,0.04)',
    marginBottom: '24px'
  },
  tab: {
    padding: '8px',
    background: 'transparent',
    color: '#747d8c',
    border: 'none',
    fontSize: '0.85rem',
    fontWeight: '600',
    borderRadius: '6px',
    cursor: 'pointer',
    transition: 'all 0.2s',
    textAlign: 'center'
  },
  activeTab: {
    background: 'linear-gradient(135deg, #ff4757, #ffa502)',
    color: '#fff',
    boxShadow: '0 4px 12px rgba(255, 71, 87, 0.2)'
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
    marginBottom: '16px',
    textAlign: 'left'
  },
  label: {
    fontSize: '0.82rem',
    fontWeight: '600',
    color: '#ced6e0'
  },
  input: {
    padding: '12px',
    borderRadius: '8px',
    border: '1px solid rgba(255,255,255,0.08)',
    background: 'rgba(0,0,0,0.25)',
    color: '#fff',
    fontSize: '0.9rem',
    outline: 'none'
  },
  select: {
    padding: '12px',
    borderRadius: '8px',
    border: '1px solid rgba(255,255,255,0.08)',
    background: '#1b1e2a',
    color: '#fff',
    fontSize: '0.9rem',
    outline: 'none',
    cursor: 'pointer'
  },
  otpInput: {
    padding: '14px',
    borderRadius: '8px',
    border: '1px solid rgba(255,255,255,0.08)',
    background: 'rgba(0,0,0,0.25)',
    color: '#fff',
    fontSize: '1.4rem',
    fontWeight: '800',
    letterSpacing: '8px',
    textAlign: 'center',
    outline: 'none'
  },
  submitBtn: {
    width: '100%',
    padding: '14px',
    borderRadius: '8px',
    background: 'linear-gradient(135deg, #ff4757, #ffa502)',
    color: '#fff',
    fontWeight: '700',
    fontSize: '0.95rem',
    border: 'none',
    cursor: 'pointer',
    transition: 'all 0.2s',
    marginTop: '10px'
  },
  resendBtn: {
    background: 'none',
    border: 'none',
    color: '#ff4757',
    fontSize: '0.85rem',
    fontWeight: '700',
    cursor: 'pointer'
  },
  toast: {
    position: 'fixed',
    bottom: '24px',
    right: '24px',
    padding: '14px 20px',
    borderRadius: '8px',
    fontSize: '0.88rem',
    fontWeight: '600',
    color: '#fff',
    minWidth: '260px',
    boxShadow: '0 8px 30px rgba(0,0,0,0.35)',
    zIndex: 99999,
    animation: 'slideIn 0.35s ease-out'
  },
  toastSuccess: {
    background: 'linear-gradient(135deg, #2ed573, #1e90ff)',
    borderLeft: '4px solid #2ed573'
  },
  toastError: {
    background: 'linear-gradient(135deg, #ff4757, #ff6b81)',
    borderLeft: '4px solid #ff4757'
  },
  spinnerOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    width: '100vw',
    height: '100vh',
    background: 'rgba(15,15,19,0.7)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 999999
  },
  spinner: {
    width: '45px',
    height: '45px',
    border: '4px solid rgba(255,255,255,0.1)',
    borderTopColor: '#ff4757',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite'
  }
};
