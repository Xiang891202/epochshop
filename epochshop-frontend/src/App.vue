<template>
  <router-view />
  <Toast ref="toastRef" />
</template>

<script setup lang="ts">
import { ref, provide } from 'vue';
import Toast from './components/Toast.vue';

const toastRef = ref<InstanceType<typeof Toast> | null>(null);

function toast(text: string, type: string = 'info') {
  toastRef.value?.show(text, type);
}
provide('toast', toast);
</script>