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
        System.out.println(generateToken("TENANT_001"));
    }

    public static String generateToken(String tenantId) {
        byte[] keyBytes = SECRET.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        return "Bearer " + Jwts.builder()
                .setSubject("partner-" + tenantId)
                .addClaims(Map.of("tenantId", tenantId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + 86400000L * 365
                ))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}