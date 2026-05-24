<template>
  <div class="backend-check-overlay">
    <div class="check-card">
      <h2>🛒 EpochShop</h2>
      <div v-if="checking" class="status">
        <div class="spinner"></div>
        <p>後端啟動中，請稍候...</p>
        <p class="attempt-info">自動重試次數：{{ currentAttempt }}/{{ maxRetries }}</p>
        <p class="countdown" v-if="countdown > 0">{{ countdown }} 秒後重新嘗試</p>
      </div>
      <div v-else class="error">
        <p class="error-msg">{{ errorMessage }}</p>
        <p class="attempt-info">已嘗試 {{ currentAttempt }}/{{ maxRetries }}</p>
        <button @click="startCheck" class="retry-btn">立即重試</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import axios from '../utils/axios';

const emit = defineEmits(['ready']);

const maxRetries = 3;
const currentAttempt = ref(0);      // 已尝试次数（0表示还未开始，1表示第一次尝试）
const countdown = ref(10);          // 当前倒计时秒数
const checking = ref(true);         // true: 正在检查中， false: 检查结束（失败）
const errorMessage = ref('');

let timer: number | null = null;
let countdownInterval: number | null = null;
let abortController: AbortController | null = null;

// 健康检查函数
const checkBackend = async () => {
  // 中斷上一次請求
  if (abortController) {
    abortController.abort();
  }
  abortController = new AbortController();

  try {
    const res = await axios.get('/health', {
      timeout: 5000,               // 5 秒超時
      signal: abortController.signal,
    });
    if (res.status === 200 && res.data?.status === 'UP') {
      return true;
    } else {
      throw new Error('UNEXPECTED_RESPONSE');
    }
  } catch (err: any) {
    if (err.code === 'ECONNABORTED') {
      throw new Error('TIMEOUT');   // 超時視同後端未啟動
    }
    throw err;
  } finally {
    abortController = null;
  }
};

// 啟動一輪檢測
const startRound = async () => {
  // ⚡ 優化：不要在這裡重設 countdown.value = 10，讓倒數的控制權完全交給 startCountdown
  currentAttempt.value++;

  try {
    const success = await checkBackend();
    if (success) {
      emit('ready'); // 成功則觸發 ready，通知父組件關閉 Overlay
    }
  } catch (err: any) {
    if (currentAttempt.value >= maxRetries) {
      // 最終失敗
      checking.value = false;
      if (err.message === 'BACKEND_DOWN') {
        errorMessage.value = '後端尚未啟動，請稍後再嘗試';
      } else {
        errorMessage.value = '前端與後端接口異常，請稍後再嘗試';
      }
      stopTimers();
    } else {
      // 還有重試機會，這時才乾乾淨淨地開始倒數 10 秒
      startCountdown();
    }
  }
};

// 倒计时开始
const startCountdown = () => {
  stopTimers(); // 清除之前的定时器
  
  // ⚡ 核心修復：在倒數開始的這一秒，把秒數固定重設為 10
  countdown.value = 10; 
  
  countdownInterval = window.setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    }
    
    // ⚡ 核心修復：等數字真正扣到 0 的時候，才去啟動下一輪 API，完美避開 JavaScript 執行緒卡頓
    if (countdown.value <= 0) {
      clearInterval(countdownInterval!);
      countdownInterval = null;
      
      // 倒計時完全結束，清空了執行緒，才發起下一輪
      startRound();
    }
  }, 1000);
};

// 停止所有定时器
const stopTimers = () => {
  if (countdownInterval) {
    clearInterval(countdownInterval);
    countdownInterval = null;
  }
  // ⚡ 優化：停止計時器時，主動把可能還在卡頓的 API 請求直接強行斷開
  if (abortController) {
    abortController.abort();
    abortController = null;
  }
};


// 从父组件调用重新开始
const startCheck = () => {
  currentAttempt.value = 0;
  checking.value = true;
  errorMessage.value = '';
  stopTimers();
  startRound();
};

onMounted(() => {
  startRound();
});

onBeforeUnmount(() => {
  stopTimers();
});

// 暴露方法以便父组件可调用
defineExpose({ startCheck });
</script>

<style scoped>
.backend-check-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.check-card {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  max-width: 400px;
  width: 80%;
}
.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 20px auto;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.attempt-info {
  margin-top: 10px;
  color: #666;
  font-size: 0.9rem;
}
.countdown {
  margin-top: 8px;
  font-weight: bold;
  color: #333;
}
.error-msg {
  color: #e74c3c;
  font-weight: bold;
  margin-bottom: 15px;
}
.retry-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  margin-top: 20px;
}
</style>