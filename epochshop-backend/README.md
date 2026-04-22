# EpochShop Backend

Spring Boot 3 后端服务，提供 REST API、JWT 认证、购物车、订单管理与库存乐观锁。

## 技术栈
- Java 17
- Spring Boot 3.5
- Spring Security + JWT
- Spring Data JPA + Hibernate
- MySQL（Railway 云端）
- SpringDoc OpenAPI（Swagger）

## 快速启动

### 本地开发
```bash
./mvnw spring-boot:run
服务将运行于 http://localhost:8080

Swagger API 文件
启动后访问：http://localhost:8080/swagger-ui.html

环境变量
数据库连线与 JWT Secret 建议透过环境变量设定，请参考 application.properties。

项目结构
text
src/main/java/com/example/epochshop/
├── config/          # Security、CORS、OpenAPI 配置
├── controller/      # REST 控制器
├── dto/             # 资料传输物件
├── entity/          # JPA 实体
├── repository/      # Spring Data JPA 介面
├── service/         # 业务逻辑层
└── util/            # JWT 工具类
主要功能
使用者注册 / 登入（JWT）

商品查询（分页、关键字搜寻）

购物车 CRUD（幂等性保护）

订单建立与模拟付款

库存乐观锁（@Version）