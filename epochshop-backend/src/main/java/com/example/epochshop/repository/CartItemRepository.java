package com.example.epochshop.repository;

import com.example.epochshop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByIdempotentKey(String idempotentKey);
    Optional<CartItem> findByIdempotentKey(String idempotentKey);
}