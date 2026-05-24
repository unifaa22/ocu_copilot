import { genId, nowStr } from './utils'

function createSeedDb() {
  const t = nowStr()
  const db = {
    users: [
      {
        id: 1,
        username: 'xz',
        password: '123456',
        avatar: null,
        theme: 'system',
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
      {
        id: 2,
        username: 'lisi',
        password: '123456',
        avatar: null,
        theme: 'light',
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
      {
        id: 3,
        username: 'wangwu',
        password: '123456',
        avatar: null,
        theme: 'system',
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
    ],
    roles: [
      { id: 1, code: 'USER', name: '普通用户' },
      { id: 2, code: 'TEAM_CREATOR', name: '团队创建者' },
    ],
    userRoles: [
      { id: 1, userId: 1, roleId: 1 },
      { id: 2, userId: 1, roleId: 2 },
      { id: 3, userId: 2, roleId: 1 },
      { id: 4, userId: 3, roleId: 1 },
    ],
    sessions: {},
    categories: [
      {
        id: 1,
        categoryName: 'Java学习',
        userId: 1,
        difyDatasetId: 'ds-1',
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
      {
        id: 2,
        categoryName: '算法笔记',
        userId: 1,
        difyDatasetId: null,
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
    ],
    files: [
      {
        id: 10,
        fileName: 'SpringBoot入门.pdf',
        fileType: 'pdf',
        fileSize: 1024000,
        filePath: 'files/1/1/mock.pdf',
        categoryId: 1,
        userId: 1,
        syncStatus: 1,
        difyDocumentId: 'doc-10',
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
      {
        id: 11,
        fileName: 'JVM调优.md',
        fileType: 'md',
        fileSize: 8192,
        filePath: 'files/1/1/mock.md',
        categoryId: 1,
        userId: 1,
        syncStatus: 0,
        difyDocumentId: null,
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
    ],
    notes: [
      {
        id: 1,
        title: '计算机网络核心协议总结',
        content: '# 计算机网络核心协议\n\n## TCP\n面向连接、可靠传输。',
        tags: ['复习整理', '网络'],
        userId: 1,
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
      {
        id: 2,
        title: 'SpringBoot 学习笔记',
        content: '# SpringBoot\n\n自动配置原理...',
        tags: ['Java', '期末'],
        userId: 1,
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
    ],
    teams: [
      {
        id: 1,
        teamName: '期末复习小组',
        creatorId: 1,
        isShare: 1,
        isDeleted: false,
        createTime: t,
        updateTime: t,
      },
    ],
    teamMembers: [
      {
        id: 1,
        teamId: 1,
        userId: 1,
        memberRole: 1,
        status: 1,
        isDeleted: false,
        joinTime: t,
      },
      {
        id: 2,
        teamId: 1,
        userId: 2,
        memberRole: 0,
        status: 1,
        isDeleted: false,
        joinTime: t,
      },
    ],
    chatHistory: [],
    nextHistoryId: 1,
  }

  genId()
  genId()
  genId()
  return db
}

let db = createSeedDb()

export function getDb() {
  return db
}

export function resetDb() {
  db = createSeedDb()
}

export function createToken(userId) {
  const token = `mock-token-${userId}-${Date.now()}`
  db.sessions[token] = { userId, createdAt: Date.now() }
  return token
}
