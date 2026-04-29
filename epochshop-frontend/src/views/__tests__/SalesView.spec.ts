import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import SalesView from '../SalesView.vue';
import axios from '../../utils/axios';

vi.mock('../../utils/axios');

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/admin/sales', component: SalesView }],
});

describe('SalesView.vue', () => {
  let wrapper: any;

  beforeEach(async () => {
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValue({
      data: {
        totalSales: 299000,
        paidOrders: 10,
        productSales: [
          ['iPhone 15', 5, 149500],
          ['MacBook Pro', 2, 118000],
        ],
      },
    });

    wrapper = mount(SalesView, {
      global: { plugins: [router] },
    });

    await router.push('/admin/sales');
    await router.isReady();
    await wrapper.vm.$nextTick();
  });

  it('應顯示總銷售額', () => {
    expect(wrapper.text()).toContain('$299,000');
  });

  it('應顯示已付款訂單數', () => {
    expect(wrapper.text()).toContain('10');
    expect(wrapper.text()).toContain('筆');
  });

  it('應顯示商品銷售排行', () => {
    expect(wrapper.text()).toContain('iPhone 15');
    expect(wrapper.text()).toContain('MacBook Pro');
    expect(wrapper.text()).toContain('5');    // iPhone 销量
    expect(wrapper.text()).toContain('2');    // MacBook 销量
    expect(wrapper.text()).toContain('$149,500');
    expect(wrapper.text()).toContain('$118,000');
  });

  it('應顯示排名徽章', () => {
    const badges = wrapper.findAll('.rank-badge');
    expect(badges).toHaveLength(2);
    expect(badges[0].text()).toBe('1');
    expect(badges[0].classes()).toContain('rank-1');
    expect(badges[1].text()).toBe('2');
    expect(badges[1].classes()).toContain('rank-2');
  });

  it('無銷售資料時應顯示提示', async () => {
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValueOnce({
      data: {
        totalSales: 0,
        paidOrders: 0,
        productSales: [],
      },
    });

    const emptyWrapper = mount(SalesView, {
      global: { plugins: [router] },
    });
    await flushPromises();
    await emptyWrapper.vm.$nextTick();

    expect(emptyWrapper.text()).toContain('尚無銷售資料');
  });

  it('API 錯誤時應顯示錯誤訊息', async () => {
    vi.clearAllMocks();
    (axios.get as any).mockRejectedValueOnce(new Error('網路錯誤'));

    const errorWrapper = mount(SalesView, {
      global: { plugins: [router] },
    });
    await flushPromises();
    await errorWrapper.vm.$nextTick();

    expect(errorWrapper.text()).toContain('發生錯誤');
  });
});