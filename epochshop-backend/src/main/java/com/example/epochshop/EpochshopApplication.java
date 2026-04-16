package com.example.epochshop;

import com.example.epochshop.entity.Product;
import com.example.epochshop.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.math.BigDecimal;

@SpringBootApplication
public class EpochshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(EpochshopApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(ProductRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                Product p = new Product();
                p.setName("iPhone 15 示範機");
                p.setDescription("最新 A17 晶片，超強效能");
                p.setPrice(new BigDecimal("29900"));
                p.setStock(10);
                repo.save(p);
                System.out.println("✅ 示範商品已新增");
            }
        };
    }
}