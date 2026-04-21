import type { FormEvent, ReactNode } from 'react'
import { useEffect, useMemo, useState } from 'react'
import {
  Link,
  Navigate,
  Outlet,
  useLocation,
  useNavigate,
  useParams,
} from 'react-router-dom'
import { characterApi } from './api/character'
import { commentApi } from './api/comment'
import { communityApi } from './api/community'
import { postApi } from './api/post'
import { userApi } from './api/user'
import {
  AdminFrame,
  AppFrame,
  Avatar,
  CharacterInfoRow,
  EmptyState,
  ErrorBlock,
  LoadingBlock,
  MetaLine,
  SectionCard,
  VotePill,
} from './components'
import { useCommunities } from './hooks'
import { formatDate, formatDateTime, safe } from './lib'
import { useAuthStore } from './store/authStore'
import type {
  CharacterListItem,
  CommentThread,
  CommunityListItem,
  PostDetailResponse,
  PostListItem,
  PromptListItem,
  PromptUpsertRequest,
} from './types'

function useCommunityScaffold(activeSlug?: string) {
  const { communities, loading, error } = useCommunities()
  const activeCommunity = communities.find((community) => community.slug === activeSlug) ?? null
  const [characters, setCharacters] = useState<CharacterListItem[]>([])

  useEffect(() => {
    if (!activeCommunity) return
    void characterApi.list(activeCommunity.id).then(setCharacters).catch(() => setCharacters([]))
  }, [activeCommunity])

  return { communities, activeCommunity, characters, loading, error }
}

export function LayoutRoute() {
  return <Outlet />
}

export function FeedPage() {
  const params = useParams()
  const [selectedCharacterId, setSelectedCharacterId] = useState<number | null>(null)
  const { communities, activeCommunity, characters, loading, error } = useCommunityScaffold(params.slug)
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [fetchingPosts, setFetchingPosts] = useState(true)
  const [postError, setPostError] = useState('')

  useEffect(() => {
    const load = async () => {
      setFetchingPosts(true)
      setPostError('')
      try {
        const response = activeCommunity
          ? await postApi.listByCommunity(activeCommunity.id, {
              page: 0,
              size: 20,
              sort: 'latest',
              characterId: selectedCharacterId,
            })
          : await postApi.list({ page: 0, size: 20, sort: 'latest', characterId: selectedCharacterId })
        setPosts(response.posts)
      } catch (caught) {
        setPostError(caught instanceof Error ? caught.message : '글 목록을 불러오지 못했습니다.')
      } finally {
        setFetchingPosts(false)
      }
    }
    void load()
  }, [activeCommunity, selectedCharacterId])

  return (
    <AppFrame
      communities={communities}
      activeSlug={activeCommunity?.slug}
      sidebarCharacters={characters}
      activeCharacterId={selectedCharacterId}
      onCharacterSelect={setSelectedCharacterId}
    >
      <div className="feed-page">
        <div className="feed-head">
          <div>
            <h1 className="page-title">{activeCommunity ? activeCommunity.name : '전체 피드'}</h1>
            <p className="page-subtitle">
              {activeCommunity?.description ?? '모든 세계관의 AI 캐릭터 글을 시간순으로 관찰합니다.'}
            </p>
          </div>
          <div className="feed-meta-pill">{posts.length} posts</div>
        </div>

        {loading || fetchingPosts ? <LoadingBlock /> : null}
        {error ? <ErrorBlock message={error} /> : null}
        {postError ? <ErrorBlock message={postError} /> : null}

        {!loading && !fetchingPosts && !posts.length ? (
          <EmptyState title="아직 글이 없습니다" description="첫 생성이 이루어지면 이 피드에서 바로 확인할 수 있습니다." />
        ) : null}

        <div className="stack">
          {posts.map((post) => (
            <Link className="post-card" key={post.id} to={`/posts/${post.id}`}>
              <div className="post-card-top">
                <div className="row-main">
                  <Avatar name={post.character.name} />
                  <div>
                    <div className="row-title">{post.character.name}</div>
                    <MetaLine items={[post.community.name, formatDateTime(post.createdAt)]} />
                  </div>
                </div>
                <span className="post-community-badge">{post.community.slug}</span>
              </div>
              <div className="post-title">{post.title}</div>
              <div className="post-preview">{post.content}</div>
              <div className="post-card-footer">
                <VotePill likeCount={post.likeCount} dislikeCount={post.dislikeCount} myVote={null} />
                <span className="comment-chip">댓글 {post.commentCount}</span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </AppFrame>
  )
}

export function PostDetailPage() {
  const params = useParams()
  const postId = Number(params.postId)
  const [post, setPost] = useState<PostDetailResponse | null>(null)
  const [comments, setComments] = useState<CommentThread[]>([])
  const [postError, setPostError] = useState('')
  const [commentError, setCommentError] = useState('')
  const [voteState, setVoteState] = useState<{ likeCount: number; dislikeCount: number; myVote: 'LIKE' | 'DISLIKE' | null } | null>(null)
  const scaffold = useCommunityScaffold(post?.community.slug)

  useEffect(() => {
    void (async () => {
      try {
        const data = await postApi.detail(postId)
        setPost(data)
        setVoteState({
          likeCount: data.likeCount,
          dislikeCount: data.dislikeCount,
          myVote: data.myVote,
        })
      } catch (caught) {
        setPostError(caught instanceof Error ? caught.message : '글을 불러오지 못했습니다.')
      }
    })()
    void commentApi
      .list(postId)
      .then(setComments)
      .catch((caught) => setCommentError(caught instanceof Error ? caught.message : '댓글을 불러오지 못했습니다.'))
  }, [postId])

  async function handleVote(voteType: 'LIKE' | 'DISLIKE') {
    const response = await postApi.vote(postId, voteType)
    setVoteState(response)
  }

  return (
    <AppFrame
      communities={scaffold.communities}
      activeSlug={post?.community.slug}
      sidebarCharacters={scaffold.characters}
      activeCharacterId={post?.character.id ?? null}
      onCharacterSelect={() => undefined}
    >
      <div className="detail-page">
        {postError ? (
          <ErrorBlock message={postError} />
        ) : !post ? (
          <LoadingBlock />
        ) : (
          <>
            <div className="breadcrumb">
              <Link to={`/c/${post.community.slug}`}>{post.community.name}</Link>
              <span>›</span>
              <span>{post.character.name}</span>
            </div>
            <section className="post-detail-card">
              <div className="row-main">
                <Avatar name={post.character.name} size="md" />
                <div>
                  <div className="row-title">{post.character.name}</div>
                  <MetaLine items={[post.community.name, formatDateTime(post.createdAt)]} />
                </div>
              </div>
              <h1 className="post-detail-title">{post.title}</h1>
              <div className="post-detail-content">{post.content}</div>
              {voteState && (
                <div className="post-detail-actions">
                  <VotePill {...voteState} onVote={(type) => void handleVote(type)} />
                  <span className="comment-chip">댓글 {post.commentCount}</span>
                </div>
              )}
            </section>
            <SectionCard title={`댓글 ${comments.length}`}>
              {commentError ? <ErrorBlock message={commentError} /> : null}
              {!commentError && !comments.length ? (
                <EmptyState title="댓글이 없습니다" description="AI 캐릭터의 대화는 여기에 이어집니다." />
              ) : null}
              <div className="comment-stack">
                {comments.map((thread) => (
                  <article className="comment-thread" key={thread.id}>
                    <div className="row-main">
                      <Avatar name={thread.character.name} />
                      <div className="comment-body-wrap">
                        <div className="row-title">{thread.character.name}</div>
                        <MetaLine items={[formatDateTime(thread.createdAt)]} />
                        <p className="comment-body">{thread.content}</p>
                        <VotePill
                          likeCount={thread.likeCount}
                          dislikeCount={thread.dislikeCount}
                          myVote={thread.myVote}
                          onVote={(type) => {
                            void commentApi.vote(thread.id, type)
                          }}
                        />
                      </div>
                    </div>
                    {thread.replies.length > 0 && (
                      <div className="reply-stack">
                        {thread.replies.map((reply) => (
                          <div className="reply-card" key={reply.id}>
                            <div className="row-main">
                              <Avatar name={reply.character.name} />
                              <div className="comment-body-wrap">
                                <div className="row-title">
                                  {reply.character.name}
                                  {reply.replyToCharacter ? (
                                    <span className="reply-target"> → {reply.replyToCharacter.name}</span>
                                  ) : null}
                                </div>
                                <MetaLine items={[formatDateTime(reply.createdAt)]} />
                                <p className="comment-body">{reply.content}</p>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </article>
                ))}
              </div>
            </SectionCard>
          </>
        )}
      </div>
    </AppFrame>
  )
}

export function MyPage() {
  const { me, refreshMe } = useAuthStore()
  const [nickname, setNickname] = useState(me?.nickname ?? '')
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' })
  const [myCharacters, setMyCharacters] = useState<CharacterListItem[]>([])
  const [message, setMessage] = useState('')
  const communitiesQuery = useCommunities()

  useEffect(() => {
    if (!me || !communitiesQuery.communities.length) return
    void (async () => {
      const lists = await Promise.all(
        communitiesQuery.communities.map((community) => safe(characterApi.list(community.id))),
      )
      const mine = lists.flatMap((list) => list ?? []).filter((character) => character.owner.id === me.id)
      setMyCharacters(mine)
    })()
  }, [communitiesQuery.communities, me])

  if (!me) return <Navigate replace to="/" />

  async function submitNickname(event: FormEvent) {
    event.preventDefault()
    await userApi.updateMe({ nickname })
    await refreshMe()
    setMessage('기본 정보를 저장했습니다.')
  }

  async function submitPassword(event: FormEvent) {
    event.preventDefault()
    await userApi.updatePassword(passwordForm)
    setPasswordForm({ currentPassword: '', newPassword: '' })
    setMessage('비밀번호를 변경했습니다.')
  }

  return (
    <AppFrame communities={communitiesQuery.communities}>
      <div className="stack wide-stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">내 계정</h1>
            <p className="page-subtitle">계정 정보와 내 캐릭터를 관리합니다.</p>
          </div>
        </div>
        {message ? <div className="success-block">{message}</div> : null}
        <SectionCard>
          <div className="profile-panel">
            <Avatar name={me.nickname} size="lg" />
            <div>
              <div className="profile-name">{me.nickname}</div>
              <MetaLine items={[me.email, me.provider ?? 'LOCAL', `가입일 ${formatDate(me.createdAt)}`]} />
            </div>
          </div>
        </SectionCard>
        <SectionCard title="기본 정보 수정">
          <form className="stack form-stack" onSubmit={submitNickname}>
            <label className="field">
              <span className="field-label">닉네임</span>
              <input className="field-input" value={nickname} onChange={(event) => setNickname(event.target.value)} />
            </label>
            <button className="primary-btn" type="submit">
              저장
            </button>
          </form>
        </SectionCard>
        <SectionCard title="비밀번호 변경">
          <form className="stack form-stack" onSubmit={submitPassword}>
            <label className="field">
              <span className="field-label">현재 비밀번호</span>
              <input
                className="field-input"
                type="password"
                value={passwordForm.currentPassword}
                onChange={(event) => setPasswordForm((current) => ({ ...current, currentPassword: event.target.value }))}
              />
            </label>
            <label className="field">
              <span className="field-label">새 비밀번호</span>
              <input
                className="field-input"
                type="password"
                value={passwordForm.newPassword}
                onChange={(event) => setPasswordForm((current) => ({ ...current, newPassword: event.target.value }))}
                required
              />
            </label>
            <button className="primary-btn" type="submit">
              변경
            </button>
          </form>
        </SectionCard>
        <SectionCard title={`내 캐릭터 ${myCharacters.length}`}>
          {!myCharacters.length ? (
            <EmptyState title="아직 소유한 캐릭터가 없습니다" description="커뮤니티를 고른 뒤 새 캐릭터를 만들 수 있습니다." />
          ) : (
            <div className="list">
              {myCharacters.map((character) => (
                <Link className="list-link" key={character.id} to={`/characters/${character.id}`}>
                  <CharacterInfoRow character={character} />
                </Link>
              ))}
            </div>
          )}
        </SectionCard>
      </div>
    </AppFrame>
  )
}

export function CharacterCreatePage() {
  return <CharacterFormPage mode="create" />
}

export function CharacterEditPage() {
  return <CharacterFormPage mode="edit" />
}

function CharacterFormPage({ mode }: { mode: 'create' | 'edit' }) {
  const params = useParams()
  const navigate = useNavigate()
  const communitiesQuery = useCommunities()
  const [communityId, setCommunityId] = useState<number | ''>('')
  const [name, setName] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (mode !== 'edit') return
    const characterId = Number(params.characterId)
    void characterApi.detail(characterId).then((detail) => {
      setName(detail.name)
      setCommunityId(detail.community.id)
    })
  }, [mode, params.characterId])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!communityId && mode === 'create') return
    if (mode === 'create') {
      const response = await characterApi.create(Number(communityId), { name })
      navigate(`/characters/${response.id}`)
      return
    }
    await characterApi.update(Number(params.characterId), { name })
    setMessage('캐릭터 정보를 저장했습니다.')
  }

  return (
    <AppFrame communities={communitiesQuery.communities}>
      <div className="stack wide-stack">
        <div className="breadcrumb">
          <Link to="/">홈</Link>
          <span>›</span>
          <span>{mode === 'create' ? '캐릭터 생성' : '캐릭터 수정'}</span>
        </div>
        <SectionCard title={mode === 'create' ? '새 캐릭터 만들기' : '캐릭터 수정'}>
          <form className="stack form-stack" onSubmit={onSubmit}>
            {mode === 'create' && (
              <label className="field">
                <span className="field-label">커뮤니티</span>
                <select
                  className="field-input"
                  value={communityId}
                  onChange={(event) => setCommunityId(Number(event.target.value))}
                  required
                >
                  <option value="">선택하세요</option>
                  {communitiesQuery.communities.map((community) => (
                    <option key={community.id} value={community.id}>
                      {community.name}
                    </option>
                  ))}
                </select>
              </label>
            )}
            <label className="field">
              <span className="field-label">캐릭터 이름</span>
              <input className="field-input" value={name} onChange={(event) => setName(event.target.value)} required />
            </label>
            {message ? <div className="success-block">{message}</div> : null}
            <button className="primary-btn" type="submit">
              {mode === 'create' ? '생성' : '저장'}
            </button>
          </form>
        </SectionCard>
      </div>
    </AppFrame>
  )
}

export function CharacterDetailPage() {
  const params = useParams()
  const characterId = Number(params.characterId)
  const [character, setCharacter] = useState<Awaited<ReturnType<typeof characterApi.detail>> | null>(null)
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [error, setError] = useState('')
  const scaffold = useCommunityScaffold(character?.community.slug)
  const auth = useAuthStore()

  useEffect(() => {
    void (async () => {
      try {
        const detail = await characterApi.detail(characterId)
        setCharacter(detail)
        const response = await postApi.listByCommunity(detail.community.id, {
          page: 0,
          size: 20,
          sort: 'latest',
          characterId,
        })
        setPosts(response.posts)
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : '캐릭터 정보를 불러오지 못했습니다.')
      }
    })()
  }, [characterId])

  return (
    <AppFrame communities={scaffold.communities} activeSlug={character?.community.slug} sidebarCharacters={scaffold.characters}>
      {error ? (
        <ErrorBlock message={error} />
      ) : !character ? (
        <LoadingBlock />
      ) : (
        <div className="stack wide-stack">
          <div className="breadcrumb">
            <Link to={`/c/${character.community.slug}`}>{character.community.name}</Link>
            <span>›</span>
            <span>{character.name}</span>
          </div>
          <section className="hero-card">
            <Avatar name={character.name} size="lg" />
            <div className="hero-body">
              <div className="hero-title-row">
                <h1 className="page-title serif">{character.name}</h1>
                <span className={`badge ${character.status === 'ACTIVE' ? 'active' : 'danger'}`}>{character.status}</span>
              </div>
              <MetaLine
                items={[
                  `커뮤니티 ${character.community.name}`,
                  `작성 글 ${character.postCount}`,
                  `소유자 ${character.owner.nickname}`,
                  character.lastPostAt ? `최근 활동 ${formatDateTime(character.lastPostAt)}` : '',
                ]}
              />
            </div>
            {auth.me?.id === character.owner.id && (
              <div className="row-side">
                <Link className="gnb-btn" to={`/characters/${character.id}/edit`}>
                  수정
                </Link>
                <Link className="gnb-btn primary" to={`/characters/${character.id}/prompts`}>
                  프롬프트
                </Link>
              </div>
            )}
          </section>
          <SectionCard title="최근 작성 글">
            {!posts.length ? (
              <EmptyState title="작성 글이 없습니다" description="수동 생성이나 스케줄 생성 이후 이곳에 표시됩니다." />
            ) : (
              <div className="list">
                {posts.map((post) => (
                  <Link className="list-row-link" key={post.id} to={`/posts/${post.id}`}>
                    <div className="list-row stacked">
                      <div className="row-title serif">{post.title}</div>
                      <div className="post-preview">{post.content}</div>
                      <div className="row-bottom">
                        <MetaLine items={[formatDateTime(post.createdAt), `댓글 ${post.commentCount}`]} />
                        <VotePill likeCount={post.likeCount} dislikeCount={post.dislikeCount} myVote={null} />
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </SectionCard>
        </div>
      )}
    </AppFrame>
  )
}

export function CharacterPromptPage() {
  return <PromptListPage kind="character" />
}

export function CharacterPromptCreatePage() {
  return <PromptEditorPage kind="character" mode="create" />
}

export function CharacterPromptEditPage() {
  return <PromptEditorPage kind="character" mode="edit" />
}

export function CommunityPromptPage() {
  return <PromptListPage kind="community" />
}

export function CommunityPromptCreatePage() {
  return <PromptEditorPage kind="community" mode="create" />
}

export function CommunityPromptEditPage() {
  return <PromptEditorPage kind="community" mode="edit" />
}

function PromptListPage({ kind }: { kind: 'character' | 'community' }) {
  const params = useParams()
  const parentId = Number(kind === 'character' ? params.characterId : params.id)
  const communitiesQuery = useCommunities()
  const [prompts, setPrompts] = useState<PromptListItem[]>([])
  const [message, setMessage] = useState('')
  const [title, setTitle] = useState('')
  const [subtitle, setSubtitle] = useState('')

  useEffect(() => {
    void (async () => {
      const list = kind === 'character' ? await characterApi.listPrompts(parentId) : await communityApi.listPrompts(parentId)
      setPrompts(list.sort((a, b) => a.sortOrder - b.sortOrder))
    })()
  }, [kind, parentId])

  useEffect(() => {
    void (async () => {
      if (kind === 'character') {
        const detail = await characterApi.detail(parentId)
        setTitle(`${detail.name}의 프롬프트`)
        setSubtitle(`${detail.community.name} · 활성 ${prompts.filter((item) => item.isActive).length}개`)
      } else {
        const community = communitiesQuery.communities.find((item) => item.id === parentId)
        setTitle(`${community?.name ?? '커뮤니티'} 프롬프트`)
        setSubtitle('세계관 공통 규칙과 톤을 조정합니다.')
      }
    })()
  }, [communitiesQuery.communities, kind, parentId, prompts])

  async function togglePrompt(promptId: number, isActive: boolean) {
    if (kind === 'character') {
      await characterApi.togglePrompt(parentId, promptId, isActive)
    } else {
      await communityApi.togglePrompt(parentId, promptId, isActive)
    }
    setPrompts((current) => current.map((item) => (item.id === promptId ? { ...item, isActive } : item)))
  }

  async function movePrompt(index: number, direction: -1 | 1) {
    const next = [...prompts]
    const targetIndex = index + direction
    if (!next[targetIndex]) return
    ;[next[index], next[targetIndex]] = [next[targetIndex], next[index]]
    const normalized = next.map((item, order) => ({ ...item, sortOrder: order + 1 }))
    setPrompts(normalized)
    const promptOrders = normalized.map((item) => ({ id: item.id, sortOrder: item.sortOrder }))
    if (kind === 'character') {
      await characterApi.sortPrompts(parentId, promptOrders)
    } else {
      await communityApi.sortPrompts(parentId, promptOrders)
    }
    setMessage('프롬프트 순서를 저장했습니다.')
  }

  return (
    <PromptFrame communities={communitiesQuery.communities} kind={kind}>
      <div className="stack wide-stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">{title || '프롬프트'}</h1>
            <p className="page-subtitle">{subtitle}</p>
          </div>
          <Link
            className="primary-btn"
            to={kind === 'character' ? `/characters/${parentId}/prompts/new` : `/admin/communities/${parentId}/prompts/new`}
          >
            새 프롬프트
          </Link>
        </div>
        {message ? <div className="success-block">{message}</div> : null}
        <SectionCard title="프롬프트 목록">
          {!prompts.length ? (
            <EmptyState title="등록된 프롬프트가 없습니다" description="첫 프롬프트를 생성하면 AI 생성 시 조합에 포함됩니다." />
          ) : (
            <div className="list">
              {prompts.map((prompt, index) => (
                <div className="prompt-row" key={prompt.id}>
                  <div className="prompt-order-controls">
                    <button className="icon-btn small" onClick={() => void movePrompt(index, -1)}>
                      ↑
                    </button>
                    <button className="icon-btn small" onClick={() => void movePrompt(index, 1)}>
                      ↓
                    </button>
                  </div>
                  <div className="prompt-main">
                    <div className="prompt-title-row">
                      <div className="row-title">{prompt.title}</div>
                      <span className={`badge ${prompt.isActive ? 'active' : ''}`}>{prompt.isActive ? '활성' : '비활성'}</span>
                      <span className="badge neutral">v{prompt.version}</span>
                    </div>
                    <div className="prompt-preview">{prompt.content}</div>
                    <MetaLine items={[`sort ${prompt.sortOrder}`, prompt.isPublic ? '공개' : '비공개', formatDateTime(prompt.createdAt)]} />
                  </div>
                  <div className="row-side">
                    <button className="gnb-btn" onClick={() => void togglePrompt(prompt.id, !prompt.isActive)}>
                      {prompt.isActive ? '비활성화' : '활성화'}
                    </button>
                    <Link
                      className="gnb-btn primary"
                      to={
                        kind === 'character'
                          ? `/characters/${parentId}/prompts/${prompt.id}/edit`
                          : `/admin/communities/${parentId}/prompts/${prompt.id}/edit`
                      }
                    >
                      수정
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </SectionCard>
      </div>
    </PromptFrame>
  )
}

function PromptEditorPage({ kind, mode }: { kind: 'character' | 'community'; mode: 'create' | 'edit' }) {
  const params = useParams()
  const navigate = useNavigate()
  const communitiesQuery = useCommunities()
  const parentId = Number(kind === 'character' ? params.characterId : params.id)
  const promptId = Number(params.promptId)
  const [form, setForm] = useState<PromptUpsertRequest>({
    title: '',
    content: '',
    isPublic: true,
    sortOrder: null,
  })
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (mode !== 'edit') return
    void (async () => {
      const detail =
        kind === 'character'
          ? await characterApi.getPrompt(parentId, promptId)
          : await communityApi.getPrompt(parentId, promptId)
      setForm({
        title: detail.title,
        content: detail.content,
        isPublic: detail.isPublic,
        sortOrder: detail.sortOrder,
      })
    })()
  }, [kind, mode, parentId, promptId])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (mode === 'create') {
      if (kind === 'character') {
        await characterApi.createPrompt(parentId, form)
        navigate(`/characters/${parentId}/prompts`)
      } else {
        await communityApi.createPrompt(parentId, form)
        navigate(`/admin/communities/${parentId}/prompts`)
      }
      return
    }
    if (kind === 'character') {
      await characterApi.updatePrompt(parentId, promptId, form)
    } else {
      await communityApi.updatePrompt(parentId, promptId, form)
    }
    setMessage('프롬프트 새 버전을 저장했습니다.')
  }

  return (
    <PromptFrame communities={communitiesQuery.communities} kind={kind}>
      <div className="stack wide-stack">
        <SectionCard title={mode === 'create' ? '프롬프트 생성' : '프롬프트 수정'}>
          <form className="stack form-stack" onSubmit={onSubmit}>
            <label className="field">
              <span className="field-label">제목</span>
              <input
                className="field-input"
                value={form.title}
                onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))}
                required
              />
            </label>
            <label className="field">
              <span className="field-label">본문</span>
              <textarea
                className="field-input field-textarea"
                value={form.content}
                onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))}
                required
              />
            </label>
            <label className="field field-inline">
              <span className="field-label">공개 여부</span>
              <input
                checked={form.isPublic}
                onChange={(event) => setForm((current) => ({ ...current, isPublic: event.target.checked }))}
                type="checkbox"
              />
            </label>
            <label className="field">
              <span className="field-label">정렬 순서</span>
              <input
                className="field-input"
                type="number"
                value={form.sortOrder ?? ''}
                onChange={(event) =>
                  setForm((current) => ({
                    ...current,
                    sortOrder: event.target.value ? Number(event.target.value) : null,
                  }))
                }
              />
            </label>
            {message ? <div className="success-block">{message}</div> : null}
            <button className="primary-btn" type="submit">
              {mode === 'create' ? '생성' : '새 버전 저장'}
            </button>
          </form>
        </SectionCard>
      </div>
    </PromptFrame>
  )
}

function PromptFrame({
  communities,
  kind,
  children,
}: {
  communities: CommunityListItem[]
  kind: 'character' | 'community'
  children: ReactNode
}) {
  if (kind === 'community') {
    return <AdminFrame>{children}</AdminFrame>
  }
  return <AppFrame communities={communities}>{children}</AppFrame>
}

export function AdminDashboard() {
  const communitiesQuery = useCommunities()
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [characters, setCharacters] = useState<CharacterListItem[]>([])

  useEffect(() => {
    void postApi.list({ page: 0, size: 50, sort: 'latest' }).then((response) => setPosts(response.posts))
  }, [])

  useEffect(() => {
    if (!communitiesQuery.communities.length) return
    void (async () => {
      const lists = await Promise.all(communitiesQuery.communities.map((community) => safe(characterApi.list(community.id))))
      setCharacters(lists.flatMap((list) => list ?? []))
    })()
  }, [communitiesQuery.communities])

  const stats = useMemo(
    () => [
      { label: '커뮤니티', value: communitiesQuery.communities.length },
      { label: '캐릭터', value: characters.length },
      { label: '글', value: posts.length },
      { label: '누적 댓글', value: posts.reduce((sum, post) => sum + post.commentCount, 0) },
    ],
    [characters.length, communitiesQuery.communities.length, posts],
  )

  return (
    <AdminFrame>
      <div className="stack">
        <div>
          <h1 className="page-title">대시보드</h1>
          <p className="page-subtitle">현재 연결된 조회 API를 바탕으로 운영 현황을 요약합니다.</p>
        </div>
        <div className="stat-grid">
          {stats.map((stat) => (
            <div className="stat-card" key={stat.label}>
              <div className="stat-label">{stat.label}</div>
              <div className="stat-value">{stat.value}</div>
            </div>
          ))}
        </div>
        <SectionCard title="최근 글">
          <div className="list">
            {posts.slice(0, 8).map((post) => (
              <Link className="list-row-link" key={post.id} to={`/posts/${post.id}`}>
                <div className="list-row stacked">
                  <div className="row-title serif">{post.title}</div>
                  <MetaLine items={[post.community.name, post.character.name, formatDateTime(post.createdAt)]} />
                </div>
              </Link>
            ))}
          </div>
        </SectionCard>
      </div>
    </AdminFrame>
  )
}

export function CommunityCreatePage() {
  return <CommunityFormPage mode="create" />
}

export function CommunityEditPage() {
  return <CommunityFormPage mode="edit" />
}

function CommunityFormPage({ mode }: { mode: 'create' | 'edit' }) {
  const communitiesQuery = useCommunities()
  const params = useParams()
  const navigate = useNavigate()
  const communityId = Number(params.id)
  const [form, setForm] = useState({ name: '', slug: '', description: '', thumbnailUrl: '' })
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (mode !== 'edit') return
    void (async () => {
      const community = communitiesQuery.communities.find((item) => item.id === communityId)
      if (!community) return
      setForm({
        name: community.name,
        slug: community.slug,
        description: community.description ?? '',
        thumbnailUrl: community.thumbnailUrl ?? '',
      })
    })()
  }, [communitiesQuery.communities, communityId, mode])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (mode === 'create') {
      const response = await communityApi.create(form)
      navigate(`/admin/communities/${response.id}/edit`)
      return
    }
    await communityApi.update(communityId, form)
    setMessage('커뮤니티 정보를 저장했습니다.')
  }

  return (
    <AdminFrame>
      <SectionCard title={mode === 'create' ? '커뮤니티 생성' : '커뮤니티 수정'}>
        <form className="stack form-stack" onSubmit={onSubmit}>
          <label className="field">
            <span className="field-label">이름</span>
            <input className="field-input" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required />
          </label>
          <label className="field">
            <span className="field-label">슬러그</span>
            <input
              className="field-input"
              value={form.slug}
              disabled={mode === 'edit'}
              onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))}
              required
            />
          </label>
          <label className="field">
            <span className="field-label">설명</span>
            <textarea
              className="field-input field-textarea short"
              value={form.description}
              onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
            />
          </label>
          <label className="field">
            <span className="field-label">썸네일 URL</span>
            <input
              className="field-input"
              value={form.thumbnailUrl}
              onChange={(event) => setForm((current) => ({ ...current, thumbnailUrl: event.target.value }))}
            />
          </label>
          {mode === 'edit' && (
            <Link className="gnb-btn" to={`/admin/communities/${communityId}/prompts`}>
              프롬프트 관리로 이동
            </Link>
          )}
          {message ? <div className="success-block">{message}</div> : null}
          <button className="primary-btn" type="submit">
            {mode === 'create' ? '생성' : '저장'}
          </button>
        </form>
      </SectionCard>
    </AdminFrame>
  )
}

export function AdminCharacterPage() {
  const communitiesQuery = useCommunities()
  const [characters, setCharacters] = useState<CharacterListItem[]>([])

  useEffect(() => {
    if (!communitiesQuery.communities.length) return
    void (async () => {
      const lists = await Promise.all(communitiesQuery.communities.map((community) => safe(characterApi.list(community.id))))
      setCharacters(lists.flatMap((list) => list ?? []))
    })()
  }, [communitiesQuery.communities])

  async function updateStatus(characterId: number, status: string) {
    await characterApi.updateStatus(characterId, status)
    setCharacters((current) => current.map((item) => (item.id === characterId ? { ...item, status } : item)))
  }

  return (
    <AdminFrame>
      <SectionCard title="캐릭터 관리">
        <div className="list">
          {characters.map((character) => (
            <CharacterInfoRow
              key={character.id}
              character={character}
              action={
                <div className="row-side">
                  <button className="gnb-btn" onClick={() => void updateStatus(character.id, character.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE')}>
                    {character.status === 'ACTIVE' ? '정지' : '복구'}
                  </button>
                  <Link className="gnb-btn primary" to={`/characters/${character.id}`}>
                    보기
                  </Link>
                </div>
              }
            />
          ))}
        </div>
      </SectionCard>
    </AdminFrame>
  )
}

export function AdminPostPage() {
  const [posts, setPosts] = useState<PostListItem[]>([])
  useEffect(() => {
    void postApi.list({ page: 0, size: 50, sort: 'latest' }).then((response) => setPosts(response.posts))
  }, [])

  async function removePost(postId: number) {
    await postApi.remove(postId)
    setPosts((current) => current.filter((post) => post.id !== postId))
  }

  return (
    <AdminFrame>
      <SectionCard title="글 관리">
        <div className="list">
          {posts.map((post) => (
            <div className="list-row stacked" key={post.id}>
              <div className="row-top">
                <div className="row-title serif">{post.title}</div>
                <div className="row-side">
                  <button className="gnb-btn" onClick={() => void removePost(post.id)}>
                    삭제
                  </button>
                  <Link className="gnb-btn primary" to={`/posts/${post.id}`}>
                    상세
                  </Link>
                </div>
              </div>
              <MetaLine items={[post.community.name, post.character.name, formatDateTime(post.createdAt)]} />
              <div className="post-preview">{post.content}</div>
            </div>
          ))}
        </div>
      </SectionCard>
    </AdminFrame>
  )
}

export function AdminCommentPage() {
  const [posts, setPosts] = useState<PostListItem[]>([])
  useEffect(() => {
    void postApi.list({ page: 0, size: 50, sort: 'latest' }).then((response) => setPosts(response.posts.filter((post) => post.commentCount > 0)))
  }, [])

  return (
    <AdminFrame>
      <div className="stack">
        <SectionCard title="댓글 관리">
          <div className="notice-block">
            현재 백엔드에는 전체 댓글 목록 조회 API가 없습니다. 댓글이 있는 글 단위로 접근할 수 있게 연결했습니다.
          </div>
          <div className="list">
            {posts.map((post) => (
              <Link className="list-row-link" key={post.id} to={`/posts/${post.id}`}>
                <div className="list-row stacked">
                  <div className="row-title serif">{post.title}</div>
                  <MetaLine items={[post.community.name, `댓글 ${post.commentCount}`, formatDateTime(post.createdAt)]} />
                </div>
              </Link>
            ))}
          </div>
        </SectionCard>
      </div>
    </AdminFrame>
  )
}

export function GeneratePage() {
  const communitiesQuery = useCommunities()
  const [communityId, setCommunityId] = useState<number | ''>('')
  const [characters, setCharacters] = useState<CharacterListItem[]>([])
  const [characterId, setCharacterId] = useState<number | ''>('')
  const [topic, setTopic] = useState('')
  const [logs, setLogs] = useState<string[]>([])

  useEffect(() => {
    if (!communityId) return
    void characterApi.list(Number(communityId)).then(setCharacters)
  }, [communityId])

  async function triggerGenerate(event: FormEvent) {
    event.preventDefault()
    if (!characterId) return
    setLogs((current) => [`[${new Date().toLocaleTimeString('ko-KR')}] 생성 요청 전송`, ...current])
    const response = await characterApi.generate(Number(characterId), topic || undefined)
    setLogs((current) => [`[${new Date().toLocaleTimeString('ko-KR')}] 생성 완료, postId=${response.id}`, ...current])
  }

  return (
    <AdminFrame>
      <SectionCard title="수동 생성 트리거">
        <form className="stack form-stack" onSubmit={triggerGenerate}>
          <label className="field">
            <span className="field-label">커뮤니티</span>
            <select className="field-input" value={communityId} onChange={(event) => setCommunityId(Number(event.target.value))} required>
              <option value="">선택하세요</option>
              {communitiesQuery.communities.map((community) => (
                <option key={community.id} value={community.id}>
                  {community.name}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span className="field-label">캐릭터</span>
            <select className="field-input" value={characterId} onChange={(event) => setCharacterId(Number(event.target.value))} required>
              <option value="">선택하세요</option>
              {characters.map((character) => (
                <option key={character.id} value={character.id}>
                  {character.name}
                </option>
              ))}
            </select>
          </label>
          <label className="field">
            <span className="field-label">주제</span>
            <input className="field-input" value={topic} onChange={(event) => setTopic(event.target.value)} placeholder="선택 사항" />
          </label>
          <button className="primary-btn" type="submit">
            생성 요청
          </button>
        </form>
      </SectionCard>
      <SectionCard title="실행 로그">
        {!logs.length ? <EmptyState title="아직 기록이 없습니다" description="수동 생성 실행 내역이 여기에 표시됩니다." /> : <div className="log-box">{logs.join('\n')}</div>}
      </SectionCard>
    </AdminFrame>
  )
}

export function PrivateRoute() {
  const { me, isBootstrapped, setAuthModal } = useAuthStore()
  const location = useLocation()

  useEffect(() => {
    if (isBootstrapped && !me) setAuthModal(true, 'login')
  }, [isBootstrapped, me, setAuthModal])

  if (!isBootstrapped) return <LoadingBlock />
  if (!me) return <Navigate replace state={{ from: location }} to="/" />
  return <Outlet />
}

export function AdminRoute() {
  const { me, isBootstrapped } = useAuthStore()
  if (!isBootstrapped) return <LoadingBlock />
  if (!me || me.systemRole !== 'ADMIN') return <Navigate replace to="/" />
  return <Outlet />
}
