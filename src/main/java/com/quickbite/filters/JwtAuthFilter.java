package com.quickbite.filters;

import com.quickbite.dao.UserDAO;
import com.quickbite.models.User;
import com.quickbite.security.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "JwtAuthFilter", urlPatterns = {"/*"})
public class JwtAuthFilter implements Filter {

    private UserDAO userDAO = new UserDAO();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String relativePath = uri.substring(contextPath.length());

        // 1. Skip static resources, landing, errors, and standard auth patterns
        if (relativePath.startsWith("/css/") || relativePath.startsWith("/js/") || 
            relativePath.startsWith("/assets/") || relativePath.equals("/index.jsp") || 
            relativePath.equals("/") || relativePath.startsWith("/auth") || 
            relativePath.equals("/error-404.jsp") || relativePath.equals("/error-500.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT credentials from HttpOnly cookies
        String accessToken = null;
        String refreshToken = null;
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_access".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("jwt_refresh".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        User currentUser = null;

        // First validate accessToken
        if (accessToken != null) {
            JwtUtil.Claims claims = JwtUtil.validateToken(accessToken);
            if (claims != null) {
                currentUser = userDAO.getUserById(claims.getUserId());
            }
        }

        // Validate refreshToken if access is absent or expired
        if (currentUser == null && refreshToken != null) {
            JwtUtil.Claims claims = JwtUtil.validateToken(refreshToken);
            if (claims != null) {
                currentUser = userDAO.getUserById(claims.getUserId());
                if (currentUser != null && !"BLOCKED".equalsIgnoreCase(currentUser.getStatus())) {
                    // Re-issue a new 15-minute access token cookie
                    String newAccessToken = JwtUtil.generateAccessToken(currentUser);
                    Cookie accessCookie = new Cookie("jwt_access", newAccessToken);
                    accessCookie.setHttpOnly(true);
                    accessCookie.setSecure(httpRequest.isSecure());
                    accessCookie.setPath("/");
                    accessCookie.setMaxAge(15 * 60); // 15 minutes
                    httpResponse.addCookie(accessCookie);
                }
            }
        }

        // 3. User block enforcement
        if (currentUser != null && "BLOCKED".equalsIgnoreCase(currentUser.getStatus())) {
            clearJwtCookies(httpResponse);
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(contextPath + "/index.jsp?error=blocked");
            return;
        }

        // 4. Populate session values
        if (currentUser != null) {
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute("user", currentUser);
            httpRequest.setAttribute("user", currentUser);
        } else {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                session.removeAttribute("user");
            }
        }

        // 5. Enforce RBAC rules
        String role = currentUser != null ? currentUser.getRole() : null;

        // Admin-only pages
        if (relativePath.contains("admin-dashboard.jsp") || relativePath.contains("admin-users.jsp") || relativePath.contains("/admin")) {
            if (!"SUPER_ADMIN".equalsIgnoreCase(role)) {
                httpResponse.sendRedirect(contextPath + "/index.jsp?error=unauthorized");
                return;
            }
        }

        // Restaurant Manager pages
        if (relativePath.contains("restaurant-dashboard.jsp")) {
            if (!"RESTAURANT_ADMIN".equalsIgnoreCase(role) && !"SUPER_ADMIN".equalsIgnoreCase(role)) {
                httpResponse.sendRedirect(contextPath + "/index.jsp?error=unauthorized");
                return;
            }
        }

        // Customer private dashboards
        if (relativePath.contains("customer-dashboard.jsp") || relativePath.contains("order-history.jsp") || relativePath.contains("order-tracking.jsp")) {
            if (!"CUSTOMER".equalsIgnoreCase(role) && !"RESTAURANT_ADMIN".equalsIgnoreCase(role) && !"SUPER_ADMIN".equalsIgnoreCase(role)) {
                httpResponse.sendRedirect(contextPath + "/index.jsp?error=login_required");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void clearJwtCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("jwt_access", "");
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("jwt_refresh", "");
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    @Override
    public void destroy() {}
}
