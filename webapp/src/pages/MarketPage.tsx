import { useEffect, useState } from 'react'
import { fetchMarketPrices } from '../lib/api'
import { PageCard } from '../components/Layout'
import { TrendingUp, TrendingDown, Minus } from 'lucide-react'
import type { MarketItem } from '../types/api'

export function MarketPage() {
  const [items, setItems] = useState<MarketItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchMarketPrices()
      .then((data) => setItems(data || []))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '시세 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <PageCard title="농산물 시세"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="농산물 시세">
      {error && <p className="text-red-600 mb-4">{error}</p>}

      {items.length === 0 ? (
        <p className="text-gray-500">시세 정보가 없습니다.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-green-50">
              <tr>
                <th className="text-left px-4 py-3 font-semibold text-gray-700">품목</th>
                <th className="text-left px-4 py-3 font-semibold text-gray-700">단위</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-700">가격</th>
                <th className="text-right px-4 py-3 font-semibold text-gray-700">등락</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {items.map((item, idx) => (
                <tr key={idx}>
                  <td className="px-4 py-3 font-medium">{item.name}</td>
                  <td className="px-4 py-3 text-gray-500">{item.unit}</td>
                  <td className="px-4 py-3 text-right">{item.price.toLocaleString()}원</td>
                  <td className="px-4 py-3 text-right">
                    <span className={`inline-flex items-center gap-1 ${trendColor(item.trend)}`}>
                      {item.trend === 'up' && <TrendingUp className="w-4 h-4" />}
                      {item.trend === 'down' && <TrendingDown className="w-4 h-4" />}
                      {item.trend === 'flat' && <Minus className="w-4 h-4" />}
                      {item.change_pct > 0 ? `+${item.change_pct.toFixed(1)}%` : `${item.change_pct.toFixed(1)}%`}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </PageCard>
  )
}

function trendColor(trend: string) {
  switch (trend) {
    case 'up': return 'text-red-600'
    case 'down': return 'text-blue-600'
    default: return 'text-gray-500'
  }
}
