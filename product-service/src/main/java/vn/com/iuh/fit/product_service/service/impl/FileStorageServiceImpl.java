package vn.com.iuh.fit.product_service.service.impl;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.service.FileStorageService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
    public List<String> uploadFiles(List<MultipartFile> files) throws Exception {
        List<String> imageUrls = new ArrayList<>();

        // Kiểm tra và tạo bucket nếu chưa có
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
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
            imageUrls.add("http://127.0.0.1:9001/" + bucketName + "/" + fileName);
        }
        return imageUrls;
    }

    @Override
    public void deleteFiles(List<String> fileUrls) throws Exception {
        for (String fileUrl : fileUrls) {
            if (fileUrl == null || fileUrl.isEmpty()) continue;

            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        }
    }
}
