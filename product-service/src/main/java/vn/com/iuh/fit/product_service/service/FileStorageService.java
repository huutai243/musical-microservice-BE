package vn.com.iuh.fit.product_service.service;


import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file) throws Exception;
    void deleteFile(String fileUrl) throws Exception;
}


