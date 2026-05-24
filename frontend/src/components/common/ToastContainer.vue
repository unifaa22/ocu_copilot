<script setup>
import { useToast } from '@/composables/useToast'

const { toasts } = useToast()
</script>

<template>
  <div class="pointer-events-none fixed top-4 right-4 z-[100] flex flex-col gap-2">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto flex min-w-[280px] items-center gap-3 rounded-lg border px-4 py-3 shadow-lg"
        :class="{
          'border-emerald-200 bg-emerald-50 text-emerald-800 dark:border-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300':
            toast.type === 'success',
          'border-red-200 bg-red-50 text-red-800 dark:border-red-800 dark:bg-red-950/40 dark:text-red-300':
            toast.type === 'error',
          'border-morandi-peach/30 bg-white text-morandi-charcoal dark:border-morandi-peach/20 dark:bg-morandi-dark-card dark:text-morandi-dark-text':
            toast.type === 'info',
        }"
      >
        <i
          class="fa-solid"
          :class="{
            'fa-circle-check': toast.type === 'success',
            'fa-circle-xmark': toast.type === 'error',
            'fa-circle-info': toast.type === 'info',
          }"
        />
        <span class="text-sm font-medium">{{ toast.message }}</span>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
