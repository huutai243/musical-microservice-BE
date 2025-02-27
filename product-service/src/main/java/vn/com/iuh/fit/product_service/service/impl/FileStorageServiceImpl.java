package vn.com.iuh.fit.product_service.service.impl;


import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.service.FileStorageService;

import java.io.InputStream;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public FileStorageServiceImpl(@Value("${minio.url}") String minioUrl,
                                  @Value("${minio.access-key}") String accessKey,
                                  @Value("${minio.secret-key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // Kiểm tra và tạo bucket nếu chưa có
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        // Upload file lên MinIO
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }
        return "http://127.0.0.1:9001/" + bucketName + "/" + fileName;
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        if (fileUrl == null || fileUrl.isEmpty()) return; // Nếu không có ảnh thì bỏ qua

        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1); // Lấy tên file từ URL

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

}

