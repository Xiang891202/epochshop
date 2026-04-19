package com.example.epochshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epochshop.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
}
