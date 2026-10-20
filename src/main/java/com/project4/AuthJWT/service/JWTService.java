package com.project4.AuthJWT.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {
	
	
	@Value("${jwt.access-token-expiration}")
	private Long exptime;
	
	private final SecretKey secKey;

	public JWTService(@Value("${jwt.secret}") String secret_key) {
		super();
		this.secKey= Keys.hmacShaKeyFor(secret_key.getBytes());
	}
	
	public String generateToken(String username) {
		long currentTime = System.currentTimeMillis();
		return Jwts.builder().subject(username).issuedAt(new Date(currentTime))
		.expiration(new Date(currentTime+exptime)).signWith(secKey).compact();
		
	}
	
	public String getUserName(String token) {
		
		return Jwts.parser().verifyWith(secKey).build().parseSignedClaims(token).getPayload().getSubject();
	}
	
	public boolean isTokenValid(String token,String username) {
		return username.equals(getUserName(token));
	}

	
}
