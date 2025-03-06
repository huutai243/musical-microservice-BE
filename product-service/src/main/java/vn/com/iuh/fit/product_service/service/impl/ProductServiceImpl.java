package vn.com.iuh.fit.product_service.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;
import vn.com.iuh.fit.product_service.entity.Product;
import vn.com.iuh.fit.product_service.entity.Category;
import vn.com.iuh.fit.product_service.entity.ProductImage;
import vn.com.iuh.fit.product_service.repository.CategoryRepository;
import vn.com.iuh.fit.product_service.repository.ProductRepository;
import vn.com.iuh.fit.product_service.repository.ProductImageRepository;
import vn.com.iuh.fit.product_service.service.FileStorageService;
import vn.com.iuh.fit.product_service.service.ProductService;

import java.util.ArrayList;
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

    @Autowired
    private ProductImageRepository productImageRepository;

    // 1 LẤY DANH SÁCH SẢN PHẨM
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

    // 2 THÊM SẢN PHẨM (HỖ TRỢ NHIỀU ẢNH)
    @Override
    public ProductResponse addProductWithImages(ProductRequest productRequest, List<MultipartFile> imageFiles) throws Exception {
        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .category(category)
                .build();

        product = productRepository.save(product);

        // Upload ảnh lên MinIO và lưu vào `ProductImage`
        List<String> imageUrls = fileStorageService.uploadFiles(imageFiles);
        List<ProductImage> imageEntities = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            ProductImage productImage = new ProductImage(null, imageUrl, product);
            imageEntities.add(productImageRepository.save(productImage));
        }

        // Cập nhật lại danh sách images cho product
        product.setImages(imageEntities);

        return convertToResponse(product);
    }

    // 3 CẬP NHẬT SẢN PHẨM (THAY ẢNH MỚI)
    @Override
    public ProductResponse updateProductWithImages(Long id, ProductRequest productRequest, List<MultipartFile> imageFiles) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(productRequest.getName());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setCategory(category);

        product = productRepository.save(product);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            // Xóa ảnh cũ khỏi MinIO
            List<ProductImage> existingImages = productImageRepository.findByProductId(id);
            List<String> oldUrls = existingImages.stream().map(ProductImage::getImageUrl).toList();

            fileStorageService.deleteFiles(oldUrls);
            productImageRepository.deleteAll(existingImages);

            // Upload ảnh mới
            List<String> newImageUrls = fileStorageService.uploadFiles(imageFiles);
            for (String imageUrl : newImageUrls) {
                productImageRepository.save(new ProductImage(null, imageUrl, product));
            }
        }

        return convertToResponse(product);
    }

    // 4 XOÁ SẢN PHẨM (XOÁ CẢ ẢNH)
    @Override
    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductImage> images = productImageRepository.findByProductId(id);
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).toList();

        fileStorageService.deleteFiles(imageUrls);
        productImageRepository.deleteAll(images);
        productRepository.deleteById(id);
    }

    // 5 LỌC SẢN PHẨM
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

    // 6 PHÂN TRANG & SẮP XẾP
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
        throw new UnsupportedOperationException("Best selling products should be fetched from Inventory-Service.");
    }

    // 7 CHUYỂN ĐỔI ENTITY → DTO
    public ProductResponse convertToResponse(Product product) {
        List<String> imageUrls = product.getImages() != null ?
                product.getImages().stream()
                        .map(ProductImage::getImageUrl)
                        .collect(Collectors.toList())
                : new ArrayList<>(); // Tránh lỗi NullPointerException

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryName(product.getCategory().getName())
                .imageUrls(imageUrls)
                .build();
    }

}
