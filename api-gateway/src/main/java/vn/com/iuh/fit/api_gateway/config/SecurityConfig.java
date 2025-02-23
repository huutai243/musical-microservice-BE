package vn.com.iuh.fit.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String SECRET_KEY = "z6NcAZXi9+HYC6ByT+uG+73PdgAHQomW0s7EnpAY+Ns="; // Cùng key với AUTH-SERVICE

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**").permitAll()  // ✅ Cho phép tất cả API trong auth-service
                        .pathMatchers("/auth/**").permitAll()  // 🔥 Thêm /auth nếu routing có thể khác
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                        .anyExchange().authenticated()
                )

                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)  // ✅ Vô hiệu hóa form login
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)  // ✅ Vô hiệu hóa Basic Auth
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) ->
                                Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))
                        )  // ✅ Trả về 401 thay vì redirect
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))  // ✅ Chỉ dùng JWT Auth
                );

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKeySpec).build();
    }
}
