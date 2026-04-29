package com.example.epochshop.controller;

import com.example.epochshop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getSales_ShouldReturnDashboardData() {
        // Mock 数据
        when(orderRepository.getTotalSales()).thenReturn(new BigDecimal("299000"));
        when(orderRepository.getPaidOrderCount()).thenReturn(10L);

        List<Object[]> productSales = new ArrayList<>();
        productSales.add(new Object[]{"iPhone 15", 5L, new BigDecimal("149500")});
        productSales.add(new Object[]{"MacBook Pro", 2L, new BigDecimal("118000")});
        when(orderRepository.getSalesByProduct()).thenReturn(productSales);

        // 调用
        ResponseEntity<Map<String, Object>> response = adminController.getSales();

        // 验证
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("totalSales")).isEqualTo(new BigDecimal("299000"));
        assertThat(body.get("paidOrders")).isEqualTo(10L);

        @SuppressWarnings("unchecked")
        List<Object[]> salesList = (List<Object[]>) body.get("productSales");
        assertThat(salesList).hasSize(2);
        assertThat(salesList.get(0)[0]).isEqualTo("iPhone 15");
        assertThat(salesList.get(0)[1]).isEqualTo(5L);
        assertThat(salesList.get(0)[2]).isEqualTo(new BigDecimal("149500"));
    }

    @Test
    void getSales_ShouldHandleNullValues() {
        // Mock 空数据
        when(orderRepository.getTotalSales()).thenReturn(null);
        when(orderRepository.getPaidOrderCount()).thenReturn(null);
        when(orderRepository.getSalesByProduct()).thenReturn(null);

        // 调用
        ResponseEntity<Map<String, Object>> response = adminController.getSales();

        // 验证 null 安全处理
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("totalSales")).isEqualTo(BigDecimal.ZERO);
        assertThat(body.get("paidOrders")).isEqualTo(0L);
        assertThat(body.get("productSales")).isEqualTo(new ArrayList<>());
    }
}