<script setup>
import { computed } from 'vue'
import { formatDateTime } from '@/utils/format'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps({
  question: { type: String, default: '' },
  answer: { type: String, default: '' },
  categoryNames: { type: Array, default: () => [] },
  createTime: { type: String, default: '' },
})

const answerHtml = computed(() => renderMarkdown(props.answer))
</script>

<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <div class="max-w-[85%] rounded-2xl rounded-br-sm bg-morandi-clay px-4 py-3 text-sm text-white shadow-sm">
        <p class="whitespace-pre-wrap">{{ question }}</p>
        <p v-if="createTime" class="mt-1 text-right text-[10px] text-white/70">
          {{ formatDateTime(createTime) }}
        </p>
      </div>
    </div>
    <div class="flex justify-start">
      <div
        class="max-w-[90%] rounded-2xl rounded-bl-sm border border-morandi-peach/20 bg-white px-4 py-3 shadow-sm dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
      >
        <div
          v-if="categoryNames?.length"
          class="mb-2 flex flex-wrap gap-1"
        >
          <span
            v-for="name in categoryNames"
            :key="name"
            class="rounded bg-morandi-sage/20 px-1.5 py-0.5 text-[10px] text-morandi-sage dark:text-morandi-peach"
          >
            {{ name }}
          </span>
        </div>
        <div class="markdown-body text-sm dark:text-morandi-dark-text" v-html="answerHtml" />
      </div>
    </div>
  </div>
</template>
