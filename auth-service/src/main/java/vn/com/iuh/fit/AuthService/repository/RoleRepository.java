package vn.com.iuh.fit.AuthService.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.iuh.fit.AuthService.entity.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}

