# EpochShop 瞬購秒殺商城（全端示範專案）

本專案展示前後端分離的電商平台基礎架構，已完成商品 API 與前端資料呈現。

## 🗂️ 專案結構
- `epochshop-backend/`：Spring Boot 3 後端，連接 Railway MySQL，提供 REST API
- `epochshop-frontend/`：Vue 3 前端，使用 Axios 串接後端，顯示商品列表

## ⚙️ 啟動方式
### 後端
```bash
cd epochshop-backend
./mvnw spring-boot:run