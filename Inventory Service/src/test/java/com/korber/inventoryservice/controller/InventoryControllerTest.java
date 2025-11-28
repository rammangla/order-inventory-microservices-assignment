package com.korber.inventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korber.inventoryservice.dto.InventoryBatchDTO;
import com.korber.inventoryservice.dto.InventoryUpdateRequest;
import com.korber.inventoryservice.dto.InventoryUpdateResponse;
import com.korber.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("Should return inventory batches when product exists")
    void getInventoryBatchesByProductId_WhenProductExists_ReturnsInventoryBatches() throws Exception {
        // Arrange
        List<InventoryBatchDTO> batches = new ArrayList<>();
        
        InventoryBatchDTO batch1 = new InventoryBatchDTO();
        batch1.setId(1L);
        batch1.setBatchNumber("BATCH-001");
        batch1.setQuantity(100);
        batch1.setExpiryDate(LocalDate.now().plusMonths(3));
        batch1.setProductId(1L);
        
        InventoryBatchDTO batch2 = new InventoryBatchDTO();
        batch2.setId(2L);
        batch2.setBatchNumber("BATCH-002");
        batch2.setQuantity(150);
        batch2.setExpiryDate(LocalDate.now().plusMonths(6));
        batch2.setProductId(1L);
        
        batches.add(batch1);
        batches.add(batch2);
        
        when(inventoryService.getInventoryBatchesByProductId(1L)).thenReturn(batches);

        // Act & Assert
        mockMvc.perform(get("/inventory/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].batchNumber", is("BATCH-001")))
                .andExpect(jsonPath("$[0].quantity", is(100)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].batchNumber", is("BATCH-002")))
                .andExpect(jsonPath("$[1].quantity", is(150)));
    }

    @Test
    @DisplayName("Should return 404 when product not found")
    void getInventoryBatchesByProductId_WhenProductNotFound_Returns404() throws Exception {
        // Arrange
        when(inventoryService.getInventoryBatchesByProductId(999L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/inventory/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update inventory successfully")
    void updateInventory_WhenInventoryUpdateSucceeds_ReturnsSuccessResponse() throws Exception {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(50);
        request.setHandlerType("STANDARD");

        InventoryUpdateResponse response = new InventoryUpdateResponse();
        response.setSuccess(true);
        response.setMessage("Inventory updated successfully");
        response.setProductId(1L);
        response.setUpdatedQuantity(50);

        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Inventory updated successfully")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.updatedQuantity", is(50)));
    }

    @Test
    @DisplayName("Should return bad request when inventory update fails")
    void updateInventory_WhenInventoryUpdateFails_ReturnsBadRequest() throws Exception {
        // Arrange
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(1L);
        request.setQuantity(500); // More than available
        request.setHandlerType("STANDARD");

        InventoryUpdateResponse response = new InventoryUpdateResponse();
        response.setSuccess(false);
        response.setMessage("Failed to update inventory. Insufficient stock or product not found.");
        response.setProductId(1L);
        response.setUpdatedQuantity(500);

        when(inventoryService.updateInventory(any(InventoryUpdateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/inventory/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Failed to update inventory. Insufficient stock or product not found.")))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.updatedQuantity", is(500)));
    }
}
