import request from '../utils/request'

// 认证API
export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data),
  logout: () => request.post('/auth/logout'),
}

// 用户API
export const userAPI = {
  getProfile: () => request.get('/user/profile'),
  updateProfile: (data) => request.put('/user/profile', data),
  list: (params) => request.get('/user/list', { params }),
  updateStatus: (id, status) => request.put(`/user/${id}/status`, null, { params: { status } }),
}

// 图书API
export const bookAPI = {
  list: (params) => request.get('/product/books', { params }),
  getById: (id) => request.get(`/product/books/${id}`),
  getHot: (limit = 8) => request.get('/product/books/hot', { params: { limit } }),
  getStatistics: () => request.get('/product/books/statistics'),
  add: (data) => request.post('/product/books', data),
  update: (id, data) => request.put(`/product/books/${id}`, data),
  delete: (id) => request.delete(`/product/books/${id}`),
}

// 分类API
export const categoryAPI = {
  list: () => request.get('/product/categories'),
  add: (data) => request.post('/product/categories', data),
  update: (id, data) => request.put(`/product/categories/${id}`, data),
  delete: (id) => request.delete(`/product/categories/${id}`),
}

// 订单API
export const orderAPI = {
  create: (data) => request.post('/order', data),
  getById: (id) => request.get(`/order/${id}`),
  listMy: (params) => request.get('/order/my', { params }),
  listAll: (params) => request.get('/order/all', { params }),
  getStatistics: () => request.get('/order/statistics'),
  pay: (id) => request.put(`/order/${id}/pay`),
  cancel: (id) => request.put(`/order/${id}/cancel`),
  updateStatus: (id, status) => request.put(`/order/${id}/status`, null, { params: { status } }),
}
