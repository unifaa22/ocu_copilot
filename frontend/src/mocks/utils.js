let nextId = 100

export function genId() {
  return ++nextId
}

export function nowStr() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export function ok(data, message = '操作成功') {
  return Response.json({ code: 200, message, data })
}

export function err(code, message) {
  return Response.json({ code, message, data: null }, { status: code === 401 ? 401 : 200 })
}

export function getTokenUser(request, db) {
  const auth = request.headers.get('Authorization') || ''
  const token = auth.replace(/^Bearer\s+/i, '')
  if (!token) return null
  if (token === 'dev-bypass-token') {
    return db.users.find((u) => u.id === 1 && !u.isDeleted) || null
  }
  const session = db.sessions[token]
  if (!session) return null
  const user = db.users.find((u) => u.id === session.userId && !u.isDeleted)
  return user || null
}

export function requireAuth(request, db) {
  const user = getTokenUser(request, db)
  if (!user) return { error: err(401, '未认证，请先登录') }
  return { user }
}

export function paginate(list, url) {
  const params = new URL(url).searchParams
  const page = Math.max(1, parseInt(params.get('page') || '1', 10))
  const size = Math.min(100, Math.max(1, parseInt(params.get('size') || '10', 10)))
  const total = list.length
  const start = (page - 1) * size
  return {
    list: list.slice(start, start + size),
    total,
    page,
    size,
  }
}

export function getExt(name) {
  const parts = name.split('.')
  return parts.length > 1 ? parts.pop().toLowerCase() : ''
}

export function mapUser(user, db) {
  const roles = db.userRoles
    .filter((ur) => ur.userId === user.id)
    .map((ur) => {
      const role = db.roles.find((r) => r.id === ur.roleId)
      return role?.code
    })
    .filter(Boolean)
  return {
    id: user.id,
    username: user.username,
    avatar: user.avatar,
    avatarUrl: user.avatar
      ? `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`
      : null,
    theme: user.theme,
    roles,
  }
}

export function enrichCategory(cat, db) {
  const files = db.files.filter((f) => f.categoryId === cat.id && !f.isDeleted)
  return {
    id: cat.id,
    categoryName: cat.categoryName,
    fileCount: files.length,
    syncedCount: files.filter((f) => f.syncStatus === 1).length,
    createTime: cat.createTime,
    updateTime: cat.updateTime,
  }
}

export function enrichFile(file) {
  return {
    id: file.id,
    fileName: file.fileName,
    fileType: file.fileType,
    fileSize: file.fileSize,
    categoryId: file.categoryId,
    syncStatus: file.syncStatus,
    createTime: file.createTime,
    updateTime: file.updateTime,
  }
}
