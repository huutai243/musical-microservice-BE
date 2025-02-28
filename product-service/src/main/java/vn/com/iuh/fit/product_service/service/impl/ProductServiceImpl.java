package vn.com.iuh.fit.product_service.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;
import vn.com.iuh.fit.product_service.entity.Product;
import vn.com.iuh.fit.product_service.entity.Category;
import vn.com.iuh.fit.product_service.repository.CategoryRepository;
import vn.com.iuh.fit.product_service.repository.ProductRepository;
import vn.com.iuh.fit.product_service.service.FileStorageService;
import vn.com.iuh.fit.product_service.service.ProductService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // 1️⃣ CRUD CƠ BẢN
    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToResponse(product);
    }

    @Override
    public ProductResponse addProductWithImage(ProductRequest productRequest, MultipartFile imageFile) throws Exception {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Upload ảnh lên MinIO
        String imageUrl = fileStorageService.uploadFile(imageFile);

        Product product = new Product();
        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setCategory(category);
        product.setImageUrl(imageUrl); // Lưu URL ảnh vào DB

        return convertToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProductWithImage(Long id, ProductRequest productRequest, MultipartFile imageFile) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Nếu có ảnh mới, xóa ảnh cũ và upload ảnh mới
        if (imageFile != null && !imageFile.isEmpty()) {
            fileStorageService.deleteFile(product.getImageUrl()); // Xóa ảnh cũ
            String newImageUrl = fileStorageService.uploadFile(imageFile);
            product.setImageUrl(newImageUrl);
        }

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setStockQuantity(productRequest.getStockQuantity());
        product.setCategory(category);

        return convertToResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Xóa ảnh khỏi MinIO
        fileStorageService.deleteFile(product.getImageUrl());

        // Xóa sản phẩm khỏi database
        productRepository.deleteById(id);
    }

    // 2 LỌC SẢN PHẨM
    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> filterProductsByPrice(double minPrice, double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 3️⃣ PHÂN TRANG & SẮP XẾP
    @Override
    public List<ProductResponse> getPagedProducts(int page, int size) {
        return productRepository.findAll()
                .stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getLatestProducts(int limit) {
        return productRepository.findTopByOrderByIdDesc()
                .stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getBestSellingProducts(int limit) {
        return productRepository.findTopByOrderByStockQuantityDesc()
                .stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 4️⃣ UPLOAD & XÓA ẢNH SẢN PHẨM
    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        return fileStorageService.uploadFile(file);
    }

    @Override
    public void deleteFile(String imageUrl) throws Exception {
        fileStorageService.deleteFile(imageUrl);
    }

    // CHUYỂN ĐỔI ENTITY → DTO
    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory() != null ? product.getCategory().getName() : null
        );
    }
}
