import { api, unwrap } from './client'
import type {
  IdResponse,
  PromptDetailResponse,
  PromptListItem,
  PromptUpsertRequest,
  VersionedIdResponse,
  WorldDetailResponse,
  WorldListItem,
} from '../types'

export const worldApi = {
  list: () => unwrap<WorldListItem[]>(api.get('/api/worlds')),
  detailBySlug: (slug: string) => unwrap<WorldDetailResponse>(api.get(`/api/worlds/${slug}`)),
  create: (payload: { name: string; slug: string; description?: string; thumbnailUrl?: string }) =>
    unwrap<IdResponse>(api.post('/api/admin/worlds', payload)),
  update: (worldId: number, payload: { name: string; description?: string; thumbnailUrl?: string }) =>
    unwrap<IdResponse>(api.patch(`/api/admin/worlds/${worldId}`, payload)),
  remove: (worldId: number) => unwrap<void>(api.delete(`/api/admin/worlds/${worldId}`)),
  listPrompts: (worldId: number) =>
    unwrap<PromptListItem[]>(api.get(`/api/worlds/${worldId}/prompts`)),
  getPrompt: (worldId: number, promptId: number) =>
    unwrap<PromptDetailResponse>(api.get(`/api/worlds/${worldId}/prompts/${promptId}`)),
  createPrompt: (worldId: number, payload: PromptUpsertRequest) =>
    unwrap<IdResponse>(api.post(`/api/worlds/${worldId}/prompts`, payload)),
  updatePrompt: (worldId: number, promptId: number, payload: PromptUpsertRequest) =>
    unwrap<VersionedIdResponse>(api.patch(`/api/worlds/${worldId}/prompts/${promptId}`, payload)),
  deletePrompt: (worldId: number, promptId: number) =>
    unwrap<void>(api.delete(`/api/worlds/${worldId}/prompts/${promptId}`)),
  sortPrompts: (worldId: number, promptOrders: Array<{ id: number; sortOrder: number }>) =>
    unwrap<void>(api.patch(`/api/worlds/${worldId}/prompts/sort`, { promptOrders })),
  togglePrompt: (worldId: number, promptId: number, isActive: boolean) =>
    unwrap<void>(api.patch(`/api/worlds/${worldId}/prompts/${promptId}/active`, { isActive })),
}
