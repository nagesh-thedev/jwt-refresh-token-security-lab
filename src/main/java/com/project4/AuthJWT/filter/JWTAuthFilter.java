package com.project4.AuthJWT.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project4.AuthJWT.service.CustomUserDetailservice;
import com.project4.AuthJWT.service.JWTService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthFilter extends OncePerRequestFilter {
	
	private CustomUserDetailservice userDetails;
	private JWTService jwtService;
	
	

	public JWTAuthFilter(CustomUserDetailservice userDetails, JWTService jwtService) {
		super();
		this.userDetails = userDetails;
		this.jwtService = jwtService;
	}






@Override
protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = header.substring(7);

    String username = jwtService.getUserName(token);

    if (username == null || !jwtService.isTokenValid(token, username)) {
        SecurityContextHolder.clearContext();
        response.sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Invalid or expired JWT"
        );
        return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userdata = userDetails.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userdata, null, userdata.getAuthorities()
            );

        auth.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    filterChain.doFilter(request, response);

  }

}
