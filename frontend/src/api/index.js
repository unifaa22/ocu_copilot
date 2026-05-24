import request from './request'

export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
}

export const userApi = {
  getProfile: () => request.get('/user/profile'),
  updatePassword: (data) => request.put('/user/password', data),
  updateTheme: (theme) => request.put('/user/theme', { theme }),
  uploadAvatar: (file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post('/user/avatar', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

export const categoryApi = {
  list: () => request.get('/categories'),
  create: (categoryName) => request.post('/categories', { categoryName }),
  update: (id, categoryName) => request.put(`/categories/${id}`, { categoryName }),
  remove: (id) => request.delete(`/categories/${id}`),
  sync: (id) => request.post(`/categories/${id}/sync`),
}

export const fileApi = {
  listByCategory: (categoryId, params) => request.get(`/categories/${categoryId}/files`, { params }),
  listAll: (params) => request.get('/files', { params }),
  upload: (categoryId, file) => {
    const form = new FormData()
    form.append('file', file)
    return request.post(`/categories/${categoryId}/files`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  rename: (id, fileName) => request.put(`/files/${id}`, { fileName }),
  remove: (id) => request.delete(`/files/${id}`),
  getPreviewUrl: (id) => request.get(`/files/${id}/preview-url`),
}

export const noteApi = {
  list: (params) => request.get('/notes', { params }),
  get: (id) => request.get(`/notes/${id}`),
  create: (data) => request.post('/notes', data),
  update: (id, data) => request.put(`/notes/${id}`, data),
  remove: (id) => request.delete(`/notes/${id}`),
}

export const chatApi = {
  personal: (data) => request.post('/chat/personal', data),
  team: (data) => request.post('/chat/team', data),
  conversations: (params) => request.get('/chat/conversations', { params }),
  conversationDetail: (conversationId) => request.get(`/chat/conversations/${conversationId}`),
  deleteHistory: (id) => request.delete(`/chat/history/${id}`),
  deleteConversation: (conversationId) => request.delete(`/chat/conversations/${conversationId}`),
  clearHistory: () => request.delete('/chat/history'),
}

export const teamApi = {
  create: (teamName) => request.post('/teams', { teamName }),
  managed: (params) => request.get('/teams/managed', { params }),
  joined: (params) => request.get('/teams/joined', { params }),
  detail: (id) => request.get(`/teams/${id}`),
  dissolve: (id) => request.delete(`/teams/${id}`),
  toggleShare: (id, isShare) => request.put(`/teams/${id}/share`, { isShare }),
  members: (id, params) => request.get(`/teams/${id}/members`, { params }),
  invite: (id, username) => request.post(`/teams/${id}/invite`, { username }),
  pendingInvitations: (params) => request.get('/teams/invitations/pending', { params }),
  acceptInvite: (id) => request.post(`/teams/${id}/invite/accept`),
  rejectInvite: (id) => request.post(`/teams/${id}/invite/reject`),
  removeMember: (id, userId) => request.delete(`/teams/${id}/members/${userId}`),
  leave: (id) => request.post(`/teams/${id}/leave`),
  sharedCategories: (teamId) => request.get(`/teams/${teamId}/categories`),
  sharedFiles: (teamId, categoryId, params) =>
    request.get(`/teams/${teamId}/categories/${categoryId}/files`, { params }),
}
