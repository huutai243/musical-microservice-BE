package vn.com.iuh.fit.AuthService.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.AuthService.dto.JwtResponse;
import vn.com.iuh.fit.AuthService.entity.User;
import vn.com.iuh.fit.AuthService.repository.UserRepository;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final Key secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final UserRepository userRepository;
    private final JwtParser jwtParser;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.accessToken.expiration}") long accessTokenExpiration,
            @Value("${jwt.refreshToken.expiration}") long refreshTokenExpiration,
            UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.userRepository = userRepository;
        this.jwtParser = Jwts.parser().setSigningKey(secretKey).build(); // ✅ Fix
    }

    /**
     * Tạo Access Token cho User
     */
    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    /**
     * Tạo Refresh Token cho User
     */
    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    /**
     * Kiểm tra và xác thực JWT
     */
    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Refresh Access Token bằng Refresh Token
     */
    public JwtResponse refreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String username = extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = generateAccessToken(user);
        return new JwtResponse(newAccessToken, refreshToken);
    }

    /**
     * Lấy Username từ Token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Trích xuất các Claims cụ thể từ Token
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Tạo JWT với claims
     */
    private String generateToken(User user, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList()));

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Trích xuất toàn bộ Claims từ Token
     */
    public Claims extractAllClaims(String token) {
        return jwtParser.parseSignedClaims(token).getPayload();
    }
}
