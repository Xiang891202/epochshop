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
│ │ ├── views/ # 頁面組件（Login、Products、Cart、Orders、Admin、Sales）
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
🧪 單元測試
🧪 後端單元測試總覽（14 個測試，JUnit 5 + Mockito）
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
createOrder_WithValidCart_ShouldSucceed	正常結帳	訂單建立後購物車清空，總金額正確計算
createOrder_WithDuplicateIdempotentKey_ShouldThrow	重複冪等鍵	拋出例外「訂單處理中，請勿重複提交」
createOrder_WithEmptyCart_ShouldThrow	購物車為空	拋出例外「購物車是空的」
createOrder_WithInsufficientStock_ShouldThrow	庫存不足	拋出例外「庫存不足」，訂單不被建立
測試保護了哪些真實問題？

冪等性機制：確保重複請求不會建立多筆訂單，防止超賣

庫存控制：庫存不足時正確拒絕，避免負庫存

購物車狀態：空購物車無法結帳，避免幽靈訂單

金額計算：確保訂單總金額等於明細小計總和，避免帳務錯誤

3️⃣ ImageUploadControllerTest（2 個測試）— 圖片上傳
測試名稱	測試內容	驗證重點
uploadImage_ShouldReturnImageUrl	上傳有效圖片檔案	回傳 200，imageUrl 以 /api/images/ 開頭
uploadImage_EmptyFile_ShouldReturnBadRequest	上傳空檔案	回傳 400，錯誤訊息為「請選擇檔案」
測試保護了哪些真實問題？

檔案校驗：防止空檔案寫入磁碟，避免垃圾檔案堆積

URL 格式正確性：確保回傳的圖片 URL 符合前端預期

上傳失敗處理：確保錯誤時回傳明確訊息，而非 500 伺服器錯誤

4️⃣ AdminControllerTest（2 個測試）— 銷售額儀表板
測試名稱	測試內容	驗證重點
getSales_ShouldReturnDashboardData	正常取得儀表板資料	回傳總銷售額、已付款訂單數、商品排行
getSales_ShouldHandleNullValues	無資料時的 Null 安全處理	回傳預設值 0 與空陣列
測試保護了哪些真實問題？

聚合查詢正確性：確保總銷售額與訂單數計算無誤

Null 安全處理：防止無銷售資料時回傳錯誤或空白頁

5️⃣ EpochshopApplicationTests（1 個測試）— 應用程式啟動
測試名稱	測試內容	驗證重點
contextLoads	Spring 容器啟動	所有 Bean 正確載入，資料庫連線正常
測試保護了哪些真實問題？

配置正確性：確保 Security、JPA、JWT 等配置沒有衝突

資料庫連線：驗證 Railway MySQL 連線正常

Bean 注入：確保所有 Service、Repository、Controller 能正確自動裝配

🧪 前端單元測試總覽（18 個測試，Vitest + Vue Test Utils）
1️⃣ AdminView.spec.ts（7 個測試）— 管理員後台
測試名稱	測試內容	驗證重點
應顯示所有商品（包含已下架）	Mock API 回傳上架與下架商品	列表包含 iPhone 15（上架）與 MacBook（下架）
應該發送正確的管理員請求	檢查 fetchProducts 呼叫的 API 路徑	請求路徑為 /products/admin
點擊「下架」應發送 deactivate 請求	點擊下架按鈕後的行為	呼叫 PUT /products/1/deactivate，彈出「下架成功」
點擊「恢復上架」應發送 reactivate 請求	點擊恢復按鈕後的行為	呼叫 PUT /products/2/reactivate，彈出「恢復成功」
點擊「新增商品」應顯示表單	點擊新增按鈕後表單顯示	畫面出現「新增商品」文字與表單欄位
上傳圖片應更新 imageUrl 與預覽	選擇檔案後觸發上傳流程	axios.post 被呼叫，imageUrl 與預覽圖更新
移除圖片應清空 imageUrl 與預覽	點擊移除按鈕後清空狀態	imageUrl 變空字串，預覽消失
測試保護了哪些真實問題？

管理員權限與 API 路徑正確性

軟刪除操作：確保下架/恢復按鈕行為正確

圖片上傳流程：從檔案選擇到預覽更新的完整串接

表單顯示邏輯：確保新增/編輯按鈕的正確切換

2️⃣ CartView.spec.ts（2 個測試）— 購物車與結帳
測試名稱	測試內容	驗證重點
應該正確渲染購物車內容	Mock API 回傳購物車資料	畫面包含商品名稱、單價、小計
點擊結帳按鈕應該發送 POST 請求	觸發結帳流程	先取得冪等 Token，再送出訂單請求
測試保護了哪些真實問題？

冪等性機制：確保結帳時正確攜帶 Token

購物車內容顯示：避免 API 變更導致頁面空白或金額錯誤

按鈕防連點：驗證結帳按鈕在請求期間禁用

3️⃣ OrdersView.spec.ts（3 個測試）— 訂單歷史
測試名稱	測試內容	驗證重點
應顯示訂單列表	Mock API 回傳兩筆訂單	畫面包含訂單編號、狀態、金額、商品明細
點擊模擬付款應發送 PUT 請求	點擊付款按鈕後的行為	呼叫 PUT /orders/1/pay，彈出「付款成功！」
無訂單時應顯示提示文字	Mock 空陣列	顯示「尚無訂單記錄」
測試保護了哪些真實問題？

訂單列表渲染：確保不同狀態的訂單正確顯示

模擬付款流程：驗證付款按鈕正確發送請求

空狀態處理：確保無訂單時不會出現空白頁或錯誤

4️⃣ SalesView.spec.ts（6 個測試）— 銷售額儀表板
測試名稱	測試內容	驗證重點
應顯示總銷售額	頁面渲染總銷售額	畫面包含格式化後的金額
應顯示已付款訂單數	頁面渲染訂單數	畫面包含訂單筆數
應顯示商品銷售排行	頁面顯示商品列表	畫面包含商品名稱、銷量、銷售額
應顯示排名徽章	前兩名有特殊樣式	第一名金色、第二名銀色
無銷售資料時應顯示提示	Mock 空資料	顯示「尚無銷售資料」
API 錯誤時應顯示錯誤訊息	Mock API 失敗	顯示「發生錯誤」
測試保護了哪些真實問題？

數據格式化：確保金額與數字的顯示格式正確

Null 安全處理：避免無資料時畫面崩潰

錯誤處理：確保 API 失敗時使用者能看到提示

視覺層級：排名徽章的正確渲染

🎯 測試總結
測試範圍	後端測試數	前端測試數	保護的核心功能
商品管理	5	7	分頁搜尋、軟刪除、圖片上傳、表單互動
訂單與庫存	4	2	冪等性、庫存扣減、金額計算、結帳防護
圖片上傳	2	-	檔案校驗、URL 格式、錯誤處理
銷售額儀表板	2	6	聚合查詢、數據格式化、錯誤處理
訂單歷史	-	3	訂單列表、模擬付款、空狀態
應用程式啟動	1	-	配置正確性、資料庫連線
合計	14	18	前後端共 32 個測試
這些測試確保了前後端的關鍵業務邏輯與 API 行為，未來重構或新增功能時能立即發現是否破壞了既有功能。

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

v0.7.0：單元測試導入與 CI/CD 流程

v0.8.0：管理員 UI、商品軟刪除與角色權限管理

v0.9.0：商品圖片功能（URL 方式）

v0.9.1：圖片本地上傳（multipart/form-data）

v0.9.2：管理員功能測試補齊、Swagger 全文件覆蓋

v0.10.0：銷售額儀表板、前後端共 32 個測試

📝 待辦事項
✅ 賣家 UI 介面（編輯、新增、軟刪除）

✅ 商品圖片功能（上傳、預覽、移除）

✅ 賣家銷售額

⬜ 客戶對賣家聊天視窗

⚠️ 注意事項
敏感資訊（資料庫密碼、JWT Secret）已透過環境變數管理，請勿將 .env 或 application.properties 中的明碼提交至公開倉庫。

本專案為學習與作品集用途，生產環境部署前請務必更換 JWT Secret 與資料庫憑證。

Docker 部署時，請複製 .env.example 為 .env 並填入真實連線資訊。