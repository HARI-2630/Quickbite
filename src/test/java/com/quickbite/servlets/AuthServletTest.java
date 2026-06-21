package com.quickbite.servlets;

import com.quickbite.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServletTest {

    private AuthServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @BeforeEach
    public void setUp() {
        servlet = new AuthServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        session = mock(HttpSession.class);
    }

    @Test
    public void testDoPostWithoutActionRedirects() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendRedirect("index.jsp");
        verifyNoInteractions(session);
    }

    @Test
    public void testDoPostInvalidActionRedirects() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("non_existent_action");

        servlet.doPost(request, response);

        verify(response).sendRedirect("index.jsp");
    }

    @Test
    public void testDoGetLogoutInvalidatesSessionAndClearsCookies() throws ServletException, IOException {
        when(request.getParameter("action")).thenReturn("logout");
        when(request.getSession(false)).thenReturn(session);
        
        User testUser = new User();
        testUser.setId(42);
        testUser.setName("Test User");
        testUser.setEmail("test@quickbite.com");
        testUser.setRole("CUSTOMER");
        when(session.getAttribute("user")).thenReturn(testUser);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("Mozilla JUnit");

        servlet.doGet(request, response);

        // Verify session was invalidated
        verify(session).invalidate();
        
        // Capture cookies added to response
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());
        
        var clearedCookies = cookieCaptor.getAllValues();
        assertEquals(2, clearedCookies.size());
        
        Cookie accessCookie = clearedCookies.stream()
                .filter(c -> "jwt_access".equals(c.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(accessCookie);
        assertEquals("", accessCookie.getValue());
        assertEquals(0, accessCookie.getMaxAge());

        Cookie refreshCookie = clearedCookies.stream()
                .filter(c -> "jwt_refresh".equals(c.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(refreshCookie);
        assertEquals("", refreshCookie.getValue());
        assertEquals(0, refreshCookie.getMaxAge());

        // Verify redirect
        verify(response).sendRedirect("index.jsp?msg=logged_out");
    }
}
