<script setup>
import { ref, onMounted, computed } from 'vue'
import { categoryApi, fileApi } from '@/api'
import { useToast } from '@/composables/useToast'
import { formatDateTime, formatFileSize, getFileTypeIcon } from '@/utils/format'
import { validateFileName } from '@/utils/filename'
import BaseModal from '@/components/common/BaseModal.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import Pagination from '@/components/common/Pagination.vue'
import SyncStatusBadge from '@/components/knowledge/SyncStatusBadge.vue'
import FilePreviewModal from '@/components/knowledge/FilePreviewModal.vue'

const toast = useToast()

const categories = ref([])
const selectedCategoryId = ref(null)
const files = ref({ list: [], total: 0, page: 1, size: 10 })
const filePage = ref(1)
const syncFilter = ref('')
const loadingCats = ref(false)
const loadingFiles = ref(false)
const syncing = ref(false)

const showCatModal = ref(false)
const catModalMode = ref('create')
const catFormName = ref('')
const editingCatId = ref(null)

const showRenameFile = ref(false)
const renameFileId = ref(null)
const renameFileName = ref('')

const confirmCatDelete = ref(false)
const confirmFileDelete = ref(false)
const deleteTargetId = ref(null)

const previewFileId = ref(null)
const showPreview = ref(false)

const fileInputRef = ref(null)
const uploading = ref(false)

const selectedCategory = computed(() =>
  categories.value.find((c) => c.id === selectedCategoryId.value),
)

const syncSummary = computed(() => {
  const cat = selectedCategory.value
  if (!cat) return ''
  if (cat.fileCount === 0) return '暂无文件'
  if (cat.syncedCount >= cat.fileCount) return '全部已索引'
  if (cat.syncedCount > 0) return `部分已索引 (${cat.syncedCount}/${cat.fileCount})`
  return '尚未同步'
})

async function loadCategories() {
  loadingCats.value = true
  try {
    categories.value = await categoryApi.list()
    if (!selectedCategoryId.value && categories.value.length) {
      selectedCategoryId.value = categories.value[0].id
    }
    if (selectedCategoryId.value && !categories.value.find((c) => c.id === selectedCategoryId.value)) {
      selectedCategoryId.value = categories.value[0]?.id || null
    }
  } catch (e) {
    toast.error(e.message)
  } finally {
    loadingCats.value = false
  }
}

async function loadFiles() {
  if (!selectedCategoryId.value) {
    files.value = { list: [], total: 0, page: 1, size: 10 }
    return
  }
  loadingFiles.value = true
  try {
    const params = { page: filePage.value, size: 10 }
    if (syncFilter.value !== '') params.syncStatus = syncFilter.value
    const data = await fileApi.listByCategory(selectedCategoryId.value, params)
    files.value = data
  } catch (e) {
    toast.error(e.message)
  } finally {
    loadingFiles.value = false
  }
}

function selectCategory(id) {
  selectedCategoryId.value = id
  filePage.value = 1
  loadFiles()
}

function openCreateCat() {
  catModalMode.value = 'create'
  catFormName.value = ''
  editingCatId.value = null
  showCatModal.value = true
}

function openEditCat(cat) {
  catModalMode.value = 'edit'
  catFormName.value = cat.categoryName
  editingCatId.value = cat.id
  showCatModal.value = true
}

async function saveCategory() {
  const name = catFormName.value.trim()
  if (!name) {
    toast.error('分类名称不能为空')
    return
  }
  try {
    if (catModalMode.value === 'create') {
      await categoryApi.create(name)
      toast.success('分类已创建')
    } else {
      await categoryApi.update(editingCatId.value, name)
      toast.success('分类已更新')
    }
    showCatModal.value = false
    await loadCategories()
    loadFiles()
  } catch (e) {
    toast.error(e.message)
  }
}

function requestDeleteCat(id) {
  deleteTargetId.value = id
  confirmCatDelete.value = true
}

async function deleteCategory() {
  try {
    await categoryApi.remove(deleteTargetId.value)
    toast.success('分类已删除')
    confirmCatDelete.value = false
    await loadCategories()
    loadFiles()
  } catch (e) {
    toast.error(e.message)
  }
}

async function syncCategory() {
  if (!selectedCategoryId.value || syncing.value) return
  syncing.value = true
  try {
    const result = await categoryApi.sync(selectedCategoryId.value)
    toast.success(`同步完成：成功 ${result.successCount}，失败 ${result.failCount}`)
    await loadCategories()
    loadFiles()
  } catch (e) {
    toast.error(e.message)
  } finally {
    syncing.value = false
  }
}

function triggerUpload() {
  fileInputRef.value?.click()
}

async function onFileSelected(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file || !selectedCategoryId.value) return
  uploading.value = true
  try {
    await fileApi.upload(selectedCategoryId.value, file)
    toast.success('上传成功')
    await loadCategories()
    loadFiles()
  } catch (err) {
    toast.error(err.message)
  } finally {
    uploading.value = false
  }
}

function openRename(file) {
  renameFileId.value = file.id
  renameFileName.value = file.fileName
  showRenameFile.value = true
}

async function saveRename() {
  const name = renameFileName.value.trim()
  const error = validateFileName(name)
  if (error) {
    toast.error(error)
    return
  }
  try {
    await fileApi.rename(renameFileId.value, name)
    toast.success('已重命名')
    showRenameFile.value = false
    loadFiles()
  } catch (e) {
    toast.error(e.message)
  }
}

function requestDeleteFile(id) {
  deleteTargetId.value = id
  confirmFileDelete.value = true
}

async function deleteFile() {
  try {
    await fileApi.remove(deleteTargetId.value)
    toast.success('文件已删除')
    confirmFileDelete.value = false
    await loadCategories()
    loadFiles()
  } catch (e) {
    toast.error(e.message)
  }
}

function openPreview(file) {
  previewFileId.value = file.id
  showPreview.value = true
}

onMounted(async () => {
  await loadCategories()
  loadFiles()
})
</script>

<template>
  <div class="space-y-4">
    <div class="flex justify-end">
      <button type="button" class="btn-primary" @click="openCreateCat">
        <i class="fa-solid fa-plus mr-2" aria-hidden="true" />新建分类
      </button>
    </div>

    <div v-if="loadingCats" class="text-center text-sm text-neutral-500">加载分类中…</div>
    <div v-else-if="!categories.length" class="rounded-xl border border-dashed border-morandi-peach/30 bg-white dark:bg-morandi-dark-card">
      <EmptyState title="暂无分类" description="创建第一个知识库分类开始使用" />
    </div>
    <div v-else class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="cursor-pointer rounded-xl border p-4 transition-colors duration-200"
        :class="
          selectedCategoryId === cat.id
            ? 'border-morandi-clay bg-morandi-clay/5 shadow-sm dark:bg-morandi-clay/10'
            : 'border-morandi-peach/20 bg-white hover:border-morandi-clay/40 dark:border-morandi-peach/10 dark:bg-morandi-dark-card'
        "
        @click="selectCategory(cat.id)"
      >
        <div class="flex items-start justify-between">
          <h3 class="font-semibold text-morandi-charcoal dark:text-morandi-dark-text">{{ cat.categoryName }}</h3>
          <div class="flex gap-1">
            <button
              type="button"
              aria-label="编辑分类"
              class="cursor-pointer rounded p-1 text-neutral-400 transition-colors duration-200 hover:text-morandi-clay"
              @click.stop="openEditCat(cat)"
            >
              <i class="fa-solid fa-pen text-xs" aria-hidden="true" />
            </button>
            <button
              type="button"
              aria-label="删除分类"
              class="cursor-pointer rounded p-1 text-neutral-400 transition-colors duration-200 hover:text-red-500"
              @click.stop="requestDeleteCat(cat.id)"
            >
              <i class="fa-solid fa-trash text-xs" aria-hidden="true" />
            </button>
          </div>
        </div>
        <p class="mt-2 text-xs text-neutral-500">{{ cat.fileCount }} 个文件 · {{ cat.syncedCount }} 已索引</p>
      </div>
    </div>

    <section
      v-if="selectedCategoryId"
      class="rounded-xl border border-morandi-peach/20 bg-white dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
    >
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-morandi-peach/10 px-5 py-4">
        <div>
          <h3 class="font-bold text-morandi-charcoal dark:text-morandi-dark-text">
            {{ selectedCategory?.categoryName }} · 文件列表
          </h3>
          <p class="text-xs text-neutral-500">{{ syncSummary }}</p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <select v-model="syncFilter" class="input-field w-auto py-1.5 text-xs" @change="filePage = 1; loadFiles()">
            <option value="">全部状态</option>
            <option value="0">未同步</option>
            <option value="1">已索引</option>
            <option value="2">同步失败</option>
          </select>
          <button type="button" class="btn-secondary text-xs" :disabled="syncing" @click="syncCategory">
            <i class="fa-solid fa-cloud-arrow-up mr-1" aria-hidden="true" />
            {{ syncing ? '同步中…' : '同步分类' }}
          </button>
          <button type="button" class="btn-primary text-xs" :disabled="uploading" @click="triggerUpload">
            <i class="fa-solid fa-upload mr-1" aria-hidden="true" />
            {{ uploading ? '上传中…' : '上传文件' }}
          </button>
          <input
            ref="fileInputRef"
            type="file"
            class="hidden"
            accept=".md,.pdf,.doc,.docx"
            @change="onFileSelected"
          />
        </div>
      </div>

      <div v-if="loadingFiles" class="p-8 text-center text-sm text-neutral-500">加载文件中…</div>
      <EmptyState v-else-if="!files.list.length" title="暂无文件" description="上传 md、pdf、doc、docx 格式文件" />
      <div v-else class="overflow-x-auto">
        <table class="w-full text-left text-sm">
          <thead class="border-b border-morandi-peach/10 bg-morandi-sand/50 text-xs text-neutral-500 dark:bg-morandi-dark-bg">
            <tr>
              <th class="px-5 py-3 font-medium">文件名</th>
              <th class="px-3 py-3 font-medium">大小</th>
              <th class="px-3 py-3 font-medium">状态</th>
              <th class="px-3 py-3 font-medium">更新时间</th>
              <th class="px-5 py-3 text-right font-medium">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="file in files.list"
              :key="file.id"
              class="border-b border-morandi-peach/5 transition-colors duration-200 hover:bg-morandi-sand/30 dark:hover:bg-morandi-dark-bg/50"
            >
              <td class="px-5 py-3">
                <div class="flex items-center gap-2">
                  <i class="fa-solid" :class="getFileTypeIcon(file.fileType)" aria-hidden="true" />
                  <span class="font-medium">{{ file.fileName }}</span>
                </div>
              </td>
              <td class="px-3 py-3 text-neutral-500">{{ formatFileSize(file.fileSize) }}</td>
              <td class="px-3 py-3"><SyncStatusBadge :sync-status="file.syncStatus" /></td>
              <td class="px-3 py-3 text-xs text-neutral-500">{{ formatDateTime(file.updateTime) }}</td>
              <td class="px-5 py-3 text-right">
                <button
                  type="button"
                  class="cursor-pointer px-2 text-morandi-clay transition-colors duration-200 hover:text-morandi-clay-hover"
                  @click="openPreview(file)"
                >
                  预览
                </button>
                <button
                  type="button"
                  class="cursor-pointer px-2 text-neutral-500 transition-colors duration-200 hover:text-morandi-clay"
                  @click="openRename(file)"
                >
                  重命名
                </button>
                <button
                  type="button"
                  class="cursor-pointer px-2 text-red-500 transition-colors duration-200 hover:text-red-600"
                  @click="requestDeleteFile(file.id)"
                >
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <Pagination v-model:page="filePage" :total="files.total" :size="files.size" @update:page="loadFiles" />
      </div>
    </section>

    <BaseModal :show="showCatModal" :title="catModalMode === 'create' ? '新建分类' : '重命名分类'" @close="showCatModal = false">
      <div class="px-6 py-5">
        <input v-model="catFormName" type="text" class="input-field" placeholder="分类名称" />
        <div class="mt-4 flex justify-end gap-2">
          <button type="button" class="btn-secondary" @click="showCatModal = false">取消</button>
          <button type="button" class="btn-primary" @click="saveCategory">保存</button>
        </div>
      </div>
    </BaseModal>

    <BaseModal :show="showRenameFile" title="重命名文件" @close="showRenameFile = false">
      <div class="px-6 py-5">
        <input v-model="renameFileName" type="text" class="input-field" />
        <div class="mt-4 flex justify-end gap-2">
          <button type="button" class="btn-secondary" @click="showRenameFile = false">取消</button>
          <button type="button" class="btn-primary" @click="saveRename">保存</button>
        </div>
      </div>
    </BaseModal>

    <ConfirmDialog
      :show="confirmCatDelete"
      title="删除分类"
      message="删除后该分类下所有文件也将被移除，确定继续？"
      danger
      @close="confirmCatDelete = false"
      @confirm="deleteCategory"
    />
    <ConfirmDialog
      :show="confirmFileDelete"
      title="删除文件"
      message="确定删除该文件？"
      danger
      @close="confirmFileDelete = false"
      @confirm="deleteFile"
    />
    <FilePreviewModal :show="showPreview" :file-id="previewFileId" @close="showPreview = false" />
  </div>
</template>
