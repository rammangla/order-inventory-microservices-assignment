package com.korber.orderservice.service;

import com.korber.orderservice.dto.*;
import com.korber.orderservice.exception.InsufficientInventoryException;
import com.korber.orderservice.exception.ResourceNotFoundException;
import com.korber.orderservice.model.Order;
import com.korber.orderservice.model.OrderItem;
import com.korber.orderservice.repository.OrderRepository;
import com.korber.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;
    private Order order;
    private List<InventoryResponse> inventoryResponses;

    @BeforeEach
    void setUp() {
        // Setup order request
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(5)
                .price(10.0)
                .handlerType("FIFO")
                .build();

        orderRequest = OrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .orderItems(Collections.singletonList(itemRequest))
                .build();

        // Setup order entity
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId(1L);
        orderItem.setQuantity(5);
        orderItem.setPrice(10.0);
        orderItem.setHandlerType("FIFO");

        order = new Order();
        order.setId(1L);
        order.setCustomerName("John Doe");
        order.setCustomerEmail("john@example.com");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setTotalAmount(50.0);
        order.addOrderItem(orderItem);

        // Setup inventory response
        inventoryResponses = Collections.singletonList(
                InventoryResponse.builder()
                        .id(1L)
                        .productId(1L)
                        .quantity(10)
                        .expiryDate(LocalDate.now().plusMonths(6))
                        .batchNumber("BATCH001")
                        .build()
        );
    }

    @Test
    @DisplayName("Test create order - success scenario")
    void testCreateOrder_Success() {
        // Given
        when(inventoryService.checkInventory(anyLong())).thenReturn(inventoryResponses);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // When
        OrderResponse result = orderService.createOrder(orderRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(50.0, result.getTotalAmount());
        assertEquals(1, result.getOrderItems().size());

        verify(inventoryService, times(1)).checkInventory(1L);
        verify(inventoryService, times(1)).updateInventory(any(InventoryUpdateRequest.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Test create order - insufficient inventory")
    void testCreateOrder_InsufficientInventory() {
        // Given
        InventoryResponse insufficientInventory = InventoryResponse.builder()
                .id(1L)
                .productId(1L)
                .quantity(3) // Less than requested quantity (5)
                .expiryDate(LocalDate.now().plusMonths(6))
                .batchNumber("BATCH001")
                .build();

        when(inventoryService.checkInventory(anyLong()))
                .thenReturn(Collections.singletonList(insufficientInventory));

        // When & Then
        assertThrows(InsufficientInventoryException.class, () -> {
            orderService.createOrder(orderRequest);
        });

        verify(inventoryService, times(1)).checkInventory(1L);
        verify(inventoryService, never()).updateInventory(any(InventoryUpdateRequest.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Test get order by ID - success scenario")
    void testGetOrderById_Success() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        OrderResponse result = orderService.getOrderById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john@example.com", result.getCustomerEmail());
        assertEquals(1, result.getOrderItems().size());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test get order by ID - not found")
    void testGetOrderById_NotFound() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(999L);
        });

        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Test get all orders")
    void testGetAllOrders() {
        // Given
        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerName("Jane Smith");
        order2.setCustomerEmail("jane@example.com");
        order2.setOrderDate(LocalDateTime.now());
        order2.setStatus("PLACED");
        order2.setTotalAmount(75.0);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order, order2));

        // When
        List<OrderResponse> results = orderService.getAllOrders();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("John Doe", results.get(0).getCustomerName());
        assertEquals("Jane Smith", results.get(1).getCustomerName());

        verify(orderRepository, times(1)).findAll();
    }
}
