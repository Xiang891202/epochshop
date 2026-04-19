package com.example.epochshop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.epochshop.entity.Order;
import com.example.epochshop.entity.User;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}