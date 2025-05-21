package vn.com.iuh.fit.product_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;
import vn.com.iuh.fit.product_service.entity.Product;
import vn.com.iuh.fit.product_service.entity.Category;
import vn.com.iuh.fit.product_service.entity.ProductDocument;
import vn.com.iuh.fit.product_service.entity.ProductImage;
import vn.com.iuh.fit.product_service.repository.CategoryRepository;
import vn.com.iuh.fit.product_service.repository.ProductElasticsearchRepository;
import vn.com.iuh.fit.product_service.repository.ProductRepository;
import vn.com.iuh.fit.product_service.repository.ProductImageRepository;
import vn.com.iuh.fit.product_service.service.FileStorageService;
import vn.com.iuh.fit.product_service.service.ProductService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductElasticsearchRepository productElasticsearchRepository;

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

        List<String> imageUrls = fileStorageService.uploadFiles(imageFiles);
        List<ProductImage> imageEntities = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            ProductImage productImage = new ProductImage(null, imageUrl, product);
            imageEntities.add(productImageRepository.save(productImage));
        }

        product.setImages(imageEntities);

        // Cập nhật vào Elasticsearch (Lưu cả danh sách ảnh)
        ProductDocument productDocument = ProductDocument.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId()) // Lưu categoryId
                .imageUrls(imageUrls) // Lưu danh sách ảnh
                .build();

        productElasticsearchRepository.save(productDocument);

        Set<String> keys = redisTemplate.keys("product:page:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        warmupProductPageCache();

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

        List<ProductImage> existingImages = productImageRepository.findByProductId(id);
        List<String> retainedUrls = productRequest.getRetainedImageUrls() != null ? productRequest.getRetainedImageUrls() : List.of();

        for (ProductImage image : existingImages) {
            if (!retainedUrls.contains(image.getImageUrl())) {
                fileStorageService.deleteFile(image.getImageUrl());
                productImageRepository.delete(image);
            }
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<String> uploadedUrls = fileStorageService.uploadFiles(imageFiles);
            for (String imageUrl : uploadedUrls) {
                ProductImage newImage = ProductImage.builder()
                        .imageUrl(imageUrl)
                        .product(product)
                        .build();
                productImageRepository.save(newImage);
            }
        }

        Set<String> keys = redisTemplate.keys("product:page:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        warmupProductPageCache();

        return convertToResponse(product);
    }

    // 4 XOÁ SẢN PHẨM (XOÁ CẢ ẢNH)
    @Override
    public void deleteProduct(Long id) throws Exception {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Lấy danh sách ảnh của sản phẩm
        List<ProductImage> images = productImageRepository.findByProductId(id);
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).toList();

        // Xóa ảnh khỏi bộ lưu trữ
        fileStorageService.deleteFiles(imageUrls);
        productImageRepository.deleteAll(images);

        // Xóa dữ liệu sản phẩm khỏi Elasticsearch
        productElasticsearchRepository.deleteById(id);

        Set<String> keys = redisTemplate.keys("product:page:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        warmupProductPageCache();

        // Xóa sản phẩm khỏi MySQL
        productRepository.deleteById(id);
    }

    // 5 LỌC SẢN PHẨM
    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productElasticsearchRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> filterProductsByPrice(double minPrice, double maxPrice) {
        List<ProductDocument> products = productElasticsearchRepository.findByPriceBetween(minPrice, maxPrice);

        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse convertToResponse(ProductDocument productDocument) {
        return ProductResponse.builder()
                .id(productDocument.getId())
                .name(productDocument.getName())
                .description(productDocument.getDescription())
                .price(productDocument.getPrice())
                .categoryId(productDocument.getCategoryId())
                .imageUrls(productDocument.getImageUrls())
                .build();
    }


    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        List<ProductDocument> productDocuments = productElasticsearchRepository.findByNameContainingIgnoreCase(keyword);
        return productDocuments.stream()
                .map(doc -> ProductResponse.builder()
                        .id(doc.getId())
                        .name(doc.getName())
                        .description(doc.getDescription())
                        .price(doc.getPrice())
                        .categoryId(doc.getCategoryId())
                        .imageUrls(doc.getImageUrls())
                        .build())
                .collect(Collectors.toList());
    }


    // 6 PHÂN TRANG & SẮP XẾP
//    @Override
//    public List<ProductResponse> getPagedProducts(int page, int size) {
//        return productRepository.findAll()
//                .stream()
//                .skip((long) (page - 1) * size)
//                .limit(size)
//                .map(this::convertToResponse)
//                .collect(Collectors.toList());
//    }

    @Override
    public List<ProductResponse> getPagedProducts(int page, int size) {
        String key = String.format("product:page:%d:size:%d", page, size);
        String cachedJson = redisTemplate.opsForValue().get(key);

        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ProductResponse.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<ProductResponse> result = productRepository.findAll()
                .stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(result),
                    Duration.ofMinutes(15)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
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
    public Page<ProductResponse> searchProductsPaged(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return products.map(this::convertToResponse);
    }

    @Override
    public Page<ProductResponse> filterProductsByPricePaged(double minPrice, double maxPrice, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        return products.map(this::convertToResponse);
    }

    @Override
    public Page<ProductResponse> getProductsByCategoryPaged(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(this::convertToResponse);
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
                .categoryId(product.getCategory().getId())
                .imageUrls(imageUrls)
                .build();
    }

    private void warmupProductPageCache() {
        try {
            List<ProductResponse> top10 = productRepository.findAll()
                    .stream()
                    .limit(10)
                    .map(this::convertToResponse)
                    .toList();

            redisTemplate.opsForValue().set(
                    "product:page:1:size:10",
                    objectMapper.writeValueAsString(top10),
                    Duration.ofMinutes(15)
            );

            List<ProductResponse> top5 = productRepository.findAll()
                    .stream()
                    .limit(5)
                    .map(this::convertToResponse)
                    .toList();

            redisTemplate.opsForValue().set(
                    "product:page:1:size:5",
                    objectMapper.writeValueAsString(top5),
                    Duration.ofMinutes(15)
            );

            System.out.println(" Warm-up cache trang chủ thành công.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" Warm-up cache thất bại.");
        }
    }
}
