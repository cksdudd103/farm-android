import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { PageCard } from '../components/Layout'

interface Summary {
  total_crops: number
  pending_tasks: number
  journal_entries: number
  inventory_items: number
}

export function DashboardPage() {
  const [summary, setSummary] = useState<Summary | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/api/dashboard/summary')
      .then((res) => setSummary(res.data))
      .catch((err) => setError(err.response?.data?.error || '데이터를 불러오지 못했습니다.'))
  }, [])

  const cards = [
    { label: '등록 작물', value: summary?.total_crops ?? '-' },
    { label: '예정 작업', value: summary?.pending_tasks ?? '-' },
    { label: '영농 일지', value: summary?.journal_entries ?? '-' },
    { label: '재고 품목', value: summary?.inventory_items ?? '-' },
  ]

  return (
    <PageCard title="대시보드">
      {error && <p className="text-red-600 mb-4">{error}</p>}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {cards.map((card) => (
          <div key={card.label} className="bg-green-50 rounded-xl p-5 text-center">
            <p className="text-3xl font-bold text-green-800">{card.value}</p>
            <p className="text-sm text-gray-600 mt-1">{card.label}</p>
          </div>
        ))}
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
