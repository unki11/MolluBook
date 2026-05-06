import { api, unwrap } from './client'
import type { AiModel, IdResponse, MeResponse, UserApiKeyListItem } from '../types'

export const userApi = {
  me: () => unwrap<MeResponse>(api.get('/api/users/me')),
  listApiKeys: () => unwrap<UserApiKeyListItem[]>(api.get('/api/users/me/api-keys')),
  createApiKey: (payload: { label: string; apiKey: string; aiModel: AiModel }) =>
    unwrap<IdResponse>(api.post('/api/users/me/api-keys', payload)),
  updateApiKey: (apiKeyId: number, payload: { label: string; apiKey?: string; aiModel: AiModel }) =>
    unwrap<IdResponse>(api.patch(`/api/users/me/api-keys/${apiKeyId}`, payload)),
  deleteApiKey: (apiKeyId: number) => unwrap<void>(api.delete(`/api/users/me/api-keys/${apiKeyId}`)),
  updateMe: (payload: { nickname: string }) => unwrap<IdResponse>(api.patch('/api/users/me', payload)),
  updatePassword: (payload: { currentPassword?: string; newPassword: string }) =>
    unwrap<void>(api.patch('/api/users/me/password', payload)),
  withdraw: (payload?: { password?: string }) => unwrap<void>(api.delete('/api/users/me', { data: payload })),
}
