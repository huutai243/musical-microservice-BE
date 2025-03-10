package vn.com.iuh.fit.cart_service.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String guestId = request.getHeader("guestId");

        // Nếu có `guestId` nhưng không có `accessToken`, cho phép tiếp tục mà không cần auth
        if (authHeader == null && guestId != null) {
            chain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Claims claims = extractAllClaims(token);
                String username = claims.getSubject();
                List<String> roles = claims.get("roles", List.class);

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(new User(username, "", authorities), null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //  Hàm trích xuất `userId` từ JWT token
    public static String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode("z6NcAZXi9+HYC6ByT+uG+73PdgAHQomW0s7EnpAY+Ns="))) // SECRET_KEY từ application.properties
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // `sub` trong JWT chứa `userId`
    }
}
