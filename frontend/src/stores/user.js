import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  function setAuth(userData, tokenValue) {
    user.value = userData
    token.value = tokenValue
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('token', tokenValue)
  }

  function clearAuth() {
    user.value = null
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('token')
  }

  async function login(credentials) {
    const res = await request.post('/auth/login', credentials)
    const data = res.data
    setAuth(data, data.token)
    return data
  }

  async function register(form) {
    const res = await request.post('/auth/register', form)
    const data = res.data
    setAuth(data, data.token)
    return data
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } finally {
      clearAuth()
    }
  }

  async function fetchProfile() {
    const res = await request.get('/user/profile')
    user.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
    return res.data
  }

  const isLoggedIn = () => !!token.value
  const isAdmin = () => user.value?.role === 'ADMIN'

  return { user, token, login, register, logout, fetchProfile, setAuth, clearAuth, isLoggedIn, isAdmin }
})
