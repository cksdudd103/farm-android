import { useEffect, useMemo, useState } from 'react'
import {
  fetchCrops,
  createCrop,
  updateCrop,
  deleteCrop,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import { cropGuides, getCropGuide } from '../data/cropGuides'
import type { Crop } from '../types/api'

const statuses = ['재배중', '수확완료', '휴경']

export function CropsPage() {
  const [crops, setCrops] = useState<Crop[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<Crop | null>(null)
  const [isOpen, setIsOpen] = useState(false)
  const [selectedGuide, setSelectedGuide] = useState('')

  const load = () => {
    setLoading(true)
    fetchCrops()
      .then((data) => setCrops(data || []))
      .catch((err) => setError(err?.response?.data?.msg || err.message || '작물 목록을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const guide = useMemo(() => getCropGuide(selectedGuide), [selectedGuide])

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = e.currentTarget
    const formData = new FormData(form)
    const idRaw = formData.get('id') as string
    const id = idRaw ? Number(idRaw) : 0
    try {
      if (id) {
        await updateCrop(id, formData)
      } else {
        await createCrop(formData)
      }
      setIsOpen(false)
      setEditing(null)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '저장에 실패했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
      await deleteCrop(id)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  const openNew = () => {
    setEditing(null)
    setIsOpen(true)
  }

  const openEdit = (crop: Crop) => {
    setEditing(crop)
    setIsOpen(true)
  }

  if (loading) return <PageCard title="작물 관리"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="작물 관리">
      <div className="mb-6 p-4 bg-green-50 rounded-xl border border-green-100">
        <label className="block text-sm font-semibold text-green-800 mb-2">AI 작물 가이드 검색</label>
        <select
          value={selectedGuide}
          onChange={(e) => setSelectedGuide(e.target.value)}
          className="w-full px-3 py-2 border rounded-lg"
        >
          <option value="">작물 선택</option>
          {cropGuides.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)}
        </select>
        {guide && <CropGuideCard guide={guide} />}
      </div>

      <div className="flex justify-end mb-4">
        <button
          onClick={openNew}
          className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800"
        >
          작물 등록
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {crops.length === 0 ? (
        <p className="text-gray-500">등록된 작물이 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {crops.map((crop) => (
            <li key={crop.id} className="py-4 flex justify-between items-start">
              <div className="flex gap-4">
                {crop.image && (
                  <img src={crop.image} alt={crop.name} className="w-16 h-16 object-cover rounded-lg" />
                )}
                <div>
                  <p className="font-semibold text-gray-900">{crop.name}</p>
                  <p className="text-sm text-gray-500">
                    {crop.variety || '-'} · {crop.field_location || '-'} · {crop.area ? `${crop.area}㎡` : '-'}
                  </p>
                  <p className="text-sm text-gray-500">
                    심은 날: {crop.planting_date || '-'} · 상태: {crop.status}
                  </p>
                  {crop.memo && <p className="text-sm text-gray-600 mt-1">{crop.memo}</p>}
                </div>
              </div>
              <div className="flex gap-2">
                <button onClick={() => openEdit(crop)} className="text-sm text-green-700 hover:underline">수정</button>
                <button onClick={() => handleDelete(crop.id)} className="text-sm text-red-600 hover:underline">삭제</button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {isOpen && (
        <CropModal
          crop={editing}
          onClose={() => { setIsOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </PageCard>
  )
}

function CropGuideCard({ guide }: { guide: import('../data/cropGuides').CropGuide }) {
  return (
    <div className="mt-4 text-sm text-gray-700">
      <div className="grid md:grid-cols-2 gap-2">
        <p>🌡️ 적정 온도: {guide.temperature}</p>
        <p>💧 적정 습도: {guide.humidity}</p>
        <p>🌱 파종 시기: {guide.sowingSeason}</p>
        <p>🧺 수확 시기: {guide.harvestSeason}</p>
        <p>🪴 토양: {guide.soil}</p>
        <p>💦 물주기: {guide.watering}</p>
        <p>🧪 비료: {guide.fertilizing}</p>
      </div>
      <p className="mt-2">🐛 주요 해충: {guide.commonPests.join(', ')}</p>
      <p>🦠 주요 병해: {guide.commonDiseases.join(', ')}</p>
      <ul className="mt-2 list-disc list-inside">
        {guide.tips.map((tip, i) => <li key={i}>{tip}</li>)}
      </ul>
    </div>
  )
}

function CropModal({
  crop,
  onClose,
  onSubmit,
}: {
  crop: Crop | null
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">{crop ? '작물 수정' : '작물 등록'}</h2>
        <form onSubmit={onSubmit} className="space-y-3">
          <input type="hidden" name="id" value={crop?.id || ''} />
          <select name="name" defaultValue={crop?.name} className="w-full px-3 py-2 border rounded-lg" required>
            <option value="">작물 선택</option>
            {cropGuides.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)}
          </select>
          <input name="variety" defaultValue={crop?.variety || ''} placeholder="품종" className="w-full px-3 py-2 border rounded-lg" />
          <input name="field_location" defaultValue={crop?.field_location || ''} placeholder="밭 위치" className="w-full px-3 py-2 border rounded-lg" />
          <input name="area" type="number" step="0.01" defaultValue={crop?.area || ''} placeholder="면적 (㎡)" className="w-full px-3 py-2 border rounded-lg" />
          <input name="planting_date" type="date" defaultValue={crop?.planting_date || ''} placeholder="심은 날" className="w-full px-3 py-2 border rounded-lg" />
          <input name="expected_harvest_date" type="date" defaultValue={crop?.expected_harvest_date || ''} placeholder="예상 수확일" className="w-full px-3 py-2 border rounded-lg" />
          <select name="status" defaultValue={crop?.status || '재배중'} className="w-full px-3 py-2 border rounded-lg">
            {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <textarea name="memo" defaultValue={crop?.memo || ''} placeholder="메모" className="w-full px-3 py-2 border rounded-lg" rows={3} />
          <input name="image" type="file" accept="image/*" className="w-full" />

          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg">취소</button>
            <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">저장</button>
          </div>
        </form>
      </div>
    </div>
  )
}
