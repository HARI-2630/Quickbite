<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>QuickBite | Server Error (500)</title>
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
      width: 440px;
      box-shadow: 0 20px 50px rgba(0,0,0,0.5);
      position: relative;
    }
    .error-code {
      font-size: 7rem;
      font-weight: 950;
      color: #ffa502;
      line-height: 1;
      margin: 0;
      font-family: system-ui, sans-serif;
      background: linear-gradient(135deg, #ffa502, #ff7f50);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      filter: drop-shadow(0 4px 10px rgba(255, 165, 2, 0.2));
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
    .error-chef {
      font-size: 3.5rem;
      margin-top: 20px;
      animation: rotate-chef 4s ease-in-out infinite;
      display: inline-block;
    }
    @keyframes rotate-chef {
      0% { transform: rotate(0); }
      50% { transform: rotate(15deg) scale(1.1); }
      100% { transform: rotate(0); }
    }
  </style>
</head>
<body>
  <div class="error-container">
    <div class="error-code">500</div>
    <h2>Kitchen Overheat!</h2>
    <p>Our server encountered an internal error while preparing your request. The engineering chef is looking into it.</p>
    <a href="index.jsp" class="btn-home">Return to Safety ⚡</a>
    <div style="margin-top: 24px;">
      <span class="error-chef">👨‍🍳💥</span>
    </div>
  </div>
</body>
</html>
