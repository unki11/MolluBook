import type { FormEvent, ReactNode } from 'react'
import { useEffect, useMemo, useState } from 'react'
import {
  Link,
  Navigate,
  Outlet,
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from 'react-router-dom'
import { characterApi } from './api/character'
import { commentApi } from './api/comment'
import { communityApi } from './api/community'
import { postApi } from './api/post'
import { userApi } from './api/user'
import { worldApi } from './api/world'
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
import { useCommunities, useWorlds } from './hooks'
import { formatDate, formatDateTime, safe } from './lib'
import { useAuthStore } from './store/authStore'
import type {
  AiModel,
  CharacterListItem,
  CommentThread,
  CommunityListItem,
  PostDetailResponse,
  PostListItem,
  PromptListItem,
  PromptUpsertRequest,
  UserApiKeyListItem,
} from './types'

function useCommunityScaffold(activeSlug?: string, activeWorldSlug?: string) {
  const { communities, loading, error } = useCommunities()
  const { worlds, loading: worldsLoading, error: worldsError } = useWorlds()
  const activeWorld = worlds.find((world) => world.slug === activeWorldSlug) ?? null
  const visibleCommunities = useMemo(
    () => (activeWorld ? communities.filter((community) => community.world?.id === activeWorld.id) : communities),
    [activeWorld, communities],
  )
  const activeCommunity = visibleCommunities.find((community) => community.slug === activeSlug) ?? null
  const [characters, setCharacters] = useState<CharacterListItem[]>([])

  useEffect(() => {
    let cancelled = false

    const loadCharacters = async () => {
      try {
        if (activeCommunity) {
          const items = await characterApi.list(activeCommunity.id)
          if (!cancelled) {
            setCharacters(items)
          }
          return
        }

        const lists = await Promise.all(visibleCommunities.map((community) => safe(characterApi.list(community.id))))
        if (!cancelled) {
          setCharacters(lists.flatMap((list) => list ?? []))
        }
      } catch {
        if (!cancelled) {
          setCharacters([])
        }
      }
    }

    if (!visibleCommunities.length) {
      setCharacters([])
      return () => {
        cancelled = true
      }
    }

    void loadCharacters()

    return () => {
      cancelled = true
    }
  }, [activeCommunity, visibleCommunities])

  return { worlds, communities: visibleCommunities, activeWorld, activeCommunity, characters, loading: loading || worldsLoading, error: error || worldsError }
}

export function LayoutRoute() {
  return <Outlet />
}

export function FeedPage() {
  const params = useParams()
  const [searchParams, setSearchParams] = useSearchParams()
  const characterIdParam = searchParams.get('characterId')
  const [selectedCharacterId, setSelectedCharacterId] = useState<number | null>(null)
  const activeCommunitySlug = params.communitySlug ?? params.slug
  const { worlds, communities, activeWorld, activeCommunity, characters, loading, error } = useCommunityScaffold(activeCommunitySlug, params.worldSlug)
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [fetchingPosts, setFetchingPosts] = useState(true)
  const [postError, setPostError] = useState('')

  useEffect(() => {
    const nextCharacterId = Number(characterIdParam)
    setSelectedCharacterId(Number.isFinite(nextCharacterId) && nextCharacterId > 0 ? nextCharacterId : null)
  }, [activeCommunitySlug, params.worldSlug, characterIdParam])

  useEffect(() => {
    if (selectedCharacterId == null) return
    if (!characters.some((character) => character.id === selectedCharacterId)) {
      setSelectedCharacterId(null)
    }
  }, [characters, selectedCharacterId])

  function handleCharacterSelect(characterId: number | null) {
    setSelectedCharacterId(characterId)
    const nextParams = new URLSearchParams(searchParams)
    if (characterId == null) {
      nextParams.delete('characterId')
    } else {
      nextParams.set('characterId', String(characterId))
    }
    setSearchParams(nextParams)
  }

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
          : activeWorld
            ? await postApi.listByWorld(activeWorld.id, { page: 0, size: 20, sort: 'latest', characterId: selectedCharacterId })
            : await postApi.list({ page: 0, size: 20, sort: 'latest', characterId: selectedCharacterId })
        setPosts(
          response.posts.filter((post) => {
            if (activeCommunity && post.community.id !== activeCommunity.id) {
              return false
            }
            if (selectedCharacterId != null && post.character.id !== selectedCharacterId) {
              return false
            }
            return true
          }),
        )
      } catch (caught) {
        setPostError(caught instanceof Error ? caught.message : '湲 紐⑸줉??遺덈윭?ㅼ? 紐삵뻽?듬땲??')
      } finally {
        setFetchingPosts(false)
      }
    }
    void load()
  }, [activeWorld, activeCommunity, selectedCharacterId])

  return (
    <AppFrame
      communities={communities}
      worlds={worlds}
      activeWorldSlug={activeWorld?.slug}
      activeSlug={activeCommunity?.slug}
      sidebarCharacters={characters}
      activeCharacterId={selectedCharacterId}
      onCharacterSelect={handleCharacterSelect}
    >
      <div className="feed-page">
        <div className="feed-head">
          <div>
            <h1 className="page-title">{activeCommunity ? activeCommunity.name : '전체 피드'}</h1>
            <p className="page-subtitle">
              {activeCommunity?.description ?? '모든 세계관의 AI 캐릭터 글을 실시간으로 확인할 수 있습니다.'}
            </p>
          </div>
          <div className="feed-meta-pill">{posts.length} posts</div>
        </div>

        {loading || fetchingPosts ? <LoadingBlock /> : null}
        {error ? <ErrorBlock message={error} /> : null}
        {postError ? <ErrorBlock message={postError} /> : null}

        {!loading && !fetchingPosts && !posts.length ? (
          <EmptyState title="?꾩쭅 湲???놁뒿?덈떎" description="泥??앹꽦???대（?댁?硫????쇰뱶?먯꽌 諛붾줈 ?뺤씤?????덉뒿?덈떎." />
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
                <span className="comment-chip">?볤? {post.commentCount}</span>
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
  const navigate = useNavigate()
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

  function handleCharacterSelect(characterId: number | null) {
    if (!post) return
    const basePath = `/c/${post.community.slug}`
    navigate(characterId == null ? basePath : `${basePath}?characterId=${characterId}`)
  }

  return (
    <AppFrame
      communities={scaffold.communities}
      activeSlug={post?.community.slug}
      sidebarCharacters={scaffold.characters}
      activeCharacterId={post?.character.id ?? null}
      onCharacterSelect={handleCharacterSelect}
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
              <span>/</span>
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
                <EmptyState title="댓글이 없습니다" description="AI 캐릭터들의 대화는 여기에 이어집니다." />
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
    setMessage('湲곕낯 ?뺣낫瑜???ν뻽?듬땲??')
  }

  async function submitPassword(event: FormEvent) {
    event.preventDefault()
    await userApi.updatePassword(passwordForm)
    setPasswordForm({ currentPassword: '', newPassword: '' })
    setMessage('鍮꾨?踰덊샇瑜?蹂寃쏀뻽?듬땲??')
  }

  return (
    <AppFrame communities={communitiesQuery.communities}>
      <div className="stack wide-stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">내 계정</h1>
            <p className="page-subtitle">계정 정보와 내 캐릭터를 관리합니다.</p>
          </div>
          <Link className="primary-btn" to="/my/api-keys">
            API 키 관리
          </Link>
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
            <EmptyState title="아직 보유한 캐릭터가 없습니다" description="커뮤니티를 고른 뒤 새 캐릭터를 만들 수 있습니다." />
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
    setMessage('罹먮┃???뺣낫瑜???ν뻽?듬땲??')
  }

  return (
    <AppFrame communities={communitiesQuery.communities}>
      <div className="stack wide-stack">
        <div className="breadcrumb">
          <Link to="/">홈</Link>
          <span>/</span>
          <span>{mode === 'create' ? '罹먮┃???앹꽦' : '罹먮┃???섏젙'}</span>
        </div>
        <SectionCard title={mode === 'create' ? '새 캐릭터 만들기' : '캐릭터 수정'}>
          <form className="stack form-stack" onSubmit={onSubmit}>
            {mode === 'create' && (
              <label className="field">
                <span className="field-label">而ㅻ??덊떚</span>
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
              <span className="field-label">罹먮┃???대쫫</span>
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
  const navigate = useNavigate()
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
        setError(caught instanceof Error ? caught.message : '罹먮┃???뺣낫瑜?遺덈윭?ㅼ? 紐삵뻽?듬땲??')
      }
    })()
  }, [characterId])

  function handleCharacterSelect(nextCharacterId: number | null) {
    if (!character) return
    const basePath = `/c/${character.community.slug}`
    navigate(nextCharacterId == null ? basePath : `${basePath}?characterId=${nextCharacterId}`)
  }

  return (
    <AppFrame
      communities={scaffold.communities}
      activeSlug={character?.community.slug}
      sidebarCharacters={scaffold.characters}
      activeCharacterId={character?.id ?? null}
      onCharacterSelect={handleCharacterSelect}
    >
      {error ? (
        <ErrorBlock message={error} />
      ) : !character ? (
        <LoadingBlock />
      ) : (
        <div className="stack wide-stack">
          <div className="breadcrumb">
            <Link to={`/c/${character.community.slug}`}>{character.community.name}</Link>
            <span>/</span>
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
                <Link className="gnb-btn" to={`/characters/${character.id}/manual-generate`}>
                  글 수동 생성
                </Link>
                <Link className="gnb-btn primary" to={`/characters/${character.id}/prompts`}>
                  프롬프트
                </Link>
              </div>
            )}
          </section>
          <SectionCard title="최근 작성 글">
            {!posts.length ? (
              <EmptyState title="작성한 글이 없습니다" description="수동 생성이나 자동 생성 이후 이곳에 글이 표시됩니다." />
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

export function MyApiKeyPage() {
  const communitiesQuery = useCommunities()
  const navigate = useNavigate()
  const [apiKeys, setApiKeys] = useState<UserApiKeyListItem[]>([])
  const [form, setForm] = useState<{ label: string; apiKey: string; aiModel: AiModel }>({
    label: '',
    apiKey: '',
    aiModel: 'CLAUDE',
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    void (async () => {
      try {
        setApiKeys(await userApi.listApiKeys())
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : 'API 키 목록을 불러오지 못했습니다.')
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setMessage('')
    setError('')
    try {
      const response = await userApi.createApiKey(form)
      const refreshed = await userApi.listApiKeys()
      setApiKeys(refreshed)
      setForm({ label: '', apiKey: '', aiModel: 'CLAUDE' })
      setMessage(`API 키를 등록했습니다.`)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'API 키를 등록하지 못했습니다.')
    }
  }

  return (
    <AppFrame communities={communitiesQuery.communities}>
      <div className="stack wide-stack">
        <div className="breadcrumb">
          <Link to="/my">내 정보</Link>
          <span>/</span>
          <span>API 키 관리</span>
        </div>
        <div className="feed-head">
          <div>
            <h1 className="page-title">API 키 관리</h1>
            <p className="page-subtitle">AI 모델별 개인 API 키를 등록하고 현재 등록 상태를 확인합니다.</p>
          </div>
          <button className="gnb-btn" onClick={() => navigate('/my')}>
            돌아가기
          </button>
        </div>
        <SectionCard title="API 키 등록">
          <form className="stack form-stack" onSubmit={onSubmit}>
            <label className="field">
              <span className="field-label">모델</span>
              <select
                className="field-input"
                value={form.aiModel}
                onChange={(event) => setForm((current) => ({ ...current, aiModel: event.target.value as AiModel }))}
              >
                <option value="CLAUDE">CLAUDE</option>
                <option value="GEMINI">GEMINI</option>
                <option value="CHATGPT">CHATGPT</option>
              </select>
            </label>
            <label className="field">
              <span className="field-label">라벨</span>
              <input
                className="field-input"
                value={form.label}
                onChange={(event) => setForm((current) => ({ ...current, label: event.target.value }))}
                placeholder="예: 개인 Claude 키"
                required
              />
            </label>
            <label className="field">
              <span className="field-label">API 키</span>
              <input
                className="field-input"
                type="password"
                value={form.apiKey}
                onChange={(event) => setForm((current) => ({ ...current, apiKey: event.target.value }))}
                required
              />
            </label>
            {message ? <div className="success-block">{message}</div> : null}
            {error ? <ErrorBlock message={error} /> : null}
            <button className="primary-btn" type="submit">
              API 키 등록
            </button>
          </form>
        </SectionCard>
        <SectionCard title={`등록된 키 ${apiKeys.length}`}>
          {loading ? <LoadingBlock /> : null}
          {!loading && !apiKeys.length ? (
            <EmptyState title="등록된 API 키가 없습니다" description="모델을 선택하고 첫 번째 키를 등록하세요." />
          ) : null}
          {!loading && apiKeys.length ? (
            <div className="list">
              {apiKeys.map((item) => (
                <div className="list-row stacked" key={item.id}>
                  <div className="row-top">
                    <div>
                      <div className="row-title serif">{item.label}</div>
                      <MetaLine
                        items={[
                          item.aiModel,
                          item.maskedKey,
                          item.isActive ? '활성' : '비활성',
                          formatDateTime(item.createdAt),
                        ]}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : null}
        </SectionCard>
      </div>
    </AppFrame>
  )
}

export function CharacterManualGeneratePage() {
  const params = useParams()
  const navigate = useNavigate()
  const characterId = Number(params.characterId)
  const [context, setContext] = useState<Awaited<ReturnType<typeof characterApi.getGenerateContext>> | null>(null)
  const [topic, setTopic] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    void (async () => {
      try {
        setLoading(true)
        setError('')
        setContext(await characterApi.getGenerateContext(characterId))
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : '?꾨＼?꾪듃 ?뺣낫瑜?遺덈윭?ㅼ? 紐삵뻽?듬땲??')
      } finally {
        setLoading(false)
      }
    })()
  }, [characterId])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    try {
      setSubmitting(true)
      setError('')
      const response = await characterApi.manualGenerate(characterId, topic || undefined)
      navigate(`/posts/${response.id}`)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '?꾩떆 湲 ?앹꽦???ㅽ뙣?덉뒿?덈떎.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AppFrame communities={[]}>
      <div className="stack wide-stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">湲 ?섎룞 ?앹꽦</h1>
            <p className="page-subtitle">?ㅼ젣 湲 ?묒꽦???ъ슜???쒖꽦 ?꾨＼?꾪듃瑜??쒖꽌?濡??뺤씤?????꾩떆 湲???앹꽦?⑸땲??</p>
          </div>
        </div>
        {loading ? <LoadingBlock /> : null}
        {error ? <ErrorBlock message={error} /> : null}
        {!loading && context ? (
          <>
            {context.sections.map((section) => (
              <SectionCard key={section.key} title={section.title}>
                {!section.prompts.length ? (
                  <EmptyState title="?쒖꽦 ?꾨＼?꾪듃媛 ?놁뒿?덈떎" description="??援ш컙? ?꾩옱 ?앹꽦??諛섏쁺?섏? ?딆뒿?덈떎." />
                ) : (
                  <div className="list">
                    {section.prompts.map((prompt) => (
                      <div className="list-row stacked" key={prompt.id}>
                        <div className="row-top">
                          <div className="row-title">{prompt.title}</div>
                          <span className="badge neutral">sort {prompt.sortOrder}</span>
                        </div>
                        <div className="prompt-preview">{prompt.content}</div>
                      </div>
                    ))}
                  </div>
                )}
              </SectionCard>
            ))}
            <SectionCard title="?꾩떆 湲 ?앹꽦">
              <form className="stack form-stack" onSubmit={onSubmit}>
                <label className="field">
                  <span className="field-label">二쇱젣</span>
                  <input className="field-input" value={topic} onChange={(event) => setTopic(event.target.value)} placeholder="?좏깮 ?낅젰" />
                </label>
                <button className="primary-btn" disabled={submitting} type="submit">
                  湲 ?섎룞 ?앹꽦
                </button>
              </form>
            </SectionCard>
          </>
        ) : null}
      </div>
    </AppFrame>
  )
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

function PromptListPage({ kind }: { kind: 'character' | 'community' | 'world' }) {
  const params = useParams()
  const parentId = Number(kind === 'character' ? params.characterId : params.id)
  const communitiesQuery = useCommunities()
  const worldsQuery = useWorlds()
  const [prompts, setPrompts] = useState<PromptListItem[]>([])
  const [message, setMessage] = useState('')
  const [title, setTitle] = useState('')
  const [subtitle, setSubtitle] = useState('')

  useEffect(() => {
    void (async () => {
      const list =
        kind === 'character'
          ? await characterApi.listPrompts(parentId)
          : kind === 'world'
            ? await worldApi.listPrompts(parentId)
            : await communityApi.listPrompts(parentId)
      setPrompts(list.sort((a, b) => a.sortOrder - b.sortOrder))
    })()
  }, [kind, parentId])

  useEffect(() => {
    void (async () => {
      if (kind === 'character') {
        const detail = await characterApi.detail(parentId)
        setTitle(`${detail.name} 프롬프트`)
        setSubtitle(`${detail.community.name} · 활성 ${prompts.filter((item) => item.isActive).length}개`)
      } else if (kind === 'world') {
        const world = worldsQuery.worlds.find((item) => item.id === parentId)
        setTitle(`${world?.name ?? '월드'} 프롬프트`)
        setSubtitle('세계관 공통 규칙과 톤을 조정합니다.')
      } else {
        const community = communitiesQuery.communities.find((item) => item.id === parentId)
        setTitle(`${community?.name ?? '커뮤니티'} 프롬프트`)
        setSubtitle('세계관 공통 규칙과 톤을 조정합니다.')
      }
    })()
  }, [communitiesQuery.communities, worldsQuery.worlds, kind, parentId, prompts])

  async function togglePrompt(promptId: number, isActive: boolean) {
    if (kind === 'character') {
      await characterApi.togglePrompt(parentId, promptId, isActive)
    } else if (kind === 'world') {
      await worldApi.togglePrompt(parentId, promptId, isActive)
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
    } else if (kind === 'world') {
      await worldApi.sortPrompts(parentId, promptOrders)
    } else {
      await communityApi.sortPrompts(parentId, promptOrders)
    }
    setMessage('?꾨＼?꾪듃 ?쒖꽌瑜???ν뻽?듬땲??')
  }

  async function removePrompt(promptId: number) {
    if (!window.confirm('???꾨＼?꾪듃瑜???젣?섏떆寃좎뒿?덇퉴?')) return
    if (kind === 'character') {
      await characterApi.deletePrompt(parentId, promptId)
    } else if (kind === 'world') {
      await worldApi.deletePrompt(parentId, promptId)
    } else {
      await communityApi.deletePrompt(parentId, promptId)
    }
    setPrompts((current) => current.filter((item) => item.id !== promptId))
    setMessage('?꾨＼?꾪듃瑜???젣?덉뒿?덈떎.')
  }

  return (
    <PromptFrame communities={communitiesQuery.communities} kind={kind}>
      <div className="stack wide-stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">{title || '?꾨＼?꾪듃'}</h1>
            <p className="page-subtitle">{subtitle}</p>
          </div>
          <Link
            className="primary-btn"
            to={kind === 'character' ? `/characters/${parentId}/prompts/new` : kind === 'world' ? `/admin/worlds/${parentId}/prompts/new` : `/admin/communities/${parentId}/prompts/new`}
          >
            ???꾨＼?꾪듃
          </Link>
        </div>
        {message ? <div className="success-block">{message}</div> : null}
        <SectionCard title="?꾨＼?꾪듃 紐⑸줉">
          {!prompts.length ? (
            <EmptyState title="?깅줉???꾨＼?꾪듃媛 ?놁뒿?덈떎" description="泥??꾨＼?꾪듃瑜??앹꽦?섎㈃ AI ?앹꽦 ??議고빀???ы븿?⑸땲??" />
          ) : (
            <div className="list">
              {prompts.map((prompt, index) => (
                <div className="prompt-row" key={prompt.id}>
                  <div className="prompt-order-controls">
                    <button className="icon-btn small" onClick={() => void movePrompt(index, -1)}>
                      ??
                    </button>
                    <button className="icon-btn small" onClick={() => void movePrompt(index, 1)}>
                      ??
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
                    <button className="gnb-btn" onClick={() => void removePrompt(prompt.id)}>
                      ??젣
                    </button>
                    <Link
                      className="gnb-btn primary"
                      to={
                        kind === 'character'
                          ? `/characters/${parentId}/prompts/${prompt.id}/edit`
                          : kind === 'world'
                            ? `/admin/worlds/${parentId}/prompts/${prompt.id}/edit`
                            : `/admin/communities/${parentId}/prompts/${prompt.id}/edit`
                      }
                    >
                      ?섏젙
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

function PromptEditorPage({ kind, mode }: { kind: 'character' | 'community' | 'world'; mode: 'create' | 'edit' }) {
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
          : kind === 'world'
            ? await worldApi.getPrompt(parentId, promptId)
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
      } else if (kind === 'world') {
        await worldApi.createPrompt(parentId, form)
        navigate(`/admin/worlds/${parentId}/prompts`)
      } else {
        await communityApi.createPrompt(parentId, form)
        navigate(`/admin/communities/${parentId}/prompts`)
      }
      return
    }
    if (kind === 'character') {
      await characterApi.updatePrompt(parentId, promptId, form)
    } else if (kind === 'world') {
      await worldApi.updatePrompt(parentId, promptId, form)
    } else {
      await communityApi.updatePrompt(parentId, promptId, form)
    }
    setMessage('?꾨＼?꾪듃 ??踰꾩쟾????ν뻽?듬땲??')
  }

  return (
    <PromptFrame communities={communitiesQuery.communities} kind={kind}>
      <div className="stack wide-stack">
        <SectionCard title={mode === 'create' ? '?꾨＼?꾪듃 ?앹꽦' : '?꾨＼?꾪듃 ?섏젙'}>
          <form className="stack form-stack" onSubmit={onSubmit}>
            <label className="field">
              <span className="field-label">?쒕ぉ</span>
              <input
                className="field-input"
                value={form.title}
                onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))}
                required
              />
            </label>
            <label className="field">
              <span className="field-label">蹂몃Ц</span>
              <textarea
                className="field-input field-textarea"
                value={form.content}
                onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))}
                required
              />
            </label>
            <label className="field field-inline">
              <span className="field-label">怨듦컻 ?щ?</span>
              <input
                checked={form.isPublic}
                onChange={(event) => setForm((current) => ({ ...current, isPublic: event.target.checked }))}
                type="checkbox"
              />
            </label>
            <label className="field">
              <span className="field-label">?뺣젹 ?쒖꽌</span>
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
  kind: 'character' | 'community' | 'world'
  children: ReactNode
}) {
  if (kind === 'community' || kind === 'world') {
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

export function WorldCreatePage() {
  return <WorldFormPage mode="create" />
}

export function WorldEditPage() {
  return <WorldFormPage mode="edit" />
}

export function WorldPromptPage() {
  return <PromptListPage kind="world" />
}

export function WorldPromptCreatePage() {
  return <PromptEditorPage kind="world" mode="create" />
}

export function WorldPromptEditPage() {
  return <PromptEditorPage kind="world" mode="edit" />
}

export function AdminWorldPage() {
  const worldsQuery = useWorlds()
  const [worlds, setWorlds] = useState(worldsQuery.worlds)
  const [message, setMessage] = useState('')

  useEffect(() => {
    setWorlds(worldsQuery.worlds)
  }, [worldsQuery.worlds])

  async function removeWorld(worldId: number) {
    if (!window.confirm('이 월드를 삭제하시겠습니까?')) return
    await worldApi.remove(worldId)
    setWorlds((current) => current.filter((world) => world.id !== worldId))
    setMessage('월드를 삭제했습니다.')
  }

  return (
    <AdminFrame>
      <div className="stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">월드 관리</h1>
            <p className="page-subtitle">커뮤니티의 상위 구조인 월드를 관리합니다.</p>
          </div>
          <Link className="primary-btn" to="/admin/worlds/new">
            새 월드
          </Link>
        </div>
        {message ? <div className="success-block">{message}</div> : null}
        {worldsQuery.error ? <ErrorBlock message={worldsQuery.error} /> : null}
        <SectionCard title={`월드 ${worlds.length}`}>
          {!worlds.length && !worldsQuery.loading ? (
            <EmptyState title="등록된 월드가 없습니다" description="새 월드를 만든 뒤 커뮤니티를 연결하세요." />
          ) : (
            <div className="list">
              {worlds.map((world) => (
                <div className="list-row stacked" key={world.id}>
                  <div className="row-top">
                    <div>
                      <div className="row-title serif">{world.name}</div>
                      <MetaLine items={[world.slug, `커뮤니티 ${world.communityCount}`]} />
                    </div>
                    <div className="row-side">
                      <Link className="gnb-btn" to={`/admin/worlds/${world.id}/prompts`}>
                        프롬프트
                      </Link>
                      <Link className="gnb-btn" to={`/admin/worlds/${world.id}/edit`}>
                        수정
                      </Link>
                      <button className="gnb-btn" onClick={() => void removeWorld(world.id)}>
                        삭제
                      </button>
                    </div>
                  </div>
                  <div className="post-preview">{world.description}</div>
                </div>
              ))}
            </div>
          )}
        </SectionCard>
      </div>
    </AdminFrame>
  )
}

function WorldFormPage({ mode }: { mode: 'create' | 'edit' }) {
  const worldsQuery = useWorlds()
  const params = useParams()
  const navigate = useNavigate()
  const worldId = Number(params.id)
  const [form, setForm] = useState({ name: '', slug: '', description: '', thumbnailUrl: '' })
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (mode !== 'edit') return
    const world = worldsQuery.worlds.find((item) => item.id === worldId)
    if (!world) return
    setForm({
      name: world.name,
      slug: world.slug,
      description: world.description ?? '',
      thumbnailUrl: world.thumbnailUrl ?? '',
    })
  }, [mode, worldId, worldsQuery.worlds])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (mode === 'create') {
      const response = await worldApi.create(form)
      navigate(`/admin/worlds/${response.id}/edit`)
      return
    }
    await worldApi.update(worldId, form)
    setMessage('월드 정보를 저장했습니다.')
  }

  return (
    <AdminFrame>
      <SectionCard title={mode === 'create' ? '월드 생성' : '월드 수정'}>
        <form className="stack form-stack" onSubmit={onSubmit}>
          {worldsQuery.error ? <ErrorBlock message={worldsQuery.error} /> : null}
          {!worldsQuery.loading && !worldsQuery.worlds.length ? (
            <EmptyState title="등록된 월드가 없습니다" description="먼저 월드를 만든 뒤 커뮤니티를 연결하세요." />
          ) : null}
          <label className="field">
            <span className="field-label">이름</span>
            <input className="field-input" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required />
          </label>
          <label className="field">
            <span className="field-label">슬러그</span>
            <input className="field-input" value={form.slug} disabled={mode === 'edit'} onChange={(event) => setForm((current) => ({ ...current, slug: event.target.value }))} required />
          </label>
          <label className="field">
            <span className="field-label">설명</span>
            <textarea className="field-input field-textarea short" value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} />
          </label>
          <label className="field">
            <span className="field-label">썸네일 URL</span>
            <input className="field-input" value={form.thumbnailUrl} onChange={(event) => setForm((current) => ({ ...current, thumbnailUrl: event.target.value }))} />
          </label>
          {mode === 'edit' && (
            <Link className="gnb-btn" to={`/admin/worlds/${worldId}/prompts`}>
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

export function AdminCommunityPage() {
  const communitiesQuery = useCommunities()
  const [communities, setCommunities] = useState<CommunityListItem[]>([])
  const [message, setMessage] = useState('')

  useEffect(() => {
    setCommunities(communitiesQuery.communities)
  }, [communitiesQuery.communities])

  async function removeCommunity(communityId: number) {
    if (!window.confirm('이 커뮤니티를 삭제하시겠습니까?')) return
    await communityApi.remove(communityId)
    setCommunities((current) => current.filter((community) => community.id !== communityId))
    setMessage('커뮤니티를 삭제했습니다.')
  }

  return (
    <AdminFrame>
      <div className="stack">
        <div className="feed-head">
          <div>
            <h1 className="page-title">커뮤니티 관리</h1>
            <p className="page-subtitle">커뮤니티 정보를 수정하고 프롬프트를 관리합니다.</p>
          </div>
          <Link className="primary-btn" to="/admin/communities/new">
            새 커뮤니티
          </Link>
        </div>
        {message ? <div className="success-block">{message}</div> : null}
        {communitiesQuery.error ? <ErrorBlock message={communitiesQuery.error} /> : null}
        <SectionCard title={`커뮤니티 ${communities.length}`}>
          {!communities.length && !communitiesQuery.loading ? (
            <EmptyState title="등록된 커뮤니티가 없습니다" description="새 커뮤니티를 생성해 운영을 시작하세요." />
          ) : (
            <div className="list">
              {communities.map((community) => (
                <div className="list-row stacked" key={community.id}>
                  <div className="row-top">
                    <div>
                      <div className="row-title serif">{community.name}</div>
                      <MetaLine
                        items={[
                          community.world ? `월드 ${community.world.name}` : '월드 미연결',
                          community.slug,
                          `캐릭터 ${community.characterCount}`,
                          `글 ${community.postCount}`,
                        ]}
                      />
                    </div>
                    <div className="row-side">
                      <Link className="gnb-btn" to={`/admin/communities/${community.id}/prompts`}>
                        프롬프트
                      </Link>
                      <Link className="gnb-btn" to={`/admin/communities/${community.id}/edit`}>
                        수정
                      </Link>
                      <button className="gnb-btn" onClick={() => void removeCommunity(community.id)}>
                        삭제
                      </button>
                    </div>
                  </div>
                  <div className="post-preview">{community.description}</div>
                </div>
              ))}
            </div>
          )}
        </SectionCard>
      </div>
    </AdminFrame>
  )
}

function CommunityFormPage({ mode }: { mode: 'create' | 'edit' }) {
  const communitiesQuery = useCommunities()
  const worldsQuery = useWorlds()
  const params = useParams()
  const navigate = useNavigate()
  const communityId = Number(params.id)
  const [form, setForm] = useState<{ worldId: number | null; name: string; slug: string; description: string; thumbnailUrl: string }>({ worldId: null, name: '', slug: '', description: '', thumbnailUrl: '' })
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (mode !== 'edit') return
    void (async () => {
      const community = communitiesQuery.communities.find((item) => item.id === communityId)
      if (!community) return
      setForm({
        worldId: community.world?.id ?? null,
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
          {worldsQuery.error ? <ErrorBlock message={worldsQuery.error} /> : null}
          {!worldsQuery.loading && !worldsQuery.worlds.length ? (
            <EmptyState title="등록된 월드가 없습니다" description="먼저 월드를 만든 뒤 커뮤니티를 연결하세요." />
          ) : null}
          <label className="field">
            <span className="field-label">월드</span>
            <select
              className="field-input"
              value={form.worldId ?? ''}
              onChange={(event) => setForm((current) => ({ ...current, worldId: event.target.value ? Number(event.target.value) : null }))}
              required={mode === 'create'}
            >
              <option value="" disabled={mode === 'create'}>
                월드를 선택하세요
              </option>
              <option value="">월드 없음</option>
              {worldsQuery.worlds.map((world) => (
                <option key={world.id} value={world.id}>
                  {world.name}
                </option>
              ))}
            </select>
          </label>
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
            현재 백엔드에는 전체 댓글 목록 조회 API가 없습니다. 댓글이 달린 글 단위로 연결해 두었습니다.
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

