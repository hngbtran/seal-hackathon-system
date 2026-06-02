package com.minhtung.hackathon.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static  final String SECRET ="mySecretKey_SWP391_Hackathon_2024_MinhTung!";
    private static final long EXPIRATION_MS = 1_800_000L; // thoi gia ton tai token la 30p

    private Key getKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }
    public String generateToken(String username , String role){
        return Jwts.builder()
                .setSubject(username)
                .claim("role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_MS))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String extracRole(String token){
        return (String) parseClaims(token).get("role");
    }


    //ham nay dung de giai ma token
    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token){
        try{
            parseClaims(token);
            return true ;
        }catch (JwtException | IllegalArgumentException e ){
            return false ;
        }
    }

    public String extracUserName(String token){
        return parseClaims(token).getSubject() ;
    }
}
