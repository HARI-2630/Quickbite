package com.quickbite.servlets;

import com.quickbite.dao.UserDAO;
import com.quickbite.dao.OtpDAO;
import com.quickbite.models.User;
import com.quickbite.models.OtpSession;
import com.quickbite.security.Config;
import com.quickbite.security.SmsEmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Random;

@WebServlet("/google-login")
public class GoogleLoginServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private OtpDAO otpDAO = new OtpDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String clientId = Config.get("GOOGLE_CLIENT_ID");
        String clientSecret = Config.get("GOOGLE_CLIENT_SECRET");
        String redirectUri = Config.get("GOOGLE_REDIRECT_URI");

        String code = request.getParameter("code");
        String mockEmail = request.getParameter("mock_email");

        // 1. Google OAuth Simulation Mode (Triggered if GOOGLE_CLIENT_ID is missing)
        if (clientId == null || clientId.trim().isEmpty() || "demo".equalsIgnoreCase(clientId)) {
            if (mockEmail == null || mockEmail.trim().isEmpty()) {
                // Render a mock page for Google Account selection
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().write(
                    "<!DOCTYPE html>" +
                    "<html><head><title>Google Sign-In Sandbox</title>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "<style>" +
                    "body { background: #0f0f13; color: #f3f4f6; font-family: 'Segoe UI', system-ui, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }" +
                    ".card { background: rgba(255, 255, 255, 0.05); padding: 2.5rem; border-radius: 16px; backdrop-filter: blur(12px); border: 1px solid rgba(255,255,255,0.08); width: 360px; text-align: center; box-shadow: 0 10px 40px rgba(0,0,0,0.4); }" +
                    "h2 { margin-bottom: 0.5rem; color: #ff5e3a; font-weight: 600; }" +
                    "p { font-size: 0.85rem; color: #9ca3af; margin-bottom: 2rem; line-height: 1.4; }" +
                    "input { width: 100%; padding: 0.8rem; border-radius: 8px; border: 1px solid rgba(255,255,255,0.15); background: rgba(0,0,0,0.3); color: #fff; margin-bottom: 1.2rem; box-sizing: border-box; font-size: 0.9rem; }" +
                    "input:focus { border-color: #ff5e3a; outline: none; }" +
                    "button { width: 100%; padding: 0.8rem; border: none; border-radius: 8px; background: #ff5e3a; color: white; font-weight: bold; cursor: pointer; transition: 0.2s; font-size: 0.9rem; }" +
                    "button:hover { background: #e04d2b; transform: translateY(-1px); }" +
                    "</style></head><body>" +
                    "<div class='card'>" +
                    "<h2>Google Sign-In Sandbox</h2>" +
                    "<p>Simulating OAuth 2.0 flow because GOOGLE_CLIENT_ID is not configured in .env.</p>" +
                    "<form action='google-login' method='GET'>" +
                    "<input type='email' name='mock_email' placeholder='name@gmail.com' required value='recruiter@gmail.com'>" +
                    "<button type='submit'>Continue Mock Google Sign-In</button>" +
                    "</form></div></body></html>"
                );
                return;
            } else {
                // Execute simulated auth process
                processGoogleUser(mockEmail.trim(), "Enterprise Recruiter", "simulated_google_id_" + mockEmail.hashCode(), "https://api.dicebear.com/7.x/bottts/svg?seed=" + mockEmail, response, request);
                return;
            }
        }

        // 2. Redirect to Google Authorization Endpoint
        if (code == null || code.trim().isEmpty()) {
            String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                    + "?client_id=" + clientId
                    + "&redirect_uri=" + redirectUri
                    + "&response_type=code"
                    + "&scope=" + java.net.URLEncoder.encode("openid email profile", "UTF-8")
                    + "&state=quickbite_oauth_state";
            response.sendRedirect(googleAuthUrl);
            return;
        }

        // 3. Callback Code Exchange
        try {
            String tokenUrl = "https://oauth2.googleapis.com/token";
            URL url = new URL(tokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String params = "code=" + code
                    + "&client_id=" + clientId
                    + "&client_secret=" + clientSecret
                    + "&redirect_uri=" + redirectUri
                    + "&grant_type=authorization_code";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                response.sendRedirect("index.jsp?error=google_auth_failed");
                return;
            }

            StringBuilder jsonResponse = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }
            }

            String accessToken = extractJsonField(jsonResponse.toString(), "access_token");
            if (accessToken == null) {
                response.sendRedirect("index.jsp?error=google_token_failed");
                return;
            }

            // Query Profile Endpoint
            URL profileUrl = new URL("https://www.googleapis.com/oauth2/v3/userinfo");
            HttpURLConnection profileConn = (HttpURLConnection) profileUrl.openConnection();
            profileConn.setRequestProperty("Authorization", "Bearer " + accessToken);

            StringBuilder profileJson = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(profileConn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    profileJson.append(line);
                }
            }

            String email = extractJsonField(profileJson.toString(), "email");
            String name = extractJsonField(profileJson.toString(), "name");
            String sub = extractJsonField(profileJson.toString(), "sub"); 
            String picture = extractJsonField(profileJson.toString(), "picture");

            if (email == null || sub == null) {
                response.sendRedirect("index.jsp?error=google_profile_failed");
                return;
            }

            processGoogleUser(email, name, sub, picture, response, request);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("index.jsp?error=google_exception");
        }
    }

    private void processGoogleUser(String email, String name, String googleId, String picture, HttpServletResponse response, HttpServletRequest request) 
            throws IOException {
        
        User user = userDAO.getUserByGoogleId(googleId);
        if (user == null) {
            // Attempt email linking
            user = userDAO.getUserByEmail(email);
            if (user != null) {
                user.setGoogleId(googleId);
                if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
                    user.setAvatarUrl(picture);
                }
                userDAO.updateUserProfile(user);
            } else {
                // Register brand new user (defaulting to CUSTOMER role)
                user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPassword(com.quickbite.security.PasswordHash.hash(Double.toString(Math.random()))); 
                user.setRole("CUSTOMER");
                user.setGoogleId(googleId);
                user.setAvatarUrl(picture);
                user.setStatus("ACTIVE");
                
                boolean registered = userDAO.registerUser(user);
                if (registered) {
                    user = userDAO.getUserByGoogleId(googleId);
                    SmsEmailService.sendWelcomeEmail(email, name);
                } else {
                    response.sendRedirect("index.jsp?error=registration_failed");
                    return;
                }
            }
        }

        // Active/Blocked status validation
        if ("BLOCKED".equalsIgnoreCase(user.getStatus())) {
            response.sendRedirect("index.jsp?error=blocked");
            return;
        }

        // Dispatch secondary Gmail OTP code
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        long now = System.currentTimeMillis();

        OtpSession session = new OtpSession();
        session.setPhoneOrEmail(email);
        session.setOtpCode(otpCode);
        session.setResendCount(1);
        session.setLastRequestedAt(new Timestamp(now));
        session.setExpiresAt(new Timestamp(now + 5 * 60 * 1000)); // 5 mins expiration
        session.setVerified(false);

        otpDAO.saveSession(session);
        boolean sent = SmsEmailService.sendEmailOtp(email, otpCode);

        // Redirect to index page to open the OTP dialog
        String redirectUrl = "index.jsp?action=verifyGoogleOtp&email=" + java.net.URLEncoder.encode(email, "UTF-8");
        if (!sent) {
            redirectUrl += "&demoOtp=" + otpCode;
        }
        response.sendRedirect(redirectUrl);
    }

    private String extractJsonField(String json, String fieldName) {
        String keyPattern = "\"" + fieldName + "\":";
        int startIdx = json.indexOf(keyPattern);
        if (startIdx == -1) {
            return null;
        }
        startIdx += keyPattern.length();
        
        char firstChar = json.charAt(startIdx);
        if (firstChar == '"') {
            int endIdx = json.indexOf('"', startIdx + 1);
            if (endIdx != -1) {
                return json.substring(startIdx + 1, endIdx);
            }
        } else {
            int endIdx = json.indexOf(',', startIdx);
            if (endIdx == -1) {
                endIdx = json.indexOf('}', startIdx);
            }
            if (endIdx != -1) {
                return json.substring(startIdx, endIdx).trim();
            }
        }
        return null;
    }
}
