package com.example.epochshop.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.epochshop.dto.OrderResponse;
import com.example.epochshop.entity.Cart;
import com.example.epochshop.entity.CartItem;
import com.example.epochshop.entity.Order;
import com.example.epochshop.entity.OrderItem;
import com.example.epochshop.entity.Product;
import com.example.epochshop.entity.User;
import com.example.epochshop.repository.CartRepository;
import com.example.epochshop.repository.OrderRepository;
import com.example.epochshop.repository.ProductRepository;
import com.example.epochshop.repository.UserRepository;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("購物車是空的"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("購物車是空的");
        }

        // 建立訂單
        Order order = new Order();
        order.setUser(user);

        // 扣庫存並建立明細
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            // 檢查庫存是否足夠（樂觀鎖會在 save 時驗證版本）
            if (product.getStock() < quantity) {
                throw new RuntimeException("商品 " + product.getName() + " 庫存不足");
            }

            product.setStock(product.getStock() - quantity);
            // 儲存商品時會自動檢查 @Version，若版本不符會拋 OptimisticLockException
            try {
                productRepository.save(product);
            } catch (OptimisticLockException e) {
                throw new RuntimeException("商品庫存已被其他交易修改，請重新確認");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());  // 記錄下單時價格
            order.getItems().add(orderItem);
        }

        // 計算總金額
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        orderRepository.save(order);

        // 清空購物車
        cart.getItems().clear();
        cartRepository.save(cart);

        return buildOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUser(user).stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse payOrder(Long orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("無權限操作");
        }
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);
        return buildOrderResponse(order);
    }

    private OrderResponse buildOrderResponse(Order order) {
        OrderResponse resp = new OrderResponse();
        resp.setOrderId(order.getId());
        resp.setCreatedAt(order.getCreatedAt());
        resp.setStatus(order.getStatus().name());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setItems(order.getItems().stream().map(item -> {
            OrderResponse.OrderItemResponse dto = new OrderResponse.OrderItemResponse();
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice());
            dto.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            return dto;
        }).collect(Collectors.toList()));
        return resp;
    }
}