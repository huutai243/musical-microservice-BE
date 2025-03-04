package vn.com.iuh.fit.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.user_service.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
}
