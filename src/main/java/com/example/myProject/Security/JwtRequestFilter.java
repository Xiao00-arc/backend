package com.example.myProject.Security;

import io.jsonwebtoken.Claims; // <-- ADD THIS IMPORT
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- ADD THIS IMPORT
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays; // <-- ADD THIS IMPORT
import java.util.List; // <-- ADD THIS IMPORT
import java.util.stream.Collectors; // <-- ADD THIS IMPORT

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        System.out.println("=== JWT FILTER DEBUG ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());
        
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + (authorizationHeader != null ? "Present (Bearer Token)" : "Missing"));

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT Token extracted: " + jwt.substring(0, Math.min(50, jwt.length())) + "...");
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("Username extracted from JWT: " + username);
            } catch (Exception e) {
                // Handle invalid token
                System.out.println("Cannot parse JWT token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No Bearer token found in Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                try {
                    // --- THIS IS THE NEW LOGIC ---
                    // Extract roles from the token claims
                    Claims claims = (Claims) jwtUtil.extractClaim(jwt, (c) -> c);
                    String rolesString = claims.get("roles", String.class);
                    
                    System.out.println("[JwtRequestFilter] Username: " + username);
                    System.out.println("[JwtRequestFilter] Roles from token: " + rolesString);
                    
                    List<SimpleGrantedAuthority> authorities;
                    if (rolesString != null && !rolesString.isEmpty()) {
                        authorities = Arrays.stream(rolesString.split(","))
                                .map(role -> {
                                    String trimmedRole = role.trim();
                                    // Ensure roles have ROLE_ prefix for Spring Security
                                    if (!trimmedRole.startsWith("ROLE_")) {
                                        trimmedRole = "ROLE_" + trimmedRole;
                                    }
                                    return new SimpleGrantedAuthority(trimmedRole);
                                })
                                .collect(Collectors.toList());
                    } else {
                        // Fallback: use roles from UserDetails
                        authorities = userDetails.getAuthorities().stream()
                                .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                                .collect(Collectors.toList());
                    }
                    
                    System.out.println("[JwtRequestFilter] Final authorities: " + authorities);
                    // ---------------------------

                    // Create an authentication token WITH the roles
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities); // Pass authorities here
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    
                    System.out.println("[JwtRequestFilter] Authentication set successfully for user: " + username);
                    System.out.println("[JwtRequestFilter] SecurityContext authentication: " + SecurityContextHolder.getContext().getAuthentication());
                } catch (Exception e) {
                    System.err.println("[JwtRequestFilter] Error processing token: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("[JwtRequestFilter] Token validation failed for user: " + username);
            }
        } else {
            System.out.println("[JwtRequestFilter] No username found or authentication already exists");
        }
        
        System.out.println("=== END JWT FILTER DEBUG ===");
        chain.doFilter(request, response);
    }
}