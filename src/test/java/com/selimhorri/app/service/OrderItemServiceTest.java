package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.OrderStatus;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.helper.OrderItemMappingHelper;
import com.selimhorri.app.repository.OrderItemRepository;
import com.selimhorri.app.service.impl.OrderItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;
    
    @Mock
    private RestTemplate restTemplate;
    
    @InjectMocks
    private OrderItemServiceImpl orderItemService;
    
    private OrderItem orderItem;
    private OrderItemDto orderItemDto;
    private ProductDto productDto;
    private OrderDto orderDto;
    
    @BeforeEach
    void setUp() {
        orderItem = OrderItem.builder()
                .orderId(1)
                .productId(1)
                .orderedQuantity(5)
                .isActive(true)
                .build();
        
        productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Test Product")
                .quantity(10)
                .build();
        
        orderDto = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.ORDERED.name())
                .build();
        
        orderItemDto = OrderItemDto.builder()
                .orderId(1)
                .productId(1)
                .orderedQuantity(5)
                .productDto(productDto)
                .orderDto(orderDto)
                .build();
    }
    
    @Test
    void findAll_ShouldReturnListOfOrderItemDtos_WhenOrderItemsExist() {
        // Arrange
        when(orderItemRepository.findByIsActiveTrue()).thenReturn(List.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        
        // Act
        List<OrderItemDto> result = orderItemService.findAll();
        
        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(orderItemRepository, times(1)).findByIsActiveTrue();
    }
    
    @Test
    void findAll_ShouldReturnEmptyList_WhenNoActiveOrderItemsExist() {
        // Arrange
        when(orderItemRepository.findByIsActiveTrue()).thenReturn(List.of());
        
        // Act
        List<OrderItemDto> result = orderItemService.findAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderItemRepository, times(1)).findByIsActiveTrue();
    }
    
    @Test
    void findAll_ShouldFilterOutItems_WhenProductNotFound() {
        // Arrange
        when(orderItemRepository.findByIsActiveTrue()).thenReturn(List.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(null);
        
        // Act
        List<OrderItemDto> result = orderItemService.findAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findAll_ShouldFilterOutItems_WhenOrderNotFound() {
        // Arrange
        when(orderItemRepository.findByIsActiveTrue()).thenReturn(List.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(null);
        
        // Act
        List<OrderItemDto> result = orderItemService.findAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findAll_ShouldFilterOutItems_WhenOrderStatusNotOrdered() {
        // Arrange
        OrderDto wrongStatusOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();
        
        when(orderItemRepository.findByIsActiveTrue()).thenReturn(List.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(wrongStatusOrder);
        
        // Act
        List<OrderItemDto> result = orderItemService.findAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findById_ShouldReturnOrderItemDto_WhenOrderItemExists() {
        // Arrange
        when(orderItemRepository.findById(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        
        // Act
        OrderItemDto result = orderItemService.findById(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals(1, result.getProductId());
        assertEquals(5, result.getOrderedQuantity());
        assertNotNull(result.getProductDto());
        assertNotNull(result.getOrderDto());
    }
    
    @Test
    void findById_ShouldThrowOrderItemNotFoundException_WhenOrderItemNotFound() {
        // Arrange
        when(orderItemRepository.findById(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.findById(1);
        });
    }
    
    @Test
    void findById_ShouldThrowOrderItemNotFoundException_WhenOrderItemNotActive() {
        // Arrange
        OrderItem inactiveOrderItem = OrderItem.builder()
                .orderId(1)
                .productId(1)
                .orderedQuantity(5)
                .isActive(false)
                .build();
        
        when(orderItemRepository.findById(anyInt())).thenReturn(Optional.of(inactiveOrderItem));
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.findById(1);
        });
    }
    
    @Test
    void findById_ShouldThrowOrderItemNotFoundException_WhenProductNotFound() {
        // Arrange
        when(orderItemRepository.findById(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenThrow(RestClientException.class);
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.findById(1);
        });
    }
    
    @Test
    void findById_ShouldThrowOrderItemNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderItemRepository.findById(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenThrow(RestClientException.class);
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.findById(1);
        });
    }
    
    @Test
    void findById_ShouldThrowOrderItemNotFoundException_WhenOrderStatusNotOrdered() {
        // Arrange
        OrderDto wrongStatusOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();
        
        when(orderItemRepository.findById(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(wrongStatusOrder);
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.findById(1);
        });
    }
    
    @Test
    void save_ShouldReturnSavedOrderItemDto_WhenValidInput() {
        // Arrange
        OrderDto createdOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();
        
        when(restTemplate.getForObject(contains("/orders/1"), eq(OrderDto.class))).thenReturn(createdOrder);
        when(restTemplate.getForObject(contains("/products/1"), eq(ProductDto.class))).thenReturn(productDto);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        
        // Act
        OrderItemDto result = orderItemService.save(orderItemDto);
        
        // Assert
        assertNotNull(result);
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(restTemplate, times(1)).patchForObject(anyString(), isNull(), eq(Void.class));
    }
    
    @Test
    void save_ShouldThrowIllegalArgumentException_WhenMissingRequiredFields() {
        // Arrange
        OrderItemDto invalidDto = new OrderItemDto();
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderItemService.save(invalidDto);
        });
    }
    
    @Test
    void save_ShouldThrowOrderItemNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(restTemplate.getForObject(contains("/orders/1"), eq(OrderDto.class))).thenReturn(null);
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.save(orderItemDto);
        });
    }
    
    @Test
    void save_ShouldThrowIllegalArgumentException_WhenOrderStatusNotCreated() {
        // Arrange
        OrderDto orderedOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.ORDERED.name())
                .build();
        
        when(restTemplate.getForObject(contains("/orders/1"), eq(OrderDto.class))).thenReturn(orderedOrder);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderItemService.save(orderItemDto);
        });
    }
    
    @Test
    void save_ShouldThrowOrderItemNotFoundException_WhenProductNotFound() {
        // Arrange
        OrderDto createdOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();
        
        when(restTemplate.getForObject(contains("/orders/1"), eq(OrderDto.class))).thenReturn(createdOrder);
        when(restTemplate.getForObject(contains("/products/1"), eq(ProductDto.class))).thenReturn(null);
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.save(orderItemDto);
        });
    }
    
    @Test
    void save_ShouldThrowIllegalArgumentException_WhenInsufficientProductQuantity() {
        // Arrange
        OrderDto createdOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();
        
        ProductDto lowQuantityProduct = ProductDto.builder()
                .productId(1)
                .quantity(2) // Less than ordered quantity (5)
                .build();
        
        when(restTemplate.getForObject(contains("/orders/1"), eq(OrderDto.class))).thenReturn(createdOrder);
        when(restTemplate.getForObject(contains("/products/1"), eq(ProductDto.class))).thenReturn(lowQuantityProduct);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            orderItemService.save(orderItemDto);
        });
    }
    
    @Test
    void deleteById_ShouldDeactivateOrderItem_WhenValidId() {
        // Arrange
        when(orderItemRepository.findByOrderIdAndIsActiveTrue(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        
        // Act
        orderItemService.deleteById(1);
        
        // Assert
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        assertFalse(orderItem.isActive());
    }
    
    @Test
    void deleteById_ShouldThrowOrderItemNotFoundException_WhenOrderItemNotFound() {
        // Arrange
        when(orderItemRepository.findByOrderIdAndIsActiveTrue(anyInt())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.deleteById(1);
        });
    }
    
    @Test
    void deleteById_ShouldThrowOrderItemNotFoundException_WhenOrderNotFound() {
        // Arrange
        when(orderItemRepository.findByOrderIdAndIsActiveTrue(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(null);
        
        // Act & Assert
        assertThrows(OrderItemNotFoundException.class, () -> {
            orderItemService.deleteById(1);
        });
    }
    
    @Test
    void deleteById_ShouldThrowIllegalStateException_WhenOrderStatusNotOrdered() {
        // Arrange
        OrderDto wrongStatusOrder = OrderDto.builder()
                .orderId(1)
                .orderStatus(OrderStatus.CREATED.name())
                .build();
        
        when(orderItemRepository.findByOrderIdAndIsActiveTrue(anyInt())).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(wrongStatusOrder);
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            orderItemService.deleteById(1);
        });
    }
}