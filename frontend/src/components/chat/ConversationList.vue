<script setup>
import { formatDateTime } from '@/utils/format'

defineProps({
  conversations: { type: Array, default: () => [] },
  activeId: { type: String, default: null },
  loading: { type: Boolean, default: false },
})
const emit = defineEmits(['select', 'new', 'delete'])
</script>

<template>
  <div class="flex h-full flex-col border-r border-morandi-peach/20 bg-white dark:border-morandi-peach/10 dark:bg-morandi-dark-card">
    <div class="flex items-center justify-between border-b border-morandi-peach/10 p-3">
      <h3 class="text-sm font-bold text-morandi-charcoal dark:text-morandi-dark-text">会话列表</h3>
      <button
        type="button"
        aria-label="新建会话"
        class="cursor-pointer rounded-lg bg-morandi-clay px-2.5 py-1 text-xs text-white transition-colors duration-200 hover:bg-morandi-clay-hover"
        @click="emit('new')"
      >
        <i class="fa-solid fa-plus mr-1" aria-hidden="true" />新建
      </button>
    </div>
    <div v-if="loading" class="p-4 text-center text-xs text-neutral-500">
      <i class="fa-solid fa-spinner fa-spin" aria-hidden="true" /> 加载中…
    </div>
    <ul v-else class="flex-1 space-y-1 overflow-y-auto p-2" role="list">
      <li v-for="conv in conversations" :key="conv.conversationId" role="listitem">
        <div
          class="group flex cursor-pointer items-start gap-2 rounded-lg p-2.5 transition-colors duration-200"
          :class="
            activeId === conv.conversationId
              ? 'bg-morandi-clay/15 dark:bg-morandi-clay/25'
              : 'hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg'
          "
          @click="emit('select', conv.conversationId)"
        >
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium text-morandi-charcoal dark:text-morandi-dark-text">
              {{ conv.lastQuestion || '新会话' }}
            </p>
            <p class="mt-0.5 truncate text-xs text-neutral-500">
              {{ conv.messageCount }} 条 · {{ formatDateTime(conv.lastTime) }}
            </p>
            <p v-if="conv.teamName" class="mt-0.5 truncate text-[10px] text-morandi-clay">
              {{ conv.teamName }}
            </p>
          </div>
          <button
            type="button"
            aria-label="删除会话"
            class="shrink-0 cursor-pointer rounded p-1 text-neutral-400 opacity-0 transition-all duration-200 group-hover:opacity-100 hover:bg-red-50 hover:text-red-500"
            @click.stop="emit('delete', conv.conversationId)"
          >
            <i class="fa-solid fa-trash-can text-xs" aria-hidden="true" />
          </button>
        </div>
      </li>
      <li v-if="!conversations.length" class="p-6 text-center text-xs text-neutral-500">暂无会话</li>
    </ul>
  </div>
</template>
