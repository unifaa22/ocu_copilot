<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { teamApi, chatApi } from '@/api'
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
const teams = ref([])
const selectedTeamId = ref(null)
const question = ref('')
const sending = ref(false)
const confirmDeleteConv = ref(false)
const deleteConvId = ref(null)

const selectedTeam = computed(() => teams.value.find((t) => t.id === selectedTeamId.value))

const shareEnabled = computed(() => selectedTeam.value?.isShare === 1)

const canSend = computed(
  () => question.value.trim() && selectedTeamId.value && shareEnabled.value && !sending.value,
)

async function loadConversations() {
  convLoading.value = true
  try {
    const data = await chatApi.conversations({ type: 'team', page: 1, size: 50 })
    conversations.value = data.list || []
  } catch (e) {
    toast.error(e.message)
  } finally {
    convLoading.value = false
  }
}

async function loadTeams() {
  try {
    const data = await teamApi.joined({ page: 1, size: 50 })
    teams.value = data.list || []
    if (!selectedTeamId.value && teams.value.length) {
      const shared = teams.value.find((t) => t.isShare === 1)
      selectedTeamId.value = shared?.id || teams.value[0].id
    }
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
    if (data.teamId) selectedTeamId.value = data.teamId
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

async function sendMessage() {
  if (!canSend.value) return
  sending.value = true
  try {
    const payload = {
      question: question.value.trim(),
      teamId: selectedTeamId.value,
    }
    if (activeConversationId.value) payload.conversationId = activeConversationId.value
    const res = await chatApi.team(payload)
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

onMounted(() => {
  loadConversations()
  loadTeams()
  const q = typeof route.query.q === 'string' ? route.query.q.trim() : ''
  if (q) question.value = q
})
</script>

<template>
  <div class="flex h-[calc(100vh-6.5rem)] flex-col">
    <div class="flex min-h-0 flex-1 overflow-hidden rounded-xl border border-morandi-peach/20 dark:border-morandi-peach/10">
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
          <label for="team-select" class="mb-1 block text-xs font-semibold text-neutral-500">选择团队</label>
          <select id="team-select" v-model="selectedTeamId" class="input-field max-w-md py-2 text-sm">
            <option v-for="t in teams" :key="t.id" :value="t.id">
              {{ t.teamName }} · {{ t.isShare === 1 ? '共享已开启' : '共享未开启' }}
            </option>
          </select>
          <p
            v-if="selectedTeam && !shareEnabled"
            class="mt-2 rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-800 dark:bg-amber-950/30 dark:text-amber-200"
          >
            该团队尚未开启知识库共享，请联系创建者在团队管理中开启后再提问。
          </p>
        </div>

        <div class="flex-1 space-y-6 overflow-y-auto p-4">
          <p v-if="!messages.length" class="text-center text-sm text-neutral-500">选择团队后输入问题</p>
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
              placeholder="输入团队相关问题…"
              :disabled="sending || !shareEnabled"
              aria-label="团队问题输入"
            />
            <button type="submit" class="btn-primary shrink-0" :disabled="!canSend">发送</button>
          </form>
        </div>
      </div>
    </div>

    <ConfirmDialog
      :show="confirmDeleteConv"
      title="删除会话"
      message="确定删除该会话？"
      danger
      @close="confirmDeleteConv = false"
      @confirm="deleteConversation"
    />
  </div>
</template>
