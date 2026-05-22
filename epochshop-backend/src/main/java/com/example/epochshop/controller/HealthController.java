package com.example.epochshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        // 简单返回200即可，前端只要请求成功就认为后端就绪
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}