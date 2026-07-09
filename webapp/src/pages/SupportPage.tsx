import { useEffect, useState } from 'react'
import { fetchSupportPrograms } from '../lib/api'
import { PageCard } from '../components/Layout'
import type { SupportProgram } from '../types/api'

export function SupportPage() {
  const [programs, setPrograms] = useState<SupportProgram[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchSupportPrograms()
      .then((data) => setPrograms(data || []))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '지원사업 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <PageCard title="정부 지원사업"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="정부 지원사업">
      {error && <p className="text-red-600 mb-4">{error}</p>}

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
