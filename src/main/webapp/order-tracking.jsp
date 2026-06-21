<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.dao.OrderDAO, com.quickbite.models.Order, com.quickbite.models.User" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"CUSTOMER".equals(currentUser.getRole())) {
        response.sendRedirect("index.jsp");
        return;
    }
    
    int orderId = -1;
    Order order = null;
    try {
        orderId = Integer.parseInt(request.getParameter("orderId"));
        OrderDAO orderDAO = new OrderDAO();
        order = orderDAO.getOrderById(orderId);
    } catch (Exception e) {}
    
    if (order == null || order.getUserId() != currentUser.getId()) {
        response.sendRedirect("customer-dashboard.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Track Order #<%= order.getId() %></title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
</head>
<body>

  <!-- Confetti Canvas for Completion -->
  <canvas id="confetti-canvas" class="confetti-canvas"></canvas>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="customer-dashboard.jsp" class="logo">
        <span>⚡</span> QuickBite
      </a>
      <div class="nav-actions">
        <a href="customer-dashboard.jsp" class="icon-btn" title="Back to Dashboard" style="font-size: 1.1rem; line-height: 44px; text-align: center;">🏠</a>
        <a href="order-history.jsp" class="icon-btn" title="Order History" style="font-size: 1.1rem; line-height: 44px; text-align: center;">📜</a>
        <a href="auth?action=logout" class="icon-btn" title="Logout" style="font-size: 1.1rem; line-height: 44px; text-align: center;">🚪</a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <!-- Live Order Tracking Dashboard Section -->
  <section class="tracker-section active" style="padding: 40px 0;">
    <div class="container">
      <div class="section-header">
        <h2 class="section-title">Order Status: #<%= order.getId() %></h2>
      </div>
      
      <div class="tracker-grid">
        <!-- Canvas Map -->
        <div class="map-card">
          <canvas id="map-canvas"></canvas>
        </div>
        
        <!-- Live details & status steps -->
        <div class="tracker-details">
          <div class="tracker-status-box">
            <div class="tracking-header">
              <h3 class="tracking-title" id="track-status-title">Locating Rider...</h3>
              <span class="tracking-order-id">QB-<%= order.getId() %></span>
            </div>
            <p id="track-status-desc" style="color: var(--text-secondary); margin-bottom: 24px; font-size: 0.9rem;">Your payment is secured and restaurant has accepted your order.</p>
            
            <div class="status-steps">
              <div class="status-step active" id="step-received">
                <div class="step-info">
                  <h4>Order Placed</h4>
                  <p>We've received your request.</p>
                </div>
              </div>
              <div class="status-step" id="step-preparing">
                <div class="step-info">
                  <h4>Preparing</h4>
                  <p>Your meal is cooking in the kitchen.</p>
                </div>
              </div>
              <div class="status-step" id="step-picked_up">
                <div class="step-info">
                  <h4>Out for Delivery</h4>
                  <p>Rider has departed the restaurant.</p>
                </div>
              </div>
              <div class="status-step" id="step-arriving">
                <div class="step-info">
                  <h4>Arriving</h4>
                  <p>Rider is close to your location.</p>
                </div>
              </div>
              <div class="status-step" id="step-delivered">
                <div class="step-info">
                  <h4>Delivered</h4>
                  <p>Enjoy your warm food!</p>
                </div>
            </div>
          </div>

          <!-- Delivery Details Card -->
          <div class="tracker-status-box" style="margin-top: 20px; padding: 20px 24px;">
            <h4 style="color: var(--text-color); margin-bottom: 12px; display: flex; align-items: center; gap: 8px;">
              📍 Delivery Details
            </h4>
            <div style="font-size: 0.85rem; line-height: 1.5; color: var(--text-secondary); display: flex; flex-direction: column; gap: 8px;">
              <div>
                <strong>Address:</strong> <span id="display-address" style="color: var(--text-color); font-weight: 500;">Not specified</span>
              </div>
              <div>
                <strong>Payment Method:</strong> <span id="display-payment-method" style="color: var(--text-color); font-weight: 500;">Paid Online</span>
              </div>
            </div>
          </div>
          
          <!-- Debugging Fast Forward Controls -->
          <div class="debug-panel">
            <div class="debug-title">Simulation Preview</div>
            <p style="font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 12px; line-height: 1.4;">
              This tracking widget connects with the backend DB. You can fast-forward the route simulation locally or update status directly in the restaurant console.
            </p>
            <div class="debug-btn-group">
              <button id="fast-forward-btn" class="debug-btn" style="background-color: var(--color-primary); color: white; border-color: transparent;">⏩ Fast Forward Route</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Delivery Feedback & Star Rating Modal -->
  <div id="rating-overlay" class="overlay">
    <div class="modal" style="max-width: 440px;">
      <div class="rating-modal-content">
        <button id="rating-close" class="modal-close-btn" style="background-color: var(--bg-input); color: var(--text-primary);">✕</button>
        <span style="font-size: 3.5rem;">🎉</span>
        <h3 style="font-size: 1.4rem; margin-top: 16px; margin-bottom: 8px;">Order Arrived!</h3>
        <p style="color: var(--text-secondary); font-size: 0.9rem;">We hope you loved your meal. Give us a feedback rating below:</p>
        
        <!-- Interactive Star Selector -->
        <div class="rating-stars" style="margin: 20px 0;">
          <span class="star" data-star="1" style="font-size: 2.2rem; cursor: pointer; color: var(--border-color);">★</span>
          <span class="star" data-star="2" style="font-size: 2.2rem; cursor: pointer; color: var(--border-color);">★</span>
          <span class="star" data-star="3" style="font-size: 2.2rem; cursor: pointer; color: var(--border-color);">★</span>
          <span class="star" data-star="4" style="font-size: 2.2rem; cursor: pointer; color: var(--border-color);">★</span>
          <span class="star" data-star="5" style="font-size: 2.2rem; cursor: pointer; color: var(--border-color);">★</span>
        </div>
        
        <button id="rating-submit-btn" class="checkout-btn" style="margin-top: 10px;" onclick="submitRating()">
          Submit Feedback
        </button>
      </div>
    </div>
  </div>

  <script type="module">
    import { DeliveryTracker } from './js/tracker.js';

    let currentOrderState = '<%= order.getStatus() %>';
    const orderId = <%= order.getId() %>;
    let trackerInstance = null;

    document.addEventListener('DOMContentLoaded', () => {
      const canvas = document.getElementById('map-canvas');
      trackerInstance = new DeliveryTracker(canvas, onStatusUpdate, onDeliveryComplete);
      trackerInstance.start();

      // Initialize progress based on current database status
      if (currentOrderState === 'PREPARING') {
        trackerInstance.progress = 0.25;
      } else if (currentOrderState === 'OUT_FOR_DELIVERY') {
        trackerInstance.progress = 0.50;
      } else if (currentOrderState === 'ARRIVING') {
        trackerInstance.progress = 0.85;
      } else if (currentOrderState === 'DELIVERED') {
        trackerInstance.progress = 1.0;
      }

      // Setup Fast Forward button
      document.getElementById('fast-forward-btn').onclick = () => {
        trackerInstance.fastForward();
        document.getElementById('fast-forward-btn').textContent = '⚡ Speeding up...';
        document.getElementById('fast-forward-btn').disabled = true;
      };

      // Establish Real-Time SSE connection for status updates
      if (window.EventSource) {
        const sseSource = new EventSource('order-status-sse?orderId=' + orderId);
        sseSource.onmessage = function(event) {
          try {
            const data = JSON.parse(event.data);
            if (data.status === 'success' && data.orderStatus !== currentOrderState) {
              currentOrderState = data.orderStatus;
              
              // Adjust local animation progress based on SSE updates
              if (currentOrderState === 'PREPARING' && trackerInstance.progress < 0.2) {
                trackerInstance.progress = 0.25;
              } else if (currentOrderState === 'OUT_FOR_DELIVERY' && trackerInstance.progress < 0.45) {
                trackerInstance.progress = 0.50;
              } else if (currentOrderState === 'ARRIVING' && trackerInstance.progress < 0.8) {
                trackerInstance.progress = 0.85;
              } else if (currentOrderState === 'DELIVERED') {
                trackerInstance.progress = 1.0;
                sseSource.close(); // Close SSE stream on complete
              }
            }
          } catch(e) {
            console.error("SSE parse error:", e);
          }
        };
        sseSource.onerror = function() {
          console.warn("SSE connection error. Falling back to HTTP polling...");
          sseSource.close();
          setInterval(checkDatabaseStatus, 5000); // Failover fallback to polling
        };
      } else {
        setInterval(checkDatabaseStatus, 3000);
      }
      
      // Setup Rating stars
      const stars = document.querySelectorAll('.rating-stars .star');
      stars.forEach(star => {
        star.onclick = () => {
          const rating = parseInt(star.dataset.star);
          stars.forEach((s, idx) => {
            if (idx < rating) {
              s.style.color = 'var(--color-warning)';
            } else {
              s.style.color = 'var(--border-color)';
            }
          });
        };
      });
    });

    function onStatusUpdate(code, title, desc) {
      document.getElementById('track-status-title').textContent = title;
      document.getElementById('track-status-desc').textContent = desc;

      const steps = ['received', 'preparing', 'picked_up', 'arriving', 'delivered'];
      const activeIdx = steps.indexOf(code);
      
      steps.forEach((step, idx) => {
        const node = document.getElementById('step-' + step);
        if (!node) return;
        node.className = 'status-step';
        if (idx < activeIdx) {
          node.classList.add('completed');
        } else if (idx === activeIdx) {
          node.classList.add('active');
        }
      });

      // Synchronize database status via REST call if status changed
      if (trackerInstance && trackerInstance.speed > 0.002) {
        // Fast-forwarded status: sync back to database
        let dbStatus = code.toUpperCase();
        if (code === 'received') dbStatus = 'PLACED';
        if (code === 'picked_up') dbStatus = 'OUT_FOR_DELIVERY';
        fetch('restaurant?action=updateOrderStatus&orderId=' + orderId + '&status=' + dbStatus, { method: 'POST' });
      }
    }

    function onDeliveryComplete() {
      startConfetti();
      setTimeout(() => {
        document.getElementById('rating-overlay').classList.add('active');
      }, 1500);
    }

    function checkDatabaseStatus() {
      fetch('customer?action=getOrderStatus&orderId=' + orderId)
        .then(res => res.json())
        .then(data => {
          if (data.status === 'success' && data.orderStatus !== currentOrderState) {
            currentOrderState = data.orderStatus;
            
            // Adjust local animation progress based on database updates
            if (currentOrderState === 'PREPARING' && trackerInstance.progress < 0.2) {
              trackerInstance.progress = 0.25;
            } else if (currentOrderState === 'OUT_FOR_DELIVERY' && trackerInstance.progress < 0.45) {
              trackerInstance.progress = 0.50;
            } else if (currentOrderState === 'ARRIVING' && trackerInstance.progress < 0.8) {
              trackerInstance.progress = 0.85;
            } else if (currentOrderState === 'DELIVERED' && trackerInstance.progress < 1.0) {
              trackerInstance.progress = 1.0;
            }
          }
        })
        .catch(err => console.error(err));
    }

    window.submitRating = function() {
      alert('Thank you for your review!');
      window.location.href = 'customer-dashboard.jsp';
    }

    document.getElementById('rating-close').onclick = () => {
      document.getElementById('rating-overlay').classList.remove('active');
    };

    // Confetti code
    function startConfetti() {
      const canvas = document.getElementById('confetti-canvas');
      const ctx = canvas.getContext('2d');
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
      
      const colors = ['#ff5e3a', '#7047eb', '#00b894', '#f1c40f'];
      let particles = [];
      for (let i = 0; i < 100; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height - canvas.height,
          r: Math.random() * 6 + 4,
          color: colors[Math.floor(Math.random() * colors.length)]
        });
      }
      
      let frames = 150;
      function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        particles.forEach(p => {
          p.y += 4;
          ctx.beginPath();
          ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
          ctx.fillStyle = p.color;
          ctx.fill();
        });
        frames--;
        if (frames > 0) requestAnimationFrame(draw);
      }
      requestAnimationFrame(draw);
    }

    // Retrieve and display last saved address and payment method from localStorage
    document.addEventListener('DOMContentLoaded', () => {
      const address = localStorage.getItem('qb_last_address');
      const payMethod = localStorage.getItem('qb_last_payment_method');
      if (address) {
        document.getElementById('display-address').textContent = address;
      }
      if (payMethod) {
        const payLabels = {
          card: '💳 Credit / Debit Card',
          upi: '⚡ UPI (Scan & Pay)',
          pod: '🛵 Cash / UPI on Delivery (POD)'
        };
        document.getElementById('display-payment-method').textContent = payLabels[payMethod] || payMethod;
      }
    });
  </script>
</body>
</html>
