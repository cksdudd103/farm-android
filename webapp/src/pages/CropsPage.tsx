import { useEffect, useMemo, useState } from 'react'
import {
  fetchCrops,
  createCrop,
  updateCrop,
  deleteCrop,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import { cropGuides, getCropGuide, calculateHarvestDate, getDaysUntil } from '../data/cropGuides'
import { useAuth } from '../contexts/AuthContext'
import type { Crop } from '../types/api'

const statuses = ['재배중', '수확완료', '휴경']

export function CropsPage() {
  const { user } = useAuth()
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
    const fd = new FormData(form)

    const cropName = (fd.get('name') as string) || ''
    const plantingDate = (fd.get('planting_date') as string) || ''
    const matchedGuide = getCropGuide(cropName)

    const payload: Partial<Crop> = {
      name: cropName,
      variety: (fd.get('variety') as string) || undefined,
      field_location: (fd.get('field_location') as string) || undefined,
      area: fd.get('area') ? Number(fd.get('area')) : undefined,
      planting_date: plantingDate || undefined,
      status: (fd.get('status') as string) || '재배중',
      memo: (fd.get('memo') as string) || undefined,
      is_public: fd.get('is_public') === 'on',
    }

    if (matchedGuide && plantingDate) {
      payload.expected_harvest_date = calculateHarvestDate(plantingDate, matchedGuide.harvestDays)
    }

    try {
      if (editing) {
        await updateCrop(editing.id, payload)
      } else {
        await createCrop(payload)
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
            <CropItem key={crop.id} crop={crop} currentUserId={user?.id} onEdit={openEdit} onDelete={handleDelete} />
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

function CropItem({
  crop,
  currentUserId,
  onEdit,
  onDelete,
}: {
  crop: Crop
  currentUserId?: number
  onEdit: (crop: Crop) => void
  onDelete: (id: number) => void
}) {
  const isOwner = currentUserId === undefined || crop.user_id === currentUserId
  const g = useMemo(() => getCropGuide(crop.name), [crop.name])
  const estimate = useMemo(() => {
    if (crop.expected_harvest_date) {
      return { date: crop.expected_harvest_date, days: getDaysUntil(crop.expected_harvest_date) }
    }
    if (g && crop.planting_date) {
      const date = calculateHarvestDate(crop.planting_date, g.harvestDays)
      return { date, days: getDaysUntil(date) }
    }
    return null
  }, [crop.expected_harvest_date, crop.planting_date, g])

  return (
    <li className="py-4 flex justify-between items-start">
      <div className="flex gap-4">
        {crop.image && (
          <img src={crop.image} alt={crop.name} className="w-16 h-16 object-cover rounded-lg" />
        )}
        <div>
          <p className="font-semibold text-gray-900 flex items-center gap-1.5">
            {crop.name}
            <span className={`text-[11px] px-1.5 py-0.5 rounded-full font-normal ${crop.is_public ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-500'}`}>
              {crop.is_public ? '공개' : '비공개'}
            </span>
            {!isOwner && crop.owner_name && (
              <span className="text-[11px] text-gray-400 font-normal">by {crop.owner_name}</span>
            )}
          </p>
          <p className="text-sm text-gray-500">
            {crop.variety || '-'} · {crop.field_location || '-'} · {crop.area ? `${crop.area}㎡` : '-'}
          </p>
          <p className="text-sm text-gray-500">
            심은 날: {crop.planting_date || '-'} · 상태: {crop.status}
          </p>
          {estimate && (
            <p className={`text-sm mt-1 font-medium ${estimate.days <= 7 ? 'text-red-600' : estimate.days <= 14 ? 'text-orange-600' : 'text-green-700'}`}>
              예상 수확일: {estimate.date} ({estimate.days > 0 ? `D-${estimate.days}` : estimate.days === 0 ? '오늘 수확 예정' : `D+${Math.abs(estimate.days)}`})
            </p>
          )}
          {crop.memo && <p className="text-sm text-gray-600 mt-1">{crop.memo}</p>}
        </div>
      </div>
      {isOwner && (
        <div className="flex gap-2">
          <button onClick={() => onEdit(crop)} className="text-sm text-green-700 hover:underline">수정</button>
          <button onClick={() => onDelete(crop.id)} className="text-sm text-red-600 hover:underline">삭제</button>
        </div>
      )}
    </li>
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
          <select name="name" defaultValue={crop?.name} className="w-full px-3 py-2 border rounded-lg" required>
            <option value="">작물 선택</option>
            {cropGuides.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)}
          </select>
          <input name="variety" defaultValue={crop?.variety || ''} placeholder="품종" className="w-full px-3 py-2 border rounded-lg" />
          <input name="field_location" defaultValue={crop?.field_location || ''} placeholder="밭 위치" className="w-full px-3 py-2 border rounded-lg" />
          <input name="area" type="number" step="0.01" defaultValue={crop?.area || ''} placeholder="면적 (㎡)" className="w-full px-3 py-2 border rounded-lg" />
          <input name="planting_date" type="date" defaultValue={crop?.planting_date || ''} placeholder="심은 날" className="w-full px-3 py-2 border rounded-lg" />
          <select name="status" defaultValue={crop?.status || '재배중'} className="w-full px-3 py-2 border rounded-lg">
            {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <textarea name="memo" defaultValue={crop?.memo || ''} placeholder="메모" className="w-full px-3 py-2 border rounded-lg" rows={3} />
          <label className="flex items-center gap-2 text-sm text-gray-700 select-none">
            <input type="checkbox" name="is_public" defaultChecked={crop?.is_public || false} className="rounded" />
            다른 회원에게 공개하기 (공개 시 다른 회원도 조회 가능)
          </label>

          <div className="flex gap-2 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg">취소</button>
            <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">저장</button>
          </div>
        </form>
      </div>
    </div>
  )
}
