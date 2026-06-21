<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.dao.OrderDAO, com.quickbite.models.Order, com.quickbite.models.OrderItem, com.quickbite.models.User, java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"CUSTOMER".equals(currentUser.getRole())) {
        response.sendRedirect("index.jsp");
        return;
    }
    
    OrderDAO orderDAO = new OrderDAO();
    List<Order> orders = orderDAO.getOrdersByUserId(currentUser.getId());
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Order History</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    .history-card {
      background-color: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      padding: 24px;
      margin-bottom: 20px;
      box-shadow: var(--shadow-sm);
    }
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      border-bottom: 1px dashed var(--border-color);
      padding-bottom: 12px;
    }
    .history-restaurant {
      font-size: 1.2rem;
      font-weight: 700;
      color: var(--text-primary);
    }
    .history-date {
      font-size: 0.85rem;
      color: var(--text-muted);
    }
    .history-items {
      display: flex;
      flex-direction: column;
      gap: 6px;
      margin-bottom: 16px;
    }
    .history-item {
      display: flex;
      justify-content: space-between;
      font-size: 0.9rem;
      color: var(--text-secondary);
    }
    .status-badge {
      padding: 6px 12px;
      border-radius: var(--radius-full);
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
    }
    .status-placed { background-color: rgba(112, 71, 235, 0.1); color: var(--color-secondary); }
    .status-preparing { background-color: rgba(241, 196, 15, 0.1); color: var(--color-warning); }
    .status-out_for_delivery { background-color: rgba(255, 94, 58, 0.1); color: var(--color-primary); }
    .status-arriving { background-color: rgba(9, 132, 227, 0.1); color: #0984e3; }
    .status-delivered { background-color: rgba(0, 184, 148, 0.1); color: var(--color-success); }
  </style>
</head>
<body>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="customer-dashboard.jsp" class="logo">
        <span>⚡</span> QuickBite
      </a>
      <div class="nav-actions">
        <a href="customer-dashboard.jsp" class="icon-btn" title="Back to Dashboard" style="font-size: 1.1rem; line-height: 44px; text-align: center;">🏠</a>
        <a href="auth?action=logout" class="icon-btn" title="Logout" style="font-size: 1.1rem; line-height: 44px; text-align: center;">🚪</a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <section style="padding: 40px 0;">
    <div class="container" style="max-width: 800px;">
      <div class="section-header">
        <h2 class="section-title">Order History</h2>
      </div>

      <% if (orders.isEmpty()) { %>
        <div style="text-align: center; padding: 60px 20px; color: var(--text-muted);">
          <span style="font-size: 3rem;">📜</span>
          <h3 style="margin-top: 16px; margin-bottom: 8px;">No orders found</h3>
          <p>You haven't ordered any foods yet. Head to your dashboard to place your first order!</p>
          <a href="customer-dashboard.jsp" class="hero-btn" style="display: inline-block; margin-top: 20px;">Order Now</a>
        </div>
      <% } else { 
          for (Order o : orders) {
      %>
        <div class="history-card">
          <div class="history-header">
            <div>
              <div class="history-restaurant"><%= o.getRestaurantName() %></div>
              <div class="history-date">Ordered: <%= o.getCreatedAt() %></div>
            </div>
            <div>
              <span class="status-badge status-<%= o.getStatus().toLowerCase() %>">
                <%= o.getStatus().replace("_", " ") %>
              </span>
            </div>
          </div>
          
          <div class="history-items">
            <% for (OrderItem oi : o.getItems()) { %>
              <div class="history-item">
                <span><%= oi.getQuantity() %>x <%= oi.getMenuItemName() %></span>
                <span>₹<%= String.format("%.2f", oi.getPrice() * oi.getQuantity()) %></span>
              </div>
            <% } %>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 12px;">
            <span style="font-weight: 700; color: var(--text-primary);">Total Paid</span>
            <span style="font-size: 1.25rem; font-weight: 800; color: var(--color-primary);">₹<%= String.format("%.2f", o.getTotal()) %></span>
          </div>
          
          <% if (!"DELIVERED".equals(o.getStatus())) { %>
            <div style="margin-top: 16px; text-align: right;">
              <a href="order-tracking.jsp?orderId=<%= o.getId() %>" class="hero-btn" style="padding: 8px 16px; font-size: 0.85rem;">
                Track Live Order 📍
              </a>
            </div>
          <% } %>
        </div>
      <% 
          }
        } 
      %>
    </div>
  </section>

</body>
</html>
