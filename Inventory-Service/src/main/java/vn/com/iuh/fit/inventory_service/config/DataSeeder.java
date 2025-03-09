package vn.com.iuh.fit.inventory_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.com.iuh.fit.inventory_service.entity.Inventory;
import vn.com.iuh.fit.inventory_service.repository.InventoryRepository;

import java.util.Arrays;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;

    public DataSeeder(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) {
        if (inventoryRepository.count() == 0) {
            List<Inventory> initialStock = Arrays.asList(
                    new Inventory(1L, 10),  // Sản phẩm ID = 1 có 10 cái trong kho
                    new Inventory(2L, 5)    // Sản phẩm ID = 2 có 5 cái trong kho
            );

            inventoryRepository.saveAll(initialStock);
            System.out.println("✅ Inventory seeded successfully!");
        } else {
            System.out.println("📦 Inventory already exists, skipping seeding.");
        }
    }
}
