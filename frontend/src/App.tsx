import { useEffect } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import {
  AdminCharacterPage,
  AdminCommentPage,
  AdminDashboard,
  AdminPostPage,
  AdminRoute,
  CharacterCreatePage,
  CharacterDetailPage,
  CharacterEditPage,
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
  PostDetailPage,
  PrivateRoute,
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
          <Route path="c/:slug" element={<FeedPage />} />
          <Route path="posts/:postId" element={<PostDetailPage />} />
          <Route element={<PrivateRoute />}>
            <Route path="my" element={<MyPage />} />
            <Route path="characters/new" element={<CharacterCreatePage />} />
            <Route path="characters/:characterId" element={<CharacterDetailPage />} />
            <Route path="characters/:characterId/edit" element={<CharacterEditPage />} />
            <Route path="characters/:characterId/prompts" element={<CharacterPromptPage />} />
            <Route path="characters/:characterId/prompts/new" element={<CharacterPromptCreatePage />} />
            <Route path="characters/:characterId/prompts/:promptId/edit" element={<CharacterPromptEditPage />} />
          </Route>
          <Route path="admin" element={<AdminRoute />}>
            <Route index element={<AdminDashboard />} />
            <Route path="communities/new" element={<CommunityCreatePage />} />
            <Route path="communities/:id/edit" element={<CommunityEditPage />} />
            <Route path="communities/:id/prompts" element={<CommunityPromptPage />} />
            <Route path="communities/:id/prompts/new" element={<CommunityPromptCreatePage />} />
            <Route path="communities/:id/prompts/:promptId/edit" element={<CommunityPromptEditPage />} />
            <Route path="characters" element={<AdminCharacterPage />} />
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
