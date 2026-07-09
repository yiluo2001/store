package de.yiluo.store.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Keys.hmacShaKeyFor requires password be at least 256-bits long (32 characters)
    // Keys.hmacShaKeyFor () turn string into SecretKey obj to optimize math and guarantee length
    private static final String SECRET_KEY = "my-ultra-secure-and-very-long-secret-must-change-this-in-production";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public String generateToken(UserDetails userDetails) {
        // to add custom claim in the future
        Map<String, Object> extraClaims = new HashMap<>();
        // method chaining, same as apply one method and use returned value for next method
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                // unit is millisecond
                .issuedAt(new Date(System.currentTimeMillis()))
                // valid for 10 hours
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // <T> defines a generic type named T
    // T is the return type
    // Function<Claims, T> is a type, meaning function that take Claims and return T
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        // claimsResolver is obj that contains a function
        // it is not method, so claimsResolver.(claims) does not work
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        // extractUsername checks key behind the scene
        final String username = extractUsername(token);
        // more checking can be added later
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}