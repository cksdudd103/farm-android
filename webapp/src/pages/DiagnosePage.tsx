import { useEffect, useState, useRef } from 'react'
import {
  fetchDiagnoses,
  createDiagnosis,
  deleteDiagnosis,
  fetchCrops,
} from '../lib/api'
import { PageCard } from '../components/Layout'
import type { Diagnosis, Crop } from '../types/api'

const severityColor: Record<string, string> = {
  정상: 'bg-green-100 text-green-800',
  주의: 'bg-yellow-100 text-yellow-800',
  경고: 'bg-orange-100 text-orange-800',
  위험: 'bg-red-100 text-red-800',
}

export function DiagnosePage() {
  const [diagnoses, setDiagnoses] = useState<Diagnosis[]>([])
  const [crops, setCrops] = useState<Crop[]>([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [preview, setPreview] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const load = () => {
    setLoading(true)
    Promise.all([fetchDiagnoses(), fetchCrops()])
      .then(([d, c]) => {
        setDiagnoses(d || [])
        setCrops(c || [])
      })
      .catch((err) => setError(err?.response?.data?.msg || err.message || '데이터를 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) {
      setPreview(null)
      return
    }
    const reader = new FileReader()
    reader.onloadend = () => setPreview(reader.result as string)
    reader.readAsDataURL(file)
  }

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setError('')
    const form = e.currentTarget
    const fd = new FormData(form)
    const file = fd.get('image') as File
    if (!file || file.size === 0) {
      setError('사진을 선택해주세요.')
      return
    }

    const cropId = fd.get('crop_id') ? Number(fd.get('crop_id')) : undefined
    const cropName = crops.find((c) => c.id === cropId)?.name
    if (cropName) fd.set('crop_name', cropName)

    setSubmitting(true)
    try {
      await createDiagnosis(fd)
      form.reset()
      setPreview(null)
      load()
    } catch (err: any) {
      setError(err?.response?.data?.msg || err.message || '진단 요청에 실패했습니다.')
    } finally {
      setSubmitting(false)
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

  if (loading) return <PageCard title="병해충 진단"><div className="py-10 text-center text-gray-500">불러오는 중...</div></PageCard>

  return (
    <PageCard title="병해충 진단">
      <form onSubmit={handleSubmit} className="mb-8 p-4 bg-green-50 rounded-xl border border-green-100 space-y-3">
        <p className="font-semibold text-green-800">AI 병해충 진단 (Gemini)</p>
        <select name="crop_id" className="w-full px-3 py-2 border rounded-lg">
          <option value="">작물 선택</option>
          {crops.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <input
          ref={fileInputRef}
          name="image"
          type="file"
          accept="image/*"
          capture="environment"
          className="w-full"
          required
          onChange={handleFileChange}
        />
        {preview && (
          <div className="flex justify-center">
            <img src={preview} alt="미리보기" className="h-40 object-contain rounded-lg border border-green-200" />
          </div>
        )}
        <button
          type="submit"
          disabled={submitting}
          className="w-full py-2 bg-green-700 text-white rounded-lg hover:bg-green-800 disabled:opacity-60"
        >
          {submitting ? 'Gemini AI 분석 중...' : '진단하기'}
        </button>
      </form>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {diagnoses.length === 0 ? (
        <p className="text-gray-500">진단 기록이 없습니다.</p>
      ) : (
        <ul className="divide-y divide-gray-200">
          {diagnoses.map((d) => (
            <li key={d.id} className="py-4">
              <div className="flex justify-between items-start">
                <div className="flex gap-4">
                  {d.image && (
                    <img
                      src={d.image}
                      alt="진단 사진"
                      className="w-20 h-20 object-cover rounded-lg border border-gray-200"
                      onError={(e) => { e.currentTarget.style.display = 'none' }}
                    />
                  )}
                  <div>
                    <p className="font-semibold text-gray-900">{d.disease_name || '진단 결과 없음'}</p>
                    <p className="text-sm text-gray-500">
                      {d.crop_name || '-'} · {d.confidence ? `${(d.confidence).toFixed(1)}%` : '-'}
                    </p>
                    <p className="text-sm text-gray-700 mt-1">{d.advice}</p>
                  </div>
                </div>
                <button onClick={() => handleDelete(d.id)} className="text-sm text-red-600 hover:underline">삭제</button>
              </div>
              {d.severity && (
                <span className={`inline-block mt-2 text-xs px-2 py-1 rounded-full ${severityColor[d.severity] || 'bg-gray-100 text-gray-800'}`}>
                  {d.severity}
                </span>
              )}
            </li>
          ))}
        </ul>
      )}
    </PageCard>
  )
}
