package com.example.epochshop;

import java.math.BigDecimal;
import java.util.Random;

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
            // 初始化管理员
            if (!userRepo.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(new BCryptPasswordEncoder().encode("123456"));
                admin.setRole("ROLE_ADMIN");
                admin.setDisplayName("管理員");
                userRepo.save(admin);
                System.out.println("✅ 管理員帳號已建立: admin / 123456");
            }

            // 初始化买家账号（如果不存在）
            if (!userRepo.existsByUsername("testuser")) {
                User buyer = new User();
                buyer.setUsername("testuser");
                buyer.setPassword(new BCryptPasswordEncoder().encode("123456"));
                buyer.setRole("ROLE_USER");
                buyer.setDisplayName("測試買家");
                userRepo.save(buyer);
                System.out.println("✅ 買家帳號已建立: testuser / 123456");
            }

            // 生成 100 个种子商品
            if (productRepo.count() == 0) {
                Random random = new Random();
                String[] names = {
                    "iPhone 15", "MacBook Pro", "AirPods Pro", "iPad Air", "Apple Watch",
                    "Samsung Galaxy S24", "Galaxy Tab", "Sony WH-1000XM5", "Nintendo Switch",
                    "PS5 光碟版", "Xbox Series X", "Dyson V15", "Roomba j7+", "Kindle Paperwhite",
                    "GoPro Hero 12", "DJI Mini 4 Pro", "Bose QuietComfort", "Sony A7 IV",
                    "Canon EOS R6", "Logitech MX Master 3S", "機械鍵盤 Keychron K2",
                    "Samsung 990 Pro SSD", "WD My Passport", "Razer DeathAdder", "SteelSeries Arctis",
                    "LG OLED C3", "Samsung Odyssey G9", "Apple Magic Keyboard", "Microsoft Surface Pro",
                    "Google Pixel 8"
                };
                String[] descriptions = {
                    "最新旗艦，效能王者", "專業級設備，無可取代", "熱銷好評，CP值爆表",
                    "限時特價，錯過不再", "經典不敗款，必買推薦", "超強續航，整天使用",
                    "極致輕薄，攜帶方便", "專業級效能，創作者首選", "沉浸式體驗，遊戲必備",
                    "智慧生活，從此開始"
                };
                String[] imageUrls = {
                    "https://picsum.photos/400/400?random=1",
                    "https://picsum.photos/400/400?random=2",
                    "https://picsum.photos/400/400?random=3",
                    "https://picsum.photos/400/400?random=4",
                    "https://picsum.photos/400/400?random=5",
                    "https://picsum.photos/400/400?random=6",
                    "https://picsum.photos/400/400?random=7",
                    "https://picsum.photos/400/400?random=8",
                    "https://picsum.photos/400/400?random=9",
                    "https://picsum.photos/400/400?random=10",
                };

                // 获取管理员作为卖家
                User seller = userRepo.findByUsername("admin").orElse(null);

                for (int i = 1; i <= 100; i++) {
                    Product product = new Product();
                    product.setName(names[random.nextInt(names.length)] + " (" + i + ")");
                    product.setDescription(descriptions[random.nextInt(descriptions.length)]);
                    product.setPrice(new BigDecimal(random.nextInt(50000) + 1000));
                    product.setStock(random.nextInt(100) + 5);
                    product.setImageUrl(imageUrls[random.nextInt(imageUrls.length)]);
                    product.setActive(true);
                    product.setSeller(seller);
                    productRepo.save(product);
                }
                System.out.println("✅ 100 個種子商品已新增");
            }
        };
    }
}