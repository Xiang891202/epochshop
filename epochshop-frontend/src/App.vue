<template>
  <BackendCheck v-if="!backendReady" @ready="onBackendReady" ref="checkRef" />
  <template v-else>
    <router-view />
    <Toast ref="toastRef" />
  </template>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue';
import Toast from './components/Toast.vue';
import BackendCheck from './components/BackendCheck.vue';

const backendReady = ref(false);
const checkRef = ref<InstanceType<typeof BackendCheck> | null>(null);

const toastRef = ref<InstanceType<typeof Toast> | null>(null);
function toast(text: string, type: string = 'info') {
  toastRef.value?.show(text, type);
}
provide('toast', toast);

const onBackendReady = () => {
  backendReady.value = true;
};
</script>