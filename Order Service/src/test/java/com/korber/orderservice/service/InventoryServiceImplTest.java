package com.korber.orderservice.service;

import com.korber.orderservice.dto.InventoryResponse;
import com.korber.orderservice.dto.InventoryUpdateRequest;
import com.korber.orderservice.exception.InventoryServiceException;
import com.korber.orderservice.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private final String INVENTORY_SERVICE_URL = "http://localhost:8082";
    private InventoryResponse inventoryResponse;
    private InventoryUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventoryService, "inventoryServiceUrl", INVENTORY_SERVICE_URL);

        inventoryResponse = InventoryResponse.builder()
                .id(1L)
                .productId(1L)
                .quantity(10)
                .expiryDate(LocalDate.now().plusMonths(6))
                .batchNumber("BATCH001")
                .build();

        updateRequest = InventoryUpdateRequest.builder()
                .productId(1L)
                .quantity(5)
                .handlerType("FIFO")
                .build();
    }

    @Test
    @DisplayName("Test check inventory - success scenario")
    void testCheckInventory_Success() {
        // Given
        List<InventoryResponse> expectedResponse = Collections.singletonList(inventoryResponse);
        ResponseEntity<List<InventoryResponse>> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        // When
        List<InventoryResponse> result = inventoryService.checkInventory(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(10, result.get(0).getQuantity());

        verify(restTemplate, times(1)).exchange(
                eq(INVENTORY_SERVICE_URL + "/inventory/1"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    @DisplayName("Test check inventory - service exception")
    void testCheckInventory_ServiceException() {
        // Given
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RestClientException("Service unavailable"));

        // When & Then
        assertThrows(InventoryServiceException.class, () -> {
            inventoryService.checkInventory(1L);
        });

        verify(restTemplate, times(1)).exchange(
                eq(INVENTORY_SERVICE_URL + "/inventory/1"),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    @DisplayName("Test update inventory - success scenario")
    void testUpdateInventory_Success() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // When & Then (no exception should be thrown)
        assertDoesNotThrow(() -> {
            inventoryService.updateInventory(updateRequest);
        });

        verify(restTemplate, times(1)).postForEntity(
                eq(INVENTORY_SERVICE_URL + "/inventory/update"),
                eq(updateRequest),
                eq(Void.class)
        );
    }

    @Test
    @DisplayName("Test update inventory - service exception")
    void testUpdateInventory_ServiceException() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        // When & Then
        assertThrows(InventoryServiceException.class, () -> {
            inventoryService.updateInventory(updateRequest);
        });

        verify(restTemplate, times(1)).postForEntity(
                eq(INVENTORY_SERVICE_URL + "/inventory/update"),
                eq(updateRequest),
                eq(Void.class)
        );
    }
}
