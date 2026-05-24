<script setup>
defineProps({
  icon: { type: String, default: 'fa-inbox' },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
})
</script>

<template>
  <div class="flex flex-col items-center justify-center p-12 text-center">
    <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-morandi-sand-dark dark:bg-morandi-dark-bg">
      <i class="fa-solid text-3xl text-neutral-400" :class="icon" />
    </div>
    <h4 class="font-bold text-neutral-700 dark:text-neutral-300">{{ title }}</h4>
    <p v-if="description" class="mt-1 max-w-sm text-xs text-neutral-500">{{ description }}</p>
    <slot />
  </div>
</template>
