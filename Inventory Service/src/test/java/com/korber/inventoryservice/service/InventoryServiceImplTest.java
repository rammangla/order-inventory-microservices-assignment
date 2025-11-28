package com.korber.inventoryservice.service;

import com.korber.inventoryservice.dto.InventoryBatchDTO;
import com.korber.inventoryservice.dto.InventoryUpdateRequest;
import com.korber.inventoryservice.dto.InventoryUpdateResponse;
import com.korber.inventoryservice.factory.InventoryHandler;
import com.korber.inventoryservice.factory.InventoryHandlerFactory;
import com.korber.inventoryservice.model.InventoryBatch;
import com.korber.inventoryservice.model.Product;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryHandlerFactory inventoryHandlerFactory;

    @Mock
    private InventoryHandler inventoryHandler;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

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
        batch1.setExpiryDate(LocalDate.now().plusMonths(6));
        batch1.setProduct(product);
        
        InventoryBatch batch2 = new InventoryBatch();
        batch2.setId(2L);
        batch2.setBatchNumber("BATCH-002");
        batch2.setQuantity(150);
        batch2.setExpiryDate(LocalDate.now().plusMonths(12));
        batch2.setProduct(product);
        
        inventoryBatches.add(batch1);
        inventoryBatches.add(batch2);
    }

    @Test
    @DisplayName("Should return inventory batches when product exists")
    void getInventoryBatchesByProductId_WhenProductExists_ReturnsInventoryBatches() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(inventoryHandlerFactory.getDefaultHandler()).thenReturn(inventoryHandler);
        when(inventoryHandler.getInventoryBatchesByExpiryDate(product)).thenReturn(inventoryBatches);

        // Act
        List<InventoryBatchDTO> result = inventoryService.getInventoryBatchesByProductId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findById(1L);
        verify(inventoryHandlerFactory, times(1)).getDefaultHandler();
        verify(inventoryHandler, times(1)).getInventoryBatchesByExpiryDate(product);
    }

    @Test
    @DisplayName("Should return empty list when product does not exist")
    void getInventoryBatchesByProductId_WhenProductDoesNotExist_ReturnsEmptyList() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        List<InventoryBatchDTO> result = inventoryService.getInventoryBatchesByProductId(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository, times(1)).findById(999L);
        verify(inventoryHandlerFactory, never()).getDefaultHandler();
        verify(inventoryHandler, never()).getInventoryBatchesByExpiryDate(any());
    }

    @Test
    @DisplayName("Should update inventory successfully")
    void updateInventory_WhenInventoryUpdateSucceeds_ReturnsSuccessResponse() {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(50);
        request.setHandlerType("STANDARD");

        when(inventoryHandlerFactory.getHandler("STANDARD")).thenReturn(inventoryHandler);
        when(inventoryHandler.updateInventory(1L, 50)).thenReturn(true);

        // Act
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Inventory updated successfully", response.getMessage());
        assertEquals(1L, response.getProductId());
        assertEquals(50, response.getUpdatedQuantity());
        verify(inventoryHandlerFactory, times(1)).getHandler("STANDARD");
        verify(inventoryHandler, times(1)).updateInventory(1L, 50);
    }

    @Test
    @DisplayName("Should return failure response when inventory update fails")
    void updateInventory_WhenInventoryUpdateFails_ReturnsFailureResponse() {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(500); // More than available
        request.setHandlerType("STANDARD");

        when(inventoryHandlerFactory.getHandler("STANDARD")).thenReturn(inventoryHandler);
        when(inventoryHandler.updateInventory(1L, 500)).thenReturn(false);

        // Act
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Failed to update inventory. Insufficient stock or product not found.", response.getMessage());
        assertEquals(1L, response.getProductId());
        assertEquals(500, response.getUpdatedQuantity());
        verify(inventoryHandlerFactory, times(1)).getHandler("STANDARD");
        verify(inventoryHandler, times(1)).updateInventory(1L, 500);
    }

    @Test
    @DisplayName("Should use default handler when handler type is null")
    void updateInventory_WhenHandlerTypeIsNull_UsesDefaultHandler() {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(50);
        request.setHandlerType(null);

        when(inventoryHandlerFactory.getDefaultHandler()).thenReturn(inventoryHandler);
        when(inventoryHandler.updateInventory(1L, 50)).thenReturn(true);

        // Act
        InventoryUpdateResponse response = inventoryService.updateInventory(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(inventoryHandlerFactory, times(1)).getDefaultHandler();
        verify(inventoryHandler, times(1)).updateInventory(1L, 50);
    }
}
