# 🛒 EpochShop 瞬購秒殺商城（全端示範專案）

本專案為前後端分離的電商平台，涵蓋使用者認證、商品瀏覽、購物車、訂單管理與庫存樂觀鎖等核心功能。

## 📁 專案結構
epochshop/
├── backend/ # Spring Boot 3 後端
│ ├── src/main/java/com/example/epochshop/
│ │ ├── config/ # Security、CORS 配置
│ │ ├── controller/ # REST API 控制器
│ │ ├── dto/ # 資料傳輸物件
│ │ ├── entity/ # JPA 實體（User、Product、Cart、Order...）
│ │ ├── repository/ # Spring Data JPA 儲存庫
│ │ ├── service/ # 業務邏輯層
│ │ └── util/ # JWT 工具類
│ └── pom.xml
│
├── frontend/ # Vue 3 前端
│ ├── src/
│ │ ├── views/ # 頁面組件（Login、Products、Cart、Orders）
│ │ ├── router/ # 路由與守衛
│ │ └── utils/ # Axios 攔截器
│ └── package.json
│
└── .gitignore


## 🚀 已實現功能

| 模組 | 功能說明 | 技術亮點 |
|------|----------|----------|
| 🔐 使用者認證 | 註冊 / 登入，JWT 驗證 | Spring Security + BCrypt |
| 🛍️ 商品瀏覽 | 商品列表查詢 | JPA + CORS 跨域設定 |
| 🛒 購物車 | 加入商品、修改數量、刪除品項 | MySQL 關聯儲存，自動關聯用戶 |
| 📦 訂單管理 | 從購物車建立訂單、模擬付款 | 交易管理、樂觀鎖（@Version）防止超賣 |
| 📊 庫存扣減 | 訂單成立時自動扣庫存 | 樂觀鎖確保併發安全 |
| 🎨 前端整合 | Vue 3 + TypeScript + Axios | 路由守衛、Token 自動附加、錯誤攔截 |

## ⚙️ 技術棧

- **後端**：Java 17、Spring Boot 3.5、Spring Security、JWT (jjwt 0.12)、Spring Data JPA、Hibernate、MySQL
- **前端**：Vue 3、TypeScript、Vite、Vue Router、Axios
- **資料庫**：Railway MySQL（雲端）
- **版本控制**：Git + GitHub

## 🔧 本地啟動方式

### 後端（需 Java 17+）

```bash
cd backend
./mvnw spring-boot:run
預設運行於 http://localhost:8080

前端
bash
cd frontend
npm install
npm run dev
預設運行於 http://localhost:5173

📡 API 端點摘要
方法	路徑	                         說明
POST	/api/auth/register	            註冊
POST	/api/auth/login	                登入
GET	    /api/products	                商品列表（需認證）
GET	    /api/cart	                    查詢購物車
POST	/api/cart/items	                加入購物車
PUT	    /api/cart/items/{id}	        更新數量
DELETE	/api/cart/items/{id}	        刪除品項
POST	/api/orders	                    結帳建立訂單
GET	    /api/orders	                    查詢訂單
PUT	    /api/orders/{id}/pay	        模擬付款

📌 開發里程碑
v0.1.0：商品 API 與前端串接

v0.2.0：JWT 使用者認證

v0.3.0：購物車模組

v0.4.0：訂單管理與庫存樂觀鎖

📝 待辦事項
商品分頁與關鍵字搜尋

冪等性處理（防重複下單）

SQL 查詢優化（慢查詢分析與索引）

Swagger API 文件

單元測試與整合測試

⚠️ 注意事項
application.properties 中的資料庫密碼已透過環境變數管理，請勿將明碼提交至公開倉庫。

本專案為學習與作品集用途，生產環境部署前請務必更換 JWT Secret 與資料庫憑證。