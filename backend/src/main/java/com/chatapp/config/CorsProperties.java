package com.chatapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CORS configuration properties.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    
    private String allowedOrigins;
    
    public String getAllowedOrigins() {
        return allowedOrigins;
    }
    
    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
