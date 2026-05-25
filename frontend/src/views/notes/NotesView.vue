<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useDebounceFn, usePreferredDark } from '@vueuse/core'
import { storeToRefs } from 'pinia'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { noteApi } from '@/api'
import { useThemeStore } from '@/stores/theme'
import { useToast } from '@/composables/useToast'
import { formatDateTime } from '@/utils/format'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const toast = useToast()
const themeStore = useThemeStore()
const { theme } = storeToRefs(themeStore)
const preferredDark = usePreferredDark()

const editorTheme = computed(() => {
  if (theme.value === 'dark') return 'dark'
  if (theme.value === 'light') return 'light'
  return preferredDark.value ? 'dark' : 'light'
})

const notes = ref([])
const keyword = ref('')
const selectedTag = ref('')
const activeNoteId = ref(null)
const editorContent = ref('')
const editorTitle = ref('')
const editorTags = ref([])
const tagInput = ref('')
const loading = ref(false)
const saving = ref(false)
const showDeleteConfirm = ref(false)

const allTags = computed(() => {
  const set = new Set()
  notes.value.forEach((n) => (n.tags || []).forEach((t) => set.add(t)))
  return [...set].sort()
})

const filteredNotes = computed(() => {
  let list = notes.value
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter((n) => n.title.toLowerCase().includes(kw))
  }
  if (selectedTag.value) {
    const tag = selectedTag.value.toLowerCase()
    list = list.filter((n) => n.tags?.some((t) => t.toLowerCase().includes(tag)))
  }
  return list
})

async function loadNotes() {
  loading.value = true
  try {
    const params = { page: 1, size: 100 }
    if (keyword.value) params.keyword = keyword.value
    if (selectedTag.value) params.tag = selectedTag.value
    const data = await noteApi.list(params)
    notes.value = data.list || []
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}

async function selectNote(id) {
  try {
    const note = await noteApi.get(id)
    activeNoteId.value = note.id
    editorTitle.value = note.title
    editorContent.value = note.content || ''
    editorTags.value = [...(note.tags || [])]
  } catch (e) {
    toast.error(e.message)
  }
}

async function createNote() {
  try {
    const note = await noteApi.create({ title: '未命名笔记', content: '', tags: [] })
    await loadNotes()
    selectNote(note.id)
    toast.success('已创建笔记')
  } catch (e) {
    toast.error(e.message)
  }
}

function patchNoteInList(updated) {
  const idx = notes.value.findIndex((n) => n.id === updated.id)
  if (idx === -1) return
  notes.value[idx] = {
    ...notes.value[idx],
    title: updated.title,
    tags: updated.tags,
    updateTime: updated.updateTime,
  }
  notes.value.sort((a, b) => new Date(b.updateTime) - new Date(a.updateTime))
}

const debouncedSave = useDebounceFn(async () => {
  if (!activeNoteId.value) return
  saving.value = true
  try {
    const updated = await noteApi.update(activeNoteId.value, {
      title: editorTitle.value,
      content: editorContent.value,
      tags: editorTags.value,
    })
    patchNoteInList(updated)
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}, 800)

watch([editorTitle, editorContent, editorTags], () => {
  if (activeNoteId.value) debouncedSave()
})

function addTag() {
  const t = tagInput.value.trim()
  if (!t) return
  if (!editorTags.value.includes(t)) editorTags.value.push(t)
  tagInput.value = ''
}

function removeTag(tag) {
  editorTags.value = editorTags.value.filter((t) => t !== tag)
}

async function confirmDelete() {
  try {
    await noteApi.remove(activeNoteId.value)
    toast.success('笔记已删除')
    activeNoteId.value = null
    editorTitle.value = ''
    editorContent.value = ''
    editorTags.value = []
    showDeleteConfirm.value = false
    loadNotes()
  } catch (e) {
    toast.error(e.message)
  }
}

onMounted(loadNotes)
</script>

<template>
  <div class="flex h-[calc(100vh-6.5rem)] flex-col">
    <div class="mb-2 flex justify-end">
      <button type="button" class="btn-primary text-sm" @click="createNote">
        <i class="fa-solid fa-plus mr-1" aria-hidden="true" />新建笔记
      </button>
    </div>

    <div class="flex min-h-0 flex-1 gap-4 overflow-hidden rounded-xl border border-morandi-peach/20 bg-white dark:border-morandi-peach/10 dark:bg-morandi-dark-card">
      <aside class="flex w-72 shrink-0 flex-col border-r border-morandi-peach/10">
        <div class="space-y-2 border-b border-morandi-peach/10 p-3">
          <input
            v-model="keyword"
            type="search"
            class="input-field py-2 text-xs"
            placeholder="搜索标题…"
            aria-label="搜索笔记"
            @keyup.enter="loadNotes"
          />
          <div class="flex flex-wrap gap-1">
            <button
              type="button"
              class="cursor-pointer rounded-full px-2 py-0.5 text-[10px] transition-colors duration-200"
              :class="!selectedTag ? 'bg-morandi-clay text-white' : 'bg-morandi-sand text-neutral-600 dark:bg-morandi-dark-bg'"
              @click="selectedTag = ''; loadNotes()"
            >
              全部
            </button>
            <button
              v-for="tag in allTags"
              :key="tag"
              type="button"
              class="cursor-pointer rounded-full px-2 py-0.5 text-[10px] transition-colors duration-200"
              :class="selectedTag === tag ? 'bg-morandi-clay text-white' : 'bg-morandi-sand text-neutral-600 dark:bg-morandi-dark-bg'"
              @click="selectedTag = tag; loadNotes()"
            >
              {{ tag }}
            </button>
          </div>
        </div>
        <ul class="flex-1 overflow-y-auto p-2">
          <li v-if="loading" class="p-4 text-center text-xs text-neutral-500">加载中…</li>
          <li v-else-if="!filteredNotes.length" class="p-4 text-center text-xs text-neutral-500">暂无笔记</li>
          <li
            v-for="note in filteredNotes"
            :key="note.id"
            class="cursor-pointer rounded-lg p-2.5 transition-colors duration-200"
            :class="
              activeNoteId === note.id
                ? 'bg-morandi-clay/15 dark:bg-morandi-clay/25'
                : 'hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg'
            "
            @click="selectNote(note.id)"
          >
            <p class="truncate text-sm font-medium">{{ note.title }}</p>
            <div class="mt-1 flex flex-wrap gap-1">
              <span
                v-for="t in (note.tags || []).slice(0, 3)"
                :key="t"
                class="rounded bg-morandi-peach/15 px-1 text-[10px] text-morandi-clay"
              >
                {{ t }}
              </span>
            </div>
            <p class="mt-1 text-[10px] text-neutral-400">{{ formatDateTime(note.updateTime) }}</p>
          </li>
        </ul>
      </aside>

      <div v-if="activeNoteId" class="flex min-w-0 flex-1 flex-col">
        <div class="flex flex-wrap items-center gap-2 border-b border-morandi-peach/10 p-3">
          <input
            v-model="editorTitle"
            type="text"
            class="input-field flex-1 border-0 bg-transparent text-lg font-bold focus:ring-0"
            placeholder="笔记标题"
          />
          <span v-if="saving" class="text-xs text-neutral-400">保存中…</span>
          <button
            type="button"
            class="cursor-pointer rounded-lg px-3 py-1.5 text-xs text-red-500 transition-colors duration-200 hover:bg-red-50"
            @click="showDeleteConfirm = true"
          >
            删除
          </button>
        </div>
        <div class="flex flex-wrap items-center gap-2 border-b border-morandi-peach/10 px-3 py-2">
          <span
            v-for="tag in editorTags"
            :key="tag"
            class="inline-flex items-center gap-1 rounded-full bg-morandi-sage/20 px-2 py-0.5 text-xs text-morandi-sage"
          >
            {{ tag }}
            <button
              type="button"
              aria-label="移除标签"
              class="cursor-pointer hover:text-red-500"
              @click="removeTag(tag)"
            >
              <i class="fa-solid fa-xmark text-[10px]" aria-hidden="true" />
            </button>
          </span>
          <input
            v-model="tagInput"
            type="text"
            class="w-24 border-0 bg-transparent text-xs outline-none"
            placeholder="添加标签"
            @keydown.enter.prevent="addTag"
          />
        </div>
        <MdEditor
          v-model="editorContent"
          class="flex-1"
          :theme="editorTheme"
          language="zh-CN"
          preview-theme="default"
          :toolbars="['bold', 'italic', 'strikeThrough', 'title', 'quote', 'unorderedList', 'orderedList', 'code', 'link', 'preview', 'fullscreen']"
          style="height: calc(100% - 7rem)"
        />
      </div>
      <EmptyState v-else class="flex-1" title="选择或创建笔记" description="在左侧列表选择笔记开始编辑" />
    </div>

    <ConfirmDialog
      :show="showDeleteConfirm"
      title="删除笔记"
      message="确定删除当前笔记？"
      danger
      @close="showDeleteConfirm = false"
      @confirm="confirmDelete"
    />
  </div>
</template>
