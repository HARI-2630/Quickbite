<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.models.User" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null || !"SUPER_ADMIN".equalsIgnoreCase(user.getRole())) {
        response.sendRedirect(request.getContextPath() + "/index.jsp?error=unauthorized");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Admin Control Panel</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    .admin-container {
      margin-top: 40px;
      margin-bottom: 60px;
    }
    .admin-card {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 30px;
      box-shadow: var(--shadow-sm);
    }
    .admin-title {
      font-size: 1.5rem;
      font-weight: 900;
      margin-bottom: 30px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 16px;
    }
    .users-table-wrapper {
      overflow-x: auto;
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
      background: var(--bg-secondary);
    }
    .users-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.88rem;
    }
    .users-table th {
      background: var(--bg-card);
      color: var(--text-primary);
      padding: 16px 20px;
      font-weight: 800;
      border-bottom: 1px solid var(--border-color);
    }
    .users-table td {
      padding: 16px 20px;
      border-top: 1px solid var(--border-color);
      color: var(--text-secondary);
      vertical-align: middle;
    }
    .users-table tr:hover {
      background: rgba(255, 255, 255, 0.015);
    }
    .user-avatar-mini {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      object-fit: cover;
      vertical-align: middle;
      margin-right: 10px;
      border: 1px solid var(--color-primary);
      background: var(--bg-card);
    }
    .status-badge {
      display: inline-block;
      padding: 4px 10px;
      border-radius: var(--radius-full);
      font-size: 0.72rem;
      font-weight: 800;
      text-transform: uppercase;
    }
    .status-active {
      background: rgba(46, 213, 115, 0.1);
      color: #2ed573;
    }
    .status-blocked {
      background: rgba(255, 71, 87, 0.1);
      color: #ff4757;
    }
    .action-group {
      display: flex;
      gap: 8px;
      align-items: center;
    }
    .action-btn {
      padding: 6px 12px;
      border-radius: 4px;
      font-size: 0.78rem;
      font-weight: 700;
      border: 1px solid var(--border-color);
      background: var(--bg-card);
      color: var(--text-primary);
      cursor: pointer;
      transition: all var(--transition-fast);
    }
    .action-btn:hover {
      border-color: var(--text-primary);
    }
    .action-block {
      background: rgba(255, 165, 2, 0.08);
      color: #ffa502;
      border-color: rgba(255, 165, 2, 0.2);
    }
    .action-block:hover {
      background: #ffa502;
      color: white;
    }
    .action-delete {
      background: rgba(255, 71, 87, 0.08);
      color: #ff4757;
      border-color: rgba(255, 71, 87, 0.2);
    }
    .action-delete:hover {
      background: #ff4757;
      color: white;
    }
    .role-select {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      color: var(--text-primary);
      padding: 6px 10px;
      border-radius: 4px;
      font-size: 0.8rem;
      font-weight: 600;
      cursor: pointer;
      transition: border var(--transition-fast);
    }
    .role-select:focus {
      border-color: var(--color-primary);
      outline: none;
    }
  </style>
</head>
<body>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="index.jsp" class="logo"><span>⚡</span> QuickBite Admin</a>
      
      <div class="nav-actions">
        <button id="theme-toggle" class="icon-btn" aria-label="Toggle Dark Mode" style="margin-right: 12px;">🌙</button>
        <span style="font-size: 0.88rem; font-weight: 750; color: var(--text-secondary); margin-right: 20px;">
          Console: <c:out value="${user.name}"/>
        </span>
        <a href="auth?action=logout" class="checkout-btn" style="padding: 8px 18px; font-size: 0.85rem; background: var(--border-color); color: var(--text-primary);">
          Logout
        </a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <main class="container admin-container">
    
    <!-- Success/Error Alerts -->
    <c:if test="${not empty adminSuccess}">
      <div class="alert-box alert-success" style="margin-bottom: 24px;">${adminSuccess}</div>
    </c:if>
    <c:if test="${not empty adminError}">
      <div class="alert-box alert-error" style="margin-bottom: 24px;">${adminError}</div>
    </c:if>

    <!-- Admin Console Card -->
    <div class="admin-card">
      <div class="admin-title">
        <span>👥 User Directory & Role Control (RBAC)</span>
        <a href="dashboard/profile" style="font-size: 0.85rem; color: var(--color-primary); font-weight: 750; text-decoration: none;">View My Activity Logs →</a>
      </div>

      <div class="users-table-wrapper">
        <table class="users-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>User Details</th>
              <th>Email Address</th>
              <th>Phone Number</th>
              <th>System Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="u" items="${userList}">
              <tr>
                <td><code>#<c:out value="${u.id}"/></code></td>
                <td>
                  <img class="user-avatar-mini" src="${not empty u.avatarUrl ? u.avatarUrl : 'https://api.dicebear.com/7.x/bottts/svg?seed=' + u.email}" alt="Avatar">
                  <span style="font-weight: 750; color: var(--text-primary);"><c:out value="${u.name}"/></span>
                </td>
                <td><c:out value="${u.email}"/></td>
                <td><c:out value="${not empty u.phone ? u.phone : '-'}"/></td>
                <td>
                  <!-- Role Modification Form -->
                  <form action="admin/users" method="post" style="margin: 0; display: inline-block;">
                    <input type="hidden" name="action" value="updateRole">
                    <input type="hidden" name="userId" value="${u.id}">
                    <select name="role" class="role-select" onchange="this.form.submit()">
                      <option value="CUSTOMER" ${u.role == 'CUSTOMER' ? 'selected' : ''}>Customer</option>
                      <option value="RESTAURANT_ADMIN" ${u.role == 'RESTAURANT_ADMIN' ? 'selected' : ''}>Restaurant Manager</option>
                      <option value="SUPER_ADMIN" ${u.role == 'SUPER_ADMIN' ? 'selected' : ''}>Super Admin</option>
                    </select>
                  </form>
                </td>
                <td>
                  <span class="status-badge ${'BLOCKED' == u.status ? 'status-blocked' : 'status-active'}">
                    <c:out value="${u.status}"/>
                  </span>
                </td>
                <td>
                  <div class="action-group">
                    <!-- Toggle block status form -->
                    <form action="admin/users" method="post" style="margin: 0;">
                      <input type="hidden" name="action" value="toggleBlock">
                      <input type="hidden" name="userId" value="${u.id}">
                      <button type="submit" class="action-btn action-block">
                        <c:choose>
                          <c:when test="${u.status == 'BLOCKED'}">Unblock</c:when>
                          <c:otherwise>Block</c:otherwise>
                        </c:choose>
                      </button>
                    </form>

                    <!-- Delete user form -->
                    <form action="admin/users" method="post" style="margin: 0;" onsubmit="return confirm('Are you sure you want to permanently delete this user account?')">
                      <input type="hidden" name="action" value="delete">
                      <input type="hidden" name="userId" value="${u.id}">
                      <button type="submit" class="action-btn action-delete">Delete</button>
                    </form>
                  </div>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty userList}">
              <tr>
                <td colspan="7" style="text-align: center; padding: 30px; color: var(--text-muted);">No users found in database.</td>
              </tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </main>

  <script>
    // Theme Mode Switcher
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
  </script>
</body>
</html>
