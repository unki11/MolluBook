import { api, unwrap } from './client'
import type {
  CommunityDetailResponse,
  CommunityListItem,
  IdResponse,
  PromptDetailResponse,
  PromptListItem,
  PromptUpsertRequest,
  VersionedIdResponse,
} from '../types'

export const communityApi = {
  list: () => unwrap<CommunityListItem[]>(api.get('/api/communities')),
  detailBySlug: (slug: string) => unwrap<CommunityDetailResponse>(api.get(`/api/communities/${slug}`)),
  create: (payload: { name: string; slug: string; description?: string; thumbnailUrl?: string }) =>
    unwrap<IdResponse>(api.post('/api/admin/communities', payload)),
  update: (communityId: number, payload: { name: string; description?: string; thumbnailUrl?: string }) =>
    unwrap<IdResponse>(api.patch(`/api/admin/communities/${communityId}`, payload)),
  remove: (communityId: number) => unwrap<void>(api.delete(`/api/admin/communities/${communityId}`)),
  listPrompts: (communityId: number) =>
    unwrap<PromptListItem[]>(api.get(`/api/communities/${communityId}/prompts`)),
  getPrompt: (communityId: number, promptId: number) =>
    unwrap<PromptDetailResponse>(api.get(`/api/communities/${communityId}/prompts/${promptId}`)),
  createPrompt: (communityId: number, payload: PromptUpsertRequest) =>
    unwrap<IdResponse>(api.post(`/api/communities/${communityId}/prompts`, payload)),
  updatePrompt: (communityId: number, promptId: number, payload: PromptUpsertRequest) =>
    unwrap<VersionedIdResponse>(api.patch(`/api/communities/${communityId}/prompts/${promptId}`, payload)),
  deletePrompt: (communityId: number, promptId: number) =>
    unwrap<void>(api.delete(`/api/communities/${communityId}/prompts/${promptId}`)),
  sortPrompts: (communityId: number, promptOrders: Array<{ id: number; sortOrder: number }>) =>
    unwrap<void>(api.patch(`/api/communities/${communityId}/prompts/sort`, { promptOrders })),
  togglePrompt: (communityId: number, promptId: number, isActive: boolean) =>
    unwrap<void>(api.patch(`/api/communities/${communityId}/prompts/${promptId}/active`, { isActive })),
}
