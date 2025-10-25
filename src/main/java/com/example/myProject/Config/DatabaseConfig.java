package com.example.myProject.Config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                // Parse the Render DATABASE_URL format: postgresql://user:password@host:port/database
                URI uri = new URI(databaseUrl);
                
                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 5432; // Default PostgreSQL port
                String database = uri.getPath().substring(1); // Remove leading slash
                String[] userInfo = uri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo[1];
                
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
                
                System.out.println("Connecting to: " + jdbcUrl + " with user: " + username);
                
                return DataSourceBuilder
                    .create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
                    
            } catch (Exception e) {
                System.err.println("Failed to parse DATABASE_URL: " + databaseUrl);
                e.printStackTrace();
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            }
        }
        
        // Fallback to default configuration for local development
        return DataSourceBuilder
            .create()
            .url("jdbc:postgresql://localhost:5432/expense_db")
            .username("postgres")
            .password("password")
            .driverClassName("org.postgresql.Driver")
            .build();
    }
}