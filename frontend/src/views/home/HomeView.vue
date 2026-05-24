<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import HomeMenuDropdown from '@/components/home/HomeMenuDropdown.vue'

const router = useRouter()

const question = ref('')
const chatMode = ref('personal')
const searchMode = ref('fast')
/** 同时仅允许一个下拉展开 */
const openMenu = ref(null)
const menuGroupRef = ref(null)

const chatMenuOpen = computed({
  get: () => openMenu.value === 'chat',
  set(open) {
    openMenu.value = open ? 'chat' : openMenu.value === 'chat' ? null : openMenu.value
  },
})

const searchMenuOpen = computed({
  get: () => openMenu.value === 'search',
  set(open) {
    openMenu.value = open ? 'search' : openMenu.value === 'search' ? null : openMenu.value
  },
})

function onDocumentClick(e) {
  if (menuGroupRef.value?.contains(e.target)) return
  openMenu.value = null
}

onMounted(() => document.addEventListener('click', onDocumentClick))
onUnmounted(() => document.removeEventListener('click', onDocumentClick))

const chatModes = [
  { value: 'personal', label: '个人问答', icon: 'fa-comments' },
  { value: 'team', label: '团队问答', icon: 'fa-users-rays' },
]

const searchModes = [
  { value: 'fast', label: '快速检索', icon: 'fa-bolt' },
  { value: 'deep', label: '深度检索', icon: 'fa-magnifying-glass' },
]

const quickActions = [
  { label: '知识库', icon: 'fa-book', route: { name: 'knowledge' } },
  { label: '文档解读', icon: 'fa-file-lines', route: { name: 'knowledge' } },
  { label: '学习笔记', icon: 'fa-note-sticky', route: { name: 'notes' } },
  { label: '个人问答', icon: 'fa-wand-magic-sparkles', route: { name: 'chat-personal' } },
  { label: '团队问答', icon: 'fa-people-group', route: { name: 'chat-team' } },
]

const canSend = computed(() => question.value.trim().length > 0)

const targetChatRoute = computed(() =>
  chatMode.value === 'team' ? 'chat-team' : 'chat-personal',
)

function sendQuestion() {
  if (!canSend.value) return
  router.push({
    name: targetChatRoute.value,
    query: { q: question.value.trim(), mode: searchMode.value },
  })
}

function goQuick(action) {
  router.push(action.route)
}
</script>

<template>
  <div class="home-page -m-4 flex min-h-[calc(100vh-5rem)] flex-col items-center justify-center px-6 py-10">
    <div class="mb-12 text-center">
      <span
        class="mb-5 inline-block rounded-full border border-morandi-sage/25 bg-morandi-sage/10 px-3.5 py-1 text-xs font-medium text-morandi-sage dark:border-morandi-sage/20 dark:bg-morandi-sage/10 dark:text-morandi-peach"
      >
        智能学习助手
      </span>
      <div class="flex flex-col items-center">
        <div class="flex items-end gap-2.5">
          <div
            class="mb-1 flex h-11 w-11 items-center justify-center rounded-2xl bg-morandi-clay text-white shadow-md"
          >
            <i class="fa-solid fa-graduation-cap text-xl" aria-hidden="true" />
          </div>
          <h1 class="text-[2.5rem] font-bold leading-none tracking-tight text-morandi-charcoal dark:text-morandi-dark-text">
            OCU
          </h1>
        </div>
        <p class="mt-1 text-base font-light tracking-[0.4em] text-neutral-400 dark:text-neutral-500">copilot</p>
      </div>
    </div>

    <div class="home-composer-wrap">
      <div
        class="home-composer flex h-[128px] flex-col rounded-3xl border border-white/85 bg-white px-4 pt-3.5 pb-2.5 shadow-[0_10px_40px_rgba(45,39,34,0.08)] transition-shadow duration-300 focus-within:shadow-[0_16px_48px_rgba(192,130,97,0.14)] sm:h-[136px] sm:px-[22px] sm:pt-[18px] sm:pb-3 dark:border-morandi-peach/15 dark:bg-morandi-dark-card dark:shadow-[0_10px_40px_rgba(0,0,0,0.35)] dark:focus-within:shadow-[0_16px_48px_rgba(192,130,97,0.12)]"
      >
        <textarea
          v-model="question"
          class="min-h-0 w-full flex-1 resize-none border-0 bg-transparent text-[15px] leading-normal text-morandi-charcoal placeholder:text-neutral-400 focus:outline-none dark:text-morandi-dark-text dark:placeholder:text-neutral-500"
          placeholder="有问题尽管问 OCU"
          aria-label="向 OCU 提问"
          @keydown.enter.exact.prevent="sendQuestion"
        />

        <div
          class="mt-2 flex shrink-0 items-center justify-between gap-3 border-t border-neutral-100 pt-2.5 dark:border-morandi-peach/15"
        >
          <div ref="menuGroupRef" class="flex flex-wrap items-center gap-2">
            <HomeMenuDropdown
              v-model="chatMode"
              v-model:open="chatMenuOpen"
              :options="chatModes"
              aria-label="对话模式"
            />
            <HomeMenuDropdown
              v-model="searchMode"
              v-model:open="searchMenuOpen"
              :options="searchModes"
              aria-label="检索模式"
              leading-icon="fa-globe"
            />
            <button
              type="button"
              class="flex h-8 w-8 cursor-not-allowed items-center justify-center rounded-full border border-neutral-200/80 bg-neutral-50/80 text-sm text-neutral-300 dark:border-morandi-peach/20 dark:bg-morandi-dark-bg dark:text-neutral-500"
              title="提及功能即将推出"
              disabled
              aria-label="提及"
            >
              @
            </button>
          </div>

          <div class="flex items-center gap-1.5">
            <button
              type="button"
              class="flex h-9 w-9 cursor-not-allowed items-center justify-center rounded-full text-neutral-300 transition-colors dark:text-neutral-500"
              title="附件上传即将推出"
              disabled
              aria-label="添加附件"
            >
              <i class="fa-solid fa-paperclip text-[15px]" aria-hidden="true" />
            </button>
            <button
              type="button"
              class="flex h-9 w-9 cursor-not-allowed items-center justify-center rounded-full text-neutral-300 transition-colors dark:text-neutral-500"
              title="截图提问即将推出"
              disabled
              aria-label="截图"
            >
              <i class="fa-solid fa-scissors text-[15px]" aria-hidden="true" />
            </button>
            <button
              type="button"
              class="ml-1 flex h-9 w-9 cursor-pointer items-center justify-center rounded-full transition-all duration-200"
              :class="
                canSend
                  ? 'bg-morandi-clay text-white shadow-md hover:bg-morandi-clay-hover'
                  : 'cursor-not-allowed bg-neutral-200 text-neutral-400 dark:bg-neutral-700 dark:text-neutral-500'
              "
              :disabled="!canSend"
              aria-label="发送问题"
              @click="sendQuestion"
            >
              <i class="fa-solid fa-paper-plane text-sm" aria-hidden="true" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="home-quick-actions">
      <button
        v-for="action in quickActions"
        :key="action.label"
        type="button"
        class="group flex w-[4.5rem] shrink-0 cursor-pointer flex-col items-center gap-2.5"
        @click="goQuick(action)"
      >
        <span
          class="flex h-[3.25rem] w-[3.25rem] items-center justify-center rounded-full bg-white text-neutral-500 shadow-[0_2px_12px_rgba(45,39,34,0.06)] ring-1 ring-neutral-200/60 transition-all duration-200 group-hover:-translate-y-0.5 group-hover:bg-morandi-clay/8 group-hover:text-morandi-clay group-hover:ring-morandi-clay/25 group-hover:shadow-[0_6px_20px_rgba(192,130,97,0.15)] dark:bg-morandi-dark-card dark:text-neutral-300 dark:ring-morandi-peach/10 dark:group-hover:bg-morandi-clay/15 dark:group-hover:text-morandi-peach"
        >
          <i class="fa-solid text-[1.15rem]" :class="action.icon" aria-hidden="true" />
        </span>
        <span
          class="text-center text-xs leading-tight text-neutral-500 transition-colors duration-200 group-hover:text-morandi-clay dark:text-neutral-400 dark:group-hover:text-morandi-peach"
        >
          {{ action.label }}
        </span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  background: radial-gradient(ellipse 80% 50% at 50% 0%, rgba(217, 155, 130, 0.08), transparent 70%);
}

:global(.dark) .home-page {
  background: radial-gradient(ellipse 80% 50% at 50% 0%, rgba(192, 130, 97, 0.06), transparent 70%);
}

/* 参照 ima copilot：宽约 56% 视口，固定高度紧凑输入框 */
.home-composer-wrap,
.home-quick-actions {
  width: min(56vw, 720px);
  max-width: calc(100vw - 3rem);
  min-width: min(100%, 520px);
}

.home-quick-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 2rem 2.5rem;
  margin-top: 3.5rem;
}

@media (max-width: 640px) {
  .home-composer-wrap,
  .home-quick-actions {
    width: calc(100vw - 2rem);
    min-width: unset;
  }
}
</style>
