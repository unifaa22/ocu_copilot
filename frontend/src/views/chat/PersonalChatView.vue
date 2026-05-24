<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { categoryApi, chatApi } from '@/api'
import { useToast } from '@/composables/useToast'
import ConversationList from '@/components/chat/ConversationList.vue'
import ChatMessage from '@/components/chat/ChatMessage.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const route = useRoute()
const toast = useToast()

const conversations = ref([])
const convLoading = ref(false)
const activeConversationId = ref(null)
const messages = ref([])
const categories = ref([])
const selectedCategoryIds = ref([])
const question = ref('')
const sending = ref(false)
const confirmClear = ref(false)
const confirmDeleteConv = ref(false)
const deleteConvId = ref(null)

const canSend = computed(
  () => question.value.trim() && selectedCategoryIds.value.length > 0 && !sending.value,
)

async function loadConversations() {
  convLoading.value = true
  try {
    const data = await chatApi.conversations({ type: 'personal', page: 1, size: 50 })
    conversations.value = data.list || []
  } catch (e) {
    toast.error(e.message)
  } finally {
    convLoading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = await categoryApi.list()
  } catch (e) {
    toast.error(e.message)
  }
}

async function loadMessages(conversationId) {
  if (!conversationId) {
    messages.value = []
    return
  }
  try {
    const data = await chatApi.conversationDetail(conversationId)
    messages.value = data.messages || []
    activeConversationId.value = conversationId
  } catch (e) {
    toast.error(e.message)
  }
}

function newConversation() {
  activeConversationId.value = null
  messages.value = []
}

function selectConversation(id) {
  loadMessages(id)
}

function requestDeleteConv(id) {
  deleteConvId.value = id
  confirmDeleteConv.value = true
}

async function deleteConversation() {
  try {
    await chatApi.deleteConversation(deleteConvId.value)
    toast.success('会话已删除')
    if (activeConversationId.value === deleteConvId.value) newConversation()
    confirmDeleteConv.value = false
    loadConversations()
  } catch (e) {
    toast.error(e.message)
  }
}

function toggleCategory(id) {
  const idx = selectedCategoryIds.value.indexOf(id)
  if (idx >= 0) selectedCategoryIds.value.splice(idx, 1)
  else selectedCategoryIds.value.push(id)
}

async function sendMessage() {
  if (!canSend.value) return
  sending.value = true
  try {
    const payload = {
      question: question.value.trim(),
      categoryIds: [...selectedCategoryIds.value],
    }
    if (activeConversationId.value) payload.conversationId = activeConversationId.value
    const res = await chatApi.personal(payload)
    activeConversationId.value = res.conversationId
    messages.value.push({
      id: res.historyId,
      question: question.value.trim(),
      answer: res.answer,
      categoryNames: res.categoryNames,
      createTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
    })
    question.value = ''
    loadConversations()
  } catch (e) {
    toast.error(e.message)
  } finally {
    sending.value = false
  }
}

async function clearAllHistory() {
  try {
    await chatApi.clearHistory()
    toast.success('历史已清空')
    newConversation()
    confirmClear.value = false
    loadConversations()
  } catch (e) {
    toast.error(e.message)
  }
}

onMounted(() => {
  loadConversations()
  loadCategories()
  const q = typeof route.query.q === 'string' ? route.query.q.trim() : ''
  if (q) question.value = q
})
</script>

<template>
  <div class="flex h-[calc(100vh-6.5rem)] flex-col">
    <div class="relative flex min-h-0 flex-1 overflow-hidden rounded-xl border border-morandi-peach/20 dark:border-morandi-peach/10">
      <button
        type="button"
        class="absolute top-2 right-2 z-10 cursor-pointer rounded-lg bg-white/90 px-2 py-1 text-xs text-red-500 shadow-sm transition-colors duration-200 hover:text-red-600 dark:bg-morandi-dark-card/90"
        @click="confirmClear = true"
      >
        清空全部历史
      </button>
      <div class="w-56 shrink-0">
        <ConversationList
          :conversations="conversations"
          :active-id="activeConversationId"
          :loading="convLoading"
          @select="selectConversation"
          @new="newConversation"
          @delete="requestDeleteConv"
        />
      </div>

      <div class="flex min-w-0 flex-1 flex-col bg-morandi-sand/30 dark:bg-morandi-dark-bg/50">
        <div class="border-b border-morandi-peach/15 bg-white px-4 py-3 dark:bg-morandi-dark-card">
          <p class="mb-2 text-xs font-semibold text-neutral-500">选择知识库分类（至少 1 个）</p>
          <div class="flex flex-wrap gap-3">
            <label
              v-for="cat in categories"
              :key="cat.id"
              class="flex cursor-pointer items-center gap-2 text-sm"
            >
              <input
                v-model="selectedCategoryIds"
                type="checkbox"
                :value="cat.id"
                class="rounded border-morandi-peach text-morandi-clay focus:ring-morandi-clay"
              />
              <span>{{ cat.categoryName }}</span>
            </label>
            <span v-if="!categories.length" class="text-xs text-neutral-400">请先在知识库创建分类</span>
          </div>
        </div>

        <div class="flex-1 space-y-6 overflow-y-auto p-4">
          <p v-if="!messages.length" class="text-center text-sm text-neutral-500">
            输入问题开始对话，或从左侧选择历史会话
          </p>
          <ChatMessage
            v-for="msg in messages"
            :key="msg.id"
            :question="msg.question"
            :answer="msg.answer"
            :category-names="msg.categoryNames"
            :create-time="msg.createTime"
          />
          <div v-if="sending" class="flex items-center gap-2 text-sm text-neutral-500">
            <i class="fa-solid fa-spinner fa-spin" aria-hidden="true" /> AI 思考中…
          </div>
        </div>

        <div class="border-t border-morandi-peach/15 bg-white p-4 dark:bg-morandi-dark-card">
          <form class="flex gap-2" @submit.prevent="sendMessage">
            <input
              v-model="question"
              type="text"
              class="input-field flex-1"
              placeholder="输入你的问题…"
              :disabled="sending"
              aria-label="问题输入"
            />
            <button type="submit" class="btn-primary shrink-0" :disabled="!canSend">
              <i class="fa-solid fa-paper-plane" aria-hidden="true" />
            </button>
          </form>
        </div>
      </div>
    </div>

    <ConfirmDialog
      :show="confirmClear"
      title="清空历史"
      message="将删除所有问答历史记录，确定继续？"
      danger
      @close="confirmClear = false"
      @confirm="clearAllHistory"
    />
    <ConfirmDialog
      :show="confirmDeleteConv"
      title="删除会话"
      message="确定删除该会话及其中所有消息？"
      danger
      @close="confirmDeleteConv = false"
      @confirm="deleteConversation"
    />
  </div>
</template>
