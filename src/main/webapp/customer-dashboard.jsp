<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.dao.RestaurantDAO, com.quickbite.dao.MenuItemDAO, com.quickbite.models.Restaurant, com.quickbite.models.MenuItem, com.quickbite.models.User, java.util.List" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"CUSTOMER".equals(currentUser.getRole())) {
        response.sendRedirect("index.jsp");
        return;
    }
    
    RestaurantDAO restDAO = new RestaurantDAO();
    MenuItemDAO menuDAO = new MenuItemDAO();
    List<Restaurant> restaurants = restDAO.getAllRestaurants();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Customer Dashboard</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    /* Inline adjustment for dashboard */
    .dashboard-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
    }
    .user-greeting {
      font-size: 1.1rem;
      font-weight: 600;
      color: var(--text-secondary);
    }
  </style>
</head>
<body>

  <!-- Confetti Canvas for Order Completion -->
  <canvas id="confetti-canvas" class="confetti-canvas"></canvas>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="customer-dashboard.jsp" class="logo">
        <span>⚡</span> QuickBite
      </a>
      
      <div class="nav-search">
        <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
        </svg>
        <input type="text" id="search-input" placeholder="Search dishes or cuisines...">
      </div>

      <div class="nav-actions">
        <span class="user-greeting">Hi, <%= currentUser.getName() %>!</span>
        <button id="theme-toggle" class="icon-btn" aria-label="Toggle Dark Mode">🌙</button>
        <button id="cart-toggle" class="icon-btn" aria-label="Open Cart">
          <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 10.5V6a3.75 3.75 0 10-7.5 0v4.5m11.356-1.993l1.263 12c.07.665-.45 1.243-1.119 1.243H4.25a1.125 1.125 0 01-1.12-1.243l1.264-12A1.125 1.125 0 015.513 7.5h12.974c.576 0 1.059.435 1.119 1.007zM8.625 10.5a.375.375 0 11-.75 0 .375.375 0 01.75 0zm7.5 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z"></path>
          </svg>
          <span id="cart-badge-count" class="badge-count" style="display: none;">0</span>
        </button>
        <a href="order-history.jsp" class="icon-btn" title="Order History" style="font-size: 1.1rem; line-height: 44px; text-align: center;">📜</a>
        <a href="auth?action=logout" class="icon-btn" title="Logout" style="font-size: 1.1rem; line-height: 44px; text-align: center;">🚪</a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <!-- Hero Section -->
  <section class="hero">
    <div class="container">
      <div class="hero-card">
        <div class="hero-content">
          <!-- AI recommendation based on local time -->
          <div id="ai-recommender" style="display: inline-flex; align-items: center; gap: 8px; margin-bottom: 16px; padding: 6px 14px; background: rgba(var(--color-secondary-rgb), 0.1); border-radius: var(--radius-full); border: 1px solid rgba(var(--color-secondary-rgb), 0.2); font-size: 0.78rem; font-weight: 750; color: var(--color-secondary);">
            <span class="pulse-dot" style="width: 8px; height: 8px; border-radius: 50%; background-color: var(--color-secondary); display: inline-block; animation: pulse-ai 1.5s infinite;"></span>
            <span id="ai-recommender-text">Analyzing your campus taste...</span>
          </div>
          <br>
          <span class="hero-tag">Student Special</span>
          <h1 style="font-size: 2.8rem;">Gourmet Meal<br>Delivery <span class="text-gradient">Simplified.</span></h1>
          <p>Order from premium campus kitchens and local favorites. Secure database cart, order logging, and interactive mapping tracking.</p>
          <button class="hero-btn" onclick="document.getElementById('menu-section').scrollIntoView({ behavior: 'smooth' });">View Restaurants</button>
        </div>
        <div class="hero-art">
          <svg viewBox="0 0 500 500" fill="none" xmlns="http://www.w3.org/2000/svg">
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
            <!-- Outer rotating ring -->
            <circle cx="250" cy="250" r="220" stroke="url(#hero-grad)" stroke-width="2.5" stroke-dasharray="12 18" opacity="0.25" class="hero-svg-ring" />
            <!-- Middle glowing blur layer -->
            <circle cx="250" cy="250" r="170" fill="url(#hero-grad)" opacity="0.12" filter="url(#glow)" />
            <!-- Inner solid ring -->
            <circle cx="250" cy="250" r="130" stroke="url(#hero-grad)" stroke-width="1.5" opacity="0.35" />
            <circle cx="250" cy="250" r="110" fill="url(#hero-grad)" opacity="0.08" />
            <!-- Centered floating food emoji -->
            <text x="250" y="295" font-size="130" text-anchor="middle" filter="url(#glow)">🍔</text>
          </svg>
        </div>
      </div>
    </div>
  </section>

  <!-- Categories Slider -->
  <section class="categories-section">
    <div class="container">
      <div class="category-container" id="category-container">
        <button class="category-pill active" data-id="all"><span class="category-icon">🍽️</span><span>All Foods</span></button>
        <button class="category-pill" data-id="biryani"><span class="category-icon">🍛</span><span>Biryani</span></button>
        <button class="category-pill" data-id="maggi"><span class="category-icon">🍜</span><span>Maggi</span></button>
        <button class="category-pill" data-id="burgers"><span class="category-icon">🍔</span><span>Burgers</span></button>
        <button class="category-pill" data-id="pizza"><span class="category-icon">🍕</span><span>Pizzas</span></button>
        <button class="category-pill" data-id="icecream"><span class="category-icon">🍦</span><span>Ice Cream</span></button>
        <button class="category-pill" data-id="drinks"><span class="category-icon">🥤</span><span>Drinks</span></button>
      </div>
    </div>
  </section>

  <!-- Restaurant & Menu Lists -->
  <section id="menu-section" class="menu-section">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Available Menus</h2>
      </div>

      <div class="menu-grid" id="menu-grid">
        <% 
          boolean hasItems = false;
          for (Restaurant rest : restaurants) {
              if ("CLOSED".equals(rest.getStatus())) continue;
              List<MenuItem> items = menuDAO.getMenuItemsByRestaurant(rest.getId());
              for (MenuItem item : items) {
                  hasItems = true;
        %>
          <article class="dish-card" data-id="<%= item.getId() %>" data-category="<%= item.getCategory() %>" data-restaurant-id="<%= rest.getId() %>" data-price="<%= item.getPrice() %>" data-name="<%= item.getName() %>">
            <div class="card-img-container">
              <img src="<%= item.getImageUrl() %>" alt="<%= item.getName() %>" onerror="this.src='https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=400&auto=format&fit=crop&q=60'">
              <div class="card-badges">
                <span class="card-badge-tag"><%= rest.getCuisine() %></span>
              </div>
            </div>
            <div class="card-body">
              <span class="card-restaurant"><%= rest.getName() %></span>
              <h3 class="card-title"><%= item.getName() %></h3>
              <p class="card-desc">Freshly prepared, high-quality dish served warm from our kitchen.</p>
              
              <div class="card-info-row">
                <span class="info-item">🕒 15-20 min</span>
                <span class="info-item">📍 Local Delivery</span>
              </div>
              
              <div class="card-footer">
                <span class="card-price">₹<%= String.format("%.2f", item.getPrice()) %></span>
                <button class="add-cart-btn btn-trigger-customize" onclick="openCustomizationModal(<%= item.getId() %>, '<%= item.getName().replace("'", "\\'") %>', <%= item.getPrice() %>, '<%= item.getCategory() %>')">
                  <svg fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15"/></svg>
                  Add
                </button>
              </div>
            </div>
          </article>
        <% 
              }
          }
          if (!hasItems) {
        %>
          <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-muted);">
            <p style="font-size: 1.2rem; margin-bottom: 8px;">No restaurants are currently open.</p>
            <p>Please check back later!</p>
          </div>
        <% } %>
      </div>
    </div>
  </section>

  <!-- Shopping Cart Side Drawer -->
  <aside id="cart-drawer" class="drawer">
    <div class="drawer-header">
      <h3 class="drawer-title">🛒 Your Cart</h3>
      <button id="cart-close" class="drawer-close">✕</button>
    </div>
    
    <div class="drawer-body" id="cart-body">
      <!-- Empty state -->
      <div id="cart-empty-state" class="cart-empty-state">
        <svg fill="none" stroke="currentColor" stroke-width="1.5" viewBox="0 0 24 24">
          <path d="M15.75 10.5V6a3.75 3.75 0 10-7.5 0v4.5m11.356-1.993l1.263 12c.07.665-.45 1.243-1.119 1.243H4.25a1.125 1.125 0 01-1.12-1.243l1.264-12A1.125 1.125 0 015.513 7.5h12.974c.576 0 1.059.435 1.119 1.007zM8.625 10.5a.375.375 0 11-.75 0 .375.375 0 01.75 0zm7.5 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z"></path>
        </svg>
        <p>Your cart is empty</p>
      </div>
      
      <!-- Cart Items List -->
      <div id="cart-items-list" class="cart-items-list" style="display: none;"></div>
    </div>
    
    <!-- Calculations & Checkout -->
    <div class="cart-checkout-section">
      <div class="summary-details">
        <div class="summary-row">
          <span>Subtotal</span>
          <span id="subtotal-val">₹0.00</span>
        </div>
        <div class="summary-row">
          <span>Delivery Fee</span>
          <span id="delivery-val">₹30.00</span>
        </div>
        <div class="summary-row">
          <span>Taxes (5% GST)</span>
          <span id="tax-val">₹0.00</span>
        </div>
        <div class="summary-row total">
          <span>Grand Total</span>
          <span id="total-val">₹0.00</span>
        </div>
      </div>
      
      <button id="checkout-btn" class="checkout-btn">
        Proceed to Checkout
      </button>
    </div>
  </aside>

  <!-- Customize Modal Overlay -->
  <div id="customize-overlay" class="overlay">
    <div class="modal">
      <div class="modal-header-img" style="height: 200px;">
        <img src="https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500&auto=format&fit=crop&q=60" alt="Dish cover">
        <button id="modal-close" class="modal-close-btn">✕</button>
      </div>
      <div class="modal-body">
        <div class="modal-title-row">
          <h3 id="modal-dish-name" class="modal-title">Dish Name</h3>
          <span id="modal-dish-price" class="modal-price-tag">₹0.00</span>
        </div>
        <p class="modal-desc">Authentic gourmet recipe customized to your liking. Choose size and add-ons below.</p>
        
        <div class="modal-section-title">Select size</div>
        <div class="option-group">
          <label class="option-label">
            <div class="option-input-wrapper">
              <input type="radio" name="size-select" value="0" checked>
              <span>Regular Size</span>
            </div>
            <span class="option-price-modifier">Free</span>
          </label>
        </div>
      </div>
      <div class="modal-footer">
        <div class="quantity-selector">
          <button id="modal-qty-dec" class="qty-btn">-</button>
          <span id="modal-qty-val" class="qty-value">1</span>
          <button id="modal-qty-inc" class="qty-btn">+</button>
        </div>
        <button id="modal-add-to-cart-submit" class="add-to-cart-submit">Add to Cart</button>
      </div>
    </div>
  </div>

  <!-- Secure Checkout Modal Overlay -->
  <div id="checkout-overlay" class="overlay">
    <div class="modal checkout-modal">
      <div class="drawer-header" style="padding: 20px 24px;">
        <h3 class="drawer-title">💳 Secure Payment</h3>
        <button id="checkout-close" class="drawer-close">✕</button>
      </div>
      
      <form id="checkout-form">
        <div class="checkout-grid">
          <div class="checkout-left">
            <div class="form-group">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                <label for="del-address" style="margin-bottom: 0;">Delivery Address</label>
                <button type="button" onclick="getCurrentLocation()" style="background: none; border: none; color: var(--primary-color); cursor: pointer; display: flex; align-items: center; gap: 4px; font-size: 0.85rem; font-weight: 600; padding: 0;">
                  📍 Locate Me
                </button>
              </div>
              <input type="text" id="del-address" class="form-input" placeholder="e.g. 104 Westside Lane, Apt 4" required>
              <iframe id="google-map-iframe" width="100%" height="150" style="border:0; border-radius: var(--radius-md); background: rgba(0,0,0,0.1); margin-top: 10px;" src="https://maps.google.com/maps?q=Mumbai&t=&z=13&ie=UTF8&iwloc=&output=embed" allowfullscreen></iframe>
            </div>

            <!-- Payment Method Selector -->
            <div class="form-group">
              <label>Select Payment Method</label>
              <div class="payment-methods-tabs">
                <button type="button" class="pay-method-btn active" onclick="switchPayMethod('card')">💳 Card</button>
                <button type="button" class="pay-method-btn" onclick="switchPayMethod('upi')">⚡ UPI</button>
                <button type="button" class="pay-method-btn" onclick="switchPayMethod('pod')">🛵 Pay on Delivery</button>
              </div>
            </div>
            
            <!-- Card Fields Container -->
            <div id="card-payment-fields" style="display: block;">
              <div class="form-group">
                <label for="cc-number">Card Number</label>
                <input type="text" id="cc-number" class="form-input" placeholder="•••• •••• •••• ••••" required>
              </div>
              
              <div class="form-group" style="margin-top: 14px;">
                <label for="cc-name">Cardholder Name</label>
                <input type="text" id="cc-name" class="form-input" placeholder="e.g. JOHN DOE" required>
              </div>
              
              <div class="form-row" style="margin-top: 14px; display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
                <div class="form-group">
                  <label for="cc-expiry">Expiry Date</label>
                  <input type="text" id="cc-expiry" class="form-input" placeholder="MM/YY" required>
                </div>
                <div class="form-group">
                  <label for="cc-cvv">CVV</label>
                  <input type="password" id="cc-cvv" class="form-input" placeholder="•••" required>
                </div>
              </div>
            </div>

            <!-- UPI Fields Container -->
            <div id="upi-payment-fields" style="display: none;">
              <div class="form-group">
                <label for="upi-id">UPI ID / VPA</label>
                <input type="text" id="upi-id" class="form-input" placeholder="username@okaxis">
                <p style="font-size: 0.75rem; opacity: 0.6; margin-top: 6px;">You will receive a collect request on your UPI app (Google Pay, PhonePe, Paytm etc.)</p>
              </div>
              
              <!-- Dynamic QR Code Display -->
              <div style="margin-top: 15px; text-align: center; background: rgba(255,255,255,0.03); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px;">
                <p style="font-size: 0.8rem; margin-bottom: 10px; font-weight: 500; color: var(--text-color);">Or scan QR Code to Pay Instantly:</p>
                <img id="upi-qr-code" src="https://api.qrserver.com/v1/create-qr-code/?size=130x130&data=upi://pay?pa=quickbite@bank%26pn=QuickBite%26am=0.00%26cu=INR" alt="UPI QR Code" style="border-radius: var(--radius-md); display: inline-block; background: white; padding: 6px;">
              </div>
            </div>

            <!-- Pay on Delivery Container -->
            <div id="pod-payment-fields" style="display: none;">
              <div style="background: rgba(255,255,255,0.03); border: 1px dashed var(--border-color); border-radius: var(--radius-md); padding: 20px; text-align: center; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px;">
                <div style="font-size: 2.2rem;">🛵</div>
                <h4 style="margin: 0; color: var(--text-color);">Cash / UPI on Delivery</h4>
                <p style="font-size: 0.8rem; opacity: 0.7; margin: 0; max-width: 250px;">You can pay our delivery partner via Cash or scanning their UPI QR code at your doorstep.</p>
              </div>
            </div>
          </div>
          
          <div class="checkout-right">
            <div class="card-container">
              <div id="credit-card" class="credit-card">
                <div class="card-face front">
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div class="card-chip"></div>
                    <div class="card-logo">QuickPay</div>
                  </div>
                  <div class="card-number-display" id="cc-display-number">•••• •••• •••• ••••</div>
                  <div class="card-details-row">
                    <div>
                      <div class="card-detail-label">Card Holder</div>
                      <div class="card-detail-val" id="cc-display-name">CARDHOLDER NAME</div>
                    </div>
                    <div>
                      <div class="card-detail-label">Expires</div>
                      <div class="card-detail-val" id="cc-display-expiry">MM/YY</div>
                    </div>
                  </div>
                </div>
                <div class="card-face back">
                  <div class="card-stripe"></div>
                  <div>
                    <div class="card-detail-label" style="text-align: right; margin-right: 10px;">CVV</div>
                    <div class="card-signature-bar">
                      <span id="cc-display-cvv">•••</span>
                    </div>
                  </div>
                  <div style="font-size: 0.55rem; text-align: center; opacity: 0.7;">
                    Student Resume Demo Secured Transaction.
                  </div>
                </div>
              </div>
            </div>
            
            <button type="submit" class="checkout-btn" style="margin-top: 0; width: 100%;">
              Pay & Order
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>

  <script src="./js/app.js?v=<%= System.currentTimeMillis() %>"></script>
  <script>
    // AI Sensory Recommender based on client hour
    (function() {
      const recommenderText = document.getElementById('ai-recommender-text');
      if (recommenderText) {
        const hours = new Date().getHours();
        let suggestion = '';
        if (hours >= 5 && hours < 11) {
          suggestion = "Recommended Morning fuel: Sizzling hot Maggi & Premium Shakes ☕";
        } else if (hours >= 11 && hours < 16) {
          suggestion = "Recommended Lunch fuel: Kolkata Special Dum Biryani 🍛";
        } else if (hours >= 16 && hours < 22) {
          suggestion = "Recommended Dinner fuel: Double Cheese Burgers & Truffle Pizza 🍕";
        } else {
          suggestion = "Late Night Cravings: Cheese Masala Maggi & Cold Drinks 🍜";
        }
        recommenderText.textContent = suggestion;
      }
    })();

    // Simple state variables for custom items
    let selectedMenuItemId = null;
    let selectedPrice = 0;
    let selectedQty = 1;
    let activeCategory = 'all';

    function openCustomizationModal(id, name, price, category) {
      selectedMenuItemId = id;
      selectedPrice = price;
      selectedQty = 1;
      
      document.getElementById('modal-dish-name').textContent = name;
      document.getElementById('modal-dish-price').textContent = '₹' + price.toFixed(2);
      document.getElementById('modal-qty-val').textContent = '1';
      document.getElementById('customize-overlay').classList.add('active');
    }

    // Modal Qty increments
    document.getElementById('modal-qty-dec').onclick = () => {
      if (selectedQty > 1) {
        selectedQty--;
        document.getElementById('modal-qty-val').textContent = selectedQty;
        document.getElementById('modal-dish-price').textContent = '₹' + (selectedPrice * selectedQty).toFixed(2);
      }
    };
    document.getElementById('modal-qty-inc').onclick = () => {
      selectedQty++;
      document.getElementById('modal-qty-val').textContent = selectedQty;
      document.getElementById('modal-dish-price').textContent = '₹' + (selectedPrice * selectedQty).toFixed(2);
    };

    document.getElementById('modal-close').onclick = () => {
      document.getElementById('customize-overlay').classList.remove('active');
    };

    // Category pills click handlers
    document.querySelectorAll('.category-pill').forEach(pill => {
      pill.onclick = () => {
        document.querySelectorAll('.category-pill').forEach(p => p.classList.remove('active'));
        pill.classList.add('active');
        activeCategory = pill.dataset.id;
        filterMenuGrid();
      };
    });

    // Filtering dish list
    function filterMenuGrid() {
      const query = document.getElementById('search-input').value.toLowerCase();
      document.querySelectorAll('.dish-card').forEach(card => {
        const name = card.dataset.name.toLowerCase();
        const cat = card.dataset.category;
        
        const catMatch = (activeCategory === 'all' || cat === activeCategory);
        const queryMatch = (!query || name.includes(query));
        
        if (catMatch && queryMatch) {
          card.style.display = 'flex';
        } else {
          card.style.display = 'none';
        }
      });
    }

    document.getElementById('search-input').oninput = filterMenuGrid;
  </script>
</body>
</html>
