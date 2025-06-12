package com.selimhorri.app.resource;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.exception.ApiExceptionHandler;
import com.selimhorri.app.service.OrderItemService;

@ExtendWith(MockitoExtension.class)
class OrderItemResourceTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @Mock
    private OrderItemService orderItemService;
    
    @InjectMocks
    private OrderItemResource orderItemResource;
    
    private OrderItemDto orderItemDto;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderItemResource).setControllerAdvice(new ApiExceptionHandler())
        .build();
        objectMapper = new ObjectMapper();
        
        orderItemDto = OrderItemDto.builder()
                .orderId(1)
                .productId(1)
                .orderedQuantity(5)
                .build();
    }
    
    
    @Test
    void findById_ShouldReturnOrderItem_WhenValidId() throws Exception {
        // Arrange
        when(orderItemService.findById(anyInt())).thenReturn(orderItemDto);
        
        // Act & Assert
        mockMvc.perform(get("/api/shippings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
        
        verify(orderItemService, times(1)).findById(1);
    }
    
    @Test
    void save_ShouldReturnSavedOrderItem() throws Exception {
        // Arrange
        when(orderItemService.save(any(OrderItemDto.class))).thenReturn(orderItemDto);
        
        // Act & Assert
        mockMvc.perform(post("/api/shippings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
        
        verify(orderItemService, times(1)).save(any(OrderItemDto.class));
    }
    
    @Test
    void deleteById_ShouldReturnTrue_WhenSuccessful() throws Exception {
        // Arrange
        doNothing().when(orderItemService).deleteById(anyInt());
        
        // Act & Assert
        mockMvc.perform(delete("/api/shippings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
        
        verify(orderItemService, times(1)).deleteById(1);
    }
    

}