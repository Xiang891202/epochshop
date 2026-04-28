import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import OrdersView from '../OrdersView.vue';
import axios from '../../utils/axios';

vi.mock('../../utils/axios');
global.alert = vi.fn();

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/orders', component: OrdersView }],
});

describe('OrdersView.vue', () => {
  let wrapper: any;

  beforeEach(async () => {
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValueOnce({
      data: [
        {
          orderId: 1,
          createdAt: '2026-04-28T12:00:00',
          status: 'PENDING',
          totalAmount: 29900,
          items: [
            { productId: 1, productName: 'iPhone 15', quantity: 1, unitPrice: 29900, subtotal: 29900 },
          ],
        },
        {
          orderId: 2,
          createdAt: '2026-04-28T13:00:00',
          status: 'PAID',
          totalAmount: 59800,
          items: [
            { productId: 1, productName: 'iPhone 15', quantity: 2, unitPrice: 29900, subtotal: 59800 },
          ],
        },
      ],
    });

    wrapper = mount(OrdersView, {
      global: { plugins: [router] },
    });

    router.push('/orders');
    await router.isReady();
    await wrapper.vm.$nextTick();
  });

  it('應顯示訂單列表', () => {
    expect(wrapper.text()).toContain('iPhone 15');
    expect(wrapper.text()).toContain('$29900');
    expect(wrapper.text()).toContain('$59800');
    expect(wrapper.text()).toContain('訂單編號：1');
    expect(wrapper.text()).toContain('PENDING');
    expect(wrapper.text()).toContain('PAID');
  });

  it('點擊模擬付款應發送 PUT 請求', async () => {
    (axios.put as any).mockResolvedValueOnce({ data: {} });

    const payBtn = wrapper.find('.pay-btn');
    await payBtn.trigger('click');
    await flushPromises();

    expect(axios.put).toHaveBeenCalledWith('/orders/1/pay');
    expect(global.alert).toHaveBeenCalledWith('付款成功！');
  });

  it('無訂單時應顯示提示文字', async () => {
    // 重新掛載一個空資料的元件
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValueOnce({ data: [] });

    const emptyWrapper = mount(OrdersView, {
      global: { plugins: [router] },
    });
    await flushPromises();
    await emptyWrapper.vm.$nextTick();

    expect(emptyWrapper.text()).toContain('尚無訂單記錄');
  });
});