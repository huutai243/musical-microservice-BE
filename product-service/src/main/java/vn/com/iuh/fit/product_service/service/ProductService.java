package vn.com.iuh.fit.product_service.service;

import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    // 1 CRUD CƠ BẢN
    List<ProductResponse> getAllProducts(); // Lấy tất cả sản phẩm
    ProductResponse getProductById(Long id); // Lấy chi tiết sản phẩm kèm ảnh
    ProductResponse addProductWithImage(ProductRequest productRequest, MultipartFile imageFile) throws Exception; // Thêm sản phẩm + ảnh
    ProductResponse updateProductWithImage(Long id, ProductRequest productRequest, MultipartFile imageFile) throws Exception; // Cập nhật sản phẩm + ảnh mới
    void deleteProduct(Long id) throws Exception; // Xóa sản phẩm + ảnh

    // 2 LỌC SẢN PHẨM
    List<ProductResponse> getProductsByCategory(Long categoryId); // Lọc theo danh mục
    List<ProductResponse> searchProducts(String keyword); // Tìm kiếm sản phẩm theo từ khóa
    List<ProductResponse> filterProductsByPrice(double minPrice, double maxPrice); // Lọc theo giá

    // 3 PHÂN TRANG & SẮP XẾP
    List<ProductResponse> getPagedProducts(int page, int size); // Phân trang sản phẩm
    List<ProductResponse> getLatestProducts(int limit); // Lấy sản phẩm mới nhất
    List<ProductResponse> getBestSellingProducts(int limit); // Lấy sản phẩm bán chạy

    // 4 UPLOAD & XÓA ẢNH SẢN PHẨM
    String uploadFile(MultipartFile file) throws Exception; // Upload ảnh lên MinIO
    void deleteFile(String imageUrl) throws Exception; // Xóa ảnh khỏi MinIO
}
