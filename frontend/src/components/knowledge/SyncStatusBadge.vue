<script setup>
import { computed } from 'vue'
import { syncStatusLabel } from '@/utils/format'

const props = defineProps({
  syncStatus: { type: Number, default: 0 },
})

const meta = computed(() => syncStatusLabel(props.syncStatus))
</script>

<template>
  <span
    class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
    :class="meta.class"
    :aria-label="`同步状态：${meta.text}`"
  >
    {{ meta.text }}
  </span>
</template>
