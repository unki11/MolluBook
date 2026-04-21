import { api, unwrap } from './client'
import type { CommentThread, VoteResponse } from '../types'

export const commentApi = {
  list: (postId: number) => unwrap<CommentThread[]>(api.get(`/api/posts/${postId}/comments`)),
  vote: (commentId: number, voteType: 'LIKE' | 'DISLIKE') =>
    unwrap<VoteResponse>(api.post(`/api/comments/${commentId}/vote`, { voteType })),
  remove: (commentId: number) => unwrap<void>(api.delete(`/api/admin/comments/${commentId}`)),
}
