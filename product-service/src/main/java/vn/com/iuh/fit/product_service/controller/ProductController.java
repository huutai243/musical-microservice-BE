package vn.com.iuh.fit.product_service.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.iuh.fit.product_service.dto.ProductRequest;
import vn.com.iuh.fit.product_service.dto.ProductResponse;
import vn.com.iuh.fit.product_service.service.AuthClient;
import vn.com.iuh.fit.product_service.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthClient authClient;

    /**
     *  API lấy danh sách sản phẩm (Yêu cầu JWT token)
     */
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestHeader("Authorization") String token) {
        if (!authClient.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     *  API lấy chi tiết sản phẩm theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     *  API thêm sản phẩm
     */
    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest productRequest) {
        return ResponseEntity.ok(productService.addProduct(productRequest));
    }

    /**
     *  API xóa sản phẩm theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     *  API Upload ảnh sản phẩm lên MinIO
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(productService.uploadFile(file));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}



