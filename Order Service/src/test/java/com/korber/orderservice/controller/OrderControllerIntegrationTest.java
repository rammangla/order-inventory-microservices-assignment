package com.korber.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.korber.orderservice.dto.InventoryResponse;
import com.korber.orderservice.dto.OrderItemRequest;
import com.korber.orderservice.dto.OrderRequest;
import com.korber.orderservice.model.Order;
import com.korber.orderservice.model.OrderItem;
import com.korber.orderservice.repository.OrderRepository;
import com.korber.orderservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private InventoryService inventoryService;

    private OrderRequest orderRequest;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        orderRepository.deleteAll();

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

        // Setup inventory response
        InventoryResponse inventoryResponse = InventoryResponse.builder()
                .id(1L)
                .productId(1L)
                .quantity(10)
                .expiryDate(LocalDate.now().plusMonths(6))
                .batchNumber("BATCH001")
                .build();

        when(inventoryService.checkInventory(anyLong()))
                .thenReturn(Collections.singletonList(inventoryResponse));

        // Create a test order in the database
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(2L);
        orderItem.setQuantity(3);
        orderItem.setPrice(15.0);
        orderItem.setHandlerType("FIFO");

        testOrder = new Order();
        testOrder.setCustomerName("Jane Smith");
        testOrder.setCustomerEmail("jane@example.com");
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus("PLACED");
        testOrder.setTotalAmount(45.0);
        
        testOrder.addOrderItem(orderItem);
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("Integration test - Create order")
    void testCreateOrder() throws Exception {
        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName", is("John Doe")))
                .andExpect(jsonPath("$.customerEmail", is("john@example.com")))
                .andExpect(jsonPath("$.orderItems", hasSize(1)))
                .andExpect(jsonPath("$.orderItems[0].productId", is(1)))
                .andExpect(jsonPath("$.orderItems[0].quantity", is(5)))
                .andExpect(jsonPath("$.totalAmount", is(50.0)));
    }

    @Test
    @DisplayName("Integration test - Get order by ID")
    void testGetOrderById() throws Exception {
        mockMvc.perform(get("/order/{id}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testOrder.getId().intValue())))
                .andExpect(jsonPath("$.customerName", is("Jane Smith")))
                .andExpect(jsonPath("$.customerEmail", is("jane@example.com")))
                .andExpect(jsonPath("$.orderItems", hasSize(1)))
                .andExpect(jsonPath("$.orderItems[0].productId", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(45.0)));
    }

    @Test
    @DisplayName("Integration test - Get all orders")
    void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testOrder.getId().intValue())))
                .andExpect(jsonPath("$[0].customerName", is("Jane Smith")));
    }
}
