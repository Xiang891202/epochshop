package com.example.epochshop;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // 可選，用於未來環境隔離
class EpochshopApplicationTests {

    @Test
    void contextLoads() {
        // 單純驗證 Spring 容器能成功啟動
    }
}