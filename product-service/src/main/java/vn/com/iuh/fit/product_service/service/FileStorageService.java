package vn.com.iuh.fit.product_service.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileStorageService {
    List<String> uploadFiles(List<MultipartFile> files) throws Exception; // Upload nhiều ảnh
    void deleteFiles(List<String> fileUrls) throws Exception; // Xóa nhiều ảnh
    void deleteFile(String fileUrl) throws Exception;
}
