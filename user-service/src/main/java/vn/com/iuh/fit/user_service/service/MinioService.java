package vn.com.iuh.fit.user_service.service;

import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final String bucketName;

    // 📌 Inject tất cả các giá trị từ application.properties vào constructor
    public MinioService(@Value("${minio.url}") String url,
                        @Value("${minio.access-key}") String accessKey,
                        @Value("${minio.secret-key}") String secretKey,
                        @Value("${minio.bucket-name}") String bucketName) throws Exception {
        // 🛠 Kiểm tra giá trị bucketName trước khi gán
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("MinIO bucket name must not be null or empty.");
        }
        this.bucketName = bucketName;

        // 🔗 Khởi tạo MinioClient với các thông tin từ application.properties
        this.minioClient = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
    }

    // 📌 Gọi phương thức này sau khi bean được khởi tạo để kiểm tra bucket
    @PostConstruct
    public void init() {
        initializeBucket();
    }

    /**
     * 📌 Kiểm tra và tạo bucket nếu chưa tồn tại.
     */
    private void initializeBucket() {
        try {
            // 🛠 Kiểm tra xem bucket đã tồn tại hay chưa
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                // 🆕 Nếu bucket chưa tồn tại, tạo mới
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("✅ Bucket '" + bucketName + "' created successfully.");
            } else {
                System.out.println("ℹ️ Bucket '" + bucketName + "' already exists.");
            }
        } catch (Exception e) {
            throw new RuntimeException("❌ Error initializing MinIO bucket: " + e.getMessage(), e);
        }
    }

    /**
     * 📌 Upload file lên MinIO.
     */
    public String uploadFile(MultipartFile file, String fileName) throws Exception {
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName) // Sử dụng tên file truyền vào
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        return fileName; // Trả về tên file đã upload
    }
}
