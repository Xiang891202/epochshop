# 🛒 EpochShop 瞬購秒殺商城（全端示範專案）

本專案為前後端分離的電商平台，涵蓋使用者認證、商品瀏覽、購物車、訂單管理、庫存樂觀鎖、冪等性處理、SQL 效能優化、Swagger API 文件、分頁搜尋、Docker 容器化、單元測試與 CI/CD 等核心功能。

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
│ │ ├── views/ # 頁面組件（Login、Products、Cart、Orders、Admin、Sales、Chat）
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
| 🔐 使用者認證 | 註冊 / 登入，JWT 驗證，角色區分（買家/管理員） | Spring Security + BCrypt |
| 🛍️ 商品瀏覽 | 商品列表查詢、分頁、關鍵字搜尋、圖片展示 | JPA + Pageable + 模糊查詢 |
| 🛒 購物車 | 加入商品、修改數量、刪除品項 | MySQL 關聯儲存，自動關聯用戶，加入冪等性防重複 |
| 📦 訂單管理 | 從購物車建立訂單、模擬付款 | 交易管理、樂觀鎖（@Version）防止超賣 |
| 📊 庫存扣減 | 訂單成立時自動扣庫存 | 樂觀鎖確保併發安全 |
| 🔒 冪等性處理 | 防止重複下單與重複加入購物車 | 資料庫唯一鍵（idempotent_key）+ 前端 UUID |
| 📈 SQL 優化案例 | 訂單列表 JOIN 查詢效能調校 | EXPLAIN 分析、索引建立、優化前後對比 |
| 📚 Swagger API 文件 | 自動生成 API 文件與測試介面 | SpringDoc OpenAPI + JWT 授權 |
| 🐳 Docker 化 | 前後端容器化，支援一鍵部署 | Dockerfile + docker-compose（連接雲端 MySQL） |
| ⚙️ 管理後台 | 管理員可新增、編輯、下架（軟刪除）商品，圖片上傳 | Vue AdminView + @PreAuthorize + 商品 active 欄位 |
| 💰 銷售額儀表板 | 管理員可查看總銷售額、已付款訂單數、商品銷售排行 | Spring Data JPA 聚合查詢 + Vue 數據視覺化 |
| 💬 聊天系統 | 客戶與賣家即時對話，動態聯絡人、未讀計數、角色過濾 | Vue ChatView + Spring Data JPA + ConversationDTO |
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
GET	/api/auth/me	取得目前使用者資訊（含角色）
GET	/api/products	商品列表（支援分頁與關鍵字 ?keyword=xxx&page=0&size=10）
POST	/api/products	新增商品
PUT	/api/products/{id}	更新商品
DELETE	/api/products/{id}	刪除商品（軟刪除，改為下架）
PUT	/api/products/{id}/deactivate	下架商品（管理員）
PUT	/api/products/{id}/reactivate	恢復上架（管理員）
GET	/api/products/admin	管理員專用商品列表（含已下架）
GET	/api/cart	查詢購物車
GET	/api/cart/idempotent-token	取得購物車冪等 Token
POST	/api/cart/items	加入購物車（需冪等 Token）
PUT	/api/cart/items/{id}	更新數量
DELETE	/api/cart/items/{id}	刪除品項
GET	/api/orders/idempotent-token	取得訂單冪等 Token
POST	/api/orders	結帳建立訂單（需冪等 Token）
GET	/api/orders	查詢訂單
PUT	/api/orders/{id}/pay	模擬付款
POST	/api/upload/image	上傳商品圖片（multipart/form-data）
GET	/api/images/{filename}	讀取圖片資源
GET	/api/admin/sales	取得銷售額儀表板（管理員）
POST	/api/messages	發送訊息
GET	/api/messages/conversations	獲取對話列表
GET	/api/messages/conversation/{otherUserId}	獲取與某用戶的對話記錄
PUT	/api/messages/conversation/{otherUserId}/read	標記對話已讀
PUT	/api/messages/{id}/read	標記單條訊息已讀
GET	/api/messages/unread-count	獲取未讀訊息數
🧪 單元測試
後端測試（19 個）
ProductServiceTest（5）、OrderServiceTest（4）、ImageUploadControllerTest（2）

AdminControllerTest（2）、ChatServiceTest（5）、ApplicationTest（1）

前端測試（23 個）
AdminView.spec.ts（7）、CartView.spec.ts（2）、OrdersView.spec.ts（3）

SalesView.spec.ts（6）、ChatView.spec.ts（5）

前後端共 42 個測試，覆蓋所有核心模組。

📈 效能優化案例：訂單列表查詢
（保持原內容不變）

📌 開發里程碑
v0.1.0：商品 API 與前端串接

v0.2.0：JWT 使用者認證

v0.3.0：購物車模組

v0.4.0：訂單管理與庫存樂觀鎖

v0.5.0：冪等性處理與 SQL 優化案例

v0.6.0：Swagger API 文件、商品分頁搜尋、Docker 化準備

v0.7.0：單元測試導入與 CI/CD 流程

v0.8.0：管理員 UI、商品軟刪除與角色權限管理

v0.9.0：商品圖片功能（URL 方式）

v0.9.1：圖片本地上傳（multipart/form-data）

v0.9.2：管理員功能測試補齊、Swagger 全文件覆蓋

v0.10.0：銷售額儀表板、前後端共 32 個測試

v0.11.0：客戶對賣家聊天視窗（基礎對話、未讀角標、角色過濾）

v0.12.0：聊天系統增強（標記已讀、聯繫人清單優化）

v0.12.1：商品關聯賣家、動態「聯絡賣家」按鈕

v0.12.2：聊天系統前後端單元測試（後端 +5，前端 +5）

v0.12.3：修復所有測試，前後端共 42 個測試

📝 待辦事項
✅ 賣家 UI 介面（編輯、新增、軟刪除）

✅ 商品圖片功能（上傳、預覽、移除）

✅ 賣家銷售額

✅ 客戶對賣家聊天視窗

⚠️ 注意事項
敏感資訊（資料庫密碼、JWT Secret）已透過環境變數管理，請勿將 .env 或 application.properties 中的明碼提交至公開倉庫。

本專案為學習與作品集用途，生產環境部署前請務必更換 JWT Secret 與資料庫憑證。

Docker 部署時，請複製 .env.example 為 .env 並填入真實連線資訊。