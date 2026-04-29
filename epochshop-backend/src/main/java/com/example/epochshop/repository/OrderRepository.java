package com.example.epochshop.repository;

import com.example.epochshop.entity.Order;
import com.example.epochshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    boolean existsByIdempotentKey(String idempotentKey);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'PAID'")
    BigDecimal getTotalSales();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PAID'")
    Long getPaidOrderCount();

    @Query("SELECT oi.product.name, SUM(oi.quantity), SUM(oi.unitPrice * oi.quantity) " +
           "FROM OrderItem oi WHERE oi.order.status = 'PAID' " +
           "GROUP BY oi.product.name ORDER BY SUM(oi.unitPrice * oi.quantity) DESC")
    List<Object[]> getSalesByProduct();
}