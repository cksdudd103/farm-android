import { useEffect, useState } from 'react'
import {
  fetchJournals,
  createJournal,
  updateJournal,
  deleteJournal,
  fetchCrops,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import type { Journal, Crop } from '../types/api'

export function JournalPage() {
  const [journals, setJournals] = useState<Journal[]>([])
  const [crops, setCrops] = useState<Crop[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState<Journal | null>(null)
  const [isOpen, setIsOpen] = useState(false)

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

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = e.currentTarget
    const formData = new FormData(form)
    const id = Number(formData.get('id'))
    try {
      if (id) {
        await updateJournal(id, formData)
      } else {
        await createJournal(formData)
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
          {journals.map((j) => (
            <li key={j.id} className="py-4 flex justify-between items-start">
              <div className="flex gap-4">
                {j.image && <img src={j.image} alt="" className="w-16 h-16 object-cover rounded-lg" />}
                <div>
                  <p className="font-semibold text-gray-900">{j.date}</p>
                  <p className="text-sm text-gray-500">
                    {j.crop_name || '-'} · {j.work_type || '-'} · {j.weather || '-'}
                  </p>
                  <p className="text-gray-700 mt-1">{j.content}</p>
                </div>
              </div>
              <div className="flex gap-2">
                <button onClick={() => { setEditing(j); setIsOpen(true) }} className="text-sm text-green-700 hover:underline">수정</button>
                <button onClick={() => handleDelete(j.id)} className="text-sm text-red-600 hover:underline">삭제</button>
              </div>
            </li>
          ))}
        </ul>
      )}

      {isOpen && (
        <JournalModal
          journal={editing}
          crops={crops}
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
  onClose,
  onSubmit,
}: {
  journal: Journal | null
  crops: Crop[]
  onClose: () => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">{journal ? '일지 수정' : '일지 작성'}</h2>
        <form onSubmit={onSubmit} className="space-y-3">
          <input type="hidden" name="id" value={journal?.id || ''} />
          <input name="date" type="date" defaultValue={journal?.date} className="w-full px-3 py-2 border rounded-lg" required />
          <select name="crop_id" defaultValue={journal?.crop_id || ''} className="w-full px-3 py-2 border rounded-lg">
            <option value="">작물 선택</option>
            {crops.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
          <input name="work_type" defaultValue={journal?.work_type || ''} placeholder="작업 종류 (예: 파종, 비료)" className="w-full px-3 py-2 border rounded-lg" />
          <input name="weather" defaultValue={journal?.weather || ''} placeholder="날씨" className="w-full px-3 py-2 border rounded-lg" />
          <textarea name="content" defaultValue={journal?.content || ''} placeholder="내용" className="w-full px-3 py-2 border rounded-lg" rows={4} required />
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
