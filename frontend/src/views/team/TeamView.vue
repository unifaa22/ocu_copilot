<script setup>
import { ref, onMounted } from 'vue'
import { teamApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useToast } from '@/composables/useToast'
import { formatDateTime, formatFileSize, getFileTypeIcon } from '@/utils/format'
import BaseModal from '@/components/common/BaseModal.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Pagination from '@/components/common/Pagination.vue'
import FilePreviewModal from '@/components/knowledge/FilePreviewModal.vue'

const toast = useToast()
const authStore = useAuthStore()

const pending = ref([])
const managed = ref([])
const joined = ref([])
const loading = ref(true)

const showCreate = ref(false)
const createName = ref('')
const showInvite = ref(false)
const inviteTeamId = ref(null)
const inviteUsername = ref('')

const members = ref({ list: [], total: 0 })
const memberTeamId = ref(null)
const memberPage = ref(1)
const showMembers = ref(false)

const sharedTeamId = ref(null)
const sharedCategories = ref([])
const sharedCategoryId = ref(null)
const sharedFiles = ref({ list: [], total: 0, page: 1, size: 10 })
const sharedFilePage = ref(1)
const showSharedBrowse = ref(false)

const confirmDissolve = ref(false)
const dissolveTeamId = ref(null)
const confirmLeave = ref(false)
const leaveTeamId = ref(null)

const previewFileId = ref(null)
const showPreview = ref(false)

async function loadAll() {
  loading.value = true
  try {
    const [p, m, j] = await Promise.all([
      teamApi.pendingInvitations({ page: 1, size: 20 }),
      teamApi.managed({ page: 1, size: 20 }),
      teamApi.joined({ page: 1, size: 20 }),
    ])
    pending.value = p.list || []
    managed.value = m.list || []
    joined.value = j.list || []
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function createTeam() {
  const name = createName.value.trim()
  if (!name) {
    toast.error('团队名称不能为空')
    return
  }
  try {
    await teamApi.create(name)
    toast.success('团队已创建')
    showCreate.value = false
    createName.value = ''
    authStore.fetchProfile().catch(() => {})
    loadAll()
  } catch (e) {
    toast.error(e.message)
  }
}

async function acceptInvite(teamId) {
  try {
    await teamApi.acceptInvite(teamId)
    toast.success('已加入团队')
    loadAll()
  } catch (e) {
    toast.error(e.message)
  }
}

async function rejectInvite(teamId) {
  try {
    await teamApi.rejectInvite(teamId)
    toast.success('已拒绝邀请')
    loadAll()
  } catch (e) {
    toast.error(e.message)
  }
}

async function toggleShare(team, enabled) {
  try {
    await teamApi.toggleShare(team.id, enabled)
    toast.success(enabled ? '共享已开启' : '共享已关闭')
    loadAll()
  } catch (e) {
    toast.error(e.message)
  }
}

function openInvite(teamId) {
  inviteTeamId.value = teamId
  inviteUsername.value = ''
  showInvite.value = true
}

async function sendInvite() {
  try {
    await teamApi.invite(inviteTeamId.value, inviteUsername.value.trim())
    toast.success('邀请已发送')
    showInvite.value = false
  } catch (e) {
    toast.error(e.message)
  }
}

function requestDissolve(id) {
  dissolveTeamId.value = id
  confirmDissolve.value = true
}

async function dissolveTeam() {
  try {
    await teamApi.dissolve(dissolveTeamId.value)
    toast.success('团队已解散')
    confirmDissolve.value = false
    loadAll()
  } catch (e) {
    toast.error(e.message)
  }
}

function requestLeave(id) {
  leaveTeamId.value = id
  confirmLeave.value = true
}

async function leaveTeam() {
  try {
    await teamApi.leave(leaveTeamId.value)
    toast.success('已退出团队')
    confirmLeave.value = false
    loadAll()
  } catch (e) {
    toast.error(e.message)
  }
}

async function openMembers(teamId) {
  memberTeamId.value = teamId
  memberPage.value = 1
  showMembers.value = true
  loadMembers()
}

async function loadMembers() {
  try {
    members.value = await teamApi.members(memberTeamId.value, { page: memberPage.value, size: 10 })
  } catch (e) {
    toast.error(e.message)
  }
}

async function removeMember(userId) {
  try {
    await teamApi.removeMember(memberTeamId.value, userId)
    toast.success('成员已移除')
    loadMembers()
  } catch (e) {
    toast.error(e.message)
  }
}

async function openSharedBrowse(teamId) {
  sharedTeamId.value = teamId
  sharedCategoryId.value = null
  sharedFilePage.value = 1
  sharedFiles.value = { list: [], total: 0, page: 1, size: 10 }
  showSharedBrowse.value = true
  try {
    sharedCategories.value = await teamApi.sharedCategories(teamId)
    if (sharedCategories.value.length) {
      sharedCategoryId.value = sharedCategories.value[0].id
      loadSharedFiles()
    }
  } catch (e) {
    toast.error(e.message)
  }
}

async function loadSharedFiles() {
  if (!sharedTeamId.value || !sharedCategoryId.value) return
  try {
    sharedFiles.value = await teamApi.sharedFiles(sharedTeamId.value, sharedCategoryId.value, {
      page: sharedFilePage.value,
      size: 10,
    })
  } catch (e) {
    toast.error(e.message)
  }
}

function previewShared(file) {
  previewFileId.value = file.id
  showPreview.value = true
}

onMounted(loadAll)
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-end">
      <button type="button" class="btn-primary" @click="showCreate = true">
        <i class="fa-solid fa-plus mr-2" aria-hidden="true" />创建团队
      </button>
    </div>

    <div v-if="loading" class="text-center text-sm text-neutral-500">加载中…</div>

    <template v-else>
      <section>
        <h3 class="mb-3 text-sm font-bold text-morandi-clay">待接受邀请</h3>
        <EmptyState v-if="!pending.length" title="暂无待处理邀请" />
        <div v-else class="grid gap-3 sm:grid-cols-2">
          <div
            v-for="inv in pending"
            :key="inv.teamId"
            class="rounded-xl border border-morandi-peach/20 bg-white p-4 dark:bg-morandi-dark-card"
          >
            <p class="font-semibold">{{ inv.teamName }}</p>
            <p class="text-xs text-neutral-500">邀请人：{{ inv.creatorName }} · {{ formatDateTime(inv.inviteTime) }}</p>
            <div class="mt-3 flex gap-2">
              <button type="button" class="btn-primary text-xs" @click="acceptInvite(inv.teamId)">接受</button>
              <button type="button" class="btn-secondary text-xs" @click="rejectInvite(inv.teamId)">拒绝</button>
            </div>
          </div>
        </div>
      </section>

      <section>
        <h3 class="mb-3 text-sm font-bold text-morandi-clay">我创建的团队</h3>
        <EmptyState v-if="!managed.length" title="尚未创建团队" />
        <div v-else class="space-y-3">
          <div
            v-for="team in managed"
            :key="team.id"
            class="rounded-xl border border-morandi-peach/20 bg-white p-4 dark:bg-morandi-dark-card"
          >
            <div class="flex flex-wrap items-center justify-between gap-2">
              <div>
                <p class="font-semibold">{{ team.teamName }}</p>
                <p class="text-xs text-neutral-500">创建于 {{ formatDateTime(team.createTime) }}</p>
              </div>
              <label class="flex cursor-pointer items-center gap-2 text-sm">
                <input
                  type="checkbox"
                  class="rounded text-morandi-clay focus:ring-morandi-clay"
                  :checked="team.isShare === 1"
                  @change="toggleShare(team, $event.target.checked)"
                />
                <span>知识库共享</span>
              </label>
            </div>
            <div class="mt-3 flex flex-wrap gap-2">
              <button type="button" class="btn-secondary text-xs" @click="openMembers(team.id)">成员管理</button>
              <button type="button" class="btn-secondary text-xs" @click="openInvite(team.id)">邀请成员</button>
              <button
                v-if="team.isShare === 1"
                type="button"
                class="btn-secondary text-xs"
                @click="openSharedBrowse(team.id)"
              >
                浏览共享资源
              </button>
              <button
                type="button"
                class="cursor-pointer rounded-lg px-3 py-1.5 text-xs text-red-500 transition-colors duration-200 hover:bg-red-50"
                @click="requestDissolve(team.id)"
              >
                解散团队
              </button>
            </div>
          </div>
        </div>
      </section>

      <section>
        <h3 class="mb-3 text-sm font-bold text-morandi-clay">我加入的团队</h3>
        <EmptyState v-if="!joined.filter((t) => !t.isCreator).length" title="尚未加入其他团队" />
        <div v-else class="space-y-3">
          <div
            v-for="team in joined.filter((t) => !t.isCreator)"
            :key="'j-' + team.id"
            class="rounded-xl border border-morandi-peach/20 bg-white p-4 dark:bg-morandi-dark-card"
          >
            <p class="font-semibold">{{ team.teamName }}</p>
            <p class="text-xs text-neutral-500">
              创建者：{{ team.creatorName }} · 加入于 {{ formatDateTime(team.joinTime) }}
              · {{ team.isShare === 1 ? '共享已开启' : '共享未开启' }}
            </p>
            <div class="mt-3 flex gap-2">
              <button
                v-if="team.isShare === 1"
                type="button"
                class="btn-secondary text-xs"
                @click="openSharedBrowse(team.id)"
              >
                浏览共享资源
              </button>
              <button
                type="button"
                class="cursor-pointer text-xs text-red-500 transition-colors duration-200 hover:text-red-600"
                @click="requestLeave(team.id)"
              >
                退出团队
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>

    <BaseModal :show="showCreate" title="创建团队" @close="showCreate = false">
      <div class="px-6 py-5">
        <input v-model="createName" type="text" class="input-field" placeholder="团队名称" />
        <div class="mt-4 flex justify-end gap-2">
          <button type="button" class="btn-secondary" @click="showCreate = false">取消</button>
          <button type="button" class="btn-primary" @click="createTeam">创建</button>
        </div>
      </div>
    </BaseModal>

    <BaseModal :show="showInvite" title="邀请成员" @close="showInvite = false">
      <div class="px-6 py-5">
        <input v-model="inviteUsername" type="text" class="input-field" placeholder="用户名（精确匹配）" />
        <div class="mt-4 flex justify-end gap-2">
          <button type="button" class="btn-secondary" @click="showInvite = false">取消</button>
          <button type="button" class="btn-primary" @click="sendInvite">发送邀请</button>
        </div>
      </div>
    </BaseModal>

    <BaseModal :show="showMembers" title="团队成员" width="max-w-lg" @close="showMembers = false">
      <div class="max-h-96 overflow-y-auto px-6 py-4">
        <ul class="space-y-2">
          <li
            v-for="m in members.list"
            :key="m.id"
            class="flex items-center justify-between rounded-lg border border-morandi-peach/10 px-3 py-2 text-sm"
          >
            <span>{{ m.username }} <span class="text-xs text-neutral-400">({{ m.memberRole === 1 ? '创建者' : '成员' }})</span></span>
            <button
              v-if="m.memberRole !== 1"
              type="button"
              class="cursor-pointer text-xs text-red-500"
              @click="removeMember(m.userId)"
            >
              移除
            </button>
          </li>
        </ul>
        <Pagination v-model:page="memberPage" :total="members.total" :size="10" @update:page="loadMembers" />
      </div>
    </BaseModal>

    <BaseModal :show="showSharedBrowse" title="共享知识库浏览" width="max-w-3xl" @close="showSharedBrowse = false">
      <div class="px-6 py-4">
        <select
          v-model="sharedCategoryId"
          class="input-field mb-4 max-w-xs"
          @change="sharedFilePage = 1; loadSharedFiles()"
        >
          <option v-for="c in sharedCategories" :key="c.id" :value="c.id">{{ c.categoryName }}</option>
        </select>
        <div v-if="!sharedCategories.length" class="text-sm text-neutral-500">暂无共享分类</div>
        <table v-else class="w-full text-left text-sm">
          <thead class="text-xs text-neutral-500">
            <tr>
              <th class="py-2">文件</th>
              <th class="py-2">大小</th>
              <th class="py-2 text-right">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="f in sharedFiles.list" :key="f.id" class="border-t border-morandi-peach/10">
              <td class="py-2">
                <i class="fa-solid mr-2" :class="getFileTypeIcon(f.fileType)" aria-hidden="true" />
                {{ f.fileName }}
              </td>
              <td class="py-2 text-neutral-500">{{ formatFileSize(f.fileSize) }}</td>
              <td class="py-2 text-right">
                <button type="button" class="cursor-pointer text-morandi-clay text-xs" @click="previewShared(f)">
                  预览
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <Pagination
          v-model:page="sharedFilePage"
          :total="sharedFiles.total"
          :size="10"
          @update:page="loadSharedFiles"
        />
      </div>
    </BaseModal>

    <ConfirmDialog
      :show="confirmDissolve"
      title="解散团队"
      message="解散后所有成员将无法继续访问，确定继续？"
      danger
      @close="confirmDissolve = false"
      @confirm="dissolveTeam"
    />
    <ConfirmDialog
      :show="confirmLeave"
      title="退出团队"
      message="确定退出该团队？"
      danger
      @close="confirmLeave = false"
      @confirm="leaveTeam"
    />
    <FilePreviewModal :show="showPreview" :file-id="previewFileId" @close="showPreview = false" />
  </div>
</template>
