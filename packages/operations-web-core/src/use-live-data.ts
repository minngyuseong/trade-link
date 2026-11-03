import { useCallback, useEffect, useState } from 'react'

export function useLiveData<T>(urls: string[], apiBaseUrl: string) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [updatedAt, setUpdatedAt] = useState<Date | null>(null)

  const refresh = useCallback(async () => {
    try {
      const responses = await Promise.all(
        urls.map((url) => fetch(`${apiBaseUrl}${url}`, { headers: { Accept: 'application/json' } })),
      )
      const failed = responses.find((response) => !response.ok)
      if (failed) throw new Error(`서버가 ${failed.status} 응답을 반환했습니다.`)
      const values = await Promise.all(responses.map((response) => response.json()))
      setData(values as T)
      setError(null)
      setUpdatedAt(new Date())
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : '데이터를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }, [apiBaseUrl, urls])

  useEffect(() => {
    void refresh()
    const timer = window.setInterval(() => void refresh(), 3_000)
    return () => window.clearInterval(timer)
  }, [refresh])

  return { data, error, loading, updatedAt, refresh }
}
