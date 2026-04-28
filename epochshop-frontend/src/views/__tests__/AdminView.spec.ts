import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createWebHistory } from 'vue-router';
import AdminView from '../AdminView.vue';
import axios from '../../utils/axios';

vi.mock('../../utils/axios');

// 使用 vi.stubGlobal 模拟 alert 和 confirm
vi.stubGlobal('alert', vi.fn());
vi.stubGlobal('confirm', vi.fn(() => true));

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/admin', component: AdminView }],
});

describe('AdminView.vue', () => {
  let wrapper: any;

  beforeEach(async () => {
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValue({
      data: {
        content: [
          { id: 1, name: 'iPhone 15', description: 'desc', price: 29900, stock: 10, active: true },
          { id: 2, name: 'MacBook', description: 'laptop', price: 59000, stock: 5, active: false },
        ],
        totalElements: 2,
      },
    });

    wrapper = mount(AdminView, {
      global: {
        plugins: [router],
      },
    });

    await router.push('/admin');
    await router.isReady();
    await wrapper.vm.$nextTick();
  });

  it('應顯示所有商品（包含已下架）', () => {
    expect(wrapper.text()).toContain('iPhone 15');
    expect(wrapper.text()).toContain('MacBook');
  });

  it('應該發送正確的管理員請求', () => {
    expect(axios.get).toHaveBeenCalledWith('/products/admin', { params: { size: 100 } });
  });

  it('點擊「下架」應發送 deactivate 請求並顯示成功訊息', async () => {
    (axios.put as any).mockResolvedValueOnce({ data: {} });
    const deactivateBtn = wrapper.findAll('.btn-delete')[0];
    await deactivateBtn.trigger('click');
    await flushPromises();

    expect(axios.put).toHaveBeenCalledWith('/products/1/deactivate');
    expect(window.alert).toHaveBeenCalledWith('下架成功');
  });

  it('點擊「恢復上架」應發送 reactivate 請求並顯示成功訊息', async () => {
    (axios.put as any).mockResolvedValueOnce({ data: {} });
    const restoreBtn = wrapper.find('.btn-restore');
    await restoreBtn.trigger('click');
    await flushPromises();

    expect(axios.put).toHaveBeenCalledWith('/products/2/reactivate');
    expect(window.alert).toHaveBeenCalledWith('恢復成功');
  });

  it('點擊「新增商品」應顯示表單', async () => {
    const addBtn = wrapper.find('.admin-actions button');
    await addBtn.trigger('click');
    expect(wrapper.text()).toContain('新增商品');
  });

  it('上傳圖片應更新 imageUrl 與預覽', async () => {
  // 先讓表單顯示，否則 input 不存在
  const addBtn = wrapper.find('.admin-actions button');
  await addBtn.trigger('click');
  await wrapper.vm.$nextTick();

  const file = new File(['dummy content'], 'example.png', { type: 'image/png' });
  (axios.post as any).mockResolvedValueOnce({
    data: { imageUrl: 'http://localhost:8080/uploads/example.png' }
  });

  // 找到 input[type="file"]
  const input = wrapper.find('input[type="file"]');
  // 模擬 files
  Object.defineProperty(input.element, 'files', { value: [file] });
  await input.trigger('change');
  await flushPromises();

  expect(axios.post).toHaveBeenCalledWith('/upload/image', expect.any(FormData), expect.any(Object));
  // 確認 form.imageUrl 與 imagePreview 被設定
  expect(wrapper.vm.form.imageUrl).toBe('http://localhost:8080/uploads/example.png');
  expect(wrapper.vm.imagePreview).toBe('http://localhost:8080/uploads/example.png');
  expect(wrapper.find('.image-preview').exists()).toBe(true);
  });

  it('移除圖片應清空 imageUrl 與預覽', async () => {
    // 先讓表單顯示
    const addBtn = wrapper.find('.admin-actions button');
    await addBtn.trigger('click');
    await wrapper.vm.$nextTick();

    // 直接設定 vm 的響應式屬性（放到同一個 nextTick 中）
    wrapper.vm.form.imageUrl = 'http://localhost:8080/uploads/example.png';
    wrapper.vm.imagePreview = 'http://localhost:8080/uploads/example.png';
    await wrapper.vm.$nextTick();

    const removeBtn = wrapper.find('.btn-remove');
    await removeBtn.trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.form.imageUrl).toBe('');
    expect(wrapper.vm.imagePreview).toBe('');
  });
});