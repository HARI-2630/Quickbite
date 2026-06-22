/**
 * QuickBite Enterprise Authentication Controller
 * Handles Google OAuth OTP verification, SMS OTP flows, Resend Timers, and Toast Notifications.
 */

document.addEventListener("DOMContentLoaded", function() {
    // Check url params for triggering modal windows and forms
    var urlParams = new URLSearchParams(window.location.search);
    var action = urlParams.get("action");
    var email = urlParams.get("email");
    var error = urlParams.get("error");
    var msg = urlParams.get("msg");
    var demoOtp = urlParams.get("demoOtp");

    if (action === "verifyGoogleOtp" && email) {
        window.openAuthModal();
        window.switchAuthTab("google-otp");
        document.getElementById("google-otp-email").value = email;
        document.getElementById("google-otp-display-email").textContent = email;
        window.showToast("🔒 Secondary Verification Required. Please check your Gmail.", "info");
        if (demoOtp) {
            setTimeout(function() {
                window.showToast("🔑 Demo Mode OTP: " + demoOtp, "info");
            }, 1000);
        }
    } else if (action === "resetPasswordForm") {
        window.openAuthModal();
        window.switchAuthTab("reset-password");
        window.showToast("🔑 Reset your password below.", "info");
    } else if (error) {
        window.openAuthModal();
        handleAuthErrors(error);
    } else if (msg) {
        window.openAuthModal();
        handleAuthMessages(msg);
    }

    // Set up email login form submit - handled natively via POST to /auth


    // Set up phone form submit
    var phoneForm = document.getElementById("phone-form");
    if (phoneForm) {
        phoneForm.addEventListener("submit", function(e) {
            e.preventDefault();
            sendPhoneOtp();
        });
    }

    // Set up phone OTP verification form submit
    var phoneOtpForm = document.getElementById("phone-otp-form");
    if (phoneOtpForm) {
        phoneOtpForm.addEventListener("submit", function(e) {
            e.preventDefault();
            verifyPhoneOtp();
        });
    }

    // Set up Google OTP verification form submit
    var googleOtpForm = document.getElementById("google-otp-form");
    if (googleOtpForm) {
        googleOtpForm.addEventListener("submit", function(e) {
            e.preventDefault();
            verifyGoogleOtp();
        });
    }
});

var otpCooldownTimer = null;
var cooldownSeconds = 0;

// Switch modal authentication views
window.switchAuthTab = function(tab) {
    var tabs = ["login", "phone", "register", "forgot", "phone-otp", "google-otp", "reset-password"];
    
    // Hide all forms
    tabs.forEach(function(t) {
        var form = document.getElementById(t + "-form");
        if (form) form.style.display = "none";
        
        var tabBtn = document.getElementById("tab-" + t);
        if (tabBtn) tabBtn.classList.remove("active");
    });

    // Show selected form
    var activeForm = document.getElementById(tab + "-form");
    if (activeForm) activeForm.style.display = "block";

    // Set tab button active (for login, phone, register)
    var activeTabBtn = document.getElementById("tab-" + tab);
    if (activeTabBtn) activeTabBtn.classList.add("active");

    // Dynamic Title Updates
    var title = document.getElementById("auth-modal-title");
    if (title) {
        if (tab === "login") title.textContent = "🔑 Sign In";
        else if (tab === "phone") title.textContent = "📱 Phone Login";
        else if (tab === "register") title.textContent = "📝 Create Account";
        else if (tab === "forgot") title.textContent = "🤔 Forgot Password";
        else if (tab === "phone-otp" || tab === "google-otp") title.textContent = "🛡️ Two-Factor OTP";
        else if (tab === "reset-password") title.textContent = "🔒 Reset Password";
    }
};

// Error mapper
function handleAuthErrors(errCode) {
    var errorMap = {
        "invalid_credentials": "❌ Incorrect password or email address.",
        "email_exists": "❌ An account with this email already exists.",
        "phone_exists": "❌ This phone number is already registered.",
        "phone_unverified": "❌ Phone verification required before sign up.",
        "registration_failed": "❌ Registration failed. Try again later.",
        "blocked": "🚫 This account has been blocked by an administrator.",
        "unauthorized": "⚠️ Unauthorized access. Administrative rights required.",
        "login_required": "🔑 Please login to access this area.",
        "reset_invalid_token": "❌ Invalid or expired password reset link.",
        "reset_token_expired": "❌ This password reset link has expired.",
        "reset_failed": "❌ Password reset failed.",
        "google_auth_failed": "❌ Google authentication failed.",
        "google_profile_failed": "❌ Failed to retrieve Google profile info."
    };
    var message = errorMap[errCode] || "❌ An authentication error occurred.";
    window.showToast(message, "error");
}

// Success message mapper
function handleAuthMessages(msgCode) {
    var msgMap = {
        "registration_success": "🎉 Registration successful! You can now log in.",
        "logged_out": "👋 You have successfully logged out.",
        "reset_link_sent": "✉️ Password reset link sent to your email. Check inbox/spam.",
        "password_reset_success": "🔑 Password updated successfully! Please login."
    };
    var message = msgMap[msgCode] || "✔️ Operation completed.";
    window.showToast(message, "success");
}

// Phone Login - Send OTP Call
function sendPhoneOtp() {
    var phoneInput = document.getElementById("phone-number-input");
    var phone = phoneInput.value.trim();

    if (!phone) {
        window.showToast("❌ Phone number is required.", "error");
        return;
    }

    showLoading(true);
    fetch("auth/otp?action=sendPhoneOtp&phone=" + encodeURIComponent(phone), {
        method: "POST"
    })
    .then(function(res) {
        showLoading(false);
        return res.json().then(function(data) {
            if (res.ok) {
                window.showToast("📲 Verification code sent to " + phone, "success");
                if (data.demoOtp) {
                    setTimeout(function() {
                        window.showToast("🔑 Demo Mode OTP: " + data.demoOtp, "info");
                    }, 1000);
                }
                
                // Move to verification form view
                window.switchAuthTab("phone-otp");
                document.getElementById("verify-otp-phone").value = phone;
                document.getElementById("otp-display-phone").textContent = phone;
                
                startOtpCooldown();
            } else {
                window.showToast(data.error || "❌ Failed to send OTP.", "error");
            }
        });
    })
    .catch(function(err) {
        showLoading(false);
        window.showToast("❌ Network error. Please try again.", "error");
    });
}

// Phone Login - Verify OTP Call
function verifyPhoneOtp() {
    var phone = document.getElementById("verify-otp-phone").value;
    var otp = document.getElementById("phone-otp-input").value.trim();

    if (!otp || otp.length !== 6) {
        window.showToast("❌ Enter a valid 6-digit OTP code.", "error");
        return;
    }

    showLoading(true);
    fetch("auth/otp?action=verifyPhoneOtp&phone=" + encodeURIComponent(phone) + "&otp=" + encodeURIComponent(otp), {
        method: "POST"
    })
    .then(function(res) {
        showLoading(false);
        return res.json().then(function(data) {
            if (res.ok) {
                if (data.registered) {
                    window.showToast("🔓 Access Granted. Redirecting...", "success");
                    // Redirect user based on role
                    setTimeout(function() {
                        redirectUser(data.role);
                    }, 1000);
                } else {
                    window.showToast("✅ Phone verified! Complete signup form.", "success");
                    window.switchAuthTab("register");
                    
                    // Prefill phone and lock it
                    var regPhone = document.getElementById("reg-phone");
                    if (regPhone) {
                        regPhone.value = phone;
                        regPhone.readOnly = true;
                        regPhone.style.opacity = "0.7";
                    }
                }
            } else {
                window.showToast(data.error || "❌ Invalid OTP verification code.", "error");
            }
        });
    })
    .catch(function(err) {
        showLoading(false);
        window.showToast("❌ Network error during OTP validation.", "error");
    });
}

// Traditional Email OTP Login - Request OTP
function sendEmailOtp() {
    var emailInput = document.getElementById("login-email");
    var email = emailInput.value.trim();

    if (!email) {
        window.showToast("❌ Email is required.", "error");
        return;
    }

    showLoading(true);
    fetch("auth/send-otp?email=" + encodeURIComponent(email), {
        method: "POST"
    })
    .then(function(res) {
        showLoading(false);
        return res.json().then(function(data) {
            if (res.ok) {
                window.showToast("✉️ Verification code sent to " + email, "success");
                
                // Move to Google/Email OTP verification form view
                window.switchAuthTab("google-otp");
                document.getElementById("google-otp-email").value = email;
                document.getElementById("google-otp-display-email").textContent = email;
            } else {
                window.showToast(data.error || "❌ Failed to send OTP.", "error");
            }
        });
    })
    .catch(function(err) {
        showLoading(false);
        window.showToast("❌ Network error. Please try again.", "error");
    });
}

// Google Login - Verify Secondary Email OTP
function verifyGoogleOtp() {
    var email = document.getElementById("google-otp-email").value;
    var otp = document.getElementById("google-otp-input").value.trim();

    if (!otp || otp.length !== 6) {
        window.showToast("❌ Enter a valid 6-digit OTP code.", "error");
        return;
    }

    showLoading(true);
    fetch("auth/otp?action=verifyGoogleOtp&email=" + encodeURIComponent(email) + "&otp=" + encodeURIComponent(otp), {
        method: "POST"
    })
    .then(function(res) {
        showLoading(false);
        return res.json().then(function(data) {
            if (res.ok) {
                window.showToast("🔓 Access Granted. Redirecting...", "success");
                setTimeout(function() {
                    redirectUser(data.role);
                }, 1000);
            } else {
                window.showToast(data.error || "❌ Invalid verification code.", "error");
            }
        });
    })
    .catch(function(err) {
        showLoading(false);
        window.showToast("❌ Network error during OTP validation.", "error");
    });
}

// Resend OTP Cooldown Timer (30 seconds)
function startOtpCooldown() {
    var resendLink = document.getElementById("resend-otp-btn");
    if (!resendLink) return;
    
    cooldownSeconds = 30;
    resendLink.style.pointerEvents = "none";
    resendLink.style.opacity = "0.5";
    resendLink.textContent = "Resend Code in " + cooldownSeconds + "s";

    if (otpCooldownTimer) clearInterval(otpCooldownTimer);

    otpCooldownTimer = setInterval(function() {
        cooldownSeconds--;
        if (cooldownSeconds <= 0) {
            clearInterval(otpCooldownTimer);
            resendLink.style.pointerEvents = "auto";
            resendLink.style.opacity = "1";
            resendLink.textContent = "Resend Code";
        } else {
            resendLink.textContent = "Resend Code in " + cooldownSeconds + "s";
        }
    }, 1000);
}

// Trigger Phone OTP resend
window.resendPhoneOtpCode = function() {
    if (cooldownSeconds > 0) return;
    var phone = document.getElementById("verify-otp-phone").value;
    
    showLoading(true);
    fetch("auth/otp?action=sendPhoneOtp&phone=" + encodeURIComponent(phone), {
        method: "POST"
    })
    .then(function(res) {
        showLoading(false);
        return res.json().then(function(data) {
            if (res.ok) {
                window.showToast("📲 A new verification code has been dispatched.", "success");
                if (data.demoOtp) {
                    setTimeout(function() {
                        window.showToast("🔑 Demo Mode OTP: " + data.demoOtp, "info");
                    }, 1000);
                }
                startOtpCooldown();
            } else {
                window.showToast(data.error || "❌ Failed to resend code.", "error");
            }
        });
    })
    .catch(function(err) {
        showLoading(false);
        window.showToast("❌ Network error resending code.", "error");
    });
};

// Redirect helper
function redirectUser(role) {
    if (role === "SUPER_ADMIN") {
        window.location.href = "admin-dashboard.jsp";
    } else if (role === "RESTAURANT_ADMIN") {
        window.location.href = "restaurant-dashboard.jsp";
    } else {
        window.location.href = "customer-dashboard.jsp";
    }
}

// Display loading spinner overlay
function showLoading(show) {
    var loader = document.getElementById("global-spinner");
    if (!loader && show) {
        loader = document.createElement("div");
        loader.id = "global-spinner";
        loader.style.position = "fixed";
        loader.style.top = "0";
        loader.style.left = "0";
        loader.style.width = "100%";
        loader.style.height = "100%";
        loader.style.backgroundColor = "rgba(15,15,19,0.7)";
        loader.style.display = "flex";
        loader.style.alignItems = "center";
        loader.style.justifyContent = "center";
        loader.style.zIndex = "999999";
        loader.innerHTML = '<div class="loader-spinner"></div>';
        
        // Add styling if spinner is missing in CSS
        var style = document.createElement("style");
        style.textContent = "\n" +
            "            .loader-spinner {\n" +
            "                width: 50px;\n" +
            "                height: 50px;\n" +
            "                border: 4px solid rgba(255,255,255,0.1);\n" +
            "                border-top-color: #ff5e3a;\n" +
            "                border-radius: 50%;\n" +
            "                animation: spin 1s linear infinite;\n" +
            "            }\n" +
            "            @keyframes spin {\n" +
            "                to { transform: rotate(360deg); }\n" +
            "            }\n" +
            "        ";
        document.head.appendChild(style);
        document.body.appendChild(loader);
    }
    
    if (loader) {
        loader.style.display = show ? "flex" : "none";
    }
}

// Interactive custom toast message
window.showToast = function(message, type) {
    if (type === undefined) type = "success";
    var container = document.getElementById("toast-container");
    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        container.style.position = "fixed";
        container.style.bottom = "24px";
        container.style.right = "24px";
        container.style.display = "flex";
        container.style.flexDirection = "column";
        container.style.gap = "12px";
        container.style.zIndex = "999999";
        document.body.appendChild(container);
    }

    var toast = document.createElement("div");
    toast.className = "toast-notice toast-" + type;
    toast.style.padding = "14px 20px";
    toast.style.borderRadius = "8px";
    toast.style.fontSize = "0.9rem";
    toast.style.fontWeight = "600";
    toast.style.color = "#fff";
    toast.style.minWidth = "260px";
    toast.style.boxShadow = "0 8px 30px rgba(0,0,0,0.35)";
    toast.style.transform = "translateY(50px) scale(0.9)";
    toast.style.opacity = "0";
    toast.style.transition = "all 0.35s cubic-bezier(0.175, 0.885, 0.32, 1.275)";
    
    // Choose theme colors based on type
    if (type === "success") {
        toast.style.background = "linear-gradient(135deg, #2ed573, #1e90ff)";
        toast.style.borderLeft = "4px solid #2ed573";
    } else if (type === "error") {
        toast.style.background = "linear-gradient(135deg, #ff4757, #ff6b81)";
        toast.style.borderLeft = "4px solid #ff4757";
    } else { // info
        toast.style.background = "linear-gradient(135deg, #2f3542, #70a1ff)";
        toast.style.borderLeft = "4px solid #70a1ff";
    }

    toast.textContent = message;
    container.appendChild(toast);

    // Trigger animate-in
    setTimeout(function() {
        toast.style.transform = "translateY(0) scale(1)";
        toast.style.opacity = "1";
    }, 50);

    // Trigger animate-out after 3.5 seconds
    setTimeout(function() {
        toast.style.transform = "translateY(-20px) scale(0.9)";
        toast.style.opacity = "0";
        setTimeout(function() {
            toast.remove();
        }, 350);
    }, 3500);
};
