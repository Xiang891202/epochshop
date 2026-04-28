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
│ │ ├── views/ # 頁面組件（Login、Products、Cart、Orders、Admin）
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

🧪 單元測試
🧪 後端單元測試總覽（12 個測試，JUnit 5 + Mockito）
1️⃣ ProductServiceTest（5 個測試）— 商品服務
測試名稱	測試內容	驗證重點
searchProducts_WithKeyword	有關鍵字時呼叫模糊查詢	呼叫 searchByName，不呼叫 findAll
searchProducts_WithBlankKeyword	空白關鍵字時查詢全部	呼叫 findAll，不呼叫 searchByName
createProduct_ShouldSaveAndReturnProduct	新增商品	回傳已儲存的商品物件，驗證 save 被呼叫
deactivateProduct_ShouldSetActiveFalse	下架商品	active 欄位變為 false，save 被呼叫
reactivateProduct_ShouldSetActiveTrue	恢復上架	active 欄位變為 true，save 被呼叫
測試保護了哪些真實問題？

搜尋分流邏輯：確保關鍵字與空白查詢的正確分流，避免空字串導致模糊查詢異常

軟刪除邏輯：確保下架/恢復的狀態切換正確，避免商品狀態卡死或誤刪

新增商品：確保 Repository 的 save 方法被正確呼叫，避免資料未寫入

2️⃣ OrderServiceTest（4 個測試）— 訂單服務
測試名稱	測試內容	驗證重點
createOrder_WithValidCart_ShouldSucceed	正常結帳	訂單建立後購物車清空，總金額正確計算（29900×2=59800），productRepository.save 被呼叫
createOrder_WithDuplicateIdempotentKey_ShouldThrow	重複冪等鍵	拋出例外「訂單處理中，請勿重複提交」，購物車不被查詢
createOrder_WithEmptyCart_ShouldThrow	購物車為空	拋出例外「購物車是空的」
createOrder_WithInsufficientStock_ShouldThrow	庫存不足	拋出例外「庫存不足」，訂單不被建立
測試保護了哪些真實問題？

冪等性機制：確保重複請求不會建立多筆訂單，防止使用者誤點或網路重試造成超賣

庫存控制：庫存不足時正確拒絕，避免負庫存

購物車狀態：空購物車無法結帳，避免幽靈訂單

金額計算：確保訂單總金額等於明細小計總和，避免帳務錯誤

3️⃣ ImageUploadControllerTest（2 個測試）— 圖片上傳
測試名稱	測試內容	驗證重點
uploadImage_ShouldReturnImageUrl	上傳有效圖片檔案	回傳 200，imageUrl 以 http://localhost:8080/api/images/ 開頭
uploadImage_EmptyFile_ShouldReturnBadRequest	上傳空檔案	回傳 400，錯誤訊息為「請選擇檔案」
測試保護了哪些真實問題？

檔案校驗：防止空檔案寫入磁碟，避免垃圾檔案堆積

URL 格式正確性：確保回傳的圖片 URL 符合前端預期，避免圖片無法顯示

上傳失敗處理：確保錯誤時回傳明確訊息，而非 500 伺服器錯誤

4️⃣ EpochshopApplicationTests（1 個測試）— 應用程式啟動
測試名稱	測試內容	驗證重點
contextLoads	Spring 容器啟動	所有 Bean 正確載入，資料庫連線正常，自動初始化 Admin 帳號
測試保護了哪些真實問題？

配置正確性：確保 Security、JPA、JWT 等配置沒有衝突

資料庫連線：驗證 Railway MySQL 連線正常，避免部署後才發現連線失敗

Bean 注入：確保所有 Service、Repository、Controller 能正確自動裝配

🎯 總結
測試範圍	測試數量	保護的核心功能
商品管理	5 個	分頁搜尋、軟刪除、新增商品
訂單與庫存	4 個	冪等性、庫存扣減、金額計算、結帳防護
圖片上傳	2 個	檔案校驗、URL 格式、錯誤處理
應用程式啟動	1 個	配置正確性、資料庫連線、Bean 注入
合計	12 個	全端核心業務邏輯
這些測試確保了後端的關鍵業務邏輯與 API 行為，未來重構或新增功能時能立即發現是否破壞了既有功能。



🧪 前端單元測試總覽（12 個測試，Vitest + Vue Test Utils）
1️⃣ AdminView.spec.ts（7 個測試）— 管理員後台
測試名稱	測試內容	驗證重點
應顯示所有商品（包含已下架）	Mock API 回傳上架與下架商品，確認頁面渲染	列表包含 iPhone 15（上架）與 MacBook（下架）
應該發送正確的管理員請求	檢查 fetchProducts 呼叫的 API 路徑	請求路徑為 /products/admin，參數包含 size=100
點擊「下架」應發送 deactivate 請求並顯示成功訊息	點擊下架按鈕後的行為	呼叫 PUT /products/1/deactivate，彈出「下架成功」
點擊「恢復上架」應發送 reactivate 請求並顯示成功訊息	點擊恢復按鈕後的行為	呼叫 PUT /products/2/reactivate，彈出「恢復成功」
點擊「新增商品」應顯示表單	點擊新增按鈕後表單顯示	畫面出現「新增商品」文字與表單欄位
上傳圖片應更新 imageUrl 與預覽	選擇檔案後觸發上傳流程	axios.post 被呼叫，imageUrl 與預覽圖更新
移除圖片應清空 imageUrl 與預覽	點擊移除按鈕後清空狀態	imageUrl 變空字串，預覽消失
測試保護了哪些真實問題？

管理員權限與 API 路徑正確性：避免前端打錯路徑（如 /admin/products vs /products/admin）

軟刪除操作：確保下架/恢復的按鈕行為與後端 API 同步，防止商品狀態不一致

圖片上傳流程：從檔案選擇、API 呼叫到預覽更新的完整串接，避免上傳失敗或預覽卡住

表單顯示邏輯：確保新增/編輯按鈕的正確切換，避免 UI 卡死

2️⃣ CartView.spec.ts（2 個測試）— 購物車與結帳
測試名稱	測試內容	驗證重點
應該正確渲染購物車內容	Mock API 回傳購物車資料，確認頁面渲染	畫面包含商品名稱 iPhone 15、單價 $29900、小計 $59800
點擊結帳按鈕應該發送 POST 請求	觸發結帳流程，驗證 API 呼叫順序與參數	先呼叫 GET /orders/idempotent-token，再呼叫 POST /orders 並攜帶正確 idempotentKey
測試保護了哪些真實問題？

冪等性機制：確保結帳時正確取得 Token 並附帶於請求中，防止重複下單

購物車內容顯示：避免因 API 回傳格式變更導致購物車頁面空白或金額錯誤

按鈕防連點：驗證結帳按鈕在請求期間禁用，避免重複提交

3️⃣ OrdersView.spec.ts（3 個測試）— 訂單歷史
測試名稱	測試內容	驗證重點
應顯示訂單列表	Mock API 回傳兩筆訂單（PENDING 與 PAID），確認頁面渲染	畫面包含訂單編號、狀態、金額、商品明細
點擊模擬付款應發送 PUT 請求	點擊付款按鈕後的行為	呼叫 PUT /orders/1/pay，彈出「付款成功！」
無訂單時應顯示提示文字	Mock 空陣列，確認頁面提示	顯示「尚無訂單記錄」
測試保護了哪些真實問題？

訂單列表渲染：確保不同狀態的訂單正確顯示（PENDING vs PAID）

模擬付款流程：驗證付款按鈕正確發送請求，避免付款失敗或重複扣款

空狀態處理：確保無訂單時不會出現空白頁或錯誤

🎯 總結
測試範圍	測試數量	保護的核心功能
管理員後台	7 個	商品 CRUD、軟刪除、圖片上傳、表單顯示
購物車與結帳	2 個	冪等性、結帳流程、金額計算
訂單歷史	3 個	訂單列表、模擬付款、空狀態
合計	12 個	全端核心業務邏輯
這些測試確保了前端的關鍵互動與 API 串接邏輯，未來修改程式碼時能立即發現是否破壞了既有功能。



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

## 📌 開發里程碑

- `v0.1.0`：商品 API 與前端串接
- `v0.2.0`：JWT 使用者認證
- `v0.3.0`：購物車模組
- `v0.4.0`：訂單管理與庫存樂觀鎖
- `v0.5.0`：冪等性處理與 SQL 優化案例
- `v0.6.0`：Swagger API 文件、商品分頁搜尋、Docker 化準備
- `v0.7.0`：單元測試導入與 CI/CD 流程（後端 8 個、前端 3 個測試）
- `v0.8.0`：管理員 UI、商品軟刪除（下架/恢復）與角色權限管理
- `v0.9.0`：商品圖片功能（URL 方式）
- `v0.9.1`：圖片本地上傳（multipart/form-data）
- `v0.9.2`：管理員功能測試補齊、Swagger 全文件覆蓋、前後端共 24 個測試

## 📝 待辦事項

- ✅ 賣家 UI 介面（編輯、新增、軟刪除）
- ✅ 商品圖片功能（上傳、預覽、移除）
- ⬜ 賣家銷售額
- ⬜ 客戶對賣家聊天視窗

⚠️ 注意事項
敏感資訊（資料庫密碼、JWT Secret）已透過環境變數管理，請勿將 .env 或 application.properties 中的明碼提交至公開倉庫。

本專案為學習與作品集用途，生產環境部署前請務必更換 JWT Secret 與資料庫憑證。

Docker 部署時，請複製 .env.example 為 .env 並填入真實連線資訊。