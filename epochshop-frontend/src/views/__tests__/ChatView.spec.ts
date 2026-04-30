import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import { createRouter, createWebHistory, useRoute } from 'vue-router';
import ChatView from '../ChatView.vue';
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
  query: {},
};

(useRoute as any).mockReturnValue(mockRoute);

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/messages', component: ChatView },
  ],
});

describe('ChatView.vue', () => {
  let wrapper: any;

  beforeEach(async () => {
    vi.clearAllMocks();
    // Mock 当前用户 (买家)
    (axios.get as any).mockResolvedValueOnce({
      data: { id: 1, username: 'testuser', role: 'ROLE_USER' },
    });
    // Mock 对话列表
    (axios.get as any).mockResolvedValueOnce({
      data: [
        {
          otherUserId: 2,
          otherUserName: '管理员',
          lastMessage: '你好',
          unreadCount: 1,
        },
      ],
    });

    wrapper = mount(ChatView, {
      global: {
        plugins: [router],
      },
    });

    await router.push('/messages');
    await router.isReady();
    await wrapper.vm.$nextTick();
    await flushPromises();
  });

  it('應顯示對話列表', () => {
    expect(wrapper.text()).toContain('管理员');
    expect(wrapper.text()).toContain('你好');
  });

  it('應顯示未讀數字徽章', () => {
    const badge = wrapper.find('.unread-badge');
    expect(badge.exists()).toBe(true);
    expect(badge.text()).toBe('1');
  });

  it('點擊對話應載入歷史訊息', async () => {
    // Mock 标记已读
    (axios.put as any).mockResolvedValueOnce({});
    // Mock 历史消息
    (axios.get as any).mockResolvedValueOnce({
      data: [
        {
          id: 1,
          sender: { id: 1, username: 'testuser' },
          receiver: { id: 2, username: 'admin' },
          content: '在吗',
          timestamp: '2026-04-30T12:00:00',
          isRead: false,
        },
      ],
    });

    const convItem = wrapper.find('.conv-item');
    await convItem.trigger('click');
    await flushPromises();

    expect(axios.put).toHaveBeenCalledWith('/messages/conversation/2/read');
    expect(axios.get).toHaveBeenCalledWith('/messages/conversation/2');
    // 历史消息应显示
    expect(wrapper.text()).toContain('在吗');
  });

  it('發送訊息應正確呼叫 API', async () => {
    // 先打开对话
    (axios.put as any).mockResolvedValueOnce({});
    (axios.get as any).mockResolvedValueOnce({ data: [] });
    const convItem = wrapper.find('.conv-item');
    await convItem.trigger('click');
    await flushPromises();

    // 输入并发送
    const input = wrapper.find('.message-input input');
    await input.setValue('新消息');
    (axios.post as any).mockResolvedValueOnce({
      data: { id: 2, content: '新消息' },
    });
    // 发送后会自动重新加载对话，需要 mock 重新调用的 get/put
    (axios.put as any).mockResolvedValueOnce({});
    (axios.get as any).mockResolvedValueOnce({ data: [] });

    const sendBtn = wrapper.find('.message-input button');
    await sendBtn.trigger('click');
    await flushPromises();

    expect(axios.post).toHaveBeenCalledWith('/messages', {
      receiverId: 2,
      content: '新消息',
    });
  });

  it('無對話時應顯示提示', async () => {
    // 重新挂载一个空列表的组件
    vi.clearAllMocks();
    (axios.get as any).mockResolvedValueOnce({
      data: { id: 1, username: 'testuser', role: 'ROLE_USER' },
    });
    (axios.get as any).mockResolvedValueOnce({ data: [] });

    const emptyWrapper = mount(ChatView, {
      global: { plugins: [router] },
    });
    await flushPromises();
    expect(emptyWrapper.text()).toContain('尚無對話');
  });
});