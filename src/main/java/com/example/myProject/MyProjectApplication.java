package com.example.myProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyProjectApplication {

    public static void main(String[] args) {
        // This single line is all you need.
        // Spring Boot will automatically read your application.properties file
        // and connect to the database for you.
        SpringApplication.run(MyProjectApplication.class, args);
    }

}