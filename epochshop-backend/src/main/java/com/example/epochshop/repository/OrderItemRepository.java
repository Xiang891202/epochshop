package com.example.epochshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.epochshop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    // 用於 SQL 優化案例的慢查詢（故意不 JOIN FETCH 來模擬 N+1 或大表掃描）
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.idempotentKey = :key")
    boolean existsByIdempotentKey(@Param("key") String idempotentKey);
}
