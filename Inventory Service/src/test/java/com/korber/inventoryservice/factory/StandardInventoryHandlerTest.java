package com.korber.inventoryservice.factory;

import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;
import com.korber.inventoryservice.repository.InventoryBatchRepository;
import com.korber.inventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandardInventoryHandlerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @InjectMocks
    private StandardInventoryHandler inventoryHandler;

    private Product product;
    private List<InventoryBatch> inventoryBatches;

    @BeforeEach
    void setUp() {
        // Set up test data
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setSku("TEST-SKU-001");

        inventoryBatches = new ArrayList<>();
        
        InventoryBatch batch1 = new InventoryBatch();
        batch1.setId(1L);
        batch1.setBatchNumber("BATCH-001");
        batch1.setQuantity(100);
        batch1.setExpiryDate(LocalDate.now().plusMonths(3));
        batch1.setProduct(product);
        
        InventoryBatch batch2 = new InventoryBatch();
        batch2.setId(2L);
        batch2.setBatchNumber("BATCH-002");
        batch2.setQuantity(150);
        batch2.setExpiryDate(LocalDate.now().plusMonths(6));
        batch2.setProduct(product);
        
        inventoryBatches.add(batch1);
        inventoryBatches.add(batch2);
    }

    @Test
    @DisplayName("Should return inventory batches sorted by expiry date")
    void getInventoryBatchesByExpiryDate_ReturnsInventoryBatchesSortedByExpiryDate() {
        // Arrange
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1L)).thenReturn(inventoryBatches);

        // Act
        List<InventoryBatch> result = inventoryHandler.getInventoryBatchesByExpiryDate(product);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("BATCH-001", result.get(0).getBatchNumber()); // First batch has earlier expiry date
        verify(inventoryBatchRepository, times(1)).findByProductIdOrderByExpiryDateAsc(1L);
    }

    @Test
    @DisplayName("Should update inventory successfully when sufficient stock")
    void updateInventory_WhenSufficientStock_ReturnsTrue() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1L)).thenReturn(inventoryBatches);

        // Act
        boolean result = inventoryHandler.updateInventory(1L, 50);

        // Assert
        assertTrue(result);
        assertEquals(50, inventoryBatches.get(0).getQuantity()); // First batch reduced by 50
        assertEquals(150, inventoryBatches.get(1).getQuantity()); // Second batch unchanged
        verify(productRepository, times(1)).findById(1L);
        verify(inventoryBatchRepository, times(1)).findByProductIdOrderByExpiryDateAsc(1L);
        verify(inventoryBatchRepository, times(1)).save(inventoryBatches.get(0));
    }

    @Test
    @DisplayName("Should update inventory across multiple batches when needed")
    void updateInventory_WhenQuantitySpansMultipleBatches_ReturnsTrue() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1L)).thenReturn(inventoryBatches);

        // Act
        boolean result = inventoryHandler.updateInventory(1L, 120);

        // Assert
        assertTrue(result);
        assertEquals(0, inventoryBatches.get(0).getQuantity()); // First batch fully used
        assertEquals(130, inventoryBatches.get(1).getQuantity()); // Second batch reduced by 20
        verify(productRepository, times(1)).findById(1L);
        verify(inventoryBatchRepository, times(1)).findByProductIdOrderByExpiryDateAsc(1L);
        verify(inventoryBatchRepository, times(1)).save(inventoryBatches.get(0));
        verify(inventoryBatchRepository, times(1)).save(inventoryBatches.get(1));
    }

    @Test
    @DisplayName("Should return false when insufficient stock")
    void updateInventory_WhenInsufficientStock_ReturnsFalse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(1L)).thenReturn(inventoryBatches);

        // Act
        boolean result = inventoryHandler.updateInventory(1L, 300); // Total stock is only 250

        // Assert
        assertFalse(result);
        assertEquals(0, inventoryBatches.get(0).getQuantity()); // First batch fully used
        assertEquals(0, inventoryBatches.get(1).getQuantity()); // Second batch fully used
        verify(productRepository, times(1)).findById(1L);
        verify(inventoryBatchRepository, times(1)).findByProductIdOrderByExpiryDateAsc(1L);
        verify(inventoryBatchRepository, times(1)).save(inventoryBatches.get(0));
        verify(inventoryBatchRepository, times(1)).save(inventoryBatches.get(1));
    }

    @Test
    @DisplayName("Should return false when product not found")
    void updateInventory_WhenProductNotFound_ReturnsFalse() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = inventoryHandler.updateInventory(999L, 50);

        // Assert
        assertFalse(result);
        verify(productRepository, times(1)).findById(999L);
        verify(inventoryBatchRepository, never()).findByProductIdOrderByExpiryDateAsc(anyLong());
        verify(inventoryBatchRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return handler type as STANDARD")
    void getType_ReturnsSTANDARD() {
        // Act
        String type = inventoryHandler.getType();

        // Assert
        assertEquals("STANDARD", type);
    }
}
