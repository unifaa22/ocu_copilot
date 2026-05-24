export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return dateStr
}

export function formatFileSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function getFileTypeIcon(fileType) {
  const map = {
    pdf: 'fa-file-pdf text-red-500',
    doc: 'fa-file-word text-blue-500',
    docx: 'fa-file-word text-blue-500',
    md: 'fa-file-lines text-morandi-clay',
  }
  return map[fileType] || 'fa-file text-neutral-400'
}

export function syncStatusLabel(status) {
  const map = {
    0: { text: '未同步', class: 'bg-neutral-100 text-neutral-500 dark:bg-neutral-800' },
    1: { text: '已索引', class: 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/20 dark:text-emerald-400' },
    2: { text: '同步失败', class: 'bg-red-50 text-red-600 dark:bg-red-950/20 dark:text-red-400' },
  }
  return map[status] || map[0]
}

export function roleLabel(roles) {
  if (!roles?.length) return '普通用户'
  if (roles.includes('TEAM_CREATOR')) return '团队创建者'
  return '普通用户'
}

export function paginate(list, page = 1, size = 10) {
  const total = list.length
  const start = (page - 1) * size
  return {
    list: list.slice(start, start + size),
    total,
    page: Number(page),
    size: Number(size),
  }
}
