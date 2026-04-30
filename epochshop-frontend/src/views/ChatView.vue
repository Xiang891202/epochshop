<template>
  <div class="chat-container">
    <h2>💬 訊息中心</h2>
    <div class="chat-layout">
      <!-- 左侧对话列表 -->
      <div class="conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.otherUserId"
          @click="openConversation(conv.otherUserId)"
          class="conv-item"
          :class="{ active: selectedUserId === conv.otherUserId }"
        >
          <span v-if="conv.unreadCount > 0" class="unread-badge">{{ conv.unreadCount }}</span>
          <div class="conv-name">{{ conv.otherUserName }}</div>
          <div class="conv-last">{{ conv.lastMessage?.substring(0, 30) }}</div>
        </div>
        <div v-if="conversations.length === 0">尚無對話</div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-window" v-if="selectedUserId">
        <div class="messages" ref="messageContainer">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message"
            :class="{ own: msg.sender.id === currentUserId }"
          >
            <div class="message-bubble">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>
        <div class="message-input">
          <input
            v-model="newMessage"
            @keyup.enter="sendMessage"
            placeholder="輸入訊息..."
          />
          <button @click="sendMessage">發送</button>
        </div>
      </div>
      <div v-else class="no-conversation">請選擇一個對話</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import axios from '../utils/axios';

interface Conversation {
  otherUserId: number;
  otherUserName: string;
  lastMessage: string;
  lastTimestamp: string;
  unreadCount: number;
}

interface User {
  id: number;
  username: string;
}

interface Message {
  id: number;
  sender: User;
  receiver: User;
  content: string;
  timestamp: string;
  isRead: boolean;
}

const route = useRoute();
const conversations = ref<Conversation[]>([]);
const messages = ref<Message[]>([]);
const selectedUserId = ref<number | null>(null);
const newMessage = ref('');
const currentUserId = ref<number | null>(null);
const messageContainer = ref<HTMLElement | null>(null);

const fetchCurrentUser = async () => {
  try {
    const res = await axios.get<{ id: number; username: string; role: string }>('/auth/me');
    currentUserId.value = res.data.id;
  } catch (err) {
    console.error('取得用戶資訊失敗', err);
  }
};

const fetchConversations = async () => {
  try {
    const res = await axios.get<Conversation[]>('/messages/conversations');
    conversations.value = res.data;
  } catch (err) {
    console.error('取得對話列表失敗', err);
  }
};

const openConversation = async (otherUserId: number) => {
  selectedUserId.value = otherUserId;
  try {
    await axios.put(`/messages/conversation/${otherUserId}/read`).catch(() => {});
    const res = await axios.get<Message[]>(`/messages/conversation/${otherUserId}`);
    messages.value = res.data;
  } catch (err) {
    console.error('載入歷史訊息失敗', err);
    messages.value = [];
  }
  await nextTick();
  scrollToBottom();
  fetchConversations(); // 更新左侧列表
};

const sendMessage = async () => {
  if (!newMessage.value.trim() || !selectedUserId.value) return;
  try {
    await axios.post('/messages', {
      receiverId: selectedUserId.value,
      content: newMessage.value,
    });
    newMessage.value = '';
    await openConversation(selectedUserId.value);
  } catch (err) {
    alert('發送失敗');
  }
};

const scrollToBottom = () => {
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const formatTime = (timestamp: string) => {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('zh-TW', { hour: '2-digit', minute: '2-digit' });
};

onMounted(async () => {
  await fetchCurrentUser();
  await fetchConversations();

  // 处理从商品页跳转过来的联系人
  if (route.query.userId) {
    const targetUserId = Number(route.query.userId);
    if (targetUserId && currentUserId.value && targetUserId !== currentUserId.value) {
      // 先设置 selectedUserId，确保右侧窗口立即显示
      selectedUserId.value = targetUserId;
      // 加载历史消息（即使失败也保留空对话窗口）
      await openConversation(targetUserId);
    }
  }
});
</script>

<style scoped>
.chat-container { max-width: 900px; margin: 0 auto; padding: 20px; }
.chat-layout { display: flex; gap: 20px; height: 600px; }
.conversation-list { width: 250px; border: 1px solid #ddd; border-radius: 8px; padding: 10px; overflow-y: auto; background: #fafafa; position: relative; }
.conv-item { padding: 10px; cursor: pointer; border-bottom: 1px solid #eee; position: relative; }
.conv-item.active { background: #e3f2fd; }
.unread-badge {
  position: absolute;
  top: 4px;
  left: 4px;
  background: red;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.conv-name { font-weight: bold; padding-left: 25px; }
.conv-last { font-size: 0.85rem; color: #666; padding-left: 25px; }
.chat-window { flex: 1; display: flex; flex-direction: column; border: 1px solid #ddd; border-radius: 8px; overflow: hidden; }
.messages { flex: 1; padding: 15px; overflow-y: auto; background: #f9f9f9; }
.message { margin-bottom: 15px; display: flex; flex-direction: column; align-items: flex-start; }
.message.own { align-items: flex-end; }
.message-bubble { padding: 10px 15px; border-radius: 18px; max-width: 70%; background: white; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.own .message-bubble { background: #007bff; color: white; }
.message-time { font-size: 0.75rem; color: #999; margin-top: 3px; }
.message-input { display: flex; padding: 10px; border-top: 1px solid #ddd; background: white; }
.message-input input { flex: 1; padding: 8px; border: 1px solid #ddd; border-radius: 20px; outline: none; }
.message-input button { margin-left: 10px; padding: 8px 20px; background: #007bff; color: white; border: none; border-radius: 20px; cursor: pointer; }
.no-conversation { flex: 1; display: flex; align-items: center; justify-content: center; color: #aaa; }
</style>