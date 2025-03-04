package vn.com.iuh.fit.AuthService.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.AuthService.entity.Role;
import vn.com.iuh.fit.AuthService.repository.RoleRepository;

import java.util.List;

@Component
public class DataInitializer {
    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initRoles() {
        List<String> defaultRoles = List.of("ADMIN", "USER");

        for (String roleName : defaultRoles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = new Role();
                role.setName(roleName);
                roleRepository.save(role);
                System.out.println(" Tạo vai trò mặc định: " + roleName);
            }
        }
    }
}
