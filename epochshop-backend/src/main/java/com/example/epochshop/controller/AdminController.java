package com.example.epochshop.controller;

import com.example.epochshop.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理員功能", description = "銷售額儀表板 API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final OrderRepository orderRepository;

    @Operation(summary = "取得銷售額儀表板資料")
    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSales() {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalSales = orderRepository.getTotalSales();
        Long paidOrders = orderRepository.getPaidOrderCount();
        List<Object[]> productSales = orderRepository.getSalesByProduct();

        result.put("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);
        result.put("paidOrders", paidOrders != null ? paidOrders : 0);
        result.put("productSales", productSales != null ? productSales : new ArrayList<>());

        return ResponseEntity.ok(result);
    }
}