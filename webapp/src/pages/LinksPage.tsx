import { useEffect, useMemo, useState } from 'react'
import { fetchExternalLinks, fetchAnnouncements } from '../lib/api'
import type { ExternalLink, Announcement } from '../lib/api'
import { PageCard } from '../components/Layout'
import { ExternalLink as LinkIcon, Newspaper, Search } from 'lucide-react'

const categories = ['전체', '정부기관', '공지사항', '정보포털', '기상정보', '시세정보', '지원사업']

const fallbackLinks: ExternalLink[] = [
  { name: '농촌진흥청', url: 'https://www.rda.go.kr', category: '정부기관', description: '농업 기술, 병해충 정보, 영농 자료' },
  { name: '농촌진흥청 공지사항', url: 'https://www.rda.go.kr/board/board.do?boardId=farmprmntinfo', category: '공지사항', description: '농촌진흥청 최신 공지 및 소식' },
  { name: '농사로', url: 'https://www.nongsaro.go.kr', category: '정보포털', description: '농업 기술, 작물 정보, 병해충 진단' },
  { name: '농림축산식품부', url: 'https://www.mafra.go.kr', category: '정부기관', description: '농식품 정책, 지원 사업, 병해충 발생 동향' },
  { name: '농림축산식품부 본부공지', url: 'https://www.mafra.go.kr/home/5004/subview.do', category: '공지사항', description: '농식품부 공지사항' },
  { name: '기상청 날씨누리', url: 'https://www.weather.go.kr', category: '기상정보', description: '기상청 공식 날씨 예보 및 특보' },
  { name: '농업관측', url: 'https://www.agweather.go.kr', category: '기상정보', description: '농업 기상 관측 및 예보' },
  { name: '농산물유통정보(KAMIS)', url: 'https://www.kamis.or.kr', category: '시세정보', description: '농산물 도매가격 및 소매가격 정보' },
  { name: '농촌일자리진흥청', url: 'https://www.rda.go.kr/youngfarmer', category: '지원사업', description: '청년 농업인 및 귀농 지원' },
  { name: '귀농귀촌종합센터', url: 'https://www.returnfarm.com', category: '지원사업', description: '귀농·귀촌 상담 및 교육' },
]

const fallbackAnnouncements: Announcement[] = [
  { title: '농촌진흥청 홈페이지 바로가기', url: 'https://www.rda.go.kr', source: '농촌진흥청', summary: '공지사항은 외부 사이트에서 직접 확인해주세요.' },
  { title: '농사로 병해충 정보', url: 'https://www.nongsaro.go.kr/portal/ps/psb/psbb/farmNocticeList.ps', source: '농사로', summary: '병해충 예보 및 방제 정보' },
  { title: '농림축산식품부 정책뉴스', url: 'https://www.mafra.go.kr/home/5013/subview.do', source: '농식품부', summary: '정책 소식 및 병해충 발생 현황' },
]

export function LinksPage() {
  const [links, setLinks] = useState<ExternalLink[]>([])
  const [announcements, setAnnouncements] = useState<Announcement[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [category, setCategory] = useState('전체')
  const [search, setSearch] = useState('')

  useEffect(() => {
    setLoading(true)
    Promise.all([
      fetchExternalLinks().catch(() => fallbackLinks),
      fetchAnnouncements().catch(() => fallbackAnnouncements),
    ])
      .then(([l, a]) => {
        setLinks(l?.length > 0 ? l : fallbackLinks)
        setAnnouncements(a?.length > 0 ? a : fallbackAnnouncements)
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  const filteredLinks = useMemo(() => {
    return links.filter((item) => {
      const matchesCategory = category === '전체' || item.category === category
      const q = search.trim().toLowerCase()
      const matchesSearch =
        !q ||
        item.name.toLowerCase().includes(q) ||
        item.description.toLowerCase().includes(q) ||
        item.category.toLowerCase().includes(q)
      return matchesCategory && matchesSearch
    })
  }, [links, category, search])

  if (loading) return <PageCard title="농업 사이트/공지"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="농업 사이트/공지">
      {error && <p className="text-red-600 mb-4">{error}</p>}

      <div className="mb-6 p-4 bg-blue-50 rounded-xl border border-blue-100">
        <div className="flex items-center gap-2 mb-3">
          <Newspaper className="w-5 h-5 text-blue-700" />
          <p className="font-semibold text-blue-800">주요 공지/뉴스 바로가기</p>
        </div>
        <ul className="space-y-2">
          {announcements.map((a, i) => (
            <li key={i} className="flex items-start gap-2">
              <LinkIcon className="w-4 h-4 text-blue-600 mt-0.5" />
              <a href={a.url} target="_blank" rel="noreferrer" className="text-sm text-blue-700 hover:underline">
                [{a.source}] {a.title}
              </a>
            </li>
          ))}
        </ul>
      </div>

      <div className="flex flex-col md:flex-row gap-3 mb-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="사이트명, 설명 검색"
            className="w-full pl-9 pr-3 py-2 border rounded-lg"
          />
        </div>
        <select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          className="px-3 py-2 border rounded-lg"
        >
          {categories.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>

      <p className="text-sm text-gray-500 mb-2">총 {filteredLinks.length}개 사이트</p>

      {filteredLinks.length === 0 ? (
        <p className="text-gray-500">해당하는 사이트가 없습니다.</p>
      ) : (
        <div className="grid md:grid-cols-2 gap-4">
          {filteredLinks.map((item, idx) => (
            <a
              key={idx}
              href={item.url}
              target="_blank"
              rel="noreferrer"
              className="block p-4 border border-gray-200 rounded-xl hover:border-green-400 hover:shadow-sm transition"
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-semibold text-gray-900">{item.name}</p>
                  <p className="text-xs text-green-700 mt-0.5">{item.category}</p>
                  <p className="text-sm text-gray-600 mt-2">{item.description}</p>
                </div>
                <LinkIcon className="w-4 h-4 text-gray-400" />
              </div>
            </a>
          ))}
        </div>
      )}
    </PageCard>
  )
}
