package com.springboot.webflux.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;

    @Value("${jwtExpTime}")
    private Long accessTokenValidTime;

    public JwtService(@Value("${jwtSecretKey}") String key) {
        var secret = Base64.getEncoder().encodeToString(key.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token).getBody();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("인증 실패");
        }
    }

    public String generateAccessToken(Long uid) {
        Claims claims = Jwts.claims().setSubject("access_token");
        claims.put("uid", uid);
        Date currentTime = new Date();
        System.out.println("accessTokenValidTime = " + accessTokenValidTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(currentTime)
                .setExpiration(new Date(currentTime.getTime() + accessTokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Long getUidFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody()
                .get("uid",
                        Long.class);
    }
}