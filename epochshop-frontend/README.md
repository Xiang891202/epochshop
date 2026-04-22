## 📄 前端 `README.md`（替换 `epochshop-frontend/README.md`）

```markdown
# EpochShop Frontend

Vue 3 + TypeScript 前端应用，提供商品浏览、购物车、订单管理等介面。

## 技术栈
- Vue 3（Composition API）
- TypeScript
- Vite
- Vue Router
- Axios
- lodash-es

## 快速启动

### 安装依赖
```bash
npm install
开发模式
bash
npm run dev
预设运行于 http://localhost:5173

生产建置
bash
npm run build
环境变量
后端 API 位址可透过 .env 档案设定：

text
VITE_API_BASE_URL=http://localhost:8080/api
项目结构
text
src/
├── views/           # 页面组件（Login、Products、Cart、Orders）
├── router/          # 路由与守卫
├── utils/           # Axios 拦截器
├── App.vue
└── main.ts
主要功能
使用者登入 / 注册

商品列表（分页、关键字搜寻）

购物车管理（加入、修改数量、删除）

订单结帐与模拟付款

路由守卫与 JWT 自动附加