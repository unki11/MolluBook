import axios from 'axios'
import type { ApiResponse, AuthTokenResponse } from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const storageKeys = {
  accessToken: 'mollubook.accessToken',
  refreshToken: 'mollubook.refreshToken',
}

export const tokenStorage = {
  getAccessToken: () => localStorage.getItem(storageKeys.accessToken),
  getRefreshToken: () => localStorage.getItem(storageKeys.refreshToken),
  setTokens: (tokens: Pick<AuthTokenResponse, 'accessToken' | 'refreshToken'>) => {
    localStorage.setItem(storageKeys.accessToken, tokens.accessToken)
    localStorage.setItem(storageKeys.refreshToken, tokens.refreshToken)
  },
  clear: () => {
    localStorage.removeItem(storageKeys.accessToken)
    localStorage.removeItem(storageKeys.refreshToken)
  },
}

export const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
})

export function extractApiErrorMessage(error: unknown) {
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    const apiMessage = error.response?.data?.error?.message
    if (apiMessage) {
      return apiMessage
    }
  }

  if (error instanceof Error && error.message) {
    return error.message
  }

  return '요청 처리에 실패했습니다.'
}

api.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise: Promise<string | null> | null = null

async function refreshAccessToken() {
  const refreshToken = tokenStorage.getRefreshToken()
  if (!refreshToken) return null
  const response = await axios.post<ApiResponse<AuthTokenResponse>>(
    `${API_BASE_URL}/api/auth/refresh`,
    { refreshToken },
    { withCredentials: true },
  )
  const tokens = response.data.data
  tokenStorage.setTokens(tokens)
  return tokens.accessToken
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status !== 401 || originalRequest?._retry) {
      return Promise.reject(error)
    }
    originalRequest._retry = true
    refreshPromise ??= refreshAccessToken().finally(() => {
      refreshPromise = null
    })
    const nextToken = await refreshPromise
    if (!nextToken) {
      tokenStorage.clear()
      return Promise.reject(error)
    }
    originalRequest.headers.Authorization = `Bearer ${nextToken}`
    return api(originalRequest)
  },
)

export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>) {
  const response = await promise
  if (!response.data.success) {
    throw new Error(response.data.error?.message ?? '요청 처리에 실패했습니다.')
  }
  return response.data.data
}
