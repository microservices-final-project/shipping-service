package com.selimhorri.app.integration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.OrderStatus;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.service.OrderItemService;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
class OrderItemResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderItemService orderItemService;

    @MockBean
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldFetchAllOrderItems() throws Exception {
        // Mock data
        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Product 1")
                .priceUnit(10.0)
                .quantity(100)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.ORDERED.name())
                .orderDate(LocalDateTime.now())
                .build();

        OrderItemDto orderItemDto1 = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
                .productDto(productDto)
                .orderDto(orderDto)
                .build();

        OrderItemDto orderItemDto2 = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(3)
                .productDto(productDto)
                .orderDto(orderDto)
                .build();

        List<OrderItemDto> orderItemDtos = List.of(orderItemDto1, orderItemDto2);

        // Mock service call
        when(orderItemService.findAll()).thenReturn(orderItemDtos);

        // Perform request and verify
        mockMvc.perform(get("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.collection.length()").value(2))
                .andExpect(jsonPath("$.collection[0].productId").value(1))
                .andExpect(jsonPath("$.collection[0].orderId").value(1))
                .andExpect(jsonPath("$.collection[1].productId").value(1))
                .andExpect(jsonPath("$.collection[1].orderId").value(1));

        verify(orderItemService, times(1)).findAll();
    }

    @Test
    void shouldFetchOrderItemById() throws Exception {
        // Mock data
        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Product 1")
                .priceUnit(10.0)
                .quantity(100)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.ORDERED.name())
                .orderDate(LocalDateTime.now())
                .build();

        OrderItemDto orderItemDto = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
                .productDto(productDto)
                .orderDto(orderDto)
                .build();

        // Mock service call
        when(orderItemService.findById(anyInt())).thenReturn(orderItemDto);

        // Perform request and verify
        mockMvc.perform(get("/api/shippings/{orderId}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.orderedQuantity").value(2))
                .andExpect(jsonPath("$.product.productId").value(1))
                .andExpect(jsonPath("$.order.orderId").value(1));

        verify(orderItemService, times(1)).findById(1);
    }

    @Test
    void shouldReturnNotFoundWhenOrderItemDoesNotExist() throws Exception {
        // Mock service to throw exception
        when(orderItemService.findById(anyInt()))
                .thenThrow(new OrderItemNotFoundException("OrderItem with id: 999 not found"));

        // Perform request and verify
        mockMvc.perform(get("/api/shippings/{orderId}", 999)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderItemService, times(1)).findById(999);
    }

    @Test
    void shouldSaveOrderItem() throws Exception {
        // Mock data
        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Product 1")
                .priceUnit(10.0)
                .quantity(100)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .orderDate(LocalDateTime.now())
                .build();

        OrderItemDto inputDto = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
                .build();

        OrderItemDto savedDto = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(2)
                .productDto(productDto)
                .orderDto(orderDto)
                .build();

        // Mock service calls
        when(orderItemService.save(any(OrderItemDto.class))).thenReturn(savedDto);
        when(restTemplate.getForObject(
                eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/1"), 
                eq(ProductDto.class)))
                .thenReturn(productDto);
        when(restTemplate.getForObject(
                eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"), 
                eq(OrderDto.class)))
                .thenReturn(orderDto);

        // Perform request and verify
        mockMvc.perform(post("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.orderedQuantity").value(2));

        verify(orderItemService, times(1)).save(any(OrderItemDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenProductNotAvailable() throws Exception {
        // Mock data
        OrderItemDto inputDto = OrderItemDto.builder()
                .productId(1)
                .orderId(1)
                .orderedQuantity(200) // Más de lo disponible
                .build();

        ProductDto productDto = ProductDto.builder()
                .productId(1)
                .quantity(100) // Solo 100 disponibles
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();

        // Mock service calls
        when(restTemplate.getForObject(
                eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/1"), 
                eq(ProductDto.class)))
                .thenReturn(productDto);
        when(restTemplate.getForObject(
                eq(AppConstant.DiscoveredDomainsApi.ORDER_SERVICE_API_URL + "/1"), 
                eq(OrderDto.class)))
                .thenReturn(orderDto);
        when(orderItemService.save(any(OrderItemDto.class)))
                .thenThrow(new IllegalArgumentException("You cannot order more units than there is available"));

        // Perform request and verify
        mockMvc.perform(post("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        verify(orderItemService, times(1)).save(any(OrderItemDto.class));
    }

    @Test
    void shouldDeleteOrderItem() throws Exception {
        // Mock service (deleteById is void)
        doNothing().when(orderItemService).deleteById(anyInt());

        // Perform request and verify
        mockMvc.perform(delete("/api/shippings/{orderId}", 1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(orderItemService, times(1)).deleteById(1);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingOrderItem() throws Exception {
        // Mock service to throw exception
        doThrow(new OrderItemNotFoundException("OrderItem with id: 999 not found"))
                .when(orderItemService).deleteById(999);

        // Perform request and verify
        mockMvc.perform(delete("/api/shippings/{orderId}", 999)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderItemService, times(1)).deleteById(999);
    }

    @Test
    void shouldReturnBadRequestWhenSaveWithNullOrderItem() throws Exception {
        mockMvc.perform(post("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());

        verify(orderItemService, never()).save(any());
    }
}