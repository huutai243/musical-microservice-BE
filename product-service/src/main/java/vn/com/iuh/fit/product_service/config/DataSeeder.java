package vn.com.iuh.fit.product_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.product_service.entity.Category;
import vn.com.iuh.fit.product_service.entity.Product;
import vn.com.iuh.fit.product_service.repository.CategoryRepository;
import vn.com.iuh.fit.product_service.repository.ProductRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // Nếu chưa có category, thêm mới:
        if (categoryRepository.count() == 0) {
            Category guitarCategory = new Category();
            guitarCategory.setName("Guitar");

            Category pianoCategory = new Category();
            pianoCategory.setName("Piano");

            categoryRepository.saveAll(Arrays.asList(guitarCategory, pianoCategory));
        }

        // Nếu chưa có product, thêm mới:
        if (productRepository.count() == 0) {
            // Lấy category "Guitar" và "Piano" vừa tạo
            Category guitarCategory = categoryRepository.findAll()
                    .stream().filter(c -> c.getName().equals("Guitar")).findFirst().orElse(null);

            Category pianoCategory = categoryRepository.findAll()
                    .stream().filter(c -> c.getName().equals("Piano")).findFirst().orElse(null);

            Product guitarABC = new Product();
            guitarABC.setName("Guitar ABC");
            guitarABC.setDescription("Đàn guitar ABC chất lượng cao");
            guitarABC.setPrice(9000000);
            guitarABC.setStockQuantity(5);
            guitarABC.setImageUrl("http://127.0.0.1:9001/musicstore/guita1.jfif");
            guitarABC.setCategory(guitarCategory);

            Product pianoXYZ = new Product();
            pianoXYZ.setName("Piano XYZ");
            pianoXYZ.setDescription("Piano cao cấp, âm thanh hay");
            pianoXYZ.setPrice(15000000);
            pianoXYZ.setStockQuantity(20);
            pianoXYZ.setImageUrl("http://127.0.0.1:9001/musicstore/piano1.jpg");
            pianoXYZ.setCategory(pianoCategory);

            productRepository.saveAll(Arrays.asList(guitarABC, pianoXYZ));
        }
    }
}
