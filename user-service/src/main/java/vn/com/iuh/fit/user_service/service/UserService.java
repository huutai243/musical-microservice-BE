package vn.com.iuh.fit.user_service.service;

import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.user_service.dto.UpdateProfileRequest;
import vn.com.iuh.fit.user_service.dto.UserRequest;
import vn.com.iuh.fit.user_service.entity.User;
import vn.com.iuh.fit.user_service.repository.UserRepository;

import java.net.URI;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final MinioService minioService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /**
     * Tìm user theo ID (Best Practice)
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Vẫn giữ tìm kiếm user theo email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(UserRequest userRequest) {
        User user = User.builder()
                .id(userRequest.getId())
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .build();
        return userRepository.save(user);
    }

    /**
     * Cập nhật thông tin user theo ID
     */
    public User updateAvatar(Long id, MultipartFile imageFile) {
        return userRepository.findById(id)
                .map(user -> {
                    // Xóa avatar cũ (nếu có)
                    if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                        try {
                            String oldFileName = new URI(user.getAvatarUrl()).getPath().replaceFirst(".*/", "");
                            if (!oldFileName.isEmpty()) { // Kiểm tra tránh lỗi xóa nhầm
                                minioService.deleteFile(oldFileName);
                                logger.info("✅ Deleted old avatar: {}", oldFileName);
                            } else {
                                logger.warn("⚠️ Extracted old avatar file name is empty, skipping delete.");
                            }
                        } catch (Exception e) {
                            logger.error("❌ Failed to delete old avatar '{}': {}", user.getAvatarUrl(), e.getMessage());
                        }
                    }

                    // Upload avatar mới
                    String avatarUrl = null;
                    if (imageFile != null && !imageFile.isEmpty()) {
                        try {
                            avatarUrl = minioService.uploadFile(imageFile);
                            logger.info("✅ Avatar uploaded successfully: {}", avatarUrl);
                        } catch (Exception e) {
                            logger.error("❌ Failed to upload new avatar: {}", e.getMessage());
                            throw new RuntimeException("Failed to upload new avatar: " + e.getMessage(), e);
                        }
                    }

                    // Chỉ cập nhật avatar nếu upload thành công
                    if (avatarUrl != null) {
                        user.setAvatarUrl(avatarUrl);
                        logger.info("✅ Updated user avatar URL: {}", avatarUrl);
                    } else {
                        logger.warn("⚠️ Avatar URL is null, skipping update.");
                    }

                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }


    /**
     * Cập nhật số điện thoại và địa chỉ cho người dùng.
     */
    public User updateProfile(Long id, UpdateProfileRequest updateProfileRequest) {
        return userRepository.findById(id)
                .map(user -> {
                    if (updateProfileRequest.getPhoneNumber() != null) {
                        user.setPhoneNumber(updateProfileRequest.getPhoneNumber());
                    }
                    if (updateProfileRequest.getAddress() != null) {
                        user.setAddress(updateProfileRequest.getAddress());
                    }
                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
