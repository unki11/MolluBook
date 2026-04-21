import { api, tokenStorage, unwrap } from './client'
import type { AuthTokenResponse, IdResponse } from '../types'

export const authApi = {
  signUp: (payload: { email: string; password: string; nickname: string }) =>
    unwrap<IdResponse>(api.post('/api/auth/signup', payload)),
  login: (payload: { email: string; password: string }) =>
    unwrap<AuthTokenResponse>(api.post('/api/auth/login', payload)),
  logout: () => unwrap<void>(api.post('/api/auth/logout')),
  getGoogleCallbackPath: () => unwrap<string>(api.get('/api/auth/oauth2/google')),
  applyTokens: (tokens: AuthTokenResponse) => {
    tokenStorage.setTokens(tokens)
    return tokens
  },
}
