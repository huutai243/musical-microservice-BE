package vn.com.iuh.fit.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Gateway không thực hiện xác thực (authenticated) theo chuẩn Spring Security,
     * mà chỉ kiểm tra sự hiện diện của JWT cho các endpoint "private".
     * Mọi request đều "permitAll" để filter tùy chỉnh xử lý 401 nếu thiếu token.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Cho tất cả request đi qua, filter tùy chỉnh sẽ quyết định chặn hay không
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                // Đưa filter kiểm tra JWT lên trước (hoặc sau) tùy ý
                .addFilterBefore(jwtExistenceFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Bộ lọc kiểm tra JWT có tồn tại (nhưng không giải mã).
     * - Nếu endpoint "public" => bỏ qua
     * - Nếu endpoint "private" => yêu cầu header Authorization: Bearer ...
     *   nếu thiếu, trả về 401 ngay
     */
    @Bean
    public WebFilter jwtExistenceFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {

            // Các endpoint public (không yêu cầu JWT)
            final String path = exchange.getRequest().getURI().getPath();
            if (path.startsWith("/api/auth")
                    || path.startsWith("/swagger-ui")
                    || path.startsWith("/api-docs")
                    || path.startsWith("/actuator")
                    || path.equals("/api/users/create")
                    || path.equals("/api/products/get-all")
                    || path.matches("^/api/products/\\d+$")
                    || path.startsWith("/api/products/search")
                    || path.startsWith("/api/products/filter")
                    || path.startsWith("/api/products/category")
                    || path.startsWith("/api/products/page")
                    || path.startsWith("/api/products/latest")
                    || path.startsWith("/api/products/bestselling")
                    || path.startsWith("/api/reviews/health")
                    || path.equals("/api/categories/get-all")
                    || path.matches("^/api/categories/\\d+$"))
            {
                // Bỏ qua, không check JWT
                return chain.filter(exchange);
            }

            // Với endpoint còn lại => cần có header Authorization: Bearer ...
            List<String> authHeaders = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null || authHeaders.isEmpty() || !authHeaders.get(0).startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Nếu có header => cho đi tiếp (không parse JWT, không set SecurityContext)
            return chain.filter(exchange);
        };
    }

    /**
     * Cấu hình CORS để không chặn JWT.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfig.setExposedHeaders(List.of("Authorization"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
