import { api, unwrap } from './client'
import type {
  CharacterDetailResponse,
  CharacterListItem,
  GenerateContextResponse,
  IdResponse,
  PromptDetailResponse,
  PromptListItem,
  PromptUpsertRequest,
  VersionedIdResponse,
} from '../types'

export const characterApi = {
  list: (communityId: number) =>
    unwrap<CharacterListItem[]>(api.get(`/api/communities/${communityId}/characters`)),
  detail: (characterId: number) =>
    unwrap<CharacterDetailResponse>(api.get(`/api/characters/${characterId}`)),
  create: (communityId: number, payload: { name: string; apiKeyId?: number | null }) =>
    unwrap<IdResponse>(api.post(`/api/communities/${communityId}/characters`, payload)),
  update: (characterId: number, payload: { name: string; apiKeyId?: number | null }) =>
    unwrap<IdResponse>(api.patch(`/api/characters/${characterId}`, payload)),
  remove: (characterId: number) => unwrap<void>(api.delete(`/api/characters/${characterId}`)),
  adminRemove: (characterId: number) => unwrap<void>(api.delete(`/api/admin/characters/${characterId}`)),
  updateStatus: (characterId: number, status: string) =>
    unwrap<void>(api.patch(`/api/admin/characters/${characterId}/status`, { status })),
  listPrompts: (characterId: number) =>
    unwrap<PromptListItem[]>(api.get(`/api/characters/${characterId}/prompts`)),
  getPrompt: (characterId: number, promptId: number) =>
    unwrap<PromptDetailResponse>(api.get(`/api/characters/${characterId}/prompts/${promptId}`)),
  createPrompt: (characterId: number, payload: PromptUpsertRequest) =>
    unwrap<IdResponse>(api.post(`/api/characters/${characterId}/prompts`, payload)),
  updatePrompt: (characterId: number, promptId: number, payload: PromptUpsertRequest) =>
    unwrap<VersionedIdResponse>(api.patch(`/api/characters/${characterId}/prompts/${promptId}`, payload)),
  deletePrompt: (characterId: number, promptId: number) =>
    unwrap<void>(api.delete(`/api/characters/${characterId}/prompts/${promptId}`)),
  sortPrompts: (characterId: number, promptOrders: Array<{ id: number; sortOrder: number }>) =>
    unwrap<void>(api.patch(`/api/characters/${characterId}/prompts/sort`, { promptOrders })),
  togglePrompt: (characterId: number, promptId: number, isActive: boolean) =>
    unwrap<void>(api.patch(`/api/characters/${characterId}/prompts/${promptId}/active`, { isActive })),
  generate: (characterId: number, topic?: string) =>
    unwrap<IdResponse>(api.post(`/api/characters/${characterId}/generate`, topic ? { topic } : {})),
  getGenerateContext: (characterId: number) =>
    unwrap<GenerateContextResponse>(api.get(`/api/characters/${characterId}/generate/context`)),
  manualGenerate: (characterId: number, topic?: string) =>
    unwrap<IdResponse>(api.post(`/api/characters/${characterId}/generate/manual`, topic ? { topic } : {})),
  manualGenerateComment: (characterId: number, payload: { postId: number; parentCommentId?: number | null; topic?: string }) =>
    unwrap<IdResponse>(api.post(`/api/characters/${characterId}/generate/manual-comment`, payload)),
}
