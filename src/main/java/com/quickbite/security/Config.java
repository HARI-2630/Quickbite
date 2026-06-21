package com.quickbite.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletContext;

public class Config {
    private static final Map<String, String> envMap = new HashMap<>();
    private static ServletContext servletContext = null;

    public static void setServletContext(ServletContext context) {
        servletContext = context;
    }

    static {
        // Try to load .env from different locations
        String[] potentialPaths = {
            ".env",
            "../.env",
            "/Users/ntr/Desktop/qucik bite/.env",
            System.getProperty("user.dir") + "/.env",
            System.getProperty("user.home") + "/.env"
        };
        
        boolean loaded = false;
        for (String path : potentialPaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            // strip quotes if any
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            } else if (value.startsWith("'") && value.endsWith("'")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            envMap.put(key, value);
                        }
                    }
                    System.out.println("Loaded environment variables from: " + envFile.getAbsolutePath());
                    loaded = true;
                    break;
                } catch (Exception e) {
                    System.err.println("Error reading .env from " + path + ": " + e.getMessage());
                }
            }
        }
        
        if (!loaded) {
            System.out.println(".env file not found. System environment variables will be used.");
        }
    }

    public static String get(String key) {
        if (servletContext != null) {
            String val = servletContext.getInitParameter(key);
            if (val != null && !val.isEmpty()) {
                return val;
            }
        }
        if (envMap.containsKey(key)) {
            return envMap.get(key);
        }
        String val = System.getenv(key);
        if (val != null) {
            return val;
        }
        return System.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null || val.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val);
    }
}
