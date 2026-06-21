<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Page Not Found (404)</title>
  <link rel="stylesheet" href="./css/style.css">
  <script>
    (function() {
      const savedTheme = localStorage.getItem('qb-theme') || 'dark';
      document.documentElement.setAttribute('data-theme', savedTheme);
    })();
  </script>
  <style>
    body {
      background: #0f0f13;
      color: #f3f4f6;
      font-family: 'Segoe UI', system-ui, sans-serif;
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      margin: 0;
      overflow: hidden;
    }
    .error-container {
      text-align: center;
      background: rgba(255, 255, 255, 0.03);
      padding: 4rem 3rem;
      border-radius: 20px;
      backdrop-filter: blur(16px);
      border: 1px solid rgba(255,255,255,0.06);
      width: 400px;
      box-shadow: 0 20px 50px rgba(0,0,0,0.5);
      position: relative;
    }
    .error-code {
      font-size: 7rem;
      font-weight: 950;
      color: #ff4757;
      line-height: 1;
      margin: 0;
      font-family: system-ui, sans-serif;
      background: linear-gradient(135deg, #ff4757, #ff6b81);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      filter: drop-shadow(0 4px 10px rgba(255, 71, 87, 0.2));
    }
    h2 {
      font-size: 1.6rem;
      margin: 20px 0 10px 0;
      font-weight: 800;
      color: var(--text-primary);
    }
    p {
      color: var(--text-secondary);
      font-size: 0.95rem;
      margin-bottom: 30px;
      line-height: 1.5;
    }
    .btn-home {
      display: inline-block;
      padding: 12px 30px;
      background: #ff5e3a;
      color: white;
      text-decoration: none;
      font-weight: 800;
      border-radius: 8px;
      transition: all 0.2s;
      box-shadow: 0 4px 15px rgba(255, 94, 58, 0.3);
    }
    .btn-home:hover {
      background: #e04d2b;
      transform: translateY(-2px);
    }
    .scooter-courier {
      font-size: 3.5rem;
      margin-top: 20px;
      animation: drive 3s ease-in-out infinite alternate;
      display: inline-block;
    }
    @keyframes drive {
      0% { transform: translateX(-15px) rotate(-3deg); }
      100% { transform: translateX(15px) rotate(3deg); }
    }
  </style>
</head>
<body>
  <div class="error-container">
    <div class="error-code">404</div>
    <h2>Lost in Transit?</h2>
    <p>The page you are looking for has been eaten or does not exist. Let's get you back to the campus kitchen.</p>
    <a href="index.jsp" class="btn-home">Back to Safety ⚡</a>
    <div style="margin-top: 24px;">
      <span class="scooter-courier">🛵</span>
    </div>
  </div>
</body>
</html>
