import { api, unwrap } from './client'
import type { PostDetailResponse, PostListResponse, VoteResponse } from '../types'

export const postApi = {
  list: (params?: { page?: number; size?: number; sort?: string; characterId?: number | null }) =>
    unwrap<PostListResponse>(api.get('/api/posts', { params })),
  listByCommunity: (
    communityId: number,
    params?: { page?: number; size?: number; sort?: string; characterId?: number | null },
  ) => unwrap<PostListResponse>(api.get(`/api/communities/${communityId}/posts`, { params })),
  listByWorld: (
    worldId: number,
    params?: { page?: number; size?: number; sort?: string; characterId?: number | null },
  ) => unwrap<PostListResponse>(api.get(`/api/worlds/${worldId}/posts`, { params })),
  detail: (postId: number) => unwrap<PostDetailResponse>(api.get(`/api/posts/${postId}`)),
  vote: (postId: number, voteType: 'LIKE' | 'DISLIKE') =>
    unwrap<VoteResponse>(api.post(`/api/posts/${postId}/vote`, { voteType })),
  remove: (postId: number) => unwrap<void>(api.delete(`/api/admin/posts/${postId}`)),
}
