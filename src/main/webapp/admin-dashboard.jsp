<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.dao.RestaurantDAO, com.quickbite.dao.UserDAO, com.quickbite.dao.OrderDAO, com.quickbite.dao.AuditLogDAO, com.quickbite.models.Restaurant, com.quickbite.models.User, com.quickbite.models.AuditLog, java.util.List, java.util.Map" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null || !"SUPER_ADMIN".equals(currentUser.getRole())) {
        response.sendRedirect("index.jsp");
        return;
    }

    UserDAO userDAO = new UserDAO();
    RestaurantDAO restDAO = new RestaurantDAO();
    OrderDAO orderDAO = new OrderDAO();
    AuditLogDAO auditDAO = new AuditLogDAO();

    List<User> allUsers = userDAO.getAllUsers();
    List<Restaurant> allRestaurants = restDAO.getAllRestaurants();
    Map<String, Object> stats = orderDAO.getAdminStatistics();
    List<AuditLog> auditLogs = auditDAO.getAllLogs();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Super Admin Console</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    .stats-row {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 20px;
      margin-bottom: 40px;
    }
    .stat-card {
      background-color: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      padding: 24px;
      text-align: center;
      box-shadow: var(--shadow-sm);
    }
    .stat-val {
      font-size: 2rem;
      font-weight: 800;
      color: var(--color-primary);
      margin-top: 8px;
    }
    .stat-lbl {
      font-size: 0.82rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.5px;
      color: var(--text-secondary);
    }
    .admin-grid {
      display: grid;
      grid-template-columns: 1.1fr 0.9fr;
      gap: 30px;
    }
    .panel-card {
      background-color: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 30px;
      box-shadow: var(--shadow-md);
      margin-bottom: 30px;
    }
    .panel-title {
      font-size: 1.35rem;
      font-weight: 800;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid var(--border-color);
    }
    table {
      width: 100%;
      border-collapse: collapse;
    }
    th, td {
      padding: 12px;
      border-bottom: 1px solid var(--border-color);
      text-align: left;
      font-size: 0.88rem;
    }
    th {
      font-weight: 700;
      color: var(--text-secondary);
    }
  </style>
</head>
<body>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="admin-dashboard.jsp" class="logo">
        <span>⚡</span> QuickBite Super Admin
      </a>
      <div class="nav-actions">
        <span class="user-greeting">System Admin: <%= currentUser.getName() %></span>
        <a href="auth?action=logout" class="icon-btn" title="Logout">🚪</a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <section style="padding: 40px 0;">
    <div class="container">
      
      <!-- Stats Blocks -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-lbl">💰 Total Revenue</div>
          <div class="stat-val">₹<%= String.format("%.2f", stats.getOrDefault("totalRevenue", 0.0)) %></div>
        </div>
        <div class="stat-card">
          <div class="stat-lbl">📦 Total Orders</div>
          <div class="stat-val"><%= stats.getOrDefault("totalOrders", 0) %></div>
        </div>
        <div class="stat-card">
          <div class="stat-lbl">👤 Registered Users</div>
          <div class="stat-val"><%= stats.getOrDefault("totalUsers", 0) %></div>
        </div>
        <div class="stat-card">
          <div class="stat-lbl">🏪 Restaurants</div>
          <div class="stat-val"><%= stats.getOrDefault("totalRestaurants", 0) %></div>
        </div>
      </div>

      <div class="admin-grid">
        <!-- Users Management Console -->
        <div class="panel-card">
          <h3 class="panel-title">👤 Manage Platform Users</h3>
          <div style="overflow-x: auto;">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <% for (User u : allUsers) { %>
                  <tr>
                    <td><%= u.getId() %></td>
                    <td><strong><%= u.getName() %></strong></td>
                    <td><%= u.getEmail() %></td>
                    <td>
                      <form action="admin" method="post" style="display: inline;">
                        <input type="hidden" name="action" value="updateUserRole">
                        <input type="hidden" name="userId" value="<%= u.getId() %>">
                        <select name="role" class="status-dropdown" onchange="this.form.submit()">
                          <option value="CUSTOMER" <%= "CUSTOMER".equals(u.getRole()) ? "selected" : "" %>>Customer</option>
                          <option value="RESTAURANT_ADMIN" <%= "RESTAURANT_ADMIN".equals(u.getRole()) ? "selected" : "" %>>Restaurant Admin</option>
                          <option value="SUPER_ADMIN" <%= "SUPER_ADMIN".equals(u.getRole()) ? "selected" : "" %>>Super Admin</option>
                        </select>
                      </form>
                    </td>
                    <td>
                      <% if (u.getId() != currentUser.getId()) { %>
                        <form action="admin" method="post" style="display: inline;">
                          <input type="hidden" name="action" value="deleteUser">
                          <input type="hidden" name="userId" value="<%= u.getId() %>">
                          <button type="submit" style="color: var(--color-danger); font-weight: 700; background: none; border: none; cursor: pointer;">Delete</button>
                        </form>
                      <% } else { %>
                        <span style="color: var(--text-muted); font-style: italic;">Self</span>
                      <% } %>
                    </td>
                  </tr>
                <% } %>
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <!-- Add Restaurant Form -->
          <div class="panel-card">
            <h3 class="panel-title">🏪 List New Restaurant</h3>
            <form action="admin" method="post" style="display: flex; flex-direction: column; gap: 16px;">
              <input type="hidden" name="action" value="addRestaurant">
              
              <div class="form-group">
                <label>Restaurant Name</label>
                <input type="text" name="name" class="form-input" placeholder="e.g. Burger Craft & Co." required>
              </div>

              <div class="form-group">
                <label>Cuisine Category</label>
                <input type="text" name="cuisine" class="form-input" placeholder="e.g. Burgers, Pizza, Shakes" required>
              </div>

              <div class="form-group">
                <label>Assign Owner (User ID)</label>
                <select name="ownerId" class="filter-select" style="width: 100%; border-radius: var(--radius-md); padding: 12px 16px;" required>
                  <% for (User u : allUsers) { 
                      if ("RESTAURANT_ADMIN".equals(u.getRole())) {
                  %>
                    <option value="<%= u.getId() %>"><%= u.getName() %> (ID: <%= u.getId() %>)</option>
                  <% 
                      }
                     } 
                  %>
                </select>
              </div>

              <button type="submit" class="checkout-btn" style="margin-top: 10px;">
                Register Restaurant 🚀
              </button>
            </form>
          </div>

          <!-- Platform Sales Velocity visual SVG chart -->
          <div class="panel-card" style="margin-bottom: 30px;">
            <h3 class="panel-title">📊 Platform Sales Velocity</h3>
            <div style="display: flex; flex-direction: column; gap: 15px; margin-top: 10px;">
              <div style="display: flex; justify-content: space-between; font-size: 0.8rem; color: var(--text-secondary);">
                <span>Weekly Growth Rate: <strong style="color: var(--color-success);">+14.8%</strong></span>
                <span>Active Channels: <strong style="color: var(--color-primary);">Stripe / POD</strong></span>
              </div>
              <svg viewBox="0 0 400 150" style="width: 100%; height: auto; background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 10px;">
                <defs>
                  <linearGradient id="bar-grad" x1="0" y1="1" x2="0" y2="0">
                    <stop offset="0%" stop-color="var(--color-secondary)" stop-opacity="0.3" />
                    <stop offset="100%" stop-color="var(--color-primary)" />
                  </linearGradient>
                </defs>
                <line x1="40" y1="20" x2="380" y2="20" stroke="rgba(255,255,255,0.05)" stroke-width="1" />
                <line x1="40" y1="60" x2="380" y2="60" stroke="rgba(255,255,255,0.05)" stroke-width="1" />
                <line x1="40" y1="100" x2="380" y2="100" stroke="rgba(255,255,255,0.05)" stroke-width="1" />
                <line x1="40" y1="130" x2="380" y2="130" stroke="var(--border-color)" stroke-width="1.5" />
                
                <text x="35" y="25" fill="var(--text-secondary)" font-size="8" text-anchor="end">₹1500</text>
                <text x="35" y="65" fill="var(--text-secondary)" font-size="8" text-anchor="end">₹1000</text>
                <text x="35" y="105" fill="var(--text-secondary)" font-size="8" text-anchor="end">₹500</text>
                <text x="35" y="133" fill="var(--text-secondary)" font-size="8" text-anchor="end">₹0</text>

                <rect x="65" y="50" width="20" height="80" rx="3" fill="url(#bar-grad)" />
                <text x="75" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Mon</text>
                
                <rect x="110" y="30" width="20" height="100" rx="3" fill="url(#bar-grad)" />
                <text x="120" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Tue</text>
                
                <rect x="155" y="70" width="20" height="60" rx="3" fill="url(#bar-grad)" />
                <text x="165" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Wed</text>
                
                <rect x="200" y="45" width="20" height="85" rx="3" fill="url(#bar-grad)" />
                <text x="210" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Thu</text>
                
                <rect x="245" y="20" width="20" height="110" rx="3" fill="url(#bar-grad)" />
                <text x="255" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Fri</text>
                
                <rect x="290" y="15" width="20" height="115" rx="3" fill="url(#bar-grad)" />
                <text x="300" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Sat</text>
                
                <rect x="335" y="40" width="20" height="90" rx="3" fill="url(#bar-grad)" />
                <text x="345" y="143" fill="var(--text-secondary)" font-size="8" text-anchor="middle">Sun</text>
              </svg>
            </div>
          </div>

          <!-- Listed Restaurants Table -->
          <div class="panel-card">
            <h3 class="panel-title"> Listed Restaurants</h3>
            <div style="overflow-x: auto;">
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Cuisine</th>
                    <th>Status</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  <% for (Restaurant r : allRestaurants) { %>
                    <tr>
                      <td><strong><%= r.getName() %></strong></td>
                      <td><%= r.getCuisine() %></td>
                      <td>
                        <span style="color: <%= "OPEN".equals(r.getStatus()) ? "var(--color-success)" : "var(--color-danger)" %>; font-weight: 700;">
                          <%= r.getStatus() %>
                        </span>
                      </td>
                      <td>
                        <form action="admin" method="post" style="display: inline;">
                          <input type="hidden" name="action" value="deleteRestaurant">
                          <input type="hidden" name="restaurantId" value="<%= r.getId() %>">
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
      </div>

      <!-- Live Audit Trail Log Console (Full Width at Bottom) -->
      <div class="panel-card" style="margin-top: 30px;">
        <h3 class="panel-title">🛡️ Security & System Audit Trail</h3>
        <div style="max-height: 250px; overflow-y: auto; background: rgba(0,0,0,0.2); border: 1px solid var(--border-color); border-radius: var(--radius-md); font-family: monospace; font-size: 0.8rem; line-height: 1.6; padding: 15px;">
          <% if (auditLogs == null || auditLogs.isEmpty()) { %>
            <div style="color: var(--text-muted); text-align: center; padding: 20px;">No audit events recorded.</div>
          <% } else { %>
            <% for (AuditLog log : auditLogs) { %>
              <div style="margin-bottom: 8px; border-bottom: 1px solid rgba(255,255,255,0.02); padding-bottom: 6px; display: flex; justify-content: space-between; flex-wrap: wrap; gap: 10px;">
                <div>
                  <span style="color: var(--color-primary); font-weight: bold;">[<%= log.getCreatedAt() %>]</span>
                  <span style="color: var(--color-success); font-weight: bold;">USER#<%= log.getUserId() != null ? log.getUserId() : "SYSTEM" %>:</span>
                  <span style="color: var(--text-primary);"><%= log.getAction() %></span>
                </div>
                <div style="color: var(--text-muted); font-size: 0.75rem;">
                  <span>IP: <%= log.getIpAddress() %></span> | 
                  <span style="display: inline-block; max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="<%= log.getUserAgent() %>">UA: <%= log.getUserAgent() %></span>
                </div>
              </div>
            <% } %>
          <% } %>
        </div>
      </div>

    </div>
  </section>

</body>
</html>
