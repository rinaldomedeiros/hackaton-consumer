package com.fiap.hackaton.grp14.consumer.infrastructure.security;


import com.fiap.hackaton.grp14.consumer.application.exceptions.TokenExpiradoException;
import com.fiap.hackaton.grp14.consumer.application.exceptions.TokenInvalidoException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;

    public String generateToken(String username) {
        return Jwts.builder()
          .setSubject(username)
          .setIssuedAt(new Date())
          .setExpiration(new Date(System.currentTimeMillis() + expiration))
          .signWith(getSigningKey())
          .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new TokenExpiradoException("Token expirado");
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException ex) {
            throw new TokenInvalidoException("Token inválido");
        } catch (IllegalArgumentException ex) {
            throw new TokenInvalidoException("Token não pode ser vazio");
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody()
          .getSubject();
      }

    public Key getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}