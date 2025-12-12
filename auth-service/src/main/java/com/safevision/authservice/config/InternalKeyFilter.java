package com.safevision.authservice.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InternalKeyFilter extends OncePerRequestFilter {

	 private final String key;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {


        String requestKey = request.getHeader("X-Internal-Key");


        if (requestKey != null && requestKey.equals(key)) {
            

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "INTERNAL_SYSTEM", 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL"))
            );

           
            SecurityContextHolder.getContext().setAuthentication(auth);
        }


        filterChain.doFilter(request, response);
    }
}