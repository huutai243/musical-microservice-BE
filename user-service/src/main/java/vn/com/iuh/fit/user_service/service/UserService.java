package vn.com.iuh.fit.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.iuh.fit.user_service.dto.UserRequest;
import vn.com.iuh.fit.user_service.entity.User;
import vn.com.iuh.fit.user_service.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * Lấy tất cả user (chỉ dành cho ADMIN)
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Tìm user theo ID
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Tìm user theo email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Tạo user mới (chỉ ADMIN)
     */
    public User createUser(UserRequest userRequest) {
        User user = User.builder()
                .id(userRequest.getId())
                .fullName(userRequest.getUsername())
                .email(userRequest.getEmail())
                .build();
        return userRepository.save(user);
    }

    /**
     * Cập nhật thông tin user (admin hoặc chính họ)
     */
    public User updateUser(Long id, User userRequest) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFullName(userRequest.getFullName());
                    user.setPhoneNumber(userRequest.getPhoneNumber());
                    user.setAddress(userRequest.getAddress());
                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    /**
     * Xóa user (chỉ ADMIN)
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
