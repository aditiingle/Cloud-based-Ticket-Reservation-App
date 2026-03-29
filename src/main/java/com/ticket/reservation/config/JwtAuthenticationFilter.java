package com.ticket.reservation.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ticket.reservation.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String header = request.getHeader("Authorization");
        String token = null;
        String email = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
            try {
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                logger.error("Error extracting email from token: " + e.getMessage());
            }
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null && Boolean.TRUE.equals(jwtUtil.validateToken(token))) {
            // Extract role from token and create authorities
            String role = jwtUtil.extractRole(token);
            SimpleGrantedAuthority authority;
            
            if (StringUtils.hasText(role)) {
                authority = new SimpleGrantedAuthority(role);
            } else {
                // Default to CUSTOMER role if no role in token (backward compatibility)
                authority = new SimpleGrantedAuthority("ROLE_CUSTOMER");
            }
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        

        filterChain.doFilter(request, response);
    }
}
