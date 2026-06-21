package com.quickbite.filters;

import com.quickbite.security.RateLimiter;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "RateLimitFilter", urlPatterns = {"/auth", "/auth/otp", "/google-login"})
public class RateLimitFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract IP address from headers or connection
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        // Apply rate limiter check
        if (!RateLimiter.isAllowed(ipAddress)) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json; charset=UTF-8");
            httpResponse.getWriter().write("{\"success\":false,\"error\":\"Too many requests. Please try again in a few seconds.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
