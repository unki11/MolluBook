import { api, unwrap } from './client'
import type { IdResponse, MeResponse } from '../types'

export const userApi = {
  me: () => unwrap<MeResponse>(api.get('/api/users/me')),
  updateMe: (payload: { nickname: string }) => unwrap<IdResponse>(api.patch('/api/users/me', payload)),
  updatePassword: (payload: { currentPassword?: string; newPassword: string }) =>
    unwrap<void>(api.patch('/api/users/me/password', payload)),
  withdraw: (payload?: { password?: string }) => unwrap<void>(api.delete('/api/users/me', { data: payload })),
}
