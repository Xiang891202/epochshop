package com.example.epochshop.repository;

import com.example.epochshop.entity.Cart;
import com.example.epochshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}