<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.quickbite.models.User" %>
<%@ page import="com.quickbite.models.AuditLog" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/index.jsp?error=login_required");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Profile Dashboard</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    .dashboard-container {
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 32px;
      margin-top: 40px;
      margin-bottom: 60px;
    }
    .profile-card {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 32px 24px;
      text-align: center;
      height: fit-content;
      box-shadow: var(--shadow-sm);
    }
    .profile-avatar-wrapper {
      position: relative;
      width: 120px;
      height: 120px;
      margin: 0 auto 16px auto;
    }
    .profile-avatar {
      width: 120px;
      height: 120px;
      border-radius: var(--radius-full);
      object-fit: cover;
      border: 3px solid var(--color-primary);
      box-shadow: var(--shadow-md);
      background: var(--bg-secondary);
    }
    .avatar-upload-label {
      position: absolute;
      bottom: 0;
      right: 0;
      background: var(--color-primary);
      color: #fff;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.9rem;
      cursor: pointer;
      box-shadow: var(--shadow-sm);
      transition: transform 0.2s;
    }
    .avatar-upload-label:hover {
      transform: scale(1.1);
    }
    .profile-role-badge {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 800;
      text-transform: uppercase;
      padding: 4px 12px;
      border-radius: var(--radius-full);
      background: rgba(var(--color-primary-rgb), 0.1);
      color: var(--color-primary);
      margin-top: 8px;
    }
    .dashboard-content {
      display: flex;
      flex-direction: column;
      gap: 32px;
    }
    .settings-section {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 30px;
      box-shadow: var(--shadow-sm);
    }
    .section-title {
      font-size: 1.25rem;
      font-weight: 850;
      margin-bottom: 24px;
      display: flex;
      align-items: center;
      gap: 10px;
      color: var(--text-primary);
      border-bottom: 1px dashed var(--border-color);
      padding-bottom: 12px;
    }
    .logs-table-wrapper {
      overflow-x: auto;
      border: 1px solid var(--border-color);
      border-radius: var(--radius-md);
    }
    .logs-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.88rem;
    }
    .logs-table th {
      background: var(--bg-secondary);
      color: var(--text-primary);
      padding: 14px 18px;
      font-weight: 750;
    }
    .logs-table td {
      padding: 14px 18px;
      border-top: 1px solid var(--border-color);
      color: var(--text-secondary);
    }
    .logs-table tr:hover {
      background: rgba(255, 255, 255, 0.02);
    }
    .lang-switcher {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-bottom: 20px;
    }
    .lang-btn {
      background: var(--bg-card);
      border: 1px solid var(--border-color);
      padding: 6px 12px;
      border-radius: 4px;
      font-size: 0.8rem;
      cursor: pointer;
      font-weight: 600;
      color: var(--text-secondary);
      transition: 0.2s;
    }
    .lang-btn.active {
      border-color: var(--color-primary);
      color: var(--color-primary);
      background: rgba(var(--color-primary-rgb), 0.05);
    }
    
    @media (max-width: 992px) {
      .dashboard-container {
        grid-template-columns: 1fr;
      }
    }
  </style>
</head>
<body>

  <!-- Navigation Bar -->
  <nav class="navbar glass">
    <div class="container">
      <a href="index.jsp" class="logo"><span>⚡</span> QuickBite</a>
      
      <div class="nav-actions">
        <button id="theme-toggle" class="icon-btn" aria-label="Toggle Dark Mode" style="margin-right: 12px;">🌙</button>
        <span style="font-size: 0.88rem; font-weight: 750; color: var(--text-secondary); margin-right: 20px;">
          Hi, <c:out value="${user.name}"/>
        </span>
        <a href="auth?action=logout" class="checkout-btn" style="padding: 8px 18px; font-size: 0.85rem; background: var(--border-color); color: var(--text-primary);">
          Logout
        </a>
      </div>
    </div>
  </nav>

  <div class="nav-spacer"></div>

  <main class="container" style="padding: 20px 0;">
    <!-- Language Switcher -->
    <div class="lang-switcher">
      <button class="lang-btn active" id="btn-en" onclick="setLanguage('en')">🇬🇧 English</button>
      <button class="lang-btn" id="btn-hi" onclick="setLanguage('hi')">🇮🇳 Hindi (हिन्दी)</button>
    </div>

    <div class="dashboard-container">
      
      <!-- Left Sidebar: Profile Overview -->
      <aside class="profile-card">
        <form action="dashboard/profile" method="post" enctype="multipart/form-data">
          <input type="hidden" name="action" value="updateProfile">
          
          <div class="profile-avatar-wrapper">
            <img class="profile-avatar" id="avatar-preview" src="${not empty user.avatarUrl ? user.avatarUrl : 'https://api.dicebear.com/7.x/bottts/svg?seed=' + user.email}" alt="Avatar">
            <label for="avatar-input" class="avatar-upload-label" title="Change Avatar">📷</label>
            <input type="file" id="avatar-input" name="avatarFile" style="display: none;" onchange="previewAvatar(this)">
          </div>

          <h3 style="font-size: 1.35rem; font-weight: 900; margin-bottom: 4px;" id="lbl-sidebar-name"><c:out value="${user.name}"/></h3>
          <p style="color: var(--text-muted); font-size: 0.85rem;"><c:out value="${user.email}"/></p>
          <span class="profile-role-badge"><c:out value="${user.role}"/></span>

          <div style="margin-top: 32px; border-top: 1px dashed var(--border-color); padding-top: 24px; text-align: left; font-size: 0.85rem; color: var(--text-secondary);">
            <div style="margin-bottom: 12px;">
              <strong id="lbl-sidebar-phone">Phone:</strong> <c:out value="${not empty user.phone ? user.phone : 'Not set'}"/>
            </div>
            <div style="margin-bottom: 12px;">
              <strong id="lbl-sidebar-status">Status:</strong> <span style="color: #2ed573; font-weight: 700;"><c:out value="${user.status}"/></span>
            </div>
            <div>
              <strong id="lbl-sidebar-joined">Joined:</strong> <span style="font-size: 0.78rem;"><c:out value="${user.createdAt}"/></span>
            </div>
          </div>
          
          <button type="submit" class="checkout-btn" style="width: 100%; margin-top: 28px; padding: 12px;" id="btn-save-avatar">
            Save Avatar ⚡
          </button>
        </form>
      </aside>

      <!-- Right Panel: Forms & History -->
      <section class="dashboard-content">
        
        <!-- Success/Error Notices -->
        <c:if test="${not empty profileSuccess}">
          <div class="alert-box alert-success">${profileSuccess}</div>
        </c:if>
        <c:if test="${not empty profileError}">
          <div class="alert-box alert-error">${profileError}</div>
        </c:if>
        <c:if test="${not empty passwordSuccess}">
          <div class="alert-box alert-success">${passwordSuccess}</div>
        </c:if>
        <c:if test="${not empty passwordError}">
          <div class="alert-box alert-error">${passwordError}</div>
        </c:if>

        <!-- Edit Profile Details -->
        <div class="settings-section">
          <h2 class="section-title" id="sec-profile-details">👤 Profile Details</h2>
          
          <form action="dashboard/profile" method="post" enctype="multipart/form-data">
            <input type="hidden" name="action" value="updateProfile">
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
              <div class="form-group">
                <label for="profile-name" id="lbl-name">Full Name</label>
                <input type="text" id="profile-name" name="name" class="form-input" value="<c:out value='${user.name}'/>" required>
              </div>
              <div class="form-group">
                <label for="profile-email" id="lbl-email">Email Address</label>
                <input type="email" id="profile-email" name="email" class="form-input" value="<c:out value='${user.email}'/>" required>
              </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 18px;">
              <div class="form-group">
                <label for="profile-phone" id="lbl-phone">Phone Number (E.164)</label>
                <input type="tel" id="profile-phone" name="phone" class="form-input" value="<c:out value='${user.phone}'/>" placeholder="+919876543210">
              </div>
              <div class="form-group">
                <label id="lbl-role">Account Role</label>
                <input type="text" class="form-input" value="<c:out value='${user.role}'/>" disabled style="opacity: 0.6; cursor: not-allowed;">
              </div>
            </div>

            <button type="submit" class="checkout-btn" style="margin-top: 28px; width: 180px;" id="btn-save-details">
              Update Profile ⚡
            </button>
          </form>
        </div>

        <!-- Change Password Section -->
        <div class="settings-section">
          <h2 class="section-title" id="sec-change-password">🔑 Change Password</h2>
          
          <form action="dashboard/profile" method="post">
            <input type="hidden" name="action" value="changePassword">
            
            <div class="form-group">
              <label for="current-pwd" id="lbl-curr-pwd">Current Password</label>
              <input type="password" id="current-pwd" name="currentPassword" class="form-input" placeholder="••••••••" required>
            </div>
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-top: 18px;">
              <div class="form-group">
                <label for="new-pwd" id="lbl-new-pwd">New Password</label>
                <input type="password" id="new-pwd" name="newPassword" class="form-input" placeholder="••••••••" minlength="6" required>
              </div>
              <div class="form-group">
                <label for="confirm-pwd" id="lbl-conf-pwd">Confirm New Password</label>
                <input type="password" id="confirm-pwd" name="confirmPassword" class="form-input" placeholder="••••••••" minlength="6" required>
              </div>
            </div>

            <button type="submit" class="checkout-btn" style="margin-top: 28px; width: 180px;" id="btn-save-pwd">
              Change Password 🔒
            </button>
          </form>
        </div>

        <!-- Security Activity & Audit Logs -->
        <div class="settings-section">
          <h2 class="section-title" id="sec-activity-log">🛡️ Security & Login History</h2>
          
          <div class="logs-table-wrapper">
            <table class="logs-table">
              <thead>
                <tr>
                  <th id="th-time">Time</th>
                  <th id="th-action">Action</th>
                  <th id="th-ip">IP Address</th>
                  <th id="th-agent">Client Metadata (User Agent)</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="log" items="${auditLogs}">
                  <tr>
                    <td><c:out value="${log.createdAt}"/></td>
                    <td><c:out value="${log.action}"/></td>
                    <td><code><c:out value="${log.ipAddress}"/></code></td>
                    <td style="font-size: 0.78rem; max-width: 250px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="<c:out value='${log.userAgent}'/>">
                      <c:out value="${log.userAgent}"/>
                    </td>
                  </tr>
                </c:forEach>
                <c:if test="${empty auditLogs}">
                  <tr>
                    <td colspan="4" style="text-align: center;" id="td-no-logs">No activity records found.</td>
                  </tr>
                </c:if>
              </tbody>
            </table>
          </div>
        </div>

      </section>

    </div>
  </main>

  <script>
    // Theme Mode Controller
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

    // Image preview helper
    function previewAvatar(input) {
      if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
          document.getElementById('avatar-preview').src = e.target.result;
        };
        reader.readAsDataURL(input.files[0]);
      }
    }

    // Dynamic Multi-Language Strings
    const translations = {
      en: {
        "sec-profile-details": "👤 Profile Details",
        "sec-change-password": "🔑 Change Password",
        "sec-activity-log": "🛡️ Security & Login History",
        "lbl-name": "Full Name",
        "lbl-email": "Email Address",
        "lbl-phone": "Phone Number (E.164)",
        "lbl-role": "Account Role",
        "lbl-curr-pwd": "Current Password",
        "lbl-new-pwd": "New Password",
        "lbl-conf-pwd": "Confirm New Password",
        "btn-save-avatar": "Save Avatar ⚡",
        "btn-save-details": "Update Profile ⚡",
        "btn-save-pwd": "Change Password 🔒",
        "th-time": "Time",
        "th-action": "Action",
        "th-ip": "IP Address",
        "th-agent": "Client Metadata (User Agent)",
        "td-no-logs": "No activity records found.",
        "lbl-sidebar-phone": "Phone:",
        "lbl-sidebar-status": "Status:",
        "lbl-sidebar-joined": "Joined:"
      },
      hi: {
        "sec-profile-details": "👤 प्रोफ़ाइल विवरण",
        "sec-change-password": "🔑 पासवर्ड बदलें",
        "sec-activity-log": "🛡️ सुरक्षा और लॉगिन इतिहास",
        "lbl-name": "पूरा नाम",
        "lbl-email": "ईमेल पता",
        "lbl-phone": "फ़ोन नंबर (E.164)",
        "lbl-role": "खाता भूमिका",
        "lbl-curr-pwd": "वर्तमान पासवर्ड",
        "lbl-new-pwd": "नया पासवर्ड",
        "lbl-conf-pwd": "नए पासवर्ड की पुष्टि करें",
        "btn-save-avatar": "अवतार सहेजें ⚡",
        "btn-save-details": "प्रोफ़ाइल अपडेट करें ⚡",
        "btn-save-pwd": "पासवर्ड बदलें 🔒",
        "th-time": "समय",
        "th-action": "कार्रवाई",
        "th-ip": "आईपी पता",
        "th-agent": "क्लाइंट मेटाडेटा (यूज़र एजेंट)",
        "td-no-logs": "कोई गतिविधि रिकॉर्ड नहीं मिला।",
        "lbl-sidebar-phone": "फ़ोन:",
        "lbl-sidebar-status": "स्थिति:",
        "lbl-sidebar-joined": "शामिल हुए:"
      }
    };

    function setLanguage(lang) {
      localStorage.setItem('qb-lang', lang);
      
      // Update Button states
      document.getElementById('btn-en').classList.toggle('active', lang === 'en');
      document.getElementById('btn-hi').classList.toggle('active', lang === 'hi');

      const strings = translations[lang];
      for (const id in strings) {
        const el = document.getElementById(id);
        if (el) {
          el.innerText = strings[id];
        }
      }
    }

    // Auto load saved language
    window.onload = () => {
      const savedLang = localStorage.getItem('qb-lang') || 'en';
      setLanguage(savedLang);
    };
  </script>
</body>
</html>
