package com.korber.inventoryservice.integration;

import com.korber.inventoryservice.dto.InventoryBatchDTO;
import com.korber.inventoryservice.dto.InventoryUpdateRequest;
import com.korber.inventoryservice.dto.InventoryUpdateResponse;
import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;
import com.korber.inventoryservice.repository.InventoryBatchRepository;
import com.korber.inventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InventoryServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryBatchRepository inventoryBatchRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Clear all data
        inventoryBatchRepository.deleteAll();
        productRepository.deleteAll();
        
        // Set up test data
        Product product = new Product();
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setSku("TEST-SKU-001");
        
        Product savedProduct = productRepository.save(product);
        
        InventoryBatch batch1 = new InventoryBatch();
        batch1.setBatchNumber("BATCH-001");
        batch1.setQuantity(100);
        batch1.setExpiryDate(LocalDate.now().plusMonths(3));
        batch1.setProduct(savedProduct);
        
        InventoryBatch batch2 = new InventoryBatch();
        batch2.setBatchNumber("BATCH-002");
        batch2.setQuantity(150);
        batch2.setExpiryDate(LocalDate.now().plusMonths(6));
        batch2.setProduct(savedProduct);
        
        inventoryBatchRepository.save(batch1);
        inventoryBatchRepository.save(batch2);
    }

    @Test
    @DisplayName("Should return inventory batches when product exists")
    void getInventoryBatchesByProductId_WhenProductExists_ReturnsInventoryBatches() {
        // Arrange
        Product product = productRepository.findAll().get(0);
        
        // Act
        ResponseEntity<List<InventoryBatchDTO>> response = restTemplate.exchange(
                baseUrl + "/inventory/" + product.getId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<InventoryBatchDTO>>() {}
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void getInventoryBatchesByProductId_WhenProductNotFound_Returns404() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/inventory/999",
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should update inventory successfully")
    void updateInventory_WhenInventoryUpdateSucceeds_ReturnsSuccessResponse() {
        // Arrange
        Product product = productRepository.findAll().get(0);
        
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(product.getId());
        request.setQuantity(50);
        request.setHandlerType("STANDARD");
        
        // Act
        ResponseEntity<InventoryUpdateResponse> response = restTemplate.postForEntity(
                baseUrl + "/inventory/update",
                new HttpEntity<>(request),
                InventoryUpdateResponse.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Inventory updated successfully", response.getBody().getMessage());
        
        // Verify inventory was actually updated
        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(product.getId());
        assertEquals(2, batches.size());
        assertEquals(50, batches.get(0).getQuantity()); // First batch should be reduced by 50
        assertEquals(150, batches.get(1).getQuantity()); // Second batch should be unchanged
    }

    @Test
    @DisplayName("Should return bad request when inventory update fails due to insufficient stock")
    void updateInventory_WhenInsufficientStock_ReturnsBadRequest() {
        // Arrange
        Product product = productRepository.findAll().get(0);
        
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(product.getId());
        request.setQuantity(300); // Total stock is only 250
        request.setHandlerType("STANDARD");
        
        // Act
        ResponseEntity<InventoryUpdateResponse> response = restTemplate.postForEntity(
                baseUrl + "/inventory/update",
                new HttpEntity<>(request),
                InventoryUpdateResponse.class
        );
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }
}
