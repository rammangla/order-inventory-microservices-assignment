package com.korber.inventoryservice.config;

import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;
import com.korber.inventoryservice.repository.InventoryBatchRepository;
import com.korber.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Configuration class to initialize sample data for testing
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create products
            Product product1 = new Product();
            product1.setName("Paracetamol");
            product1.setDescription("Pain reliever and fever reducer");
            product1.setSku("MED-PARA-001");
            
            Product product2 = new Product();
            product2.setName("Amoxicillin");
            product2.setDescription("Antibiotic medication");
            product2.setSku("MED-AMOX-001");
            
            Product product3 = new Product();
            product3.setName("Vitamin C");
            product3.setDescription("Dietary supplement");
            product3.setSku("SUP-VITC-001");
            
            // Save products
            productRepository.saveAll(Arrays.asList(product1, product2, product3));
            
            // Create inventory batches for product 1
            InventoryBatch batch1 = new InventoryBatch();
            batch1.setBatchNumber("BATCH-001");
            batch1.setQuantity(100);
            batch1.setExpiryDate(LocalDate.now().plusMonths(6));
            batch1.setProduct(product1);
            
            InventoryBatch batch2 = new InventoryBatch();
            batch2.setBatchNumber("BATCH-002");
            batch2.setQuantity(150);
            batch2.setExpiryDate(LocalDate.now().plusMonths(12));
            batch2.setProduct(product1);
            
            InventoryBatch batch3 = new InventoryBatch();
            batch3.setBatchNumber("BATCH-003");
            batch3.setQuantity(200);
            batch3.setExpiryDate(LocalDate.now().plusMonths(18));
            batch3.setProduct(product1);
            
            // Create inventory batches for product 2
            InventoryBatch batch4 = new InventoryBatch();
            batch4.setBatchNumber("BATCH-004");
            batch4.setQuantity(75);
            batch4.setExpiryDate(LocalDate.now().plusMonths(3));
            batch4.setProduct(product2);
            
            InventoryBatch batch5 = new InventoryBatch();
            batch5.setBatchNumber("BATCH-005");
            batch5.setQuantity(125);
            batch5.setExpiryDate(LocalDate.now().plusMonths(9));
            batch5.setProduct(product2);
            
            // Create inventory batches for product 3
            InventoryBatch batch6 = new InventoryBatch();
            batch6.setBatchNumber("BATCH-006");
            batch6.setQuantity(300);
            batch6.setExpiryDate(LocalDate.now().plusMonths(24));
            batch6.setProduct(product3);
            
            // Save inventory batches
            inventoryBatchRepository.saveAll(Arrays.asList(batch1, batch2, batch3, batch4, batch5, batch6));
            
            System.out.println("Sample data initialized successfully!");
        };
    }
}
