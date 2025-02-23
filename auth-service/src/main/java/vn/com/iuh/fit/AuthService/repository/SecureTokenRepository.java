package vn.com.iuh.fit.AuthService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.AuthService.entity.SecureToken;

import java.util.Optional;

public interface SecureTokenRepository extends JpaRepository<SecureToken, Long> {
    Optional<SecureToken> findByToken(String token);
}
