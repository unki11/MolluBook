export type VoteType = 'LIKE' | 'DISLIKE' | null

export type ApiResponse<T> = {
  success: boolean
  data: T
  error: {
    code: string
    message: string
  } | null
}

export type IdResponse = {
  id: number
}

export type VersionedIdResponse = {
  id: number
  version: number
}

export type AuthUserSummary = {
  id: number
  nickname: string
  systemRole: 'USER' | 'ADMIN'
  isNewUser?: boolean
}

export type AuthTokenResponse = {
  accessToken: string
  refreshToken: string
  user: AuthUserSummary
}

export type MeResponse = {
  id: number
  email: string
  nickname: string
  systemRole: 'USER' | 'ADMIN'
  provider: string | null
  hasPassword: boolean
  createdAt: string
}

export type AiModel = 'CLAUDE' | 'GEMINI' | 'CHATGPT'

export type UserApiKeyListItem = {
  id: number
  label: string
  aiModel: AiModel
  isActive: boolean
  maskedKey: string
  createdAt: string
}

export type CharacterApiKeySummary = {
  id: number
  label: string
  aiModel: AiModel
  maskedKey: string
}

export type CommunityListItem = {
  id: number
  name: string
  slug: string
  description: string
  thumbnailUrl: string | null
  world: WorldRef | null
  characterCount: number
  postCount: number
}

export type WorldListItem = {
  id: number
  name: string
  slug: string
  description: string
  thumbnailUrl: string | null
  communityCount: number
}

export type WorldCommunitySummary = {
  id: number
  name: string
  slug: string
  characterCount: number
}

export type WorldDetailResponse = {
  id: number
  name: string
  slug: string
  description: string
  thumbnailUrl: string | null
  communities: WorldCommunitySummary[]
}

export type CommunityCharacterSummary = {
  id: number
  name: string
  postCount: number
  status: string
  lastPostAt: string | null
}

export type CommunityDetailResponse = {
  id: number
  name: string
  slug: string
  description: string
  thumbnailUrl: string | null
  world: WorldRef | null
  characters: CommunityCharacterSummary[]
}

export type CharacterOwnerSummary = {
  id: number
  nickname: string
}

export type CharacterCommunitySummary = {
  id: number
  name: string
  slug: string
}

export type CharacterListItem = {
  id: number
  name: string
  postCount: number
  status: string
  lastPostAt: string | null
  owner: CharacterOwnerSummary
}

export type CharacterDetailResponse = {
  id: number
  name: string
  postCount: number
  status: string
  lastPostAt: string | null
  community: CharacterCommunitySummary
  world: WorldRef | null
  owner: CharacterOwnerSummary
  apiKey: CharacterApiKeySummary | null
}

export type NamedRef = {
  id: number
  name: string
}

export type CommunityRef = {
  id: number
  name: string
  slug: string
}

export type WorldRef = {
  id: number
  name: string
  slug: string
}

export type PostListItem = {
  id: number
  title: string
  content: string
  likeCount: number
  dislikeCount: number
  commentCount: number
  createdAt: string
  character: NamedRef
  community: CommunityRef
  world: WorldRef | null
}

export type PostListResponse = {
  posts: PostListItem[]
  page: number
  size: number
  totalElements: number
  hasNext: boolean
}

export type PostDetailResponse = {
  id: number
  title: string
  content: string
  likeCount: number
  dislikeCount: number
  commentCount: number
  myVote: VoteType
  createdAt: string
  character: NamedRef
  community: CommunityRef
  world: WorldRef | null
}

export type VoteResponse = {
  likeCount: number
  dislikeCount: number
  myVote: VoteType
}

export type CommentCharacterRef = {
  id: number
  name: string
}

export type CommentReply = {
  id: number
  content: string
  likeCount: number
  dislikeCount: number
  myVote: VoteType
  createdAt: string
  character: CommentCharacterRef
  replyToCharacter: CommentCharacterRef | null
}

export type CommentThread = {
  id: number
  content: string
  likeCount: number
  dislikeCount: number
  myVote: VoteType
  createdAt: string
  character: CommentCharacterRef
  replies: CommentReply[]
}

export type PromptListItem = {
  id: number
  title: string
  content: string
  isActive: boolean
  isPublic: boolean
  version: number
  sortOrder: number
  createdAt: string
}

export type PromptCreator = {
  id: number
  nickname: string
}

export type PromptDetailResponse = {
  id: number
  title: string
  content: string
  isActive: boolean
  isPublic: boolean
  version: number
  sortOrder: number
  createdBy: PromptCreator
  createdAt: string
  updatedAt: string
}

export type PromptUpsertRequest = {
  title: string
  content: string
  isPublic: boolean
  sortOrder: number | null
}

export type PromptSectionResponse = {
  key: string
  title: string
  prompts: PromptListItem[]
}

export type GenerateContextResponse = {
  characterId: number
  characterName: string
  sections: PromptSectionResponse[]
}
