import { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { PageCard } from '../components/Layout'

interface Crop {
  id: number
  name: string
  variety: string
  area: string
  planted_at: string
}

export function CropsPage() {
  const [crops, setCrops] = useState<Crop[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/api/crops')
      .then((res) => setCrops(res.data.crops || []))
      .catch((err) => setError(err.response?.data?.error || '작물 목록을 불러오지 못했습니다.'))
  }, [])

  return (
    <PageCard title="작물 관리">
      {error && <p className="text-red-600 mb-4">{error}</p>}
      {crops.length === 0 ? (
        <p className="text-gray-500">등록된 작물이 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {crops.map((crop) => (
            <li key={crop.id} className="py-4 flex justify-between items-center">
              <div>
                <p className="font-semibold text-gray-900">{crop.name}</p>
                <p className="text-sm text-gray-500">{crop.variety} · {crop.area}</p>
              </div>
              <span className="text-sm text-gray-400">{crop.planted_at}</span>
            </li>
          ))}
        </ul>
      )}
    </PageCard>
  )
}
