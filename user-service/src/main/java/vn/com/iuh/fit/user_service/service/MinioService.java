package vn.com.iuh.fit.user_service.service;

import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

@Service
public class MinioService {
    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;
    private final String bucketName;
    private final String minioUrl;

    public MinioService(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName,
            @Value("${minio.url}") String minioUrl) {  // 🛠 Thêm @Value
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.minioUrl = minioUrl;

        logger.info("🔍 MinIO URL initialized: {}", this.minioUrl);
    }

    // 📌 Kiểm tra bucket khi service khởi động
    @PostConstruct
    public void init() {
        try {
            initializeBucket();
            logger.info("✅ MinIO bucket check completed successfully!");
        } catch (Exception e) {
            logger.error("❌ MinIO bucket initialization failed: {}", e.getMessage());
        }
    }

    /**
     * 📌 Kiểm tra và tạo bucket nếu chưa tồn tại.
     */
    private void initializeBucket() {
        try {
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("✅ Bucket '{}' created successfully.", bucketName);
            } else {
                logger.info("ℹ️ Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Error checking bucket existence: " + e.getMessage(), e);
        }
    }

    /**
     * 📌 Upload file lên MinIO và trả về URL đầy đủ.
     */
    public String uploadFile(MultipartFile file) {
        try {
            // Tạo tên file duy nhất bằng UUID
            String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Upload file lên MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // Trả về URL chính xác
            String fileUrl = "http://127.0.0.1:9001" + "/" + bucketName + "/" + uniqueFileName;
            logger.info("✅ File uploaded successfully: {}", fileUrl);

            return fileUrl;

        } catch (MinioException e) {
            logger.error("❌ MinIO error: {}", e.getMessage());
            throw new RuntimeException("❌ MinIO error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("❌ Error uploading file: {}", e.getMessage());
            throw new RuntimeException("❌ Error uploading file: " + e.getMessage(), e);
        }
    }

    /**
     * 📌 Xóa file từ MinIO.
     */
    public void deleteFile(String fileUrl) {
        try {
            // Kiểm tra URL hợp lệ
            if (fileUrl == null || fileUrl.isEmpty()) {
                logger.warn("⚠️ File URL is empty or null, skipping delete.");
                return;
            }

            // Lấy tên file từ URL
            String fileName;
            try {
                fileName = new URI(fileUrl).getPath().replaceFirst(".*/", "");
            } catch (Exception e) {
                logger.error("❌ Invalid file URL: {}", fileUrl);
                throw new RuntimeException("❌ Invalid file URL: " + fileUrl, e);
            }

            // Kiểm tra nếu fileName rỗng (tránh lỗi MinIO)
            if (fileName.isEmpty()) {
                logger.warn("⚠️ Extracted file name is empty, skipping delete.");
                return;
            }

            // Gửi yêu cầu xóa file đến MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            logger.info("✅ File '{}' deleted successfully.", fileName);

        } catch (Exception e) {
            logger.error("❌ Error deleting file '{}': {}", fileUrl, e.getMessage());
            throw new RuntimeException("❌ Error deleting file: " + fileUrl, e);
        }
    }

}
