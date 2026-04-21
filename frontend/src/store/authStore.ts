import { create } from 'zustand'
import { authApi } from '../api/auth'
import { tokenStorage } from '../api/client'
import { userApi } from '../api/user'
import type { AuthTokenResponse, MeResponse } from '../types'

type AuthMode = 'login' | 'signup'

type AuthState = {
  me: MeResponse | null
  accessToken: string | null
  refreshToken: string | null
  authModalOpen: boolean
  authMode: AuthMode
  isBootstrapped: boolean
  setAuthModal: (open: boolean, mode?: AuthMode) => void
  bootstrap: () => Promise<void>
  applyAuth: (tokens: AuthTokenResponse) => Promise<void>
  refreshMe: () => Promise<void>
  logout: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set) => ({
  me: null,
  accessToken: tokenStorage.getAccessToken(),
  refreshToken: tokenStorage.getRefreshToken(),
  authModalOpen: false,
  authMode: 'login',
  isBootstrapped: false,
  setAuthModal: (open, mode = 'login') => set({ authModalOpen: open, authMode: mode }),
  bootstrap: async () => {
    if (!tokenStorage.getAccessToken()) {
      set({ isBootstrapped: true })
      return
    }
    try {
      const me = await userApi.me()
      set({ me, isBootstrapped: true })
    } catch {
      tokenStorage.clear()
      set({ me: null, accessToken: null, refreshToken: null, isBootstrapped: true })
    }
  },
  applyAuth: async (tokens) => {
    authApi.applyTokens(tokens)
    const me = await userApi.me()
    set({
      me,
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      authModalOpen: false,
    })
  },
  refreshMe: async () => {
    const me = await userApi.me()
    set({ me })
  },
  logout: async () => {
    try {
      await authApi.logout()
    } finally {
      tokenStorage.clear()
      set({
        me: null,
        accessToken: null,
        refreshToken: null,
        authModalOpen: false,
      })
    }
  },
}))
