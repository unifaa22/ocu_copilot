/**
 * 登录/注册成功后解析跳转目标，避免回到 /login、/register 或根路径 /
 */
export function resolveAuthRedirect(redirect) {
  const raw = Array.isArray(redirect) ? redirect[0] : redirect
  if (typeof raw !== 'string' || !raw.trim()) {
    return { name: 'home' }
  }

  let path = raw.trim()
  try {
    if (path.startsWith('http://') || path.startsWith('https://')) {
      path = new URL(path).pathname
    }
  } catch {
    return { name: 'home' }
  }

  const pathname = path.split('?')[0] || '/'
  if (pathname === '/login' || pathname === '/register' || pathname === '/') {
    return { name: 'home' }
  }

  return path
}
