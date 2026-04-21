import { useEffect, useState } from 'react'
import { communityApi } from './api/community'
import type { CommunityListItem } from './types'

export function useCommunities() {
  const [communities, setCommunities] = useState<CommunityListItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    void (async () => {
      try {
        setCommunities(await communityApi.list())
      } catch (caught) {
        setError(caught instanceof Error ? caught.message : '커뮤니티를 불러오지 못했습니다.')
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  return { communities, loading, error, setCommunities }
}
