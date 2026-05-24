import { http } from 'msw'
import { getDb, createToken } from './store'
import {
  ok,
  err,
  requireAuth,
  paginate,
  getTokenUser,
  mapUser,
  enrichCategory,
  enrichFile,
  genId,
  nowStr,
  getExt,
} from './utils'

function buildConversationSummaries(history, userId, typeFilter) {
  const map = new Map()
  history
    .filter((h) => h.userId === userId && !h.isDeleted)
    .filter((h) => !typeFilter || (typeFilter === 'personal' ? !h.teamId : h.teamId))
    .forEach((h) => {
      const existing = map.get(h.conversationId)
      if (!existing || h.createTime > existing.lastTime) {
        map.set(h.conversationId, {
          conversationId: h.conversationId,
          type: h.teamId ? 'team' : 'personal',
          teamId: h.teamId || null,
          teamName: h.teamName || null,
          lastQuestion: h.question,
          lastAnswer: h.answer,
          messageCount: 0,
          categoryNames: h.categoryNames || [],
          lastTime: h.createTime,
        })
      }
    })

  history
    .filter((h) => h.userId === userId && !h.isDeleted)
    .forEach((h) => {
      const item = map.get(h.conversationId)
      if (item) item.messageCount++
    })

  return [...map.values()].sort((a, b) => (a.lastTime < b.lastTime ? 1 : -1))
}

export const handlers = [
  // User
  http.get('/api/user/profile', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    return ok(mapUser(auth.user, db))
  }),

  http.put('/api/user/password', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    if (auth.user.password !== body.oldPassword) return err(400, '旧密码错误')
    if (body.newPassword !== body.confirmPassword) return err(400, '两次新密码不一致')
    auth.user.password = body.newPassword
    auth.user.updateTime = nowStr()
    return ok(null, '密码修改成功')
  }),

  http.put('/api/user/theme', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    auth.user.theme = body.theme
    auth.user.updateTime = nowStr()
    return ok(null)
  }),

  http.post('/api/user/avatar', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const form = await request.formData()
    const file = form.get('file')
    if (!file) return err(400, '请选择图片')
    auth.user.avatar = `avatars/${auth.user.id}/avatar.jpg`
    auth.user.updateTime = nowStr()
    return ok({
      avatar: auth.user.avatar,
      avatarUrl: `https://api.dicebear.com/7.x/avataaars/svg?seed=${auth.user.username}`,
    })
  }),

  // Categories
  http.get('/api/categories', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const list = db.categories
      .filter((c) => c.userId === auth.user.id && !c.isDeleted)
      .map((c) => enrichCategory(c, db))
    return ok(list)
  }),

  http.post('/api/categories', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    const name = body.categoryName?.trim()
    if (!name) return err(400, '分类名称不能为空')
    if (db.categories.some((c) => c.userId === auth.user.id && c.categoryName === name && !c.isDeleted))
      return err(409, '分类名已存在')
    const t = nowStr()
    const cat = {
      id: genId(),
      categoryName: name,
      userId: auth.user.id,
      difyDatasetId: null,
      isDeleted: false,
      createTime: t,
      updateTime: t,
    }
    db.categories.push(cat)
    return ok(enrichCategory(cat, db))
  }),

  http.put('/api/categories/:id', async ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const cat = db.categories.find((c) => c.id === Number(params.id) && !c.isDeleted)
    if (!cat || cat.userId !== auth.user.id) return err(404, '分类不存在')
    const body = await request.json()
    const name = body.categoryName?.trim()
    if (!name) return err(400, '分类名称不能为空')
    if (db.categories.some((c) => c.userId === auth.user.id && c.categoryName === name && c.id !== cat.id && !c.isDeleted))
      return err(409, '分类名已存在')
    cat.categoryName = name
    cat.updateTime = nowStr()
    return ok(enrichCategory(cat, db))
  }),

  http.delete('/api/categories/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const cat = db.categories.find((c) => c.id === Number(params.id) && !c.isDeleted)
    if (!cat || cat.userId !== auth.user.id) return err(404, '分类不存在')
    cat.isDeleted = true
    cat.categoryName = `${cat.categoryName}__del_${cat.id}`
    db.files.filter((f) => f.categoryId === cat.id).forEach((f) => { f.isDeleted = true })
    return ok(null)
  }),

  http.post('/api/categories/:id/sync', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const catId = Number(params.id)
    const cat = db.categories.find((c) => c.id === catId && !c.isDeleted)
    if (!cat || cat.userId !== auth.user.id) return err(404, '分类不存在')
    if (!cat.difyDatasetId) cat.difyDatasetId = `ds-${catId}`
    const pending = db.files.filter((f) => f.categoryId === catId && !f.isDeleted && f.syncStatus !== 1)
    const failedFiles = []
    pending.forEach((f) => {
      if (f.fileName.includes('损坏')) {
        f.syncStatus = 2
        failedFiles.push({ fileId: f.id, fileName: f.fileName, syncStatus: 2, errorMessage: 'Dify 解析失败' })
      } else {
        f.syncStatus = 1
        f.difyDocumentId = `doc-${f.id}`
      }
      f.updateTime = nowStr()
    })
    return ok({
      categoryId: catId,
      total: pending.length,
      successCount: pending.length - failedFiles.length,
      failCount: failedFiles.length,
      failedFiles,
    })
  }),

  // Files
  http.get('/api/categories/:categoryId/files', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const catId = Number(params.categoryId)
    const cat = db.categories.find((c) => c.id === catId && !c.isDeleted)
    if (!cat || cat.userId !== auth.user.id) return err(404, '分类不存在')
  const url = new URL(request.url)
    const syncStatus = url.searchParams.get('syncStatus')
    let list = db.files.filter((f) => f.categoryId === catId && !f.isDeleted).map(enrichFile)
    if (syncStatus !== null && syncStatus !== '') list = list.filter((f) => f.syncStatus === Number(syncStatus))
    return ok(paginate(list, request.url))
  }),

  http.get('/api/files', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const url = new URL(request.url)
    const syncStatus = url.searchParams.get('syncStatus')
    let list = db.files.filter((f) => f.userId === auth.user.id && !f.isDeleted).map(enrichFile)
    if (syncStatus !== null && syncStatus !== '') list = list.filter((f) => f.syncStatus === Number(syncStatus))
    return ok(paginate(list, request.url))
  }),

  http.post('/api/categories/:categoryId/files', async ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const catId = Number(params.categoryId)
    const cat = db.categories.find((c) => c.id === catId && !c.isDeleted)
    if (!cat || cat.userId !== auth.user.id) return err(404, '分类不存在')
    const form = await request.formData()
    const file = form.get('file')
    if (!file) return err(400, '文件不能为空')
    const ext = getExt(file.name)
    if (!['md', 'pdf', 'doc', 'docx'].includes(ext)) return err(400, '文件类型不支持')
    if (file.size > 15 * 1024 * 1024) return err(400, '文件超过大小上限')
    const t = nowStr()
    const newFile = {
      id: genId(),
      fileName: file.name,
      fileType: ext,
      fileSize: file.size,
      filePath: `files/${auth.user.id}/${catId}/${file.name}`,
      categoryId: catId,
      userId: auth.user.id,
      syncStatus: 0,
      difyDocumentId: null,
      isDeleted: false,
      createTime: t,
      updateTime: t,
    }
    db.files.push(newFile)
    return ok(enrichFile(newFile))
  }),

  http.put('/api/files/:id', async ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const file = db.files.find((f) => f.id === Number(params.id) && !f.isDeleted)
    if (!file || file.userId !== auth.user.id) return err(404, '文件不存在')
    const body = await request.json()
    file.fileName = body.fileName
    file.updateTime = nowStr()
    return ok(enrichFile(file))
  }),

  http.delete('/api/files/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const file = db.files.find((f) => f.id === Number(params.id) && !f.isDeleted)
    if (!file || file.userId !== auth.user.id) return err(404, '文件不存在')
    file.isDeleted = true
    return ok(null)
  }),

  http.get('/api/files/:fileId/preview-url', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const file = db.files.find((f) => f.id === Number(params.fileId) && !f.isDeleted)
    if (!file) return err(404, '文件不存在')
    if (file.userId !== auth.user.id) {
      const canAccess = db.teamMembers.some((m) => {
        if (m.userId !== auth.user.id || m.status !== 1 || m.isDeleted) return false
        const team = db.teams.find((t) => t.id === m.teamId && !t.isDeleted)
        return team?.isShare === 1 && team.creatorId === file.userId
      })
      if (!canAccess) return err(403, '无权限访问')
    }
    return ok({
      previewUrl: `https://example.com/preview/${file.id}`,
      expireSeconds: 3600,
      fileType: file.fileType,
      fileName: file.fileName,
      content: file.fileType === 'md' ? '# 预览内容\n\n这是 Mock 预览的 Markdown 文件内容。' : null,
    })
  }),

  // Notes
  http.get('/api/notes', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const url = new URL(request.url)
    const keyword = url.searchParams.get('keyword')?.toLowerCase()
    const tag = url.searchParams.get('tag')?.toLowerCase()
    let list = db.notes
      .filter((n) => n.userId === auth.user.id && !n.isDeleted)
      .map((n) => ({
        id: n.id,
        title: n.title,
        tags: n.tags,
        createTime: n.createTime,
        updateTime: n.updateTime,
      }))
    if (keyword) list = list.filter((n) => n.title.toLowerCase().includes(keyword))
    if (tag) list = list.filter((n) => n.tags.some((t) => t.toLowerCase().includes(tag)))
    return ok(paginate(list, request.url))
  }),

  http.get('/api/notes/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const note = db.notes.find((n) => n.id === Number(params.id) && !n.isDeleted)
    if (!note || note.userId !== auth.user.id) return err(404, '笔记不存在')
    return ok(note)
  }),

  http.post('/api/notes', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    const t = nowStr()
    const note = {
      id: genId(),
      title: body.title || '未命名笔记',
      content: body.content || '',
      tags: body.tags || [],
      userId: auth.user.id,
      isDeleted: false,
      createTime: t,
      updateTime: t,
    }
    db.notes.push(note)
    return ok(note)
  }),

  http.put('/api/notes/:id', async ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const note = db.notes.find((n) => n.id === Number(params.id) && !n.isDeleted)
    if (!note || note.userId !== auth.user.id) return err(404, '笔记不存在')
    const body = await request.json()
    Object.assign(note, {
      title: body.title ?? note.title,
      content: body.content ?? note.content,
      tags: body.tags ?? note.tags,
      updateTime: nowStr(),
    })
    return ok(note)
  }),

  http.delete('/api/notes/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const note = db.notes.find((n) => n.id === Number(params.id) && !n.isDeleted)
    if (!note || note.userId !== auth.user.id) return err(404, '笔记不存在')
    note.isDeleted = true
    return ok(null)
  }),

  // Chat
  http.post('/api/chat/personal', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    if (!body.question?.trim()) return err(400, '问题不能为空')
    if (!body.categoryIds?.length) return err(400, '请至少选择一个知识库分类')
    const cats = db.categories.filter(
      (c) => body.categoryIds.includes(c.id) && c.userId === auth.user.id && !c.isDeleted,
    )
    const conversationId = body.conversationId || `conv-${Date.now()}`
    const t = nowStr()
    const answer = `根据您选择的 **${cats.map((c) => c.categoryName).join('、')}** 知识库，关于「${body.question}」的回答如下：\n\n这是 Mock AI 基于 Dify 向量检索生成的智能答复。`
    const record = {
      id: db.nextHistoryId++,
      userId: auth.user.id,
      conversationId,
      question: body.question,
      answer,
      categoryIds: cats.map((c) => c.id),
      categoryNames: cats.map((c) => c.categoryName),
      teamId: null,
      teamName: null,
      isDeleted: false,
      createTime: t,
    }
    db.chatHistory.push(record)
    return ok({
      conversationId,
      answer,
      historyId: record.id,
      categoryIds: record.categoryIds,
      categoryNames: record.categoryNames,
    })
  }),

  http.post('/api/chat/team', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    if (!body.question?.trim()) return err(400, '问题不能为空')
    const team = db.teams.find((t) => t.id === body.teamId && !t.isDeleted)
    if (!team) return err(404, '团队不存在')
    const member = db.teamMembers.find(
      (m) => m.teamId === team.id && m.userId === auth.user.id && m.status === 1 && !m.isDeleted,
    )
    if (!member) return err(403, '非团队成员')
    if (team.isShare !== 1) return err(403, '团队未开启共享')
    const cats = db.categories.filter((c) => c.userId === team.creatorId && !c.isDeleted)
    const syncedCats = cats.filter((c) =>
      db.files.some((f) => f.categoryId === c.id && f.syncStatus === 1 && !f.isDeleted),
    )
    const conversationId = body.conversationId || `conv-team-${Date.now()}`
    const t = nowStr()
    const creator = db.users.find((u) => u.id === team.creatorId)
    const answer = `基于团队「${team.teamName}」创建者 ${creator?.username} 的全部共享知识库，关于「${body.question}」的回答如下：\n\n这是 Mock 团队 AI 答复。`
    const record = {
      id: db.nextHistoryId++,
      userId: auth.user.id,
      conversationId,
      question: body.question,
      answer,
      categoryIds: syncedCats.map((c) => c.id),
      categoryNames: syncedCats.map((c) => c.categoryName),
      teamId: team.id,
      teamName: team.teamName,
      isDeleted: false,
      createTime: t,
    }
    db.chatHistory.push(record)
    return ok({
      conversationId,
      answer,
      historyId: record.id,
      teamId: team.id,
      teamName: team.teamName,
      categoryNames: record.categoryNames,
    })
  }),

  http.get('/api/chat/conversations', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const url = new URL(request.url)
    const type = url.searchParams.get('type')
    const summaries = buildConversationSummaries(db.chatHistory, auth.user.id, type)
    return ok(paginate(summaries, request.url))
  }),

  http.get('/api/chat/conversations/:conversationId', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const messages = db.chatHistory
      .filter(
        (h) =>
          h.conversationId === params.conversationId && h.userId === auth.user.id && !h.isDeleted,
      )
      .map((h) => ({
        id: h.id,
        question: h.question,
        answer: h.answer,
        categoryIds: h.categoryIds,
        categoryNames: h.categoryNames,
        createTime: h.createTime,
      }))
    if (!messages.length) return err(404, '会话不存在')
    const first = db.chatHistory.find((h) => h.conversationId === params.conversationId)
    return ok({
      conversationId: params.conversationId,
      type: first.teamId ? 'team' : 'personal',
      teamId: first.teamId || null,
      messages,
    })
  }),

  http.delete('/api/chat/history/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const record = db.chatHistory.find((h) => h.id === Number(params.id) && h.userId === auth.user.id)
    if (!record) return err(404, '记录不存在')
    record.isDeleted = true
    return ok(null)
  }),

  http.delete('/api/chat/conversations/:conversationId', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    db.chatHistory
      .filter((h) => h.conversationId === params.conversationId && h.userId === auth.user.id)
      .forEach((h) => { h.isDeleted = true })
    return ok(null)
  }),

  http.delete('/api/chat/history', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    db.chatHistory.filter((h) => h.userId === auth.user.id).forEach((h) => { h.isDeleted = true })
    return ok(null)
  }),

  // Teams
  http.post('/api/teams', async ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const body = await request.json()
    const name = body.teamName?.trim()
    if (!name) return err(400, '团队名称不能为空')
    const t = nowStr()
    const team = {
      id: genId(),
      teamName: name,
      creatorId: auth.user.id,
      isShare: 0,
      isDeleted: false,
      createTime: t,
      updateTime: t,
    }
    db.teams.push(team)
    db.teamMembers.push({
      id: genId(),
      teamId: team.id,
      userId: auth.user.id,
      memberRole: 1,
      status: 1,
      isDeleted: false,
      joinTime: t,
    })
    if (!db.userRoles.some((ur) => ur.userId === auth.user.id && ur.roleId === 2)) {
      db.userRoles.push({ id: genId(), userId: auth.user.id, roleId: 2 })
    }
    return ok({
      id: team.id,
      teamName: team.teamName,
      creatorId: team.creatorId,
      creatorName: auth.user.username,
      isShare: team.isShare,
      createTime: team.createTime,
    })
  }),

  http.get('/api/teams/managed', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const list = db.teams
      .filter((t) => t.creatorId === auth.user.id && !t.isDeleted)
      .map((t) => ({
        id: t.id,
        teamName: t.teamName,
        creatorId: t.creatorId,
        creatorName: auth.user.username,
        isShare: t.isShare,
        createTime: t.createTime,
      }))
    return ok(paginate(list, request.url))
  }),

  http.get('/api/teams/joined', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const memberTeamIds = db.teamMembers
      .filter((m) => m.userId === auth.user.id && m.status === 1 && !m.isDeleted)
      .map((m) => m.teamId)
    const list = db.teams
      .filter((t) => memberTeamIds.includes(t.id) && !t.isDeleted)
      .map((t) => {
        const creator = db.users.find((u) => u.id === t.creatorId)
        return {
          id: t.id,
          teamName: t.teamName,
          creatorId: t.creatorId,
          creatorName: creator?.username,
          isShare: t.isShare,
          isCreator: t.creatorId === auth.user.id,
          joinTime: db.teamMembers.find((m) => m.teamId === t.id && m.userId === auth.user.id)?.joinTime,
        }
      })
    return ok(paginate(list, request.url))
  }),

  http.get('/api/teams/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team) return err(404, '团队不存在')
    const member = db.teamMembers.find(
      (m) => m.teamId === team.id && m.userId === auth.user.id && !m.isDeleted,
    )
    if (!member || (member.status !== 1 && team.creatorId !== auth.user.id))
      return err(403, '无权限')
    const creator = db.users.find((u) => u.id === team.creatorId)
    const memberCount = db.teamMembers.filter((m) => m.teamId === team.id && m.status === 1 && !m.isDeleted).length
    return ok({
      id: team.id,
      teamName: team.teamName,
      creatorId: team.creatorId,
      creatorName: creator?.username,
      isShare: team.isShare,
      myMemberRole: member.memberRole,
      myStatus: member.status,
      memberCount,
      createTime: team.createTime,
    })
  }),

  http.delete('/api/teams/:id', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team) return err(404, '团队不存在')
    if (team.creatorId !== auth.user.id) return err(403, '仅创建者可解散团队')
    team.isDeleted = true
    db.teamMembers.filter((m) => m.teamId === team.id).forEach((m) => { m.isDeleted = true })
    return ok(null)
  }),

  http.put('/api/teams/:id/share', async ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team) return err(404, '团队不存在')
    if (team.creatorId !== auth.user.id) return err(403, '仅创建者可操作')
    const body = await request.json()
    team.isShare = body.isShare ? 1 : 0
    team.updateTime = nowStr()
    return ok(null)
  }),

  http.get('/api/teams/:id/members', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team) return err(404, '团队不存在')
    const list = db.teamMembers
      .filter((m) => m.teamId === team.id && m.status === 1 && !m.isDeleted)
      .map((m) => {
        const u = db.users.find((user) => user.id === m.userId)
        return {
          id: m.id,
          userId: m.userId,
          username: u?.username,
          memberRole: m.memberRole,
          status: m.status,
          joinTime: m.joinTime,
        }
      })
    return ok(paginate(list, request.url))
  }),

  http.post('/api/teams/:id/invite', async ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team || team.creatorId !== auth.user.id) return err(403, '无权限')
    const body = await request.json()
    const target = db.users.find((u) => u.username === body.username && !u.isDeleted)
    if (!target) return err(404, '用户不存在')
    if (target.id === auth.user.id) return err(400, '不能邀请自己')
    const existing = db.teamMembers.find((m) => m.teamId === team.id && m.userId === target.id && !m.isDeleted)
    if (existing?.status === 1 || existing?.status === 0) return err(409, '用户已在团队中或待接受')
    if (existing?.status === 2 || existing?.isDeleted) {
      existing.status = 0
      existing.isDeleted = false
      return ok(null, '邀请已发送')
    }
    db.teamMembers.push({
      id: genId(),
      teamId: team.id,
      userId: target.id,
      memberRole: 0,
      status: 0,
      isDeleted: false,
      joinTime: nowStr(),
    })
    return ok(null, '邀请已发送')
  }),

  http.get('/api/teams/invitations/pending', ({ request }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const list = db.teamMembers
      .filter((m) => m.userId === auth.user.id && m.status === 0 && !m.isDeleted)
      .map((m) => {
        const team = db.teams.find((t) => t.id === m.teamId && !t.isDeleted)
        const creator = db.users.find((u) => u.id === team?.creatorId)
        return {
          teamId: m.teamId,
          teamName: team?.teamName,
          creatorName: creator?.username,
          inviteTime: m.joinTime,
        }
      })
    return ok(paginate(list, request.url))
  }),

  http.post('/api/teams/:id/invite/accept', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const member = db.teamMembers.find(
      (m) => m.teamId === Number(params.id) && m.userId === auth.user.id && !m.isDeleted,
    )
    if (!member || member.status !== 0) return err(404, '邀请不存在')
    member.status = 1
    return ok(null)
  }),

  http.post('/api/teams/:id/invite/reject', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const member = db.teamMembers.find(
      (m) => m.teamId === Number(params.id) && m.userId === auth.user.id && !m.isDeleted,
    )
    if (!member || member.status !== 0) return err(404, '邀请不存在')
    member.status = 2
    return ok(null)
  }),

  http.delete('/api/teams/:id/members/:userId', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team || team.creatorId !== auth.user.id) return err(403, '无权限')
    const member = db.teamMembers.find(
      (m) => m.teamId === team.id && m.userId === Number(params.userId) && !m.isDeleted,
    )
    if (!member) return err(404, '成员不存在')
    if (member.memberRole === 1) return err(400, '不能移除创建者')
    member.isDeleted = true
    return ok(null)
  }),

  http.post('/api/teams/:id/leave', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.id) && !t.isDeleted)
    if (!team) return err(404, '团队不存在')
    const member = db.teamMembers.find(
      (m) => m.teamId === team.id && m.userId === auth.user.id && !m.isDeleted,
    )
    if (!member) return err(404, '不在团队中')
    if (member.memberRole === 1) return err(400, '创建者不能退出，请解散团队')
    member.isDeleted = true
    return ok(null)
  }),

  http.get('/api/teams/:teamId/categories', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.teamId) && !t.isDeleted)
    if (!team || team.isShare !== 1) return err(403, '共享未开启')
    const member = db.teamMembers.find(
      (m) => m.teamId === team.id && m.userId === auth.user.id && m.status === 1 && !m.isDeleted,
    )
    if (!member) return err(403, '非团队成员')
    const list = db.categories
      .filter((c) => c.userId === team.creatorId && !c.isDeleted)
      .map((c) => enrichCategory(c, db))
    return ok(list)
  }),

  http.get('/api/teams/:teamId/categories/:categoryId/files', ({ request, params }) => {
    const db = getDb()
    const auth = requireAuth(request, db)
    if (auth.error) return auth.error
    const team = db.teams.find((t) => t.id === Number(params.teamId) && !t.isDeleted)
    if (!team || team.isShare !== 1) return err(403, '共享未开启')
    const member = db.teamMembers.find(
      (m) => m.teamId === team.id && m.userId === auth.user.id && m.status === 1 && !m.isDeleted,
    )
    if (!member) return err(403, '非团队成员')
    const catId = Number(params.categoryId)
    const cat = db.categories.find((c) => c.id === catId && c.userId === team.creatorId && !c.isDeleted)
    if (!cat) return err(404, '分类不存在')
    const list = db.files.filter((f) => f.categoryId === catId && !f.isDeleted).map(enrichFile)
    return ok(paginate(list, request.url))
  }),
]
