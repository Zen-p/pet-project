package org.youdzhin.auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtService {

    private final String SECRET_KEY;

    public JwtService(@Value("${application.secret-key}") String SECRET_KEY) {
        this.SECRET_KEY = SECRET_KEY;
    }

    public String extractEmail (String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateToken (UserDetails userDetails)  {
        return generateToken (new HashMap<String, Object>(), userDetails);
    }

    public String generateToken (
            Map<String, Object> claims,
            UserDetails userDetails
    ) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid (String token, UserDetails userDetails) {
        final String userEmail = extractEmail(token);
        return userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token);


    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim (String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);

    }

    private Claims extractAllClaims (String token) {
        return Jwts.parser()                  // Новый способ (JJWT 0.12+)
                .verifyWith(getSignInKey())   // Устанавливаем ключ для проверки подписи
                .build()                      // Собираем парсер
                .parseSignedClaims(token)     // Парсим токен и проверяем подпись
                .getPayload();                // Получаем Claims (ранее getBody())
    }

    private SecretKey getSignInKey () {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
