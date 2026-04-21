package com.example.epochshop.repository;

import com.example.epochshop.entity.Order;
import com.example.epochshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    boolean existsByIdempotentKey(String idempotentKey);
}