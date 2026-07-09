import { useEffect, useState } from 'react'
import { fetchSupportPrograms } from '../lib/api'
import { PageCard } from '../components/Layout'
import type { SupportProgram } from '../types/api'

const fallbackPrograms: SupportProgram[] = [
  {
    title: '청년 농업인 육성 지원',
    agency: '농촌진흥청',
    period: '상시',
    target: '만 18~40세 청년 농업인, 귀농인',
    content: '영농 정착 지원금, 교육, 멘토링, 창업 자금 지원',
    status: '진행중',
  },
  {
    title: '귀농귀촌 종합 지원',
    agency: '농림축산식품부',
    period: '상시',
    target: '귀농·귀촌 희망자',
    content: '상담, 교육, 체험학습, 정착 지원금, 주택 수리비 지원',
    status: '진행중',
  },
  {
    title: '농기계 임대료 지원',
    agency: '농촌진흥청/지자체',
    period: '연중',
    target: '농업인 및 농업법인',
    content: '트랙터, 이앙기, 콤바인 등 농기계 임대료 할인',
    status: '진행중',
  },
  {
    title: '친환경 농업 지원 사업',
    agency: '농림축산식품부',
    period: '연중',
    target: '친환경 인증 농가',
    content: '유기농, 물농약 농업 전환 지원, 인증비 지원',
    status: '진행중',
  },
  {
    title: '농업 재해 복구비 지원',
    agency: '농림축산식품부',
    period: '재해 발생 시',
    target: '자연재해 피해 농가',
    content: '태풍, 홍수, 가뭄, 병해충 피해 복구 비용 지원',
    status: '예정',
  },
  {
    title: '농업경영체 등록 지원',
    agency: '농림축산식품부',
    period: '상시',
    target: '농업경영체 미등록 농가',
    content: '농업경영체 등록 시 각종 정부 지원사업 참여 가능',
    status: '진행중',
  },
]

export function SupportPage() {
  const [programs, setPrograms] = useState<SupportProgram[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchSupportPrograms()
      .then((data) => setPrograms(data && data.length > 0 ? data : fallbackPrograms))
      .catch((err) => {
        console.warn('Support API failed, using fallback:', err)
        setPrograms(fallbackPrograms)
      })
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <PageCard title="정부 지원사업"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="정부 지원사업">
      <p className="text-sm text-gray-500 mb-4">* 백엔드 연결 실패 시 대표 정부 지원사업 안내 데이터를 표시합니다. 자세한 내용은 농림축산식품부/농촌진흥청 홈페이지를 확인하세요.</p>

      {programs.length === 0 ? (
        <p className="text-gray-500">지원사업 정보가 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {programs.map((p, idx) => (
            <li key={idx} className="py-4">
              <div className="flex justify-between items-start">
                <p className="font-semibold text-gray-900">{p.title}</p>
                <span className={`text-xs px-2 py-1 rounded ${statusClass(p.status)}`}>{p.status}</span>
              </div>
              <p className="text-sm text-gray-500">{p.agency} · {p.period}</p>
              <p className="text-sm text-gray-600 mt-1">{p.target}</p>
              <p className="text-sm text-gray-700 mt-2">{p.content}</p>
            </li>
          ))}
        </ul>
      )}
    </PageCard>
  )
}

function statusClass(status: string) {
  if (status?.includes('진행')) return 'bg-green-100 text-green-700'
  if (status?.includes('예정')) return 'bg-blue-100 text-blue-700'
  if (status?.includes('마감')) return 'bg-gray-100 text-gray-700'
  return 'bg-yellow-100 text-yellow-700'
}
