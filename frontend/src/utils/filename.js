const ILLEGAL_CHARS = /[\\/:*?"<>|\x00-\x1f]/
const MAX_LENGTH = 255

export function validateFileName(name) {
  const trimmed = name.trim()
  if (!trimmed) return '文件名不能为空'
  if (trimmed.length > MAX_LENGTH) return `文件名不能超过 ${MAX_LENGTH} 个字符`
  if (ILLEGAL_CHARS.test(trimmed)) return '文件名不能包含 \\ / : * ? " < > | 等非法字符'
  return null
}
