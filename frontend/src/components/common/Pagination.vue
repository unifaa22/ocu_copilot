<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
})
const emit = defineEmits(['update:page'])

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

function go(p) {
  if (p >= 1 && p <= totalPages.value) emit('update:page', p)
}
</script>

<template>
  <div v-if="total > size" class="flex items-center justify-between px-2 py-3 text-xs text-neutral-500">
    <span>共 {{ total }} 条</span>
    <div class="flex items-center gap-2">
      <button
        class="rounded px-2 py-1 hover:bg-morandi-sand disabled:opacity-40 dark:hover:bg-morandi-dark-bg"
        :disabled="page <= 1"
        @click="go(page - 1)"
      >
        上一页
      </button>
      <span>{{ page }} / {{ totalPages }}</span>
      <button
        class="rounded px-2 py-1 hover:bg-morandi-sand disabled:opacity-40 dark:hover:bg-morandi-dark-bg"
        :disabled="page >= totalPages"
        @click="go(page + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>
