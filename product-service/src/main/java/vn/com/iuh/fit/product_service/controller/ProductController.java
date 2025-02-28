package vn.com.iuh.fit.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;
import vn.com.iuh.fit.product_service.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * API lấy danh sách tất cả sản phẩm
     * @return List<ProductResponse>
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * API lấy chi tiết sản phẩm theo ID (bao gồm ảnh)
     * @param id - ID sản phẩm
     * @return ProductResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * API thêm mới sản phẩm cùng với ảnh
     * @param imageFile - Ảnh sản phẩm
     * @return ProductResponse
     */
    @PostMapping("/add")
    public ResponseEntity<ProductResponse> addProductWithImage(
            @RequestPart("product") String productJson,
            @RequestPart("image") MultipartFile imageFile) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest productRequest = objectMapper.readValue(productJson, ProductRequest.class);

        return ResponseEntity.ok(productService.addProductWithImage(productRequest, imageFile));
    }

    /**
     * API cập nhật sản phẩm kèm với ảnh mới (nếu có)
     * @param id - ID sản phẩm
     * @param productRequest - Thông tin sản phẩm
     * @param imageFile - Ảnh sản phẩm mới
     * @return ProductResponse
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponse> updateProductWithImage(
            @PathVariable Long id,
            @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
        return ResponseEntity.ok(productService.updateProductWithImage(id, productRequest, imageFile));
    }

    /**
     * API xóa sản phẩm kèm ảnh
     * @param id - ID sản phẩm
     * @return ResponseEntity
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) throws Exception {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * API tìm kiếm sản phẩm theo từ khóa
     * @param keyword - Từ khóa tìm kiếm
     * @return List<ProductResponse>
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    /**
     * API lọc sản phẩm theo giá
     * @param minPrice - Giá tối thiểu
     * @param maxPrice - Giá tối đa
     * @return List<ProductResponse>
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponse>> filterProductsByPrice(@RequestParam double minPrice, @RequestParam double maxPrice) {
        return ResponseEntity.ok(productService.filterProductsByPrice(minPrice, maxPrice));
    }

    /**
     * API lấy sản phẩm theo danh mục
     * @param categoryId - ID danh mục
     * @return List<ProductResponse>
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * API phân trang sản phẩm
     * @param page - Số trang
     * @param size - Số sản phẩm mỗi trang
     * @return List<ProductResponse>
     */
    @GetMapping("/page")
    public ResponseEntity<List<ProductResponse>> getPagedProducts(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(productService.getPagedProducts(page, size));
    }

    /**
     * API lấy sản phẩm mới nhất
     * @param limit - Số lượng sản phẩm mới
     * @return List<ProductResponse>
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ProductResponse>> getLatestProducts(@RequestParam int limit) {
        return ResponseEntity.ok(productService.getLatestProducts(limit));
    }

    /**
     * API lấy sản phẩm bán chạy
     * @param limit - Số lượng sản phẩm bán chạy
     * @return List<ProductResponse>
     */
    @GetMapping("/bestselling")
    public ResponseEntity<List<ProductResponse>> getBestSellingProducts(@RequestParam int limit) {
        return ResponseEntity.ok(productService.getBestSellingProducts(limit));
    }
}
