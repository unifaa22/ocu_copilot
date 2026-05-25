/** 清理历史 MSW Service Worker，避免升级后仍拦截 /api 请求 */
export async function unregisterLegacyMockWorker() {
  if (!('serviceWorker' in navigator)) return false

  const registrations = await navigator.serviceWorker.getRegistrations()
  const mockRegs = registrations.filter((reg) => {
    const url = reg.active?.scriptURL || reg.waiting?.scriptURL || reg.installing?.scriptURL || ''
    return url.includes('mockServiceWorker')
  })

  if (mockRegs.length === 0) return false

  await Promise.all(mockRegs.map((reg) => reg.unregister()))

  if (navigator.serviceWorker.controller?.scriptURL?.includes('mockServiceWorker')) {
    window.location.reload()
    return true
  }

  return false
}
