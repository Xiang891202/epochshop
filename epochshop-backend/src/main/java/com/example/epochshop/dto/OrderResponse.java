package com.example.epochshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrderResponse {
    private Long orderId;
    private LocalDateTime createdAt;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;

    @Data
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}