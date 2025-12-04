package com.example.sqlserver.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Global CORS configuration for the application
 * Place this class in: src/main/java/com/example/yourproject/config/
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        
        // Allow all origins (for development) - replace with specific origins in production
        config.addAllowedOriginPattern("*");
        
        // Or specify exact origins for production:
        config.setAllowedOrigins(Arrays.asList(
            "https://47fec906757e.ngrok-free.app",
            "http://localhost:3000",
           " http://localhost:4200",
           "https://f742a7c07fab.ngrok-free.app"
        ));
        
        // Allow all headers
        config.addAllowedHeader("*");
        
        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Max age of the CORS preflight request cache
        config.setMaxAge(3600L);
        
        // Expose headers that the client can access
        config.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
