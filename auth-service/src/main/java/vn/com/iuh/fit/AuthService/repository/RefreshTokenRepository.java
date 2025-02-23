package vn.com.iuh.fit.AuthService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.AuthService.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
