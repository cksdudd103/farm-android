import { useEffect, useState } from 'react'
import { fetchDashboardSummary } from '../lib/api'
import { PageCard } from '../components/Layout'
import type { DashboardSummary } from '../types/api'

export function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchDashboardSummary()
      .then((data) => setSummary(data || null))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <PageCard title="대시보드"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  const cards = [
    { label: '전체 작물', value: summary?.total_crops ?? 0 },
    { label: '재배 중', value: summary?.growing_crops ?? 0 },
    { label: '예정 작업', value: summary?.pending_tasks ?? 0 },
    { label: '오늘 작업', value: summary?.today_tasks ?? 0 },
    { label: '부족 재고', value: summary?.low_stock ?? 0 },
    { label: '출하 총액', value: summary?.total_shipment_amount?.toLocaleString() ?? 0 },
  ]

  return (
    <PageCard title="대시보드">
      {error && <p className="text-red-600 mb-4">{error}</p>}
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {cards.map((card) => (
          <div key={card.label} className="bg-green-50 rounded-xl p-5 text-center">
            <p className="text-3xl font-bold text-green-800">{card.value}</p>
            <p className="text-sm text-gray-600 mt-1">{card.label}</p>
          </div>
        ))}
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="font-semibold text-gray-800 mb-3">최근 영농 일지</h2>
          {summary?.recent_journals?.length ? (
            <ul className="space-y-2">
              {summary.recent_journals.map((j) => (
                <li key={j.id} className="text-sm border-b border-gray-100 pb-2 last:border-0">
                  <span className="text-gray-500">{j.date}</span>
                  <span className="ml-2 font-medium">{j.crop_name || j.work_type || '일지'}</span>
                  <p className="text-gray-600 mt-0.5">{j.content}</p>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-500 text-sm">최근 영농 일지가 없습니다.</p>
          )}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h2 className="font-semibold text-gray-800 mb-3">다가오는 작업</h2>
          {summary?.upcoming_tasks?.length ? (
            <ul className="space-y-2">
              {summary.upcoming_tasks.map((t) => (
                <li key={t.id} className="text-sm border-b border-gray-100 pb-2 last:border-0">
                  <span className="text-gray-500">{t.due_date}</span>
                  <span className="ml-2 font-medium">{t.title}</span>
                  <span className={`ml-2 text-xs px-1.5 py-0.5 rounded ${priorityClass(t.priority)}`}>{t.priority}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-500 text-sm">예정된 작업이 없습니다.</p>
          )}
        </div>
      </div>

      <div className="mt-8 p-5 bg-gray-50 rounded-xl">
        <h2 className="font-semibold text-gray-800 mb-2">안내</h2>
        <p className="text-gray-600 text-sm">
          스마트영농 웹 버전에 오신 것을 환영합니다. 좌측 메뉴에서 각 기능을 이용할 수 있습니다.
        </p>
      </div>
    </PageCard>
  )
}

function priorityClass(priority?: string) {
  switch (priority) {
    case '높음': return 'bg-red-100 text-red-700'
    case '낮음': return 'bg-blue-100 text-blue-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}
