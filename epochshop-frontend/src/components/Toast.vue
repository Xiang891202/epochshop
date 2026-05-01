<template>
  <TransitionGroup name="toast" tag="div" class="toast-container">
    <div v-for="msg in messages" :key="msg.id" :class="['toast', msg.type]">
      {{ msg.text }}
    </div>
  </TransitionGroup>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const messages = ref<Array<{ id: number; text: string; type: string }>>([]);
let nextId = 0;

function show(text: string, type: string = 'info') {
  const id = nextId++;
  messages.value.push({ id, text, type });
  setTimeout(() => {
    messages.value = messages.value.filter(m => m.id !== id);
  }, 3000);
}

defineExpose({ show });
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.toast {
  padding: 12px 24px;
  border-radius: 6px;
  color: white;
  font-size: 0.95rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  cursor: pointer;
}
.toast.info { background: var(--info-color); }
.toast.success { background: var(--primary-color); }
.toast.error { background: var(--danger-color); }
.toast-enter-active { animation: slideIn 0.3s ease; }
.toast-leave-active { animation: slideOut 0.3s ease; }
@keyframes slideIn {
  from { transform: translateX(100%); opacity: 0; }
  to { transform: translateX(0); opacity: 1; }
}
@keyframes slideOut {
  from { transform: translateX(0); opacity: 1; }
  to { transform: translateX(100%); opacity: 0; }
}
</style>