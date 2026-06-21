package com.quickbite.security;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Config.setServletContext(sce.getServletContext());
        System.out.println("[AppContextListener] ServletContext loaded successfully in Config.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Config.setServletContext(null);
        // Shut down background thread pool executor
        ThreadPoolManager.shutdown();
        // Shut down database connection pool
        com.quickbite.connection.DBConnection.shutdown();
    }
}
