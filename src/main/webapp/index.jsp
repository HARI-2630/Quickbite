<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.models.User" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser != null) {
        if ("CUSTOMER".equals(currentUser.getRole())) {
            response.sendRedirect("customer-dashboard.jsp");
            return;
        } else if ("RESTAURANT_ADMIN".equals(currentUser.getRole())) {
            response.sendRedirect("restaurant-dashboard.jsp");
            return;
        } else if ("SUPER_ADMIN".equals(currentUser.getRole())) {
            response.sendRedirect("admin-dashboard.jsp");
            return;
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Premium Campus Food Delivery</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    /* Landing page unique design overrides */
    .hero-landing {
      padding: 100px 0 60px 0;
      position: relative;
      overflow: hidden;
    }
    .landing-hero-grid {
      display: grid;
      grid-template-columns: 1.1fr 0.9fr;
      gap: 50px;
      align-items: center;
      position: relative;
      z-index: 2;
    }
    .search-bar-landing {
      display: flex;
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-full);
      padding: 6px;
      margin-top: 30px;
      max-width: 520px;
      box-shadow: var(--shadow-md);
      transition: all var(--transition-normal);
    }
    .search-bar-landing:focus-within {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 4px rgba(var(--color-primary-rgb), 0.1);
    }
    .search-bar-landing input {
      flex-grow: 1;
      padding: 12px 20px;
      background: transparent;
      color: var(--text-primary);
      font-size: 0.95rem;
      font-weight: 600;
    }
    .search-bar-landing button {
      padding: 12px 28px;
      border-radius: var(--radius-full);
      background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
      color: white;
      font-weight: 750;
      font-size: 0.9rem;
      box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.25);
      transition: all var(--transition-fast);
    }
    .search-bar-landing button:hover {
      transform: scale(1.03);
    }
    .stats-row {
      display: flex;
      gap: 40px;
      margin-top: 40px;
      padding-top: 24px;
      border-top: 1px dashed var(--border-color);
    }
    .stat-item {
      display: flex;
      flex-direction: column;
    }
    .stat-number {
      font-size: 1.9rem;
      font-weight: 900;
      color: var(--color-primary);
      font-family: var(--font-heading);
      letter-spacing: -0.5px;
    }
    .stat-label {
      font-size: 0.82rem;
      color: var(--text-secondary);
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    .section-spacing {
      padding: 70px 0;
    }
    .grid-4col {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 24px;
    }
    .step-card {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      padding: 30px;
      text-align: center;
      transition: all var(--transition-normal);
      box-shadow: var(--shadow-sm);
    }
    .step-card:hover {
      transform: translateY(-6px);
      box-shadow: var(--shadow-md);
      border-color: rgba(var(--color-primary-rgb), 0.25);
    }
    .step-icon {
      font-size: 2.6rem;
      margin-bottom: 16px;
      display: inline-block;
      filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.05));
    }
    .step-card h4 {
      font-size: 1.15rem;
      margin-bottom: 8px;
      font-weight: 850;
      letter-spacing: -0.3px;
    }
    .step-card p {
      font-size: 0.85rem;
      color: var(--text-secondary);
      line-height: 1.5;
    }
    .cta-bar {
      background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.04) 0%, rgba(var(--color-secondary-rgb), 0.06) 100%);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 50px 30px;
      text-align: center;
      margin-bottom: 60px;
      box-shadow: var(--shadow-sm);
    }
    footer {
      background: var(--bg-secondary);
      border-top: 1px solid var(--border-color);
      padding: 48px 0;
      font-size: 0.88rem;
      color: var(--text-secondary);
    }
    .footer-grid {
      display: grid;
      grid-template-columns: 1.4fr 0.8fr 0.8fr;
      gap: 40px;
      margin-bottom: 32px;
    }
    .footer-title {
      font-size: 1.05rem;
      font-weight: 850;
      color: var(--text-primary);
      margin-bottom: 16px;
      font-family: var(--font-heading);
    }
    .footer-links {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .footer-links a {
      transition: color var(--transition-fast);
      font-weight: 500;
    }
    .footer-links a:hover {
      color: var(--color-primary);
    }
    .landing-rest-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 32px;
    }
    .landing-rest-card {
      background-color: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      overflow: hidden;
      box-shadow: var(--shadow-sm);
      transition: border-color var(--transition-normal), box-shadow var(--transition-normal);
      cursor: pointer;
      transform-style: preserve-3d;
    }
    .landing-rest-card:hover {
      box-shadow: var(--shadow-lg);
      border-color: rgba(var(--color-primary-rgb), 0.15);
    }
    .landing-rest-img {
      height: 180px;
      background-color: var(--bg-primary);
      overflow: hidden;
      position: relative;
      transform: translateZ(20px);
    }
    .landing-rest-img img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .rating-badge {
      position: absolute;
      top: 14px;
      right: 14px;
      background: var(--color-secondary);
      color: white;
      padding: 4px 10px;
      border-radius: var(--radius-full);
      font-size: 0.75rem;
      font-weight: 800;
    }
    
    @keyframes spin-slow {
      from { transform: rotate(0deg); }
      to { transform: rotate(360deg); }
    }
    
    @media (max-width: 992px) {
      .landing-hero-grid {
        grid-template-columns: 1fr;
        text-align: center;
      }
      .search-bar-landing {
        margin: 30px auto 0 auto;
      }
      .stats-row {
        justify-content: center;
      }
      .landing-rest-grid {
        grid-template-columns: repeat(2, 1fr);
      }
      .grid-4col {
        grid-template-columns: repeat(2, 1fr);
      }
    }
    @media (max-width: 768px) {
      .landing-rest-grid, .grid-4col, .footer-grid {
        grid-template-columns: 1fr;
      }
      .footer-grid {
        text-align: center;
      }
      .nav-links-desktop {
        display: none !important;
      }
    }
  </style>
</head>
<body>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="index.jsp" class="logo">
        <span>⚡</span> QuickBite
      </a>
      
      <div style="display: flex; gap: 34px; font-weight: 750; font-size: 0.9rem; color: var(--text-secondary);" class="nav-links-desktop">
        <a href="#" onclick="document.getElementById('restaurants-section').scrollIntoView({ behavior: 'smooth' }); return false;" style="transition: color var(--transition-fast);">Restaurants</a>
        <a href="#" onclick="document.getElementById('how-it-works-section').scrollIntoView({ behavior: 'smooth' }); return false;" style="transition: color var(--transition-fast);">How it Works</a>
        <a href="#" onclick="openAuthModal(); return false;" style="transition: color var(--transition-fast);">Deals</a>
      </div>

      <div class="nav-actions">
        <button id="theme-toggle" class="icon-btn" aria-label="Toggle Dark Mode" style="margin-right: 8px;">🌙</button>
        <button class="checkout-btn" onclick="openAuthModal()" style="padding: 10px 24px; font-size: 0.88rem; margin-top: 0; box-shadow: none;">
          Sign In
        </button>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <!-- Hero Landing Section -->
  <section class="hero-landing">
    <!-- HTML5 canvas for interactive floating particle effects -->
    <canvas id="hero-particle-canvas" style="position: absolute; top:0; left:0; width:100%; height:100%; pointer-events: none; z-index: 1; opacity: 0.6;"></canvas>
    
    <div class="container">
      <div class="landing-hero-grid">
        <div>
          <span class="hero-tag">🔥 Campus Favorites</span>
          <h1 style="font-size: 3.4rem; margin-top: 12px; letter-spacing: -1px;">Premium Food<br>Delivered <span class="text-gradient">To Your Door.</span></h1>
          <p style="margin-top: 18px; font-size: 1.15rem; color: var(--text-secondary); line-height: 1.6;">Order delicious biryani, sizzling Maggi, cheesy pizzas, and more from your favorite local kitchens. Speed-animated tracking and database cart logs.</p>
          
          <div class="search-bar-landing">
            <input type="text" placeholder="Enter your delivery address..." id="address-search-input">
            <button onclick="openAuthModal()">Find Food</button>
          </div>
          
          <div class="stats-row">
            <div class="stat-item">
              <span class="stat-number">12+</span>
              <span class="stat-label">Cuisines</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">15 Min</span>
              <span class="stat-label">Delivery Time</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">4.9★</span>
              <span class="stat-label">Rating</span>
            </div>
          </div>
        </div>
        
        <!-- Interactive Platter Showcase -->
        <div class="hero-art" style="position: relative; height: 350px;">
          <!-- Ambient glowing SVG -->
          <svg viewBox="0 0 500 500" fill="none" xmlns="http://www.w3.org/2000/svg" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 1;">
            <defs>
              <linearGradient id="hero-grad" x1="0" y1="0" x2="500" y2="500">
                <stop offset="0%" stop-color="var(--color-primary)" />
                <stop offset="100%" stop-color="var(--color-secondary)" />
              </linearGradient>
              <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur stdDeviation="15" result="blur" />
                <feComposite in="SourceGraphic" in2="blur" operator="over" />
              </filter>
            </defs>
            <circle cx="250" cy="250" r="220" stroke="url(#hero-grad)" stroke-width="2.5" stroke-dasharray="12 18" opacity="0.25" class="hero-svg-ring" style="transition: transform var(--transition-normal), stroke var(--transition-normal); transform-origin: 250px 250px;" />
            <circle cx="250" cy="250" r="170" fill="url(#hero-grad)" opacity="0.1" filter="url(#glow)" />
            <circle cx="250" cy="250" r="130" stroke="url(#hero-grad)" stroke-width="1.5" opacity="0.3" />
          </svg>
          <!-- Floating Platter Image -->
          <div class="dish-showcase-container" style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 2; text-align: center; width: 260px; height: 260px;">
            <img id="hero-dish-image" src="https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500" alt="Showcase Platter" style="width: 230px; height: 230px; object-fit: cover; border-radius: var(--radius-full); filter: drop-shadow(0 15px 30px rgba(0, 0, 0, 0.2)); transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); animation: spin-slow 40s linear infinite;">
            <span id="hero-dish-text" style="position: absolute; bottom: -24px; left: 50%; transform: translateX(-50%); white-space: nowrap; font-size: 0.82rem; font-weight: 800; background: var(--bg-card); border: 1px solid var(--border-color); padding: 6px 14px; border-radius: var(--radius-full); box-shadow: var(--shadow-sm); color: var(--text-primary); transition: all 0.3s;">Hyderabadi Chicken Biryani</span>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Categories / Cuisines Slider -->
  <section class="categories-section" style="padding: 20px 0;">
    <div class="container">
      <div class="category-container" style="justify-content: center;">
        <button class="category-pill active" onclick="changeHeroDish('biryani')" style="cursor: pointer;"><span class="category-icon">🍛</span><span>Biryani</span></button>
        <button class="category-pill" onclick="changeHeroDish('maggi')" style="cursor: pointer;"><span class="category-icon">🍜</span><span>Maggi</span></button>
        <button class="category-pill" onclick="changeHeroDish('burgers')" style="cursor: pointer;"><span class="category-icon">🍔</span><span>Burgers</span></button>
        <button class="category-pill" onclick="changeHeroDish('pizza')" style="cursor: pointer;"><span class="category-icon">🍕</span><span>Pizzas</span></button>
        <button class="category-pill" onclick="changeHeroDish('icecream')" style="cursor: pointer;"><span class="category-icon">🍦</span><span>Ice Cream</span></button>
        <button class="category-pill" onclick="changeHeroDish('drinks')" style="cursor: pointer;"><span class="category-icon">🥤</span><span>Drinks</span></button>
      </div>
    </div>
  </section>

  <!-- Featured Kitchens -->
  <section id="restaurants-section" class="section-spacing">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Our Campus Partners</h2>
      </div>
      
      <div class="landing-rest-grid">
        <article class="landing-rest-card" onclick="openAuthModal()">
          <div class="landing-rest-img">
            <img src="https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500" alt="Royal Biryani House">
            <span class="rating-badge">4.9 ★</span>
          </div>
          <div style="padding: 24px; transform: translateZ(10px);">
            <h3 style="font-size: 1.25rem; font-weight: 850;">Royal Biryani House</h3>
            <p style="color: var(--text-secondary); font-size: 0.85rem; margin-top: 4px;">Authentic dum rice and Indian delicacies.</p>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 0.8rem; font-weight: 700; color: var(--text-muted);">
              <span>🕒 15-20 min</span>
              <span style="color: var(--color-primary);">Order Now →</span>
            </div>
          </div>
        </article>
        
        <article class="landing-rest-card" onclick="openAuthModal()">
          <div class="landing-rest-img">
            <img src="https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500" alt="Burger Craft">
            <span class="rating-badge">4.8 ★</span>
          </div>
          <div style="padding: 24px; transform: translateZ(10px);">
            <h3 style="font-size: 1.25rem; font-weight: 850;">Burger Craft & Co.</h3>
            <p style="color: var(--text-secondary); font-size: 0.85rem; margin-top: 4px;">Premium grilled tenderloin & potato wedges.</p>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 0.8rem; font-weight: 700; color: var(--text-muted);">
              <span>🕒 10-15 min</span>
              <span style="color: var(--color-primary);">Order Now →</span>
            </div>
          </div>
        </article>
        
        <article class="landing-rest-card" onclick="openAuthModal()">
          <div class="landing-rest-img">
            <img src="https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500" alt="Pizzeria Napoli">
            <span class="rating-badge">4.7 ★</span>
          </div>
          <div style="padding: 24px; transform: translateZ(10px);">
            <h3 style="font-size: 1.25rem; font-weight: 850;">Pizzeria Napoli</h3>
            <p style="color: var(--text-secondary); font-size: 0.85rem; margin-top: 4px;">Neapolitan crust wood-fired ovens.</p>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 0.8rem; font-weight: 700; color: var(--text-muted);">
              <span>🕒 20-25 min</span>
              <span style="color: var(--color-primary);">Order Now →</span>
            </div>
          </div>
        </article>
      </div>
    </div>
  </section>

  <!-- How It Works Section -->
  <section id="how-it-works-section" class="section-spacing">
    <div class="container">
      <div class="section-header" style="justify-content: center; text-align: center; margin-bottom: 40px;">
        <h2 class="section-title">How QuickBite Works</h2>
      </div>
      
      <div class="grid-4col">
        <div class="step-card">
          <span class="step-icon">🏪</span>
          <h4>Select Kitchen</h4>
          <p>Choose from top-tier campus partner kitchens and outlets.</p>
        </div>
        <div class="step-card">
          <span class="step-icon">🛒</span>
          <h4>Build Your Cart</h4>
          <p>Select customizations and add dishes to your persistent cart.</p>
        </div>
        <div class="step-card">
          <span class="step-icon">💳</span>
          <h4>Frictionless Pay</h4>
          <p>Double-sided 3D card flips with interactive validation checks.</p>
        </div>
        <div class="step-card">
          <span class="step-icon">🛵</span>
          <h4>Live Canvas Track</h4>
          <p>Watch the animated scooter courier deliver to your address.</p>
        </div>
      </div>
    </div>
  </section>

  <!-- CTA Box -->
  <section class="container">
    <div class="cta-bar">
      <h2 style="font-size: 2.2rem; font-weight: 900; font-family: var(--font-heading);">Ready to satisfy your cravings?</h2>
      <p style="color: var(--text-secondary); margin-top: 12px; font-size: 1.05rem;">Create a customer account to get ₹100 off your very first campus meal.</p>
      <button class="hero-btn" onclick="openAuthModal()" style="margin-top: 24px; padding: 14px 36px; box-shadow: none;">Create Account</button>
    </div>
  </section>

  <!-- Footer -->
  <footer>
    <div class="container">
      <div class="footer-grid">
        <div>
          <a href="#" class="logo" style="font-size: 1.5rem; margin-bottom: 12px;"><span>⚡</span> QuickBite</a>
          <p style="max-width: 300px; line-height: 1.5;">Gourmet campus delivery system structured for recruiters and software engineering portfolios.</p>
        </div>
        <div>
          <h4 class="footer-title">Engineering Stack</h4>
          <div class="footer-links">
            <span style="font-weight: 500;">☕ Java 17 + Servlets</span>
            <span style="font-weight: 500;">📄 JSP + JDBC Connector</span>
            <span style="font-weight: 500;">💾 SQLite & MySQL</span>
            <span style="font-weight: 500;">🎨 Vanilla CSS Variables</span>
          </div>
        </div>
        <div>
          <h4 class="footer-title">Navigation</h4>
          <div class="footer-links">
            <a href="#" onclick="openAuthModal(); return false;">Login Panel</a>
            <a href="#" onclick="openAuthModal(); return false;">Registration</a>
            <a href="https://github.com" target="_blank">Repository Source</a>
          </div>
        </div>
      </div>
      <div style="text-align: center; border-top: 1px solid var(--border-color); padding-top: 24px; font-size: 0.8rem; color: var(--text-muted); font-weight: 600;">
        © 2026 QuickBite. Designed for portfolio presentations. All rights reserved.
      </div>
    </div>
  </footer>

  <!-- Auth Modal Overlay -->
  <div id="auth-overlay" class="overlay">
    <div class="modal" style="max-width: 440px;">
      <div style="padding: 24px; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center;">
        <h3 class="drawer-title" id="auth-modal-title">🔑 Sign In</h3>
        <button id="auth-close" class="drawer-close" onclick="closeAuthModal()">✕</button>
      </div>
      
      <div class="modal-body" style="padding: 24px;">
        <!-- Google Sign-In Button -->
        <div class="google-btn-container" style="margin-bottom: 18px;">
          <a href="google-login" class="google-login-btn" style="display: flex; align-items: center; justify-content: center; gap: 10px; width: 100%; padding: 12px; border: 1px solid var(--border-color); border-radius: var(--radius-md); background: var(--bg-secondary); color: var(--text-primary); text-decoration: none; font-weight: 700; font-size: 0.9rem; transition: background 0.2s; box-shadow: var(--shadow-sm);">
            <svg viewBox="0 0 24 24" width="18" height="18" xmlns="http://www.w3.org/2000/svg">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" fill="#EA4335"/>
            </svg>
            <span>Sign in with Google</span>
          </a>
        </div>

        <div class="auth-divider" style="text-align: center; margin: 16px 0; color: var(--text-muted); font-size: 0.8rem; font-weight: 700; letter-spacing: 0.5px;">OR CONTINUE WITH</div>

        <!-- Authentication Tabs -->
        <div class="auth-tabs" style="margin-bottom: 24px; display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 8px;">
          <button id="tab-login" class="auth-tab active" onclick="switchAuthTab('login')">Email</button>
          <button id="tab-phone" class="auth-tab" onclick="switchAuthTab('phone')">Phone</button>
          <button id="tab-register" class="auth-tab" onclick="switchAuthTab('register')">Register</button>
        </div>

        <!-- 1. Email Login Form -->
        <form id="login-form" class="auth-form active" action="auth" method="post">
          <input type="hidden" name="action" value="login">
          <div class="form-group">
            <label for="login-email">Email Address</label>
            <input type="email" id="login-email" name="email" class="form-input" placeholder="yourname@gmail.com" required>
          </div>
          <div class="form-group" style="margin-top: 14px;">
            <label for="login-password">Password</label>
            <input type="password" id="login-password" name="password" class="form-input" placeholder="••••••••" required>
          </div>
          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Login 🔑
          </button>
        </form>

        <!-- 2. Phone Login Form -->
        <form id="phone-form" class="auth-form" style="display: none;">
          <div class="form-group">
            <label for="phone-number-input">Phone Number (E.164)</label>
            <input type="tel" id="phone-number-input" class="form-input" placeholder="+919876543210" required>
          </div>
          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Request OTP SMS 📲
          </button>
        </form>

        <!-- 3. Phone OTP Verification Form -->
        <form id="phone-otp-form" class="auth-form" style="display: none;">
          <input type="hidden" id="verify-otp-phone">
          <div style="text-align: center; margin-bottom: 16px;">
            <p style="font-size: 0.88rem; color: var(--text-secondary);">We sent a 6-digit OTP code to <strong id="otp-display-phone" style="color: var(--text-primary);"></strong></p>
          </div>
          <div class="form-group">
            <label for="phone-otp-input">Verification Code</label>
            <input type="text" id="phone-otp-input" class="form-input" placeholder="000000" maxlength="6" minlength="6" style="text-align: center; font-size: 1.25rem; letter-spacing: 8px; font-weight: 800;" required>
          </div>
          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Verify Code 🔓
          </button>
          <div style="text-align: center; margin-top: 16px;">
            <a href="#" id="resend-otp-btn" onclick="resendPhoneOtpCode(); return false;" style="font-size: 0.85rem; color: var(--color-primary); font-weight: 700;">Resend Code</a>
          </div>
        </form>

        <!-- 4. Google OTP Verification Form -->
        <form id="google-otp-form" class="auth-form" style="display: none;">
          <input type="hidden" id="google-otp-email">
          <div style="text-align: center; margin-bottom: 16px;">
            <p style="font-size: 0.88rem; color: var(--text-secondary);">For extra security, a 6-digit code was sent to <strong id="google-otp-display-email" style="color: var(--text-primary);"></strong></p>
          </div>
          <div class="form-group">
            <label for="google-otp-input">Enter Verification Code</label>
            <input type="text" id="google-otp-input" class="form-input" placeholder="000000" maxlength="6" minlength="6" style="text-align: center; font-size: 1.25rem; letter-spacing: 8px; font-weight: 800;" required>
          </div>
          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Verify & Continue 🛡️
          </button>
        </form>

        <!-- 5. Registration Form -->
        <form id="register-form" class="auth-form" action="auth" method="post" style="display: none;">
          <input type="hidden" name="action" value="register">

          <div class="form-group">
            <label for="reg-name">Full Name</label>
            <input type="text" id="reg-name" name="name" class="form-input" placeholder="Alice Smith" required>
          </div>
          
          <div class="form-group" style="margin-top: 14px;">
            <label for="reg-email">Email Address</label>
            <input type="email" id="reg-email" name="email" class="form-input" placeholder="alice@example.com" required>
          </div>

          <div class="form-group" style="margin-top: 14px;">
            <label for="reg-phone">Phone Number (Optional/Verified)</label>
            <input type="tel" id="reg-phone" name="phone" class="form-input" placeholder="+919876543210">
          </div>
          
          <div class="form-group" style="margin-top: 14px;">
            <label for="reg-password">Password</label>
            <input type="password" id="reg-password" name="password" class="form-input" placeholder="Min. 6 characters" minlength="6" required>
          </div>

          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Create Account 🎉
          </button>
        </form>

        <!-- 6. Forgot Password Form -->
        <form id="forgot-form" class="auth-form" action="auth" method="post" style="display: none;">
          <input type="hidden" name="action" value="forgotPassword">
          <div style="margin-bottom: 16px;">
            <p style="font-size: 0.85rem; color: var(--text-secondary);">Enter your email below. We'll send a password recovery reset link to your inbox.</p>
          </div>
          <div class="form-group">
            <label for="forgot-email">Email Address</label>
            <input type="email" id="forgot-email" name="email" class="form-input" placeholder="yourname@example.com" required>
          </div>
          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Send Reset Link ✉️
          </button>
          <div style="text-align: center; margin-top: 16px;">
            <a href="#" onclick="switchAuthTab('login'); return false;" style="font-size: 0.85rem; color: var(--text-secondary); font-weight: 600;">Back to Login</a>
          </div>
        </form>

        <!-- 7. Reset Password Form -->
        <form id="reset-password-form" class="auth-form" action="auth" method="post" style="display: none;">
          <input type="hidden" name="action" value="resetPassword">
          <input type="hidden" name="token" value="${resetToken}">
          <input type="hidden" name="email" value="${resetEmail}">
          <div style="margin-bottom: 16px;">
            <p style="font-size: 0.85rem; color: var(--text-secondary);">Enter a new secure password for <strong style="color: var(--text-primary);">${resetEmail}</strong></p>
          </div>
          <div class="form-group">
            <label for="reset-new-password">New Password</label>
            <input type="password" id="reset-new-password" name="newPassword" class="form-input" placeholder="Min. 6 characters" minlength="6" required>
          </div>
          <button type="submit" class="checkout-btn" style="margin-top: 24px; width: 100%;">
            Save New Password 🔑
          </button>
        </form>
      </div>
    </div>
  </div>

  <script src="./js/auth.js"></script>
  <script>
    // Theme Switcher Logic
    const themeBtn = document.getElementById('theme-toggle');
    function initThemeButton() {
      const currentTheme = document.documentElement.getAttribute('data-theme') || 'dark';
      themeBtn.innerHTML = currentTheme === 'dark' ? '☀️' : '🌙';
    }
    themeBtn.onclick = () => {
      const current = document.documentElement.getAttribute('data-theme') || 'light';
      const next = current === 'light' ? 'dark' : 'light';
      document.documentElement.setAttribute('data-theme', next);
      localStorage.setItem('qb-theme', next);
      themeBtn.innerHTML = next === 'dark' ? '☀️' : '🌙';
    };
    initThemeButton();

    // Modal Control Logic
    const authOverlay = document.getElementById('auth-overlay');
    window.openAuthModal = function() {
      authOverlay.classList.add('active');
    }
    window.closeAuthModal = function() {
      authOverlay.classList.remove('active');
    }

    // Interactive Platter Category Swapper
    const heroDishImage = document.getElementById('hero-dish-image');
    const heroDishText = document.getElementById('hero-dish-text');
    const heroGlowRing = document.querySelector('.hero-svg-ring');
    
    const dishes = {
      biryani: {
        src: 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500',
        name: 'Hyderabadi Chicken Biryani',
        color: '#ff4757'
      },
      maggi: {
        src: 'https://images.unsplash.com/photo-1612927601601-6638404737ce?w=500',
        name: 'Cheese Masala Maggi',
        color: '#ffa502'
      },
      burgers: {
        src: 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500',
        name: 'Truffle Umami Burger',
        color: '#ff4757'
      },
      pizza: {
        src: 'https://images.unsplash.com/photo-1574071318508-1cdbab80d001?w=500',
        name: 'Classic Margherita Pizza',
        color: '#ffa502'
      },
      icecream: {
        src: 'https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=500',
        name: 'Chocolate Fudge Ice Cream',
        color: '#ffa502'
      },
      drinks: {
        src: 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?w=500',
        name: 'Iced Hazelnut Latte',
        color: '#3742fa'
      }
    };
    
    window.changeHeroDish = function(category) {
      document.querySelectorAll('.category-pill').forEach(pill => {
        if (pill.getAttribute('onclick').includes(category)) {
          pill.classList.add('active');
        } else {
          pill.classList.remove('active');
        }
      });

      const dish = dishes[category];
      if (!dish) return;
      
      heroDishImage.style.transform = 'scale(0.7) rotate(-45deg)';
      heroDishImage.style.opacity = '0.2';
      heroGlowRing.style.stroke = dish.color;
      heroGlowRing.style.transform = 'scale(1.06) rotate(180deg)';
      
      setTimeout(() => {
        heroDishImage.src = dish.src;
        heroDishText.textContent = dish.name;
        heroDishText.style.borderColor = dish.color;
        
        const r = parseInt(dish.color.slice(1,3), 16);
        const g = parseInt(dish.color.slice(3,5), 16);
        const b = parseInt(dish.color.slice(5,7), 16);
        heroDishText.style.boxShadow = "0 4px 14px rgba(" + r + ", " + g + ", " + b + ", 0.15)";
        
        heroDishImage.style.transform = 'scale(1) rotate(0deg)';
        heroDishImage.style.opacity = '1';
        heroGlowRing.style.transform = 'scale(1) rotate(360deg)';
      }, 250);
    }

    // HTML5 Floating Particle Canvas
    const canvas = document.getElementById('hero-particle-canvas');
    const ctx = canvas.getContext('2d');
    let particles = [];
    
    function resizeCanvas() {
      const rect = canvas.getBoundingClientRect();
      canvas.width = rect.width;
      canvas.height = rect.height;
    }
    window.addEventListener('resize', resizeCanvas);
    resizeCanvas();
    
    class Particle {
      constructor() {
        this.reset();
      }
      reset() {
        this.x = Math.random() * canvas.width;
        this.y = Math.random() * canvas.height;
        this.size = Math.random() * 3.5 + 1;
        this.speedX = Math.random() * 0.5 - 0.25;
        this.speedY = Math.random() * 0.5 - 0.25;
        this.color = Math.random() > 0.5 ? '#ff4757' : '#ffa502';
        this.alpha = Math.random() * 0.6 + 0.1;
      }
      update() {
        this.x += this.speedX;
        this.y += this.speedY;
        if (this.x < 0 || this.x > canvas.width || this.y < 0 || this.y > canvas.height) {
          this.reset();
        }
      }
      draw() {
        ctx.save();
        ctx.globalAlpha = this.alpha;
        ctx.fillStyle = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }
    }
    
    for (let i = 0; i < 45; i++) {
      particles.push(new Particle());
    }
    
    function animateParticles() {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      particles.forEach(p => {
        p.update();
        p.draw();
      });
      requestAnimationFrame(animateParticles);
    }
    animateParticles();

    // 3D Parallax Tilt Card Effect
    document.querySelectorAll('.landing-rest-card').forEach(card => {
      card.onmousemove = (e) => {
        const rect = card.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        const xc = rect.width / 2;
        const yc = rect.height / 2;
        const angleX = (yc - y) / 12;
        const angleY = (xc - x) / 12;
        card.style.transform = "perspective(600px) rotateX(" + angleX + "deg) rotateY(" + (-angleY) + "deg) translateY(-6px)";
      };
      
      card.onmouseleave = () => {
        card.style.transform = 'perspective(600px) rotateX(0deg) rotateY(0deg) translateY(0)';
      };
    });
  </script>
</body>
</html>
