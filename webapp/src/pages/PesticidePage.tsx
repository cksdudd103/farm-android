import { useEffect, useMemo, useState } from 'react'
import { fetchPesticides } from '../lib/api'
import { PageCard } from '../components/Layout'
import { Search, Sprout, Leaf, Bug, Skull } from 'lucide-react'
import { getCropGuide } from '../data/cropGuides'
import { pesticidesDatabase } from '../data/pesticidesDatabase'
import type { PesticideInfo } from '../types/api'

export function PesticidePage() {
  const [items, setItems] = useState<PesticideInfo[]>([])
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('전체')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = (q: string) => {
    setLoading(true)
    setError('')
    fetchPesticides(q)
      .then((data) => setItems(data && data.length > 0 ? data : pesticidesDatabase))
      .catch((err) => {
        console.warn('Pesticide API failed, using fallback:', err)
        setItems(pesticidesDatabase)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load('')
  }, [])

  const handleSearch = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    load(query)
  }

  const filtered = useMemo(() => {
    let list = items
    if (category !== '전체') {
      list = list.filter((item) => item.type?.includes(category) || item.category === category)
    }
    const q = query.trim().toLowerCase()
    if (!q) return list
    return list.filter(
      (item) =>
        item.name.toLowerCase().includes(q) ||
        item.crops?.toLowerCase().includes(q) ||
        item.target?.toLowerCase().includes(q) ||
        item.ingredient?.toLowerCase().includes(q)
    )
  }, [items, query, category])

  const selectedCrop = useMemo(() => getCropGuide(query), [query])

  if (loading) return <PageCard title="농약 정보"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="농약 정보">
      <form onSubmit={handleSearch} className="flex flex-col md:flex-row gap-2 mb-4">
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="px-3 py-2 border rounded-lg"
        >
          <option value="전체">전체</option>
          <option value="살충제">살충제</option>
          <option value="살균제">살균제</option>
          <option value="제초제">제초제</option>
          <option value="비료">비료/영양제</option>
        </select>
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="농약명, 성분, 작물, 해충/병 검색"
            className="w-full pl-9 pr-3 py-2 border rounded-lg"
          />
        </div>
        <button type="submit" className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800">검색</button>
      </form>

      {error && <p className="text-red-600 mb-4">{error}</p>}
      {items === pesticidesDatabase && <p className="text-sm text-gray-500 mb-4">* AI/공개 DB 기반 샘플 농약 정보입니다. 실제 사용 전 반드시 농약 라벨을 확인하세요.</p>}

      {selectedCrop && (
        <div className="mb-6 p-4 bg-green-50 rounded-xl border border-green-100">
          <div className="flex items-center gap-2 mb-2">
            <Sprout className="w-5 h-5 text-green-700" />
            <h2 className="font-bold text-green-800">{selectedCrop.name} 재배 가이드</h2>
          </div>
          <div className="grid md:grid-cols-2 gap-2 text-sm text-gray-700">
            <p>🌡️ 적정 온도: {selectedCrop.temperature}</p>
            <p>💧 적정 습도: {selectedCrop.humidity}</p>
            <p>🌱 파종 시기: {selectedCrop.sowingSeason}</p>
            <p>🧺 수확 시기: {selectedCrop.harvestSeason}</p>
            <p>🪴 토양: {selectedCrop.soil}</p>
            <p>💦 물주기: {selectedCrop.watering}</p>
            <p>🧪 비료: {selectedCrop.fertilizing}</p>
          </div>
          <div className="mt-3">
            <p className="text-sm text-gray-700">🐛 주요 해충: {selectedCrop.commonPests.join(', ')}</p>
            <p className="text-sm text-gray-700">🦠 주요 병해: {selectedCrop.commonDiseases.join(', ')}</p>
          </div>
          <ul className="mt-3 list-disc list-inside text-sm text-gray-700">
            {selectedCrop.tips.map((tip, i) => <li key={i}>{tip}</li>)}
          </ul>
        </div>
      )}

      <h2 className="font-semibold text-gray-800 mb-3">{category === '전체' ? '전체 농약/제초제' : category} ({filtered.length}개)</h2>
      {filtered.length === 0 ? (
        <p className="text-gray-500">검색 결과가 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {filtered.map((item, idx) => (
            <li key={idx} className="py-4">
              <div className="flex items-center gap-2 mb-1">
                {iconForType(item.type || item.category || '')}
                <p className="font-semibold text-gray-900">
                  {item.name} <span className="text-sm font-normal text-gray-500">({item.type || item.category})</span>
                </p>
              </div>
              <p className="text-sm text-gray-600">성분: {item.ingredient || '-'}</p>
              <p className="text-sm text-gray-600">대상 해충/병/잡초: {item.target}</p>
              <p className="text-sm text-gray-600">사용 작물: {item.crops || '-'}</p>
              <p className="text-sm text-gray-600">안전사용기준: {item.safety_period || '-'}</p>
              <p className="text-sm text-gray-600">희석 배수: {item.dilution || '-'}</p>
            </li>
          ))}
        </ul>
      )}
    </PageCard>
  )
}

function iconForType(type: string) {
  if (type?.includes('제초')) return <Skull className="w-4 h-4 text-purple-600" />
  if (type?.includes('살충')) return <Bug className="w-4 h-4 text-red-600" />
  if (type?.includes('살균')) return <Sprout className="w-4 h-4 text-green-600" />
  return <Leaf className="w-4 h-4 text-teal-600" />
}
