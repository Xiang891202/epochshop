# 🛒 EpochShop 瞬購秒殺商城（全端示範專案）

本專案為前後端分離的電商平台，涵蓋使用者認證、商品瀏覽、購物車、訂單管理、庫存樂觀鎖、冪等性處理、SQL 效能優化、Swagger API 文件、分頁搜尋與 Docker 容器化等核心功能。

## 📁 專案結構
epochshop/
├── backend/ # Spring Boot 3 後端
│ ├── src/main/java/com/example/epochshop/
│ │ ├── config/ # Security、CORS、OpenAPI 配置
│ │ ├── controller/ # REST API 控制器
│ │ ├── dto/ # 資料傳輸物件
│ │ ├── entity/ # JPA 實體（User、Product、Cart、Order...）
│ │ ├── repository/ # Spring Data JPA 儲存庫
│ │ ├── service/ # 業務邏輯層
│ │ └── util/ # JWT 工具類
│ ├── Dockerfile # 後端容器化設定
│ └── pom.xml
│
├── frontend/ # Vue 3 前端
│ ├── src/
│ │ ├── views/ # 頁面組件（Login、Products、Cart、Orders）
│ │ ├── router/ # 路由與守衛
│ │ └── utils/ # Axios 攔截器
│ ├── Dockerfile # 前端容器化設定
│ └── package.json
│
├── docker-compose.yml # 容器編排設定（前後端 + 雲端 MySQL）
├── .env.example # 環境變數範本
└── .gitignore

text

## 🚀 已實現功能

| 模組 | 功能說明 | 技術亮點 |
|------|----------|----------|
| 🔐 使用者認證 | 註冊 / 登入，JWT 驗證 | Spring Security + BCrypt |
| 🛍️ 商品瀏覽 | 商品列表查詢、分頁、關鍵字搜尋 | JPA + Pageable + 模糊查詢 |
| 🛒 購物車 | 加入商品、修改數量、刪除品項 | MySQL 關聯儲存，自動關聯用戶，加入冪等性防重複 |
| 📦 訂單管理 | 從購物車建立訂單、模擬付款 | 交易管理、樂觀鎖（@Version）防止超賣 |
| 📊 庫存扣減 | 訂單成立時自動扣庫存 | 樂觀鎖確保併發安全 |
| 🔒 冪等性處理 | 防止重複下單與重複加入購物車 | 資料庫唯一鍵（idempotent_key）+ 前端 UUID |
| 📈 SQL 優化案例 | 訂單列表 JOIN 查詢效能調校 | EXPLAIN 分析、索引建立、優化前後對比 |
| 📚 Swagger API 文件 | 自動生成 API 文件與測試介面 | SpringDoc OpenAPI + JWT 授權 |
| 🐳 Docker 化 | 前後端容器化，支援一鍵部署 | Dockerfile + docker-compose（連接雲端 MySQL） |
| 🎨 前端整合 | Vue 3 + TypeScript + Axios | 路由守衛、Token 自動附加、錯誤攔截、Loading 防連點、分頁搜尋 |

## ⚙️ 技術棧

- **後端**：Java 17、Spring Boot 3.5、Spring Security、JWT (jjwt 0.12)、Spring Data JPA、Hibernate、MySQL、SpringDoc OpenAPI
- **前端**：Vue 3、TypeScript、Vite、Vue Router、Axios、lodash-es
- **資料庫**：Railway MySQL（雲端）
- **容器化**：Docker、Docker Compose
- **版本控制**：Git + GitHub

## 🔧 本地啟動方式

### 後端（需 Java 17+）

```bash
cd backend
./mvnw spring-boot:run
預設運行於 http://localhost:8080
Swagger UI：http://localhost:8080/swagger-ui.html

前端
bash
cd frontend
npm install
npm run dev
預設運行於 http://localhost:5173

Docker 一鍵啟動（需安裝 Docker Desktop）
bash
# 複製 .env.example 為 .env 並填入真實 Railway MySQL 密碼
cp .env.example .env
docker-compose up --build
📡 API 端點摘要
方法	路徑	說明
POST	/api/auth/register	註冊
POST	/api/auth/login	登入
GET	/api/products	商品列表（支援分頁與關鍵字 ?keyword=xxx&page=0&size=10）
GET	/api/cart	查詢購物車
GET	/api/cart/idempotent-token	取得購物車冪等 Token
POST	/api/cart/items	加入購物車（需冪等 Token）
PUT	/api/cart/items/{id}	更新數量
DELETE	/api/cart/items/{id}	刪除品項
GET	/api/orders/idempotent-token	取得訂單冪等 Token
POST	/api/orders	結帳建立訂單（需冪等 Token）
GET	/api/orders	查詢訂單
PUT	/api/orders/{id}/pay	模擬付款
📈 效能優化案例：訂單列表查詢
問題描述
查詢特定用戶的訂單及其明細時，由於 order_items 表缺少對 order_id 的索引，導致查詢時產生全表掃描，當資料量增長時效能將急遽下降。

優化前執行計畫（EXPLAIN）
order_items 表：type=ALL（全表掃描），rows=12

orders 表：type=eq_ref，使用 PRIMARY 索引

優化方法
為 orders.user_id 及 order_items.order_id 建立索引：

sql
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
優化後執行計畫（EXPLAIN）
order_items 表：type=ALL（因資料量過小，優化器未選用索引）

possible_keys 已顯示 idx_order_items_order_id

掃描行數變化不大（12 → 11），但在生產環境資料量大時，該索引將顯著降低查詢成本。

結論
本次優化雖在極小資料集下未見立即效能提升，但建立了正確的索引策略，為未來資料增長做好準備。實際生產環境中，該索引可將查詢時間從數秒降低至毫秒級。

📌 開發里程碑
v0.1.0：商品 API 與前端串接

v0.2.0：JWT 使用者認證

v0.3.0：購物車模組

v0.4.0：訂單管理與庫存樂觀鎖

v0.5.0：冪等性處理與 SQL 優化案例

v0.6.0：Swagger API 文件、商品分頁搜尋、Docker 化準備

📝 待辦事項
單元測試與整合測試（JUnit + Mockito + Vitest）

CI/CD 流程規劃（GitHub Actions）

⚠️ 注意事項
敏感資訊（資料庫密碼、JWT Secret）已透過環境變數管理，請勿將 .env 或 application.properties 中的明碼提交至公開倉庫。

本專案為學習與作品集用途，生產環境部署前請務必更換 JWT Secret 與資料庫憑證。

Docker 部署時，請複製 .env.example 為 .env 並填入真實連線資訊。