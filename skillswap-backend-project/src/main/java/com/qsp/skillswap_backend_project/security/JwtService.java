package com.qsp.skillswap_backend_project.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.qsp.skillswap_backend_project.enums.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(@Value("${jwt.secret}") String base64Secret, @Value("${jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
		this.expirationMs = expirationMs;
	}

	public String generateToken(String email, Role role) {
		Date now = new Date();
		return Jwts.builder().subject(email).claim("role", role.name()).issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMs)).signWith(key).compact();
	}

	public String extractEmail(String token) {
		return parse(token).getSubject();
	}

	public boolean isValid(String token) {
		try {
			parse(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private Claims parse(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}
}
