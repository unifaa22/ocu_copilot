<script setup>
import BaseModal from './BaseModal.vue'

defineProps({
  show: Boolean,
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '确定要执行此操作吗？' },
  confirmText: { type: String, default: '确定' },
  danger: { type: Boolean, default: false },
})
const emit = defineEmits(['close', 'confirm'])
</script>

<template>
  <BaseModal :show="show" :title="title" @close="emit('close')">
    <div class="px-6 py-5">
      <p class="text-sm text-neutral-600 dark:text-neutral-300">{{ message }}</p>
      <div class="mt-6 flex justify-end gap-3">
        <button
          class="rounded-lg border border-morandi-peach/20 px-4 py-2 text-sm font-medium text-neutral-600 hover:bg-morandi-sand dark:text-neutral-300 dark:hover:bg-morandi-dark-bg"
          @click="emit('close')"
        >
          取消
        </button>
        <button
          class="rounded-lg px-4 py-2 text-sm font-medium text-white"
          :class="danger ? 'bg-red-500 hover:bg-red-600' : 'bg-morandi-clay hover:bg-morandi-clay-hover'"
          @click="emit('confirm')"
        >
          {{ confirmText }}
        </button>
      </div>
    </div>
  </BaseModal>
</template>
