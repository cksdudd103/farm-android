import { useEffect, useMemo, useState } from 'react'
import { fetchDiagnoses, createDiagnosis, deleteDiagnosis } from '../lib/api'
import { PageCard } from '../components/Layout'
import { cropGuides, getCropGuide } from '../data/cropGuides'
import type { Diagnosis } from '../types/api'

export function DiagnosePage() {
  const [diagnoses, setDiagnoses] = useState<Diagnosis[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const [cropFilter, setCropFilter] = useState('')

  const load = () => {
    setLoading(true)
    fetchDiagnoses()
      .then((data) => setDiagnoses(data || []))
      .catch((err) => {
        console.warn('Diagnoses API failed:', err)
        setDiagnoses([])
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const filteredDiagnoses = useMemo(() => {
    if (!cropFilter) return diagnoses
    return diagnoses.filter((d) => d.crop_name?.includes(cropFilter))
  }, [diagnoses, cropFilter])

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = e.currentTarget
    const formData = new FormData(form)
    try {
      await createDiagnosis(formData)
      setIsOpen(false)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '진단 요청에 실패했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
      await deleteDiagnosis(id)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '삭제에 실패했습니다.')
    }
  }

  if (loading) return <PageCard title="AI 병해충 진단"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="AI 병해충 진단">
      <div className="flex justify-between items-center mb-4">
        <select
          value={cropFilter}
          onChange={(e) => setCropFilter(e.target.value)}
          className="px-3 py-2 border rounded-lg"
        >
          <option value="">전체 작물</option>
          {cropGuides.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)}
        </select>
        <button
          onClick={() => setIsOpen(true)}
          className="px-4 py-2 bg-green-700 text-white rounded-lg hover:bg-green-800"
        >
          사진으로 진단
        </button>
      </div>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      <DiseaseGuide cropName={cropFilter} />

      {filteredDiagnoses.length === 0 ? (
        <p className="text-gray-500">진단 기록이 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {filteredDiagnoses.map((d) => (
            <li key={d.id} className="py-4 flex justify-between items-start">
              <div className="flex gap-4">
                {d.image && <img src={d.image} alt="" className="w-20 h-20 object-cover rounded-lg" />}
                <div>
                  <p className="font-semibold text-gray-900">{d.disease_name || '분석 중'}</p>
                  <p className="text-sm text-gray-500">
                    {d.crop_name || '-'} · {d.confidence ? `${(d.confidence * 100).toFixed(1)}%` : '-'} · {d.severity || '-'}
                  </p>
                  {d.advice && <p className="text-sm text-gray-700 mt-1">{d.advice}</p>}
                  <p className="text-xs text-gray-400 mt-1">{d.created_at}</p>
                </div>
              </div>
              <button onClick={() => handleDelete(d.id)} className="text-sm text-red-600 hover:underline">삭제</button>
            </li>
          ))}
        </ul>
      )}

      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-6">
            <h2 className="text-xl font-bold mb-4">병해충 진단</h2>
            <form onSubmit={handleSubmit} className="space-y-3">
              <select name="crop_name" className="w-full px-3 py-2 border rounded-lg" required>
                <option value="">작물 선택</option>
                {cropGuides.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)}
              </select>
              <input name="image" type="file" accept="image/*" className="w-full" required />
              <div className="flex gap-2 pt-2">
                <button type="button" onClick={() => setIsOpen(false)} className="flex-1 py-2 border rounded-lg">취소</button>
                <button type="submit" className="flex-1 py-2 bg-green-700 text-white rounded-lg">진단 요청</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </PageCard>
  )
}

function DiseaseGuide({ cropName }: { cropName: string }) {
  const guide = useMemo(() => getCropGuide(cropName), [cropName])
  if (!guide) return null

  return (
    <div className="mb-6 p-4 bg-yellow-50 rounded-xl border border-yellow-100">
      <h3 className="font-bold text-yellow-800 mb-2">{guide.name} 주요 병해충</h3>
      <div className="grid md:grid-cols-2 gap-4 text-sm">
        <div>
          <p className="font-semibold text-gray-700">주요 해충</p>
          <ul className="list-disc list-inside text-gray-600">
            {guide.commonPests.map((p, i) => <li key={i}>{p}</li>)}
          </ul>
        </div>
        <div>
          <p className="font-semibold text-gray-700">주요 병해</p>
          <ul className="list-disc list-inside text-gray-600">
            {guide.commonDiseases.map((d, i) => <li key={i}>{d}</li>)}
          </ul>
        </div>
      </div>
    </div>
  )
}
