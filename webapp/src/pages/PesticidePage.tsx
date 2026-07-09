import { useEffect, useState } from 'react'
import { fetchPesticides } from '../lib/api'
import { PageCard } from '../components/Layout'
import type { PesticideInfo } from '../types/api'

export function PesticidePage() {
  const [items, setItems] = useState<PesticideInfo[]>([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = (q: string) => {
    setLoading(true)
    fetchPesticides(q)
      .then((data) => setItems(data || []))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '농약 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load('')
  }, [])

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    load(query)
  }

  if (loading) return <PageCard title="농약 정보"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="농약 정보">
      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="농약명 또는 작물 검색"
          className="flex-1 px-3 py-2 border rounded-lg"
        />
        <button type="submit" className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800">검색</button>
      </form>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {items.length === 0 ? (
        <p className="text-gray-500">검색 결과가 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {items.map((item, idx) => (
            <li key={idx} className="py-4">
              <p className="font-semibold text-gray-900">{item.name} <span className="text-sm font-normal text-gray-500">({item.type})</span></p>
              <p className="text-sm text-gray-600">대상 해충/병: {item.target}</p>
              <p className="text-sm text-gray-600">사용 작물: {item.crops}</p>
              <p className="text-sm text-gray-600">안전사용기준: {item.safety_period}</p>
              <p className="text-sm text-gray-600">희석 배수: {item.dilution}</p>
            </li>
          ))}
        </ul>
      )}
    </PageCard>
  )
}
