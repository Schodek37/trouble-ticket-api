package pl.netia.troubleticket.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtTestTokenGenerator {

    private static final String SECRET =
            "70c9f9ab7c9f923f44a20d57b61b7774f96ad932ec5d43c4c253122df3b9b9ae";

    public static void main(String[] args) {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        String token = Jwts.builder()
                .setSubject("partner-A")
                .addClaims(Map.of("tenantId", "TENANT_001"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + 86400000L * 365
                ))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("Bearer " + token);
    }
}