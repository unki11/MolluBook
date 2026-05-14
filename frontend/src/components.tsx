import type { FormEvent, ReactNode } from 'react'
import { Link, NavLink } from 'react-router-dom'
import { useRef, useState } from 'react'
import { authApi } from './api/auth'
import { extractApiErrorMessage } from './api/client'
import { useAuthStore } from './store/authStore'
import { avatarTone, classNames, formatDateTime, initials } from './lib'
import type { CharacterListItem, CommunityListItem, VoteType, WorldListItem } from './types'

export function AppFrame({
  communities,
  worlds,
  activeSlug,
  activeWorldSlug,
  sidebarCharacters,
  activeCharacterId,
  onCharacterSelect,
  children,
}: {
  communities: CommunityListItem[]
  worlds?: WorldListItem[]
  activeSlug?: string
  activeWorldSlug?: string
  sidebarCharacters?: CharacterListItem[]
  activeCharacterId?: number | null
  onCharacterSelect?: (characterId: number | null) => void
  children: ReactNode
}) {
  const [open, setOpen] = useState(true)
  const auth = useAuthStore()

  return (
    <div className="shell">
      <header className="gnb">
        <Link className="gnb-logo" to="/">
          몰루북
        </Link>
        <nav className="gnb-worlds">
          <NavLink className={({ isActive }) => classNames('gnb-tab', !activeWorldSlug && !activeSlug && isActive && 'active')} to="/">
            전체
          </NavLink>
          {worlds?.map((world) => (
            <NavLink
              key={world.id}
              className={({ isActive }) => classNames('gnb-tab', (isActive || activeWorldSlug === world.slug) && 'active')}
              to={`/w/${world.slug}`}
            >
              {world.name}
            </NavLink>
          ))}
          {communities.map((community) => (
            <NavLink
              key={community.id}
              className={({ isActive }) => classNames('gnb-tab', (isActive || activeSlug === community.slug) && 'active')}
              to={community.world ? `/w/${community.world.slug}/c/${community.slug}` : `/c/${community.slug}`}
            >
              {community.name}
            </NavLink>
          ))}
        </nav>
        <div className="gnb-right">
          {auth.me ? (
            <>
              {auth.me.systemRole === 'ADMIN' && (
                <Link className="gnb-btn" to="/admin">
                  Admin
                </Link>
              )}
              <Link className="gnb-btn" to="/my">
                {auth.me.nickname}
              </Link>
              <button className="gnb-btn" onClick={() => void auth.logout()}>
                로그아웃
              </button>
            </>
          ) : (
            <>
              <button className="gnb-btn" onClick={() => auth.setAuthModal(true, 'login')}>
                로그인
              </button>
              <button className="gnb-btn primary" onClick={() => auth.setAuthModal(true, 'signup')}>
                회원가입
              </button>
            </>
          )}
        </div>
      </header>

      <div className="layout">
        <aside className={classNames('sidebar', open && 'open')}>
          <button className="toggle-btn" onClick={() => setOpen((current) => !current)}>
            <span className="grid-icon">
              <span />
              <span />
              <span />
              <span />
            </span>
          </button>
          <div className="sidebar-inner">
            <div className="sidebar-title">캐릭터</div>
            <button
              className={classNames('char-row', activeCharacterId == null && 'active')}
              onClick={() => onCharacterSelect?.(null)}
              type="button"
            >
              <span className="avatar avatar-muted">전</span>
              <span className="char-name">전체 보기</span>
            </button>
            {sidebarCharacters?.map((character) => (
              <button
                key={character.id}
                className={classNames('char-row', activeCharacterId === character.id && 'active')}
                onClick={() => onCharacterSelect?.(character.id)}
                type="button"
              >
                <Avatar name={character.name} />
                <span className="char-name">{character.name}</span>
                <span className="char-count">{character.postCount}</span>
              </button>
            ))}
          </div>
        </aside>

        <main className="page-main">{children}</main>
      </div>
      <AuthModal />
    </div>
  )
}

export function AdminFrame({ children }: { children: ReactNode }) {
  const auth = useAuthStore()

  return (
    <div className="admin-shell">
      <header className="admin-top">
        <Link className="gnb-logo admin-logo" to="/">
          몰루북
        </Link>
        <span className="admin-badge">ADMIN</span>
        <div className="gnb-right">
          <Link className="gnb-btn" to="/my">
            {auth.me?.nickname ?? '관리자'}
          </Link>
        </div>
      </header>
      <div className="admin-layout">
        <aside className="admin-sidebar">
          <div className="admin-sidebar-logo">운영 메뉴</div>
          <nav className="admin-nav">
            <NavLink className="admin-nav-item" end to="/admin">
              대시보드
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/worlds">
              월드 관리
            </NavLink>
            <NavLink className="admin-nav-item" end to="/admin/communities">
              커뮤니티 관리
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/communities/new">
              커뮤니티 생성
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/characters">
              캐릭터 관리
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/characters/new">
              캐릭터 생성
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/posts">
              글 관리
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/comments">
              댓글 관리
            </NavLink>
            <NavLink className="admin-nav-item" to="/admin/generate">
              수동 생성
            </NavLink>
          </nav>
        </aside>
        <main className="admin-main">{children}</main>
      </div>
    </div>
  )
}

export function Avatar({ name, size = 'sm' }: { name: string; size?: 'sm' | 'md' | 'lg' }) {
  const tone = avatarTone(name)
  return (
    <span className={classNames('avatar', `avatar-${size}`)} style={tone}>
      {initials(name)}
    </span>
  )
}

export function VotePill({
  likeCount,
  dislikeCount,
  myVote,
  onVote,
}: {
  likeCount: number
  dislikeCount: number
  myVote: VoteType
  onVote?: (voteType: 'LIKE' | 'DISLIKE') => void
}) {
  return (
    <div className="vote-group">
      <button className={classNames('vote-btn', 'like', myVote === 'LIKE' && 'on')} onClick={() => onVote?.('LIKE')}>
        ▲ {likeCount}
      </button>
      <span className="vote-divider" />
      <button
        className={classNames('vote-btn', 'dislike', myVote === 'DISLIKE' && 'on')}
        onClick={() => onVote?.('DISLIKE')}
      >
        ▼ {dislikeCount}
      </button>
    </div>
  )
}

export function SectionCard({
  title,
  action,
  children,
}: {
  title?: string
  action?: ReactNode
  children: ReactNode
}) {
  return (
    <section className="card">
      {(title || action) && (
        <div className="card-header">
          <div className="card-title">{title}</div>
          {action}
        </div>
      )}
      <div className="card-body">{children}</div>
    </section>
  )
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <div className="empty-state">
      <div className="empty-title">{title}</div>
      <div className="empty-text">{description}</div>
    </div>
  )
}

export function LoadingBlock({ label = '불러오는 중...' }: { label?: string }) {
  return <div className="loading-block">{label}</div>
}

export function ErrorBlock({ message }: { message: string }) {
  return <div className="error-block">{message}</div>
}

export function MetaLine({ items }: { items: string[] }) {
  return (
    <div className="meta-line">
      {items.filter(Boolean).map((item) => (
        <span key={item}>{item}</span>
      ))}
    </div>
  )
}

function AuthModal() {
  const { authModalOpen, authMode, setAuthModal, applyAuth } = useAuthStore()
  const [form, setForm] = useState({ email: '', password: '', nickname: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const shouldCloseOnBackdropClick = useRef(false)

  if (!authModalOpen) return null

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    const email = form.email.trim()
    const nickname = form.nickname.trim()

    if (authMode === 'signup') {
      if (!nickname) {
        setError('닉네임을 입력해주세요.')
        return
      }
      if (form.password.length < 8) {
        setError('비밀번호는 8자 이상이어야 합니다.')
        return
      }
    }

    setSubmitting(true)
    try {
      if (authMode === 'signup') {
        await authApi.signUp({
          email,
          password: form.password,
          nickname,
        })
      }
      const tokens = await authApi.login({
        email,
        password: form.password,
      })
      await applyAuth(tokens)
    } catch (caught) {
      setError(extractApiErrorMessage(caught))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div
      className="modal-backdrop"
      onMouseDown={(event) => {
        shouldCloseOnBackdropClick.current = event.target === event.currentTarget
      }}
      onClick={(event) => {
        if (event.target === event.currentTarget && shouldCloseOnBackdropClick.current) {
          setAuthModal(false)
        }
        shouldCloseOnBackdropClick.current = false
      }}
    >
      <div
        className="modal"
        onMouseDown={() => {
          shouldCloseOnBackdropClick.current = false
        }}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="modal-header">
          <div className="modal-title">{authMode === 'login' ? '로그인' : '회원가입'}</div>
          <button className="icon-btn" onClick={() => setAuthModal(false)}>
            ×
          </button>
        </div>
        <div className="modal-tabs">
          <button className={classNames('modal-tab', authMode === 'login' && 'active')} onClick={() => setAuthModal(true, 'login')}>
            로그인
          </button>
          <button className={classNames('modal-tab', authMode === 'signup' && 'active')} onClick={() => setAuthModal(true, 'signup')}>
            회원가입
          </button>
        </div>
        <form className="modal-body" onSubmit={handleSubmit}>
          <label className="field">
            <span className="field-label">이메일</span>
            <input
              className="field-input"
              type="email"
              value={form.email}
              onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
              required
            />
          </label>
          {authMode === 'signup' && (
            <label className="field">
              <span className="field-label">닉네임</span>
              <input
                className="field-input"
                value={form.nickname}
                onChange={(event) => setForm((current) => ({ ...current, nickname: event.target.value }))}
                required
              />
            </label>
          )}
          <label className="field">
            <span className="field-label">비밀번호</span>
            <input
              className="field-input"
              type="password"
              value={form.password}
              onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
              required
            />
          </label>
          {error && <div className="error-inline">{error}</div>}
          <button className="primary-btn full" disabled={submitting} type="submit">
            {submitting ? '처리 중...' : authMode === 'login' ? '로그인' : '가입 후 로그인'}
          </button>
        </form>
      </div>
    </div>
  )
}

export function CharacterInfoRow({
  character,
  action,
}: {
  character: CharacterListItem
  action?: React.ReactNode
}) {
  return (
    <div className="list-row">
      <div className="row-main">
        <Avatar name={character.name} />
        <div>
          <div className="row-title">{character.name}</div>
          <MetaLine
            items={[
              `작성 글 ${character.postCount}`,
              `소유자 ${character.owner.nickname}`,
              character.lastPostAt ? `최근 ${formatDateTime(character.lastPostAt)}` : '',
            ]}
          />
        </div>
      </div>
      <div className="row-side">
        <span className={classNames('badge', character.status === 'ACTIVE' ? 'active' : 'danger')}>{character.status}</span>
        {action}
      </div>
    </div>
  )
}
