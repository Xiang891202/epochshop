import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createWebHistory, useRoute } from 'vue-router';
import ProductDetail from '../ProductDetail.vue';
import axios from '../../utils/axios';

vi.mock('../../utils/axios');
vi.mock('vue-router', async () => {
  const actual = await vi.importActual('vue-router');
  return {
    ...actual,
    useRoute: vi.fn(),
  };
});

const mockRoute = {
  params: { id: '1' },
  query: {},
};
(useRoute as any).mockReturnValue(mockRoute);

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/products/:id', component: ProductDetail }],
});

const toastMock = vi.fn();

describe('ProductDetail.vue', () => {
  let wrapper: any;

  beforeEach(async () => {
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValueOnce({
      data: {
        id: 1,
        name: 'iPhone 15',
        description: '最新 A17 晶片',
        price: 29900,
        stock: 10,
        imageUrl: 'http://example.com/iphone.jpg',
        seller: { id: 2, username: 'admin' },
      },
    });

    wrapper = mount(ProductDetail, {
      global: {
        plugins: [router],
        provide: { toast: toastMock },
      },
    });

    await router.push('/products/1');
    await router.isReady();
    await wrapper.vm.$nextTick();
  });

  it('應顯示商品詳細資訊', () => {
    expect(wrapper.text()).toContain('iPhone 15');
    expect(wrapper.text()).toContain('$29,900');
    expect(wrapper.text()).toContain('最新 A17 晶片');
    expect(wrapper.text()).toContain('庫存：10');
  });

  it('點擊加入購物車應發送 API 請求', async () => {
    (axios.get as any).mockResolvedValueOnce({ data: { token: 'test-token' } });
    (axios.post as any).mockResolvedValueOnce({ data: {} });

    const addBtn = wrapper.find('.btn-primary');
    await addBtn.trigger('click');
    await flushPromises();

    expect(axios.get).toHaveBeenCalledWith('/cart/idempotent-token');
    expect(axios.post).toHaveBeenCalledWith('/cart/items', {
      productId: 1,
      quantity: 1,
      idempotentKey: 'test-token',
    });
    expect(toastMock).toHaveBeenCalledWith('加入購物車成功！', 'success');
  });

  it('點擊聯絡賣家應跳轉到訊息頁', async () => {
    const contactBtn = wrapper.find('.btn-outline');
    await contactBtn.trigger('click');
    
    // 等待 Vue 的异步更新和路由跳转
    await wrapper.vm.$nextTick();
    await new Promise(resolve => setTimeout(resolve, 50)); // 给路由一点时间
    
    // 检查路由路径是否包含 /messages
    expect(router.currentRoute.value.path).toContain('/messages');
    expect(router.currentRoute.value.query).toEqual({ userId: '2' });
    });
});