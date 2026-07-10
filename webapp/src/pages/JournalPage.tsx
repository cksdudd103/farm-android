import { useEffect, useState } from 'react'
import {
  fetchJournals,
  createJournal,
  updateJournal,
  deleteJournal,
  fetchCrops,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import { useAuth } from '../contexts/AuthContext'
import type { Journal, Crop } from '../types/api'

const workTypes = ['파종', '정식', '비료', '관수', '방제', '수확', '기타']

export function JournalPage() {
  const { user } = useAuth()
  const [journals, setJournals] = useState<Journal[]>([])
  const [crops, setCrops] = useState<Crop[]>([])
  const [loading, setLoading] = useState(true)
  const [loadingCrops, setLoadingCrops] = useState(false)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<Journal | null>(null)
  const [isOpen, setIsOpen] = useState(false)

  const loadCrops = () => {
    setLoadingCrops(true)
    fetchCrops()
      .then((data) => setCrops(data || []))
      .catch(() => setCrops([]))
      .finally(() => setLoadingCrops(false))
  }

  const load = () => {
    setLoading(true)
    Promise.all([fetchJournals(), fetchCrops()])
      .then(([j, c]) => {
        setJournals(j || [])
        setCrops(c || [])
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  useEffect(() => {
    if (isOpen) loadCrops()
  }, [isOpen])

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = e.currentTarget
    const fd = new FormData(form)
    const cropId = fd.get('crop_id') ? Number(fd.get('crop_id')) : undefined
    const cropName = crops.find((c) => c.id === cropId)?.name

    const payload: Partial<Journal> = {
      date: (fd.get('date') as string) || new Date().toISOString().slice(0, 10),
      crop_id: cropId,
      crop_name: cropName,
      work_type: (fd.get('work_type') as string) || undefined,
      weather: (fd.get('weather') as string) || undefined,
      content: (fd.get('content') as string) || '',
      is_public: fd.get('is_public') === 'on',
    }

    try {
      if (editing) {
        await updateJournal(editing.id, payload)
      } else {
        await createJournal(payload)
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
      await deleteJournal(id)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  if (loading) return <PageCard title="영농 일지"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="영농 일지">
      <div className="flex justify-end mb-4">
        <button
          onClick={() => { setEditing(null); setIsOpen(true) }}
          className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800"
        >
          일지 작성
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {journals.length === 0 ? (
        <p className="text-gray-500">등록된 영농 일지가 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {journals.map((j) => {
            const isOwner = user?.id === undefined || j.user_id === user?.id
            return (
            <li key={j.id} className="py-4 flex justify-between items-start">
              <div className="flex gap-4">
                {j.image && <img src={j.image} alt="" className="w-16 h-16 object-cover rounded-lg" />}
                <div>
                  <p className="font-semibold text-gray-900 flex items-center gap-1.5">
                    {j.date}
                    <span className={`text-[11px] px-1.5 py-0.5 rounded-full font-normal ${j.is_public ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-500'}`}>
                      {j.is_public ? '공개' : '비공개'}
                    </span>
                    {!isOwner && j.owner_name && <span className="text-[11px] text-gray-400 font-normal">by {j.owner_name}</span>}
                  </p>
                  <p className="text-sm text-gray-500">
                    {j.crop_name || '-'} · {j.work_type || '-'} · {j.weather || '-'}
                  </p>
                  <p className="text-gray-700 mt-1">{j.content}</p>
                </div>
              </div>
              {isOwner && (
                <div className="flex gap-2">
                  <button onClick={() => { setEditing(j); setIsOpen(true) }} className="text-sm text-green-700 hover:underline">수정</button>
                  <button onClick={() => handleDelete(j.id)} className="text-sm text-red-600 hover:underline">삭제</button>
                </div>
              )}
            </li>
            )
          })}
        </ul>
      )}

      {isOpen && (
        <JournalModal
          journal={editing}
          crops={crops}
          loadingCrops={loadingCrops}
          onClose={() => { setIsOpen(false); setEditing(null) }}
          onSubmit={handleSubmit}
        />
      )}
    </PageCard>
  )
}

function JournalModal({
  journal,
  crops,
  loadingCrops,
  onClose,
  onSubmit,
}: {
  journal: Journal | null
  crops: Crop[]
  loadingCrops: boolean
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">{journal ? '일지 수정' : '일지 작성'}</h2>
        <form onSubmit={onSubmit} className="space-y-3">
          <input name="date" type="date" defaultValue={journal?.date || new Date().toISOString().slice(0, 10)} className="w-full px-3 py-2 border rounded-lg" required />
          <select name="crop_id" defaultValue={journal?.crop_id ?? ''} className="w-full px-3 py-2 border rounded-lg">
            <option value="">{loadingCrops ? '작물 불러오는 중...' : crops.length === 0 ? '등록된 작물 없음' : '작물 선택'}</option>
            {crops.map((c) => <option key={c.id} value={String(c.id)}>{c.name} {c.variety ? `(${c.variety})` : ''}</option>)}
          </select>
          <select name="work_type" defaultValue={journal?.work_type || ''} className="w-full px-3 py-2 border rounded-lg">
            <option value="">작업 종류</option>
            {workTypes.map((w) => <option key={w} value={w}>{w}</option>)}
          </select>
          <input name="weather" defaultValue={journal?.weather || ''} placeholder="날씨" className="w-full px-3 py-2 border rounded-lg" />
          <textarea name="content" defaultValue={journal?.content || ''} placeholder="내용" className="w-full px-3 py-2 border rounded-lg" rows={4} required />
          <label className="flex items-center gap-2 text-sm text-gray-700 select-none">
            <input type="checkbox" name="is_public" defaultChecked={journal?.is_public || false} className="rounded" />
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
