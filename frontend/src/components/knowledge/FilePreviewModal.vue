<script setup>
import { ref, watch, shallowRef } from 'vue'
import { fileApi } from '@/api'
import { renderMarkdown } from '@/utils/markdown'
import { useToast } from '@/composables/useToast'
import BaseModal from '@/components/common/BaseModal.vue'

const props = defineProps({
  show: Boolean,
  fileId: { type: Number, default: null },
})
const emit = defineEmits(['close'])

const toast = useToast()
const loading = ref(false)
const preview = ref(null)
const mdHtml = ref('')

const officePdf = shallowRef(null)
const officeDocx = shallowRef(null)

async function loadPreview() {
  if (!props.fileId) return
  loading.value = true
  preview.value = null
  mdHtml.value = ''
  try {
    const data = await fileApi.getPreviewUrl(props.fileId)
    if (data.fileType === 'pdf' && !officePdf.value) {
      const pdfMod = await import('@vue-office/pdf')
      officePdf.value = pdfMod.default
    } else if (data.fileType === 'docx' && !officeDocx.value) {
      const docxMod = await import('@vue-office/docx')
      officeDocx.value = docxMod.default
    }
    preview.value = data
    if (data.fileType === 'md') {
      const content = data.content || '# 暂无内容'
      mdHtml.value = renderMarkdown(content)
    }
  } catch (e) {
    toast.error(e.message || '预览加载失败')
    emit('close')
  } finally {
    loading.value = false
  }
}

watch(
  () => [props.show, props.fileId],
  ([show, id]) => {
    if (show && id) loadPreview()
  },
)
</script>

<template>
  <BaseModal :show="show" :title="preview?.fileName || '文件预览'" width="max-w-4xl" @close="emit('close')">
    <div class="max-h-[70vh] overflow-auto px-6 py-4">
      <div v-if="loading" class="flex items-center justify-center py-16 text-neutral-500">
        <i class="fa-solid fa-spinner fa-spin mr-2" aria-hidden="true" />
        加载预览中…
      </div>
      <template v-else-if="preview">
        <div
          v-if="preview.fileType === 'md'"
          class="markdown-body prose-sm max-w-none dark:prose-invert"
          v-html="mdHtml"
        />
        <component
          v-else-if="preview.fileType === 'pdf' && officePdf"
          :is="officePdf"
          :src="preview.previewUrl"
          class="min-h-[400px] w-full"
        />
        <component
          v-else-if="preview.fileType === 'docx' && officeDocx"
          :is="officeDocx"
          :src="preview.previewUrl"
          class="min-h-[400px] w-full"
        />
        <div
          v-else
          class="flex flex-col items-center justify-center gap-3 py-12 text-neutral-500"
        >
          <i class="fa-solid fa-file text-4xl text-morandi-clay" aria-hidden="true" />
          <p class="text-sm">
            {{ preview.fileType === 'doc' ? 'DOC 格式请下载后查看' : '暂不支持在线预览此格式' }}
          </p>
          <a
            v-if="preview.previewUrl"
            :href="preview.previewUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="btn-primary text-xs"
          >
            打开预览链接
          </a>
        </div>
      </template>
    </div>
  </BaseModal>
</template>
