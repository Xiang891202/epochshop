package com.example.epochshop;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.epochshop.entity.Product;
import com.example.epochshop.entity.User;
import com.example.epochshop.repository.ProductRepository;
import com.example.epochshop.repository.UserRepository;

@SpringBootApplication
public class EpochshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(EpochshopApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(ProductRepository productRepo, UserRepository userRepo) {
        return args -> {
            // 初始化商品
            if (productRepo.count() == 0) {
                Product p = new Product();
                p.setName("iPhone 15 示範機");
                p.setDescription("最新 A17 晶片，超強效能");
                p.setPrice(new BigDecimal("29900"));
                p.setStock(10);
                productRepo.save(p);
                System.out.println("✅ 示範商品已新增");
            }

            // 初始化管理员
            if (!userRepo.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(new BCryptPasswordEncoder().encode("123456"));
                admin.setRole("ROLE_ADMIN");
                userRepo.save(admin);
                System.out.println("✅ 管理員帳號已建立: admin / 123456");
            }
        };
    }
}