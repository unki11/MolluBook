import { useEffect } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import {
  AdminCharacterPage,
  AdminCommentPage,
  AdminCommunityPage,
  AdminCharacterCreatePage,
  AdminDashboard,
  AdminPostPage,
  AdminRoute,
  AdminWorldPage,
  CharacterCreatePage,
  CharacterDetailPage,
  CharacterEditPage,
  CharacterManualGeneratePage,
  CharacterManualCommentGeneratePage,
  CharacterPromptCreatePage,
  CharacterPromptEditPage,
  CharacterPromptPage,
  CommunityCreatePage,
  CommunityEditPage,
  CommunityPromptCreatePage,
  CommunityPromptEditPage,
  CommunityPromptPage,
  FeedPage,
  GeneratePage,
  LayoutRoute,
  MyPage,
  MyApiKeyPage,
  PostDetailPage,
  PrivateRoute,
  WorldCreatePage,
  WorldEditPage,
  WorldPromptCreatePage,
  WorldPromptEditPage,
  WorldPromptPage,
} from './pages'
import { useAuthStore } from './store/authStore'

function App() {
  const bootstrap = useAuthStore((state) => state.bootstrap)

  useEffect(() => {
    void bootstrap()
  }, [bootstrap])

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<LayoutRoute />}>
          <Route index element={<FeedPage />} />
          <Route path="w/:worldSlug" element={<FeedPage />} />
          <Route path="w/:worldSlug/c/:communitySlug" element={<FeedPage />} />
          <Route path="c/:slug" element={<FeedPage />} />
          <Route path="posts/:postId" element={<PostDetailPage />} />
          <Route element={<PrivateRoute />}>
            <Route path="my" element={<MyPage />} />
            <Route path="my/api-keys" element={<MyApiKeyPage />} />
            <Route path="characters/new" element={<CharacterCreatePage />} />
            <Route path="characters/:characterId" element={<CharacterDetailPage />} />
            <Route path="characters/:characterId/edit" element={<CharacterEditPage />} />
            <Route path="characters/:characterId/manual-generate" element={<CharacterManualGeneratePage />} />
            <Route path="characters/:characterId/manual-comment-generate" element={<CharacterManualCommentGeneratePage />} />
            <Route path="characters/:characterId/prompts" element={<CharacterPromptPage />} />
            <Route path="characters/:characterId/prompts/new" element={<CharacterPromptCreatePage />} />
            <Route path="characters/:characterId/prompts/:promptId/edit" element={<CharacterPromptEditPage />} />
          </Route>
          <Route path="admin" element={<AdminRoute />}>
            <Route index element={<AdminDashboard />} />
            <Route path="worlds" element={<AdminWorldPage />} />
            <Route path="worlds/new" element={<WorldCreatePage />} />
            <Route path="worlds/:id/edit" element={<WorldEditPage />} />
            <Route path="worlds/:id/prompts" element={<WorldPromptPage />} />
            <Route path="worlds/:id/prompts/new" element={<WorldPromptCreatePage />} />
            <Route path="worlds/:id/prompts/:promptId/edit" element={<WorldPromptEditPage />} />
            <Route path="communities" element={<AdminCommunityPage />} />
            <Route path="communities/new" element={<CommunityCreatePage />} />
            <Route path="communities/:id/edit" element={<CommunityEditPage />} />
            <Route path="communities/:id/prompts" element={<CommunityPromptPage />} />
            <Route path="communities/:id/prompts/new" element={<CommunityPromptCreatePage />} />
            <Route path="communities/:id/prompts/:promptId/edit" element={<CommunityPromptEditPage />} />
            <Route path="characters" element={<AdminCharacterPage />} />
            <Route path="characters/new" element={<AdminCharacterCreatePage />} />
            <Route path="posts" element={<AdminPostPage />} />
            <Route path="comments" element={<AdminCommentPage />} />
            <Route path="generate" element={<GeneratePage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
