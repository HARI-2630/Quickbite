<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.dao.RestaurantDAO, com.quickbite.dao.MenuItemDAO, com.quickbite.dao.OrderDAO, com.quickbite.models.Restaurant, com.quickbite.models.MenuItem, com.quickbite.models.Order, com.quickbite.models.OrderItem, com.quickbite.models.User, java.util.List" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"RESTAURANT_ADMIN".equals(currentUser.getRole())) {
        response.sendRedirect("index.jsp");
        return;
    }
    
    RestaurantDAO restDAO = new RestaurantDAO();
    MenuItemDAO menuDAO = new MenuItemDAO();
    OrderDAO orderDAO = new OrderDAO();
    
    List<Restaurant> myRestaurants = restDAO.getRestaurantsByOwner(currentUser.getId());
    Restaurant myRest = myRestaurants.isEmpty() ? null : myRestaurants.get(0);
    
    List<MenuItem> menuItems = null;
    List<Order> orders = null;
    if (myRest != null) {
        menuItems = menuDAO.getMenuItemsByRestaurant(myRest.getId());
        orders = orderDAO.getOrdersByOwnerId(currentUser.getId());
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Owner Dashboard</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    .grid-dashboard {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 30px;
      margin-top: 40px;
    }
    .panel-card {
      background-color: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 30px;
      box-shadow: var(--shadow-md);
    }
    .panel-title {
      font-size: 1.4rem;
      font-weight: 800;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid var(--border-color);
    }
    .table-container {
      width: 100%;
      overflow-x: auto;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    th, td {
      padding: 12px;
      border-bottom: 1px solid var(--border-color);
      font-size: 0.9rem;
    }
    th {
      font-weight: 700;
      color: var(--text-secondary);
    }
    .status-dropdown {
      padding: 6px;
      border-radius: var(--radius-xs);
      border: 1px solid var(--border-color);
      background-color: var(--bg-input);
      color: var(--text-primary);
      cursor: pointer;
    }
  </style>
</head>
<body>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="restaurant-dashboard.jsp" class="logo">
        <span>⚡</span> QuickBite Owner
      </a>
      <div class="nav-actions">
        <span class="user-greeting">Welcome, <%= currentUser.getName() %></span>
        <a href="auth?action=logout" class="icon-btn" title="Logout">🚪</a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <section style="padding: 40px 0;">
    <div class="container">
      <% if (myRest == null) { %>
        <div style="text-align: center; padding: 80px 20px; color: var(--text-muted);" class="panel-card">
          <span style="font-size: 4rem;">🏪</span>
          <h3 style="margin-top: 20px; margin-bottom: 12px; font-size: 1.6rem;">No Restaurant Registered</h3>
          <p style="max-width: 600px; margin: 0 auto 30px auto;">You are registered as a restaurant administrator but haven't been assigned a restaurant. Please contact a Super Administrator to register your restaurant and link it to your account.</p>
        </div>
      <% } else { %>
        <!-- Restaurant Header -->
        <div class="panel-card" style="margin-bottom: 40px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 20px;">
          <div>
            <span class="badge" style="margin-bottom: 8px;"><%= myRest.getCuisine() %></span>
            <h2 style="font-size: 2rem; font-weight: 800;"><%= myRest.getName() %></h2>
            <p style="color: var(--text-secondary); margin-top: 4px;">Manage your dishes, adjust menu offerings, and fulfill customer orders.</p>
          </div>
          <div>
            <form action="restaurant" method="post" style="display: flex; align-items: center; gap: 12px;">
              <input type="hidden" name="action" value="toggleStatus">
              <input type="hidden" name="restaurantId" value="<%= myRest.getId() %>">
              <span style="font-weight: 700;">Status: 
                <span style="color: <%= "OPEN".equals(myRest.getStatus()) ? "var(--color-success)" : "var(--color-danger)" %>">
                  <%= myRest.getStatus() %>
                </span>
              </span>
              <input type="hidden" name="status" value="<%= "OPEN".equals(myRest.getStatus()) ? "CLOSED" : "OPEN" %>">
              <button type="submit" class="hero-btn" style="background: <%= "OPEN".equals(myRest.getStatus()) ? "var(--color-danger)" : "var(--color-success)" %>; padding: 10px 20px; font-size: 0.85rem; box-shadow: none;">
                <%= "OPEN".equals(myRest.getStatus()) ? "Close Shop" : "Open Shop" %>
              </button>
            </form>
          </div>
        </div>

        <div class="grid-dashboard">
          <!-- Active Orders Console -->
          <div class="panel-card">
            <h3 class="panel-title">📦 Incoming Orders</h3>
            <div class="table-container">
              <% if (orders == null || orders.isEmpty()) { %>
                <p style="text-align: center; color: var(--text-muted); padding: 20px;">No orders received yet.</p>
              <% } else { %>
                <table>
                  <thead>
                    <tr>
                      <th>Order ID</th>
                      <th>Customer</th>
                      <th>Items</th>
                      <th>Total</th>
                      <th>Status Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <% for (Order o : orders) { %>
                      <tr>
                        <td><strong>#<%= o.getId() %></strong></td>
                        <td><%= o.getCustomerName() %></td>
                        <td>
                          <% for (OrderItem oi : o.getItems()) { %>
                            <div><%= oi.getQuantity() %>x <%= oi.getMenuItemName() %></div>
                          <% } %>
                        </td>
                        <td>₹<%= String.format("%.2f", o.getTotal()) %></td>
                        <td>
                          <form action="restaurant" method="post">
                            <input type="hidden" name="action" value="updateOrderStatus">
                            <input type="hidden" name="orderId" value="<%= o.getId() %>">
                            <select name="status" class="status-dropdown" onchange="this.form.submit()">
                              <option value="PLACED" <%= "PLACED".equals(o.getStatus()) ? "selected" : "" %>>Placed</option>
                              <option value="PREPARING" <%= "PREPARING".equals(o.getStatus()) ? "selected" : "" %>>Preparing</option>
                              <option value="OUT_FOR_DELIVERY" <%= "OUT_FOR_DELIVERY".equals(o.getStatus()) ? "selected" : "" %>>Out for Delivery</option>
                              <option value="ARRIVING" <%= "ARRIVING".equals(o.getStatus()) ? "selected" : "" %>>Arriving</option>
                              <option value="DELIVERED" <%= "DELIVERED".equals(o.getStatus()) ? "selected" : "" %>>Delivered</option>
                            </select>
                          </form>
                        </td>
                      </tr>
                    <% } %>
                  </tbody>
                </table>
              <% } %>
            </div>
          </div>

          <!-- Menu Items Console -->
          <div class="panel-card">
            <h3 class="panel-title">🍔 Manage Menu Offering</h3>
            
            <!-- Add Item Form -->
            <form action="restaurant" method="post" style="display: grid; grid-template-columns: 2fr 1fr 1fr 2fr auto; gap: 10px; margin-bottom: 24px;">
              <input type="hidden" name="action" value="addMenuItem">
              <input type="hidden" name="restaurantId" value="<%= myRest.getId() %>">
              
              <input type="text" name="name" class="form-input" placeholder="Dish Name" required>
              <input type="number" step="0.01" name="price" class="form-input" placeholder="Price (₹)" required>
              <select name="category" class="filter-select" style="border-radius: var(--radius-md);">
                <option value="snacks">Snacks</option>
                <option value="maggi">Maggi</option>
                <option value="burgers">Burgers</option>
                <option value="pizza">Pizza</option>
                <option value="drinks">Drinks</option>
              </select>
              <input type="text" name="imageUrl" class="form-input" placeholder="Image URL (optional)">
              <button type="submit" class="checkout-btn" style="margin-top: 0; padding: 0 16px;">Add</button>
            </form>

            <div class="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Dish</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  <% for (MenuItem item : menuItems) { %>
                    <tr>
                      <td><%= item.getName() %></td>
                      <td style="text-transform: capitalize;"><%= item.getCategory() %></td>
                      <td>₹<%= String.format("%.2f", item.getPrice()) %></td>
                      <td>
                        <form action="restaurant" method="post" style="display: inline;">
                          <input type="hidden" name="action" value="deleteMenuItem">
                          <input type="hidden" name="itemId" value="<%= item.getId() %>">
                          <button type="submit" style="color: var(--color-danger); font-weight: 700; background: none; border: none; cursor: pointer;">Delete</button>
                        </form>
                      </td>
                    </tr>
                  <% } %>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      <% } %>
    </div>
  </section>

</body>
</html>
