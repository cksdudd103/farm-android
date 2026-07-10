import { useEffect, useState } from 'react'
import { fetchMarketPrices, fetchMarketHistory } from '../lib/api'
import { PageCard } from '../components/Layout'
import { TrendingUp, TrendingDown, Minus, RefreshCw, X } from 'lucide-react'
import type { MarketItem } from '../types/api'

const todayStr = () => new Date().toISOString().slice(0, 10)
const MIN_DATE = '2020-01-01'

export function MarketPage() {
  const [items, setItems] = useState<MarketItem[]>([])
  const [selectedDate, setSelectedDate] = useState(todayStr())
  const [resultDate, setResultDate] = useState('')
  const [isLive, setIsLive] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatedAt, setUpdatedAt] = useState('')
  const [historyItem, setHistoryItem] = useState<string | null>(null)
  const [historyData, setHistoryData] = useState<{ date: string; price: number; change_pct: number; source: string }[]>([])
  const [historyLoading, setHistoryLoading] = useState(false)

  const load = (targetDate: string) => {
    setLoading(true)
    setError('')
    fetchMarketPrices(targetDate)
      .then((res) => {
        setItems(res.data || [])
        setResultDate(res.date || targetDate)
        setIsLive((res.data || []).some((it) => it.source === 'kamis'))
        setUpdatedAt(new Date().toLocaleString('ko-KR'))
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '시세 정보를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load(selectedDate)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleDateChange = (value: string) => {
    setSelectedDate(value)
    load(value)
  }

  const openHistory = (name: string) => {
    setHistoryItem(name)
    setHistoryLoading(true)
    fetchMarketHistory(name, 30)
      .then((res) => setHistoryData(res.data || []))
      .catch(() => setHistoryData([]))
      .finally(() => setHistoryLoading(false))
  }
  const closeHistory = () => {
    setHistoryItem(null)
    setHistoryData([])
  }

  return (
    <PageCard title="농산물 시세">
      <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div className="flex items-center gap-2">
          <label htmlFor="market-date" className="text-sm text-gray-600">조회 날짜</label>
          <input
            id="market-date"
            type="date"
            value={selectedDate}
            min={MIN_DATE}
            max={todayStr()}
            onChange={(e) => handleDateChange(e.target.value)}
            className="border rounded-lg px-2 py-1 text-sm"
          />
        </div>
        <button onClick={() => load(selectedDate)} className="flex items-center gap-1 px-3 py-1.5 text-sm border rounded-lg hover:bg-gray-50">
          <RefreshCw className="w-4 h-4" /> 새로고침
        </button>
      </div>

      <div className="flex items-center justify-between mb-4">
        <p className="text-sm text-gray-500">
          {resultDate ? `조회 기준일: ${resultDate}` : ''} {updatedAt ? `(불러온 시각: ${updatedAt})` : ''}
        </p>
        <span className={`text-xs px-2 py-1 rounded-full ${isLive ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
          {isLive ? 'KAMIS 실시간 데이터' : '샘플 데이터'}
        </span>
      </div>

      {loading ? (
        <div className="py-10 text-center text-gray-500">불러오는 중...</div>
      ) : (
        <>
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
                    <tr
                      key={idx}
                      className="cursor-pointer hover:bg-gray-50"
                      onClick={() => openHistory(item.name)}
                      title="클릭하면 최근 30일 가격 추이를 볼 수 있습니다"
                    >
                      <td className="px-4 py-3 font-medium text-green-700 underline decoration-dotted">{item.name}</td>
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
        </>
      )}

      <p className="mt-4 text-xs text-gray-400">
        * KAMIS(농산물유통정보) 인증키가 설정된 경우 실시간 데이터, 없는 경우 샘플 데이터가 표시됩니다. 품목명을 클릭하면 최근 30일 가격 추이를 확인할 수 있습니다.
      </p>

      {historyItem && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4" onClick={closeHistory}>
          <div
            className="bg-white rounded-2xl shadow-xl w-full max-w-md max-h-[80vh] overflow-y-auto p-5"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-semibold text-gray-800">{historyItem} 가격 추이 (최근 30일)</h3>
              <button onClick={closeHistory} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            {historyLoading ? (
              <div className="py-8 text-center text-gray-500">불러오는 중...</div>
            ) : historyData.length === 0 ? (
              <p className="text-gray-500 text-sm">저장된 히스토리가 없습니다. 날짜별로 조회하면 자동으로 기록됩니다.</p>
            ) : (
              <>
                {(() => {
                  const max = Math.max(...historyData.map((d) => d.price))
                  const min = Math.min(...historyData.map((d) => d.price))
                  const range = max - min || 1
                  return (
                    <div className="flex items-end gap-0.5 h-28 mb-3 border-b border-gray-100">
                      {historyData.map((d) => (
                        <div
                          key={d.date}
                          title={`${d.date}: ${d.price.toLocaleString()}원`}
                          className={`flex-1 rounded-t ${d.source === 'kamis' ? 'bg-green-400' : 'bg-gray-300'}`}
                          style={{ height: `${((d.price - min) / range) * 90 + 10}%` }}
                        />
                      ))}
                    </div>
                  )
                })()}
                <table className="w-full text-xs">
                  <thead>
                    <tr className="text-gray-500">
                      <th className="text-left py-1">날짜</th>
                      <th className="text-right py-1">가격</th>
                      <th className="text-right py-1">등락</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {historyData
                      .slice()
                      .reverse()
                      .map((d) => (
                        <tr key={d.date}>
                          <td className="py-1">{d.date}</td>
                          <td className="py-1 text-right">{d.price.toLocaleString()}원</td>
                          <td className={`py-1 text-right ${trendColor(d.change_pct > 0 ? 'up' : d.change_pct < 0 ? 'down' : 'flat')}`}>
                            {d.change_pct > 0 ? `+${d.change_pct.toFixed(1)}%` : `${d.change_pct.toFixed(1)}%`}
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </>
            )}
          </div>
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
