import { marked } from 'marked'
import DOMPurify from 'dompurify'

marked.setOptions({ breaks: true })

export function renderMarkdown(content) {
  if (!content) return ''
  const html = marked.parse(content, { async: false })
  return DOMPurify.sanitize(html)
}
