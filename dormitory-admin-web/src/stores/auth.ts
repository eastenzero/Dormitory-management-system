import { defineStore } from 'pinia'
import * as authApi from '../api/auth'
import { clearToken, getToken, setToken } from '../utils/token'

 let fetchMePromise: Promise<void> | null = null

export type MeInfo = authApi.MeResponse

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() as string,
    me: null as MeInfo | null,
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    permissions: (state) => state.me?.permissions || [],
    hasPerm: (state) => {
      return (perm: string) => (state.me?.permissions || []).includes(perm)
    },
  },
  actions: {
    async login(username: string, password: string) {
      const resp = await authApi.login(username, password)
      if (resp.code !== 0) {
        throw new Error(resp.message)
      }
      this.token = resp.data.token
      setToken(this.token)
      await this.ensureMe()
    },

    async register(username: string, password: string, realName?: string) {
      const resp = await authApi.register({ username, password, realName })
      if (resp.code !== 0) {
        throw new Error(resp.message)
      }
      this.token = resp.data.token
      setToken(this.token)
      this.me = null
      await this.ensureMe()
    },

    async fetchMe() {
      const resp = await authApi.me()
      if (resp.code !== 0) {
        throw new Error(resp.message)
      }
      this.me = resp.data
    },

    async ensureMe() {
      if (this.me) return
      if (!this.token) {
        throw new Error('unauthorized')
      }

      if (!fetchMePromise) {
        fetchMePromise = this.fetchMe().finally(() => {
          fetchMePromise = null
        })
      }
      await fetchMePromise
    },

    logout() {
      this.token = ''
      this.me = null
      clearToken()
    },
  },
})
